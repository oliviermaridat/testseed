/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.util.Random;

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
public class GeolocationObfuscatorV3 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV3.class);
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
//		algorithm = 3;
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
		while((middleObfuscationLevel = rand.nextFloat()) > obfuscationLevel-obfuscationLevel/3) {}
		middleObfuscatedGeolocation = EObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// Shift
		finalObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscatedGeolocation.getObfuscationLevel()/obfuscationLevel);
		if (DEBUG) {
			finalObfuscatedGeolocation.setObfuscationLevel(middleObfuscatedGeolocation.getObfuscationLevel()/obfuscationLevel);
			System.out.println(finalObfuscatedGeolocation.toJSON()+",");
		}
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		finalObfuscatedGeolocation.setHorizontalAccuracy(middleObfuscatedGeolocation.getHorizontalAccuracy());
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
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Shift
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) > obfuscationLevel-obfuscationLevel/3) {}
		middleObfuscatedGeolocation = SObfuscation(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		if (DEBUG) {
			System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		}
		
		// Enlarge
		finalObfuscatedGeolocation = changeRadiusAfterShifting(middleObfuscatedGeolocation, obfuscationLevel);
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
		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel+obfuscationLevel/3) {}
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
			cosAlphaOn2 = 0.0000000001;
		}
		// -- The distance between the two circles is fixed
		// So we can compute the radius (= hAccuracy) needed by this obfuscation level
		float horizontalAccuracy = (float) (geolocation.getShiftDistance()/(2*cosAlphaOn2));
		
		// -- Generate a location
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), horizontalAccuracy);
		return obfuscatedGeolocation;
	}
	/**
	 * Shift the geolocation circle centre after
	 * an enlargement of the radius
	 * @param initialLocation Location to obfuscate
	 * @param middleLocation Location enlarged
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation shiftCentreAfterEnlarging(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinxMoinsC(middleLocation.getObfuscationLevel()/obfuscationLevel);
		double d = 2*initialLocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		if (DEBUG) {
			Geolocation tmpobfuscatedGeolocation = GeolocationUtils.shitLatLgn(initialLocation, theta, d);
			tmpobfuscatedGeolocation.setObfuscationLevel(middleLocation.getObfuscationLevel()/obfuscationLevel);
			System.out.println(tmpobfuscatedGeolocation.toJSON()+",");
		}
		Geolocation obfuscatedGeolocation = GeolocationUtils.shitLatLgn(middleLocation, theta, d);
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
		
		Five iterations may be enough
		*/
		double xn = 2;
		for (int i = 0; i<10; i++) {
			double xnmoins1 = xn;
			if (0 == xnmoins1) {
				xnmoins1 = 0.0000000001;
			}
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
		}	
		return xn;
	}
}
