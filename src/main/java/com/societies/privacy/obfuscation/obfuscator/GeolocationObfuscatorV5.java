/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.obfuscation.IDataObfuscationManagerCallback;
import com.societies.privacy.obfuscation.IDataObfuscator;
import com.societies.utils.GeolocationUtils;
import com.societies.utils.RandomBetween;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscatorV5 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV5.class);
	private static final boolean DEBUG = true;
	
	private final int OPERATION_E = 0;
	private final int OPERATION_R = 1;
	private final int OPERATION_S = 2;
	private final int OPERATION_ES = 3;
	private final int OPERATION_SE = 4;
	private final int OPERATION_SR = 5;
	
	private RandomBetween rand;
	public double step = 0.1;
	public double alpha0Max = 360;
	public double precisionMax = 0.0000000000000000001;
	public int nbMaxIteration = 30;
	
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// -- Verifications
		if (!(data instanceof Geolocation)) {
			throw new Exception("It's not the right obfuscation algorithm!");
		}
		
		// -- Init
		rand = new RandomBetween();
		
		// -- Algorithm
		Geolocation geolocation = (Geolocation) data;
		Geolocation obfuscatedGeolocation = obfuscateLocation(geolocation, obfuscationLevel);
		
		// -- Send to callback
		callback.obfuscationResult(obfuscatedGeolocation);
	}
	
	/**
	 * Location obfuscation algorithm
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation obfuscateLocation(Geolocation geolocation, float obfuscationLevel) {
		/* ALGORITHM
		* Select randomly an algorithm
		* And apply it
		*/
		Geolocation obfuscatedGeolocation = null;
		int algorithm = rand.nextInt(6);
		algorithm = 4;
		switch(algorithm) {
			case OPERATION_E:
				obfuscatedGeolocation = EObfuscation(geolocation, obfuscationLevel);
			break;
			case OPERATION_R:
				obfuscatedGeolocation = RObfuscation(geolocation, obfuscationLevel);
			break;
			case OPERATION_S:
				obfuscatedGeolocation = SObfuscation(geolocation, obfuscationLevel);
			break;
			case OPERATION_ES:
				obfuscatedGeolocation = ESObfuscation(geolocation, obfuscationLevel);
			break;
			case OPERATION_SE:
				obfuscatedGeolocation = SEObfuscation(geolocation, obfuscationLevel);
			break;
			case OPERATION_SR:
				obfuscatedGeolocation = SRObfuscation(geolocation, obfuscationLevel);
			break;
		}
		return obfuscatedGeolocation;
	}
	
	/**
	 * Location obfuscation algorithm
	 * by enlarging the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @preconditions obfuscationLevel > 0
	 * @return obfuscated location
	 */
	private Geolocation EObfuscation(Geolocation geolocation, float obfuscationLevel) {
		return new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
	}
	/**
	 * Location obfuscation algorithm
	 * by reducing the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation RObfuscation(Geolocation geolocation, float obfuscationLevel) {
		return new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()*((float) Math.sqrt(obfuscationLevel)));
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation SObfuscation(Geolocation geolocation, float obfuscationLevel) {
		double theta = rand.nextDouble()*360;
		return SObfuscation(geolocation, obfuscationLevel, theta);
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @param theta Direction of shifting
	 * @return obfuscated location
	 */
	private Geolocation SObfuscation(Geolocation geolocation, float obfuscationLevel, double theta) {
		Geolocation obfuscatedGeolocation = null;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinxMoinsC(Math.PI*obfuscationLevel);
		double d = 2*geolocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		obfuscatedGeolocation = GeolocationUtils.shitLatLgn(geolocation, theta, d);
		obfuscatedGeolocation.setShiftDirection(theta);
		obfuscatedGeolocation.setShiftDistance(d);
		obfuscatedGeolocation.setShiftAlpha(alpha);
		
		return obfuscatedGeolocation;
	}
	/**
	 * Location obfuscation algorithm
	 * by enlarging the radius and then
	 * shifting the centre of the geolocation circle
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation ESObfuscation(Geolocation geolocation, float obfuscationLevel) {
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		double theta = rand.nextDouble()*360;
		return ESObfuscation(geolocation, obfuscationLevel, middleObfuscationLevel, theta);
	}
	/**
	 * Location obfuscation algorithm
	 * by enlarging the radius and then
	 * shifting the centre of the geolocation circle
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @param middleObfuscationLevel Obfuscation level for the enlargement
	 * @param theta Direction of shifting
	 * @return obfuscated location
	 */
	private Geolocation ESObfuscation(Geolocation geolocation, float obfuscationLevel, float middleObfuscationLevel, double theta) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// -- Enlarge
		middleObfuscatedGeolocation = EObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// -- Shift
		/* Solve the following system
		 * ri*sin(alpha/2) - rf*sin(gamma/2) = 0
		 * ri^2*(alpha-sin(alpha))+rf^2*(gamma-sin(gamma))-2*PI*rf^2*obfuscationLevel=0
		 * ri*cos(alpha/2) + rf*cos(gamma/2) - d = 0
		 */
		// Compute angles alpha and gamma, and distance d
		List<Double> solutions = solveAlphaGammaDByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel);
		double d = solutions.get(2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		finalObfuscatedGeolocation = GeolocationUtils.shitLatLgn(middleObfuscatedGeolocation, theta, d);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * and then enlarging the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation SEObfuscation(Geolocation geolocation, float obfuscationLevel) {
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		double theta = rand.nextDouble()*360;
		return SEObfuscation(geolocation, obfuscationLevel, middleObfuscationLevel, theta);
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * and then enlarging the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @param middleObfuscationLevel Obfuscation level for the shifting
	 * @param theta Direction of shifting
	 * @return obfuscated location
	 */
	private Geolocation SEObfuscation(Geolocation geolocation, float obfuscationLevel, float middleObfuscationLevel, double theta) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// -- Shift
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// -- Enlarge
		float rf = solveAlphaGammaRfByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation = new Geolocation(middleObfuscatedGeolocation.getLatitude(), middleObfuscatedGeolocation.getLongitude(), rf);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * and then reducing the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation SRObfuscation(Geolocation geolocation, float obfuscationLevel) {
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		double theta = rand.nextDouble()*360;
		return SRObfuscation(geolocation, obfuscationLevel, middleObfuscationLevel, theta);
	}
	/**
	 * Location obfuscation algorithm
	 * by shifting the centre of the geolocation circle
	 * and then reducing the radius
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @param middleObfuscationLevel Obfuscation level for the shifting
	 * @param theta Direction of shifting
	 * @return obfuscated location
	 */
	private Geolocation SRObfuscation(Geolocation geolocation, float obfuscationLevel, float middleObfuscationLevel, double theta) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// -- Shift
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel, theta);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// -- Reduce
		List<Double> solutions = solveAlphaGammaDByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation = new Geolocation(middleObfuscatedGeolocation.getLatitude(), middleObfuscatedGeolocation.getLongitude(), solutions.get(2).floatValue());
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	
	/**
	 * Solve x-sin(x)-C=0 with Newton's Method
	 * @param obfuscationLevel
	 * @return x
	 */
	private double solveXMoinsSinxMoinsC(double C) {
		// -- Find x in x-sin(x)-C=0
		/* Computation algorithm
		We use Newton Method
		f(x)=x-sin(x)-C
		f'(x)=1-cos(x)
		xn = xnmoins - f(x)/f'(x)
		
		The difficulty is initialization, but :
		A sign study show that f is growing
		And f(PI/2)=-1.62, and f(PI)=0.9
		So, we choose a value between PI/2 and PI, for example: 2
		*/
		double xn = 2;
		double precision = xn;
		double precisionMax = 0.00000000000000000001;
		int nbMaxIteration = 10;
		int i = 0;
		while(i<nbMaxIteration && precision > precisionMax) {
			double xnmoins1 = xn;
			if (0 == xnmoins1) {
				xnmoins1 = 0.0000000001;
			}
//			LOG.info("xn"+i+"="+xn+" (precision = "+precision+")");
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
			precision = Math.abs(xn-xnmoins1);
			i++;
		}	
//		LOG.info("xnfinal="+xn+" ou "+Math.toDegrees(xn)+"°");
		return xn;
	}
	/**
	 * Solve alpha, gamma, d using Newton's method:
	 * ri*sin(x/2) - rf*sin(y/2) = 0
	 * ri^2*(x-sin(x))+rf^2*(y-sin(y))-2*PI*rf^2*obfuscationLevel=0
	 * @param initialLocation
	 * @param middleLocation
	 * @param obfuscationLevel
	 * @return
	 */
	private List<Double> solveAlphaGammaDByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		double ri = initialLocation.getHorizontalAccuracy();
		double rf = middleLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double rf2 = Math.pow(rf, 2);
		double C = 2*Math.PI*obfuscationLevel*rf2;
		double gammaMax = 2*Math.asin(ri/rf);
		double gamma0Max = gammaMax;
		
		// Initialize
		double alpha0 = 1;
		double gamma0 = 1;
		
		double alphan;
		double gamman;
		double precisionAlpha;
		double precisionGamma;		
		boolean restart;
		// While correct values have not been computed
		do {
			restart = false;
			
			// Values n-1 = initialization values
			alphan = alpha0;
			gamman = gamma0;
			precisionAlpha = alpha0;
			precisionGamma = gamma0;
			
			// While a good precision have been reached
			int i = 0;
			while(i<nbMaxIteration && (precisionAlpha > precisionMax || precisionGamma > precisionMax)) {
				// Save precedent values
				double alphanmoins1 = alphan;
				double gammanmoins1 = gamman;
//				LOG.info("alphan"+i+"="+alphan+" ("+precisionAlpha+"), gamman"+i+"="+gamman+" ("+precisionGamma+")");
				// Compute functions and their derivates
				double f = ri*Math.sin(alphanmoins1/2)-rf*Math.sin(gammanmoins1/2);
				double dfByAlpha = ri/2*Math.cos(alphanmoins1/2);
				double dfByGamma = -rf/2*Math.cos(gammanmoins1/2);
				double g = ri2*(alphanmoins1-Math.sin(alphanmoins1))+rf2*(gammanmoins1-Math.sin(gammanmoins1))-C;
				double dgByAlpha = ri2*(1-Math.cos(alphanmoins1));
				double dgByGamma = rf2*(1-Math.cos(gammanmoins1));
				
				// Compute new values
				/* Algorithm
				 * alphan = alphan-1 - (f(alphan-1)*dgByGamma-g(alphan-1)*dfByGamma)/(dfByAlpha*dgByGamma-dfByGamma*dgByAlpha)
				 * gamman = gamman-1 - (g(gamman-1)*dfByAlpha-f(gamman-1)*dgByAlpha)/(dfByAlpha*dgByGamma-dfByGamma*dgByAlpha)
				 */
				double delta = dfByAlpha*dgByGamma-dfByGamma*dgByAlpha;
				alphan = alphanmoins1 - (f*dgByGamma-g*dfByGamma)/delta;
				gamman = gammanmoins1 - (g*dfByAlpha-f*dgByAlpha)/delta;
//				/* Algorithm
//				 * Xn = (alphan gamman)
//				 * Xn = Xn-1 - F(Xn-1)/J_F(Xn-1)
//				 */
//				double [][] valuesF = {{f}, {g}};
//				double [][] valuesJ_F = {{dfByAlpha, dfByGamma}, {dgByAlpha, dgByGamma}};
//		        RealMatrix F = new Array2DRowRealMatrix(valuesF);
//		        RealMatrix J_F = new Array2DRowRealMatrix(valuesJ_F);
//		        RealMatrix J_FinverseTimeF = new LUDecompositionImpl(J_F).getSolver().getInverse().multiply(F);
//				alphan = alphanmoins1 - J_FinverseTimeF.getEntry(0,0);
//				gamman = gammanmoins1 - J_FinverseTimeF.getEntry(1,0);
				
				// Compute precision
				precisionAlpha = Math.abs(alphan-alphanmoins1);
				precisionGamma = Math.abs(gamman-gammanmoins1);
				i++;
			}
			
			// Check if computed values are correct, else: restart
//			LOG.info("Restart");
//			LOG.info("alpha0="+alpha0+", alphanfinal="+alphan+" ou "+Math.toDegrees(alphan)+"°");
//			LOG.info("gamma0="+gamma0+", gammanfinal="+gamman+" ou "+Math.toDegrees(gamman)+"°");
			if (alpha0 < alpha0Max && (alphan <= 0 || alphan >= 2*Math.PI)) {
				restart = true;
				alpha0 += step;
			}
			if (gamma0 < gamma0Max && (gamman <= 0 || Math.toDegrees(gamman) >= gammaMax)) {
				restart = true;
				gamma0 += step;
			}
		}
		while(restart);
		
		double d = ri*Math.cos(alphan/2)+rf*Math.cos(gamman/2);
		
		List<Double> solutions = new ArrayList<Double>();
		solutions.add(alphan);
		solutions.add(gamman);
		solutions.add(d);
		return solutions;
	}
	/**
	 * Solve alpha, gamma, rf using Newton's method:
	 * ri*sin(alpha/2) - rf*sin(gamma/2) = 0
	 * ri^2*(alpha-sin(alpha))+rf^2*(gamma-sin(gamma))-2*PI*rf^2*obfuscationLevel=0
	 * ri*cos(alpha/2) + rf*cos(gamma/2) - d = 0
	 * @param initialLocation
	 * @param middleLocation
	 * @param obfuscationLevel
	 * @return
	 */
	private float solveAlphaGammaRfByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// Rename some variables
		float ri = initialLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double d = middleLocation.getShiftDistance();
		double d2 = Math.pow(d, 2);
		double rf0Max = 30*ri;
		double rfWhenAlpha360Deg = ri+d;

		// Initialization
		double rf0 = ri;
		double alpha;
		double gamma;
		double rf;	
		double precision = 0.01;
//		double step = 0.01;
		
		boolean restart;
//		int nbTour = 0;
		
//		double minRf = 0;
		double maxH = 0;
		// While correct values have not been computed
		do {
			restart = false;
			
			rf = rf0;
			double rf2 = Math.pow(rf, 2);
			if (d == 0 || ri == 0 || rf == 0 || rf >= rfWhenAlpha360Deg) {
	        	alpha = 2*Math.PI;
	        	gamma = 0;
	    	}
			else if (d > ri+rf) {
	    		alpha = 0;
	        	gamma = 0;
	    	}
	    	else {
	    		alpha = 2*Math.acos((ri2+d2-rf2)/(2*ri*d));
	        	gamma = 2*Math.acos((rf2+d2-ri2)/(2*rf*d));
	    	}
	    	
//			double f = ri*Math.sin(alpha/2)-rf*Math.sin(gamma/2);
//			double g = ri*Math.cos(alpha/2)+rf*Math.cos(gamma/2)-d;
//			double h = ri2*(alpha-Math.sin(alpha))+rf2*(gamma-Math.sin(gamma))-2*Math.PI*obfuscationLevel*rf2;
//			double h = rf2*Math.acos(d/rf)-d*Math.sqrt(rf2-d2)-2*Math.PI*obfuscationLevel*rf2;
//			double h = ri2*alpha+rf2*gamma-1/2*Math.sqrt((-d+ri+rf)*(d-ri+rf)*(d+ri-rf)*(d+ri+rf))-2*Math.PI*obfuscationLevel*rf2;
			double h = ri2/2*(alpha-Math.sin(alpha))+rf2/2*(gamma-Math.sin(gamma))-Math.PI*rf2*obfuscationLevel;
			if (0 == maxH || maxH < Math.abs(h)) {
				maxH = Math.abs(h);
			}
			
			
//			if (rf0 < rf0Max && (f < -precision || f > precision || g < -precision || g > precision || h < -precision ||  h > precision)) {
			if (rf0 < rf0Max && (h <= -precision ||  h >= precision)) {
				restart = true;
				double x = Math.abs(h)*4/(maxH+0.000001);
				double step = 2/Math.PI*Math.atan(x);
//				LOG.info(x+" "+step2+" "+Math.abs(h)+" "+step);
				rf0 += step;
			}
//			nbTour++;
//			if (!restart) {
//				System.out.println("Soluce "+nbTour+" : " +
//						"alpha="+Math.toDegrees(alpha)+"°, " +
//						"| gamma="+Math.toDegrees(gamma)+"°, " +
//						"| rf="+rf+" meters,"+
//						"| ri="+ri+" meters, "+
//						"| d="+d+" meters, "+
//						"h="+h+" (maxH="+maxH+")");
//				"f="+f+", g="+g+", h="+h+" (minH="+minH+")");
//			}
		}
		while(restart);
		
//		List<Double> solutions = new ArrayList<Double>();
//		solutions.add(alpha);
//		solutions.add(gamma);
//		solutions.add(rf);
		return (float) rf;
	}
	private List<Double> solveAlphaGammaRfByNewton2(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// Rename some variables
		double ri = initialLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double d = middleLocation.getShiftDistance();
		double d2 = Math.pow(d, 2);
		double gammaMax = 2*Math.atan(ri/d);
//		double gamma0Max = gammaMax;
		double rf0Max = 4*ri;
		double rfWhenAlpha180Deg = Math.sqrt(Math.pow(d, 2)+ri2);
		double rfWhenAlpha360Deg = ri+d;
		
		// Initialization
		double alpha0;
		double gamma0;
		double rf0 = ri;
		double precisionAlpha = 1;
		double precisionGamma = 1;
		double precisionRf = 1;
		double alphan;
		double gamman;
		double rfn;		
		boolean restart;
		boolean stop = false;
		boolean begin = true;
		int nbTour = 0;
		List<List<Double>> solutions = new ArrayList<List<Double>>();
		// While correct values have not been computed
		do {
			restart = false;
			
			// Values n-1 = initialization values
			rfn = rf0;
			double rfn2 = Math.pow(rfn, 2);
			if (d == 0 || ri == 0 || rfn == 0 || rfn >= rfWhenAlpha360Deg) {
	        	alpha0 = 2*Math.PI;
	        	gamma0 = 0;
	    	}
			else if (d > ri+rfn) {
	    		alpha0 = 0;
	        	gamma0 = 0;
	    	}
	    	else {
	    		alpha0 = 2*Math.acos((ri2+d2-rfn2)/(2*ri*d));
	        	gamma0 = 2*Math.acos((rfn2+d2-ri2)/(2*rfn*d));
	    	}
			alphan = alpha0;
			gamman = gamma0;
			precisionAlpha = 1;
			precisionGamma = 1;
			precisionRf = 1;
			nbTour++;
			// While a good precision have been reached
			int i = 0;
			while(i<nbMaxIteration && (precisionAlpha > precisionMax || precisionGamma > precisionMax || precisionRf > precisionMax)) {
				// Save precedent values
				double alphanmoins1 = alphan;
				double gammanmoins1 = gamman;
				double rfnmoins1 = rfn;
				LOG.info("alphan"+i+"="+alphan+" ("+precisionAlpha+"), gamman"+i+"="+gamman+" ("+precisionGamma+"), rf"+i+"="+rfn+" ("+precisionRf+")");
				
				if (Double.isNaN(alphanmoins1) || Double.isInfinite(alphanmoins1))
					alphanmoins1 = 0;
				if (Double.isNaN(gammanmoins1) || Double.isInfinite(gammanmoins1))
					gammanmoins1 = 0;
				
				// Compute functions and their derivates
				double rfnmoins12 = Math.pow(rfnmoins1, 2);
				double sinGamma = Math.sin(gammanmoins1);
				double cosAlphaOn2 = Math.cos(alphanmoins1/2);
				double sinAlphaOn2 = Math.sin(alphanmoins1/2);
				double cosGammaOn2 = Math.cos(gammanmoins1/2);
				double sinGammaOn2 = Math.sin(gammanmoins1/2);
				double f = ri*sinAlphaOn2-rfnmoins1*sinGammaOn2;
				double dfByAlpha = ri/2*cosAlphaOn2;
				double dfByGamma = -rfnmoins1/2*cosGammaOn2;
				double dfByRf = -sinGammaOn2;
				double g = ri2*(alphanmoins1-Math.sin(alphanmoins1))+rfnmoins12*(gammanmoins1-sinGamma)-2*Math.PI*obfuscationLevel*rfnmoins12;
				double dgByAlpha = ri2*(1-Math.cos(alphanmoins1));
				double dgByGamma = rfnmoins12*(1-Math.cos(gammanmoins1));
				double dgByRf = 2*rfnmoins1*(gammanmoins1-sinGamma)-4*Math.PI*obfuscationLevel*rfnmoins1;
				double h = ri*cosAlphaOn2+rfnmoins1*cosGammaOn2-d;
				double dhByAlpha = -ri/2*sinAlphaOn2;
				double dhByGamma = -rfnmoins1/2*sinGammaOn2;
				double dhByRf = cosGammaOn2;
				
				// Compute new values
		        /* Algorithm
				 * Xn = (alphan gamman rfn)
		         * F(Xn) = (f(Xn) g(Xn) h(Xn))
		         * J_F(Xn) = Jacobien matrice of F for Xn 
				 * Xn = Xn-1 - F(Xn-1)/J_F(Xn-1)
				 */
				// Create matrix
				double [][] valuesF = {{f}, {g}, {h}};
				double [][] valuesJ_F = {{dfByAlpha, dfByGamma, dfByRf}, {dgByAlpha, dgByGamma, dgByRf}, {dhByAlpha, dhByGamma, dhByRf}};
		        RealMatrix F = new Array2DRowRealMatrix(valuesF);
		        RealMatrix J_F = new Array2DRowRealMatrix(valuesJ_F);
		        RealMatrix J_FinverseTimeF = new LUDecompositionImpl(J_F).getSolver().getInverse().multiply(F);
		        // Compute alpha, gamman, rfn
				alphan = alphanmoins1 - J_FinverseTimeF.getEntry(0,0);
				gamman = gammanmoins1 - J_FinverseTimeF.getEntry(1,0);
				rfn = rfnmoins1 - J_FinverseTimeF.getEntry(2,0);

				// Compute precision
				precisionAlpha = Math.abs(alphan-alphanmoins1);
				precisionGamma = Math.abs(gamman-gammanmoins1);
				precisionRf = Math.abs(rfn-rfnmoins1);
				i++;
			}
			// If we don't wan't to stop there
			// or if we don't try with all rf0
			if (rf0 < rf0Max) {
				double alphanToDegrees = Math.toDegrees(alphan);
				double gammanToDegrees = Math.toDegrees(gamman);
				// TODO : alpha doit être inférieur à 180 si rf est < à d+un petit truc
				if ((rfn < rfWhenAlpha180Deg && alphan >= 180) || (rfn < ri+d && (alphanToDegrees <= 0 || alphanToDegrees >= 360))) {
					restart = true;
					rf0 += step;
				}
				else if (rfn < ri+d && (gammanToDegrees <= 0 || gammanToDegrees >= gammaMax)) {
					restart = true;
					rf0 += step;
				}
				else if (rfn < ri || rfn >= ri+d) {
					restart = true;
					rf0 += step;
				}
				else {
					List<Double> triplet = new ArrayList<Double>();
					triplet.add(alpha0);
					triplet.add(alphan);
					triplet.add(gamma0);
					triplet.add(gamman);
					triplet.add(rf0);
					triplet.add(rfn);
					triplet.add(nbTour+0.0);
					solutions.add(triplet);
					restart = true;
					rf0 += step;
				}
//				LOG.info("Restart");
//				LOG.info("alpha0="+alpha0+", alphanfinal="+alphan+" ou "+Math.toDegrees(alphan)+"°");
//				LOG.info("gamma0="+gamma0+", gammanfinal="+gamman+" ou "+Math.toDegrees(gamman)+"°");
//				LOG.info("rf0="+rf0+", rfnfinal="+rfn);
			}
		}
		while(restart);
		
		int j = 1;
		for(List<Double> triplet : solutions) {
			System.out.println("Soluce "+j+" ("+triplet.get(6)+"): " +
					"alpha0="+Math.toDegrees(triplet.get(0))+"°, alphan="+Math.toDegrees(triplet.get(1))+"°" +
					"| gamma0="+Math.toDegrees(triplet.get(2))+"°, gammanfinal="+Math.toDegrees(triplet.get(3))+"°" +
					"| rf0="+triplet.get(4)+" meters, rfnfinal="+triplet.get(5)+" meters");
			j++;
		}
		
		List<Double> solutions2 = new ArrayList<Double>();
		solutions2.add(solutions.get(0).get(1));
		solutions2.add(solutions.get(0).get(3));
		solutions2.add(solutions.get(0).get(5));
//		for(Double tmp : solutions) {
//			LOG.info("solucef = "+tmp+" ("+Math.toDegrees(tmp)+"°)");
//		}
		return solutions2;
	}
}
