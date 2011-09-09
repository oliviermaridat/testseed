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

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscatorV4 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV4.class);
	private static final boolean DEBUG = true;
	
	private final int OPERATION_E = 0;
	private final int OPERATION_R = 1;
	private final int OPERATION_S = 2;
	private final int OPERATION_ES = 3;
	private final int OPERATION_SE = 4;
	private final int OPERATION_SR = 5;
	
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// -- Verifications
		if (!(data instanceof Geolocation)) {
			throw new Exception("It's not the right obfuscation algorithm!");
		}
		
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
		Random rand = new Random();
		int algorithm = rand.nextInt(6);
//		algorithm = 4;
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
		Geolocation obfuscatedGeolocation = null;
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinxMoinsC(Math.PI*obfuscationLevel);
//		List<Double> solutions = solveXYByNewton(geolocation, geolocation, obfuscationLevel);
//		double alpha = solutions.get(0);
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
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Enlarge
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel+obfuscationLevel/7) {}
		middleObfuscatedGeolocation = EObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// Shift
		
		// Select a random theta: shift angle
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		List<Double> solutions = solveXYByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel);
		double alpha = solutions.get(0);
		double gamma = solutions.get(1);
		double d = geolocation.getHorizontalAccuracy()*Math.cos(alpha/2)+middleObfuscatedGeolocation.getHorizontalAccuracy()*Math.cos(gamma/2);
//				LOG.info("Distance="+d);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		Geolocation obfuscatedGeolocation = GeolocationUtils.shitLatLgn(middleObfuscatedGeolocation, theta, d);
		obfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return obfuscatedGeolocation;
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
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Shift
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) > obfuscationLevel-obfuscationLevel/7) {}
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// Enlarge
		finalObfuscatedGeolocation = changeRadiusAfterShifting(middleObfuscatedGeolocation, obfuscationLevel);
//		List<Double> solutions = solveXYZByNewton(geolocation, middleObfuscatedGeolocation, obfuscationLevel, 1, 1, geolocation.getHorizontalAccuracy());
//		for(Double d : solutions) {
//			LOG.info("soluce = "+d);
//		}
//		finalObfuscatedGeolocation = new Geolocation(middleObfuscatedGeolocation.getLatitude(), middleObfuscatedGeolocation.getLongitude(), solutions.get(2).floatValue());
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
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Shift
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel+obfuscationLevel/7) {}
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// Reduce
		finalObfuscatedGeolocation = changeRadiusAfterShifting(middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	
	/**
	 * Enlarge or reduce the radius depending
	 * of the previous shifting of the geolocation circle centre
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation changeRadiusAfterShifting(Geolocation geolocation, float obfuscationLevel) {
		// -- Compute the alpha needed by this obfuscation level
		double alpha = solveXMoinsSinxMoinsC(Math.PI*obfuscationLevel);
		double cosAlphaOn2 = Math.cos(alpha/2);
		if (0 == cosAlphaOn2) {
			cosAlphaOn2 = 0.0000000000000001;
		}
		// -- The distance between the two circles is fixed
		// So we can compute the radius (= hAccuracy) needed by this obfuscation level
		float horizontalAccuracy = (float) (geolocation.getShiftDistance()/(2*cosAlphaOn2));
		
		
		// -- Generate a location
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), horizontalAccuracy);
		return obfuscatedGeolocation;
	}
	
	/**
	 * Solve x-sin(x)-C=0 with Newton Method
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
	
	private List<Double> solveXYByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		double alpha0 = 1;
		double gamma0 = 1;
		return solveXYByNewton(initialLocation, middleLocation, obfuscationLevel, alpha0, gamma0);
	}
	private List<Double> solveXYByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel, double alpha0, double gamma0) {
		double ri = initialLocation.getHorizontalAccuracy();
		double rf = middleLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double rf2 = Math.pow(rf, 2);
		double C = 2*Math.PI*obfuscationLevel*rf*ri;
		double alphan = alpha0;
		double gamman = gamma0;
		double precisionAlpha = alpha0;
		double precisionGamma = gamma0;
		double step = 0.5;
		double maxAlpha0 = 360;
		double maxGamma0 = 360;
		double precisionMax = 0.0000000000000000001;
		int nbMaxIteration = 15;
		int i = 0;
		while(i<nbMaxIteration && (precisionAlpha > precisionMax || precisionGamma > precisionMax)) {
			// Save precedent values
			double alphanmoins1 = alphan;
			double gammanmoins1 = gamman;
//			LOG.info("alphan"+i+"="+alphan+" ("+precisionAlpha+"), gamman"+i+"="+gamman+" ("+precisionGamma+")");
			// Compute functions and their derivates
			double f = ri*Math.sin(alphanmoins1/2)-rf*Math.sin(gammanmoins1/2);
			double dfByAlpha = ri/2*Math.cos(alphanmoins1/2);
			double dfByGamma = -rf/2*Math.cos(gammanmoins1/2);
			double g = ri2*(alphanmoins1-Math.sin(alphanmoins1))+rf2*(gammanmoins1-Math.sin(gammanmoins1))-C;
			double dgByAlpha = ri2*(1-Math.cos(alphanmoins1));
			double dgByGamma = rf2*(1-Math.cos(gammanmoins1));
			double delta = dfByAlpha*dgByGamma-dfByGamma*dgByAlpha;
			// Compute new values
			alphan = alphanmoins1 - (f*dgByGamma-g*dfByGamma)/delta;
			gamman = gammanmoins1 - (g*dfByAlpha-f*dgByAlpha)/delta;
			// Compute precision
			precisionAlpha = Math.abs(alphan-alphanmoins1);
			precisionGamma = Math.abs(gamman-gammanmoins1);
			i++;
		}
		// Check the sens and restart if necessary
		boolean restart = false;
		double alphanInDegree = Math.toDegrees(alphan);
		double gammanInDegree = Math.toDegrees(gamman);
//		LOG.info("alphanfinal="+alphan+" ou "+alphanInDegree+"°");
//		LOG.info("gammanfinal="+gamman+" ou "+gammanInDegree+"°");
		if (alpha0 < maxAlpha0 && (alphanInDegree <= 0 || alphanInDegree >= 360)) {
			restart = true;
			alpha0 += step;
//			LOG.info("restart alpha");
		}
		if (gamma0 < maxGamma0 && (gammanInDegree <= 0 || gammanInDegree >= 360)) {
			restart = true;
			gamma0 += step;
//			LOG.info("restart gamma");
		}
		// Restart with different initialization
		if (restart) {
			return solveXYByNewton(initialLocation, middleLocation, obfuscationLevel, alpha0, gamma0);
		}
		List<Double> solutions = new ArrayList<Double>();
		solutions.add(alphan);
		solutions.add(gamman);
		return solutions;
	}
	
	private List<Double> solveXYZByNewton(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel, double alpha0, double gamma0, double rf0) {
		double ri = initialLocation.getHorizontalAccuracy();
		double d = middleLocation.getShiftDistance();
		double ri2 = Math.pow(ri, 2);
//		double C = 2*Math.PI*obfuscationLevel*rf*ri;
		double alphan = alpha0;
		double gamman = gamma0;
		double rfn = rf0;
		double precisionAlpha = alpha0;
		double precisionGamma = gamma0;
		double precisionRf = rf0;
		double step = 0.5;
		double maxAlpha0 = 360;
		double maxGamma0 = 360;
		double maxRf0 = ri+30000;
		double precisionMax = 0.0000000000000000001;
		int nbMaxIteration = 15;
		int i = 0;
		while(i<nbMaxIteration && (precisionAlpha > precisionMax || precisionGamma > precisionMax || precisionRf > precisionMax)) {
			// Save precedent values
			double alphanmoins1 = alphan;
			double gammanmoins1 = gamman;
			double rfnmoins1 = rfn;
			LOG.info("alphan"+i+"="+alphan+" ("+precisionAlpha+"), gamman"+i+"="+gamman+" ("+precisionGamma+"), rf"+i+"="+rfn+" ("+precisionRf+")");
			// Compute functions and their derivates
			double f = ri*Math.sin(alphanmoins1/2)-rfnmoins1*Math.sin(gammanmoins1/2);
			double dfByAlpha = ri/2*Math.cos(alphanmoins1/2);
			double dfByGamma = -rfnmoins1/2*Math.cos(gammanmoins1/2);
			double dfByRf = -Math.sin(gammanmoins1/2);
			double g = ri2*(alphanmoins1-Math.sin(alphanmoins1))+Math.pow(rfnmoins1, 2)*(gammanmoins1-Math.sin(gammanmoins1))-2*Math.PI*obfuscationLevel*rfnmoins1*ri;
			double dgByAlpha = ri2*(1-Math.cos(alphanmoins1));
			double dgByGamma = Math.pow(rfnmoins1, 2)*(1-Math.cos(gammanmoins1));
			double dgByRf = 2*rfnmoins1*(gammanmoins1-Math.sin(gammanmoins1))-2*Math.PI*obfuscationLevel*ri;
			double h = ri*Math.cos(alphanmoins1/2)+rfnmoins1*Math.cos(gammanmoins1/2)-d;
			double dhByAlpha = -ri/2*Math.sin(alphanmoins1/2);
			double dhByGamma = -rfnmoins1/2*Math.sin(gammanmoins1/2);
			double dhByRf = Math.cos(gammanmoins1/2);
			
			double [][] valuesF = {{f}, {g}, {h}};
			double [][] valuesJ_F = {{dfByAlpha, dfByGamma, dfByRf}, {dgByAlpha, dgByGamma, dgByRf}, {dhByAlpha, dhByGamma, dhByRf}};
	        RealMatrix F = new Array2DRowRealMatrix(valuesF);
//	        System.out.println("F matrix: " + F);
	        RealMatrix J_F = new Array2DRowRealMatrix(valuesJ_F);
//	        System.out.println("J_F matrix: " + J_F);
	        RealMatrix J_Finverse = new LUDecompositionImpl(J_F).getSolver().getInverse();
	        RealMatrix tmp = J_Finverse.multiply(F);
	        
			// Compute new values
			alphan = alphanmoins1 - tmp.getEntry(0,0);
			gamman = gammanmoins1 - tmp.getEntry(1,0);
			rfn = rfnmoins1 - tmp.getEntry(2,0);
			// Compute precision
			precisionAlpha = Math.abs(alphan-alphanmoins1);
			precisionGamma = Math.abs(gamman-gammanmoins1);
			precisionRf = Math.abs(rfn-rfnmoins1);
			i++;
		}
		// Check the sens and restart if necessary
		boolean restart = false;
		double alphanInDegree = Math.toDegrees(alphan);
		double gammanInDegree = Math.toDegrees(gamman);
		LOG.info("alphanfinal="+alphan+" ou "+alphanInDegree+"°");
		LOG.info("gammanfinal="+gamman+" ou "+gammanInDegree+"°");
		LOG.info("rfnfinal="+rfn);
		if (alpha0 < maxAlpha0 && (alphanInDegree <= 0 || alphanInDegree >= 360)) {
			restart = true;
			alpha0 += step;
//			LOG.info("restart alpha");
		}
		if (gamma0 < maxGamma0 && (gammanInDegree <= 0 || gammanInDegree >= 360)) {
			restart = true;
			gamma0 += step;
//			LOG.info("restart gamma");
		}
		if (rf0 < maxRf0 && (rfn <= ri)) {
			restart = true;
			rf0 += step;
//			LOG.info("restart rf");
		}
		// Restart with different initialization
		if (restart) {
			LOG.info("Restart");
			return solveXYZByNewton(initialLocation, middleLocation, obfuscationLevel, alpha0, gamma0, rf0);
		}
		LOG.info("Finish");
		List<Double> solutions = new ArrayList<Double>();
		solutions.add(alphan);
		solutions.add(gamman);
		solutions.add(rfn);
		for(Double tmp : solutions) {
			LOG.info("solucef = "+tmp);
		}
		return solutions;
	}
}
