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
		// Select a random direction for the shifting
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
		// Select an intermediate obfuscation level > obfuscation level
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		// Select a random direction for the shifting
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
		// Select an intermediate obfuscation level > obfuscation level
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		// Select a random direction for the shifting
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
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel, theta);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// -- Enlarge
		/* Solve the following system
		 * ri*sin(alpha/2) - rf*sin(gamma/2) = 0
		 * ri^2/2*(alpha-sin(alpha))+rf^2/2*(gamma-sin(gamma))-PI*rf^2*obfuscationLevel=0
		 * ri*cos(alpha/2) + rf*cos(gamma/2) - d = 0
		 * 
		 * obfuscationLevel, ri and d are well-known
		 */
		float rf = solveAlphaGammaRfByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel, true);
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
		// Select an intermediate obfuscation level > obfuscation level
		float middleObfuscationLevel = rand.nextFloatBetween(obfuscationLevel, 1);
		// Select a random direction for the shifting
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
		/* Solve the following system
		 * ri*sin(alpha/2) - rf*sin(gamma/2) = 0
		 * ri^2/2*(alpha-sin(alpha))+rf^2/2*(gamma-sin(gamma))-PI*rf^2*obfuscationLevel=0
		 * ri*cos(alpha/2) + rf*cos(gamma/2) - d = 0
		 * 
		 * obfuscationLevel, ri and d are well-known
		 */
		float rf = solveAlphaGammaRfByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel, false);
		finalObfuscatedGeolocation = new Geolocation(middleObfuscatedGeolocation.getLatitude(), middleObfuscatedGeolocation.getLongitude(), rf);
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
	private float solveAlphaGammaRfByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel, boolean enlargement) {
		// Rename some variables
		float ri = initialLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double d = middleLocation.getShiftDistance();
		double d2 = Math.pow(d, 2);
		double rf0Max = 100*ri;

		// Initialization
		double rf0 = ri;
		double alpha;
		double gamma;
		double rf;	
		double precision = 0.001;
		boolean restart;
		double maxH = 0;
		double minH = 0;
		int nbTour = 0;
		
		// While correct values have not been computed
		do {
			restart = false;
			
			// Select rf
			rf = rf0;
			// Compute alpha and gamma
			double rf2 = Math.pow(rf, 2);
			// Ci are in Cf or they are similar
			if (d == 0 || rf == 0 || rf >= (ri+d)) {
	        	alpha = 2*Math.PI;
	        	gamma = 0;
	    	}
			// Cf are in Ci
			else if ((d <= ri && ri >= (d+rf)) || ri == 0) {
				alpha = 0;
				gamma = 2*Math.PI;
			}
			// Circles are disjoints
			else if (d > ri+rf) {
	    		alpha = 0;
	        	gamma = 0;
	    	}
	    	else {
	    		alpha = 2*Math.acos((ri2+d2-rf2)/(2*ri*d));
	        	gamma = 2*Math.acos((rf2+d2-ri2)/(2*rf*d));
	    	}
	    	
			// Compute the function h that must be equals to 0
			double Aintersection = ri2/2*(alpha-Math.sin(alpha))+rf2/2*(gamma-Math.sin(gamma));
			double Atotal = Math.PI*ri2;
			if (rf > ri) {
				Atotal = Math.PI*rf2;
			}
			double h = Aintersection/Atotal-obfuscationLevel;
			
			// If h != 0 : restart
			if (rf0 > 0 && rf0 < rf0Max && (h <= -precision ||  h >= precision)) {
				// - Compute the step, to go faster when we are far away of the solution
				// Select maxH
				if (0 == maxH || maxH < Math.abs(h)) {
					maxH = Math.abs(h);
				}
				if (0 == minH || minH > Math.abs(h)) {
					minH = Math.abs(h);
				}
				/* ALGO
				 * We want a function  f: 
				 * lim (x -> 0) f = 0
				 * lim (x -> infinity) f = m
				 * arctan (with x > 0 and < m) is a good function 
				 */
				double m = 2;
				double step = 2*m/Math.PI*Math.atan(Math.abs(h));
//				LOG.info(Math.abs(h)+" "+step);
				// We continy only if rf0 can be > 0
				if (rf0 > step) {
					restart = true;
					if (enlargement) {
						rf0 += step;
					}
					else {
						rf0 -= step;
					}
				}
			}
			
			nbTour++;
//			if (!restart) {
//				System.out.println("Soluce "+nbTour+" : " +
//						"alpha="+Math.toDegrees(alpha)+"°, " +
//						"| gamma="+Math.toDegrees(gamma)+"°, " +
//						"| rf="+rf+" meters,"+
//						"| ri="+ri+" meters, "+
//						"| d="+d+" meters, "+
//						"h="+h+" (maxH="+maxH+", minH="+minH+")");
//			}
		}
		while(restart);
		return (float) rf;
	}
}
