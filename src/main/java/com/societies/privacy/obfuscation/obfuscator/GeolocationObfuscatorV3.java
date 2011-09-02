/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.util.Random;

import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.data.GeolocationUtils;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.obfuscation.IDataObfuscationManagerCallback;
import com.societies.privacy.obfuscation.IDataObfuscator;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscatorV3 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV3.class);
	
	private int OPERATION_E = 1;
	private int OPERATION_R = 2;
	private int OPERATION_S = 3;
	private int OPERATION_ES = 4;
	private int OPERATION_SE = 5;
	private int OPERATION_SR = 6;
	
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// Verifications
		if (!(data instanceof Geolocation)) {
			throw new Exception("It's not the right obfuscation algorithm!");
		}
		if (null == callback || null == obfuscationType) {
			throw new Exception("Wrong parameters");
		}
		
		// Algorithm
		Geolocation obfuscatedGeolocation = obfuscateLocation((Geolocation) data, obfuscationLevel);
		
		// Send to callback
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
		
		*/
		Geolocation obfuscatedGeolocation = null;
		obfuscatedGeolocation = ESObfuscation(geolocation, obfuscationLevel);
		return obfuscatedGeolocation;
	}
	
	private Geolocation EObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = null;
		obfuscatedGeolocation = enlargeRadius(geolocation, obfuscationLevel);
		return obfuscatedGeolocation;
	}
	private Geolocation RObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = null;
		obfuscatedGeolocation = reduceRadius(geolocation, obfuscationLevel);
		return obfuscatedGeolocation;
	}
	private Geolocation SObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = null;
		obfuscatedGeolocation = shiftCentre(geolocation, obfuscationLevel);
		return obfuscatedGeolocation;
	}
	private Geolocation ESObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Enlarge
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel) {}
		middleObfuscatedGeolocation = enlargeRadius(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		
		// Shift
		finalObfuscatedGeolocation = shiftCentreAfterEnlarging(geolocation, middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	private Geolocation SEObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Shift
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) > obfuscationLevel) {}
		middleObfuscatedGeolocation = shiftCentre(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		
		// Enlarge
		finalObfuscatedGeolocation = changeRadiusAfterShifting(middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	
	private Geolocation SRObfuscation(Geolocation geolocation, float obfuscationLevel) {
		Geolocation finalObfuscatedGeolocation = null;
		Geolocation middleObfuscatedGeolocation = null;
		
		// Shift
		Random rand = new Random();
		float middleObfuscationLevel = 0;
		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel) {}
		middleObfuscatedGeolocation = shiftCentre(geolocation, middleObfuscationLevel);
		middleObfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
		System.out.println(middleObfuscatedGeolocation.toJSON()+",");
		
		// Reduce
		finalObfuscatedGeolocation = changeRadiusAfterShifting(middleObfuscatedGeolocation, obfuscationLevel);
		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
		return finalObfuscatedGeolocation;
	}
	
	private Geolocation enlargeRadius(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
		return obfuscatedGeolocation;
	}
	private Geolocation reduceRadius(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()*((float) Math.sqrt(obfuscationLevel)));
		return obfuscatedGeolocation;
	}
	private Geolocation changeRadiusAfterShifting(Geolocation geolocation, float obfuscationLevel) {
		double alpha = solveXMoinsSinx(obfuscationLevel);
		double cosAlphaOn2 = Math.cos(alpha/2);
		if (0 == cosAlphaOn2) {
			cosAlphaOn2 = 0.0000000001;
		}
		float horizontalAccuracy = (float) (geolocation.getShiftDistance()/(2*cosAlphaOn2));
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), horizontalAccuracy);
		return obfuscatedGeolocation;
	}
	
	private Geolocation shiftCentre(Geolocation geolocation, float obfuscationLevel) {
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinx(obfuscationLevel);
		double d = 2*geolocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		Geolocation obfuscatedGeolocation = GeolocationUtils.shitLatLgn(geolocation, theta, d);
		obfuscatedGeolocation.setShiftDirection(theta);
		obfuscatedGeolocation.setShiftDistance(d);
		
		return obfuscatedGeolocation;
	}
	
	private Geolocation shiftCentreAfterEnlarging(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinx(obfuscationLevel);
		double d = 2*initialLocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		Geolocation tmpobfuscatedGeolocation = GeolocationUtils.shitLatLgn(initialLocation, theta, d);
		System.out.println(tmpobfuscatedGeolocation.toJSON()+",");
		Geolocation obfuscatedGeolocation = GeolocationUtils.shitLatLgn(middleLocation, theta, d+middleLocation.getHorizontalAccuracy()-initialLocation.getHorizontalAccuracy());
		return obfuscatedGeolocation;
	}
	
	/**
	 * Solve x-sin(x)=PI*obfuscationLevel with Newton Method
	 * @param obfuscationLevel
	 * @return x
	 */
	private double solveXMoinsSinx(float obfuscationLevel) {
		// -- Find x in x-sin(x)=PI*obfuscationLevel
		/* Computation algorithm
		We use Newton Method
		f(x)=x-sin(x)-pi*obfuscationLevel
		f'(x)=1-cos(x)
		xn = xnmoins - f(x)/f'(x)
		
		The difficulty is initialization, but :
		A sign study show that f is growing
		And f(PI/2)=-1.62, and f(PI)=0.9
		So, we choose a value between PI/2 and PI, for example: 2
		
		Five iterations may be enough
		*/
		double xn = 2;
		double C = Math.PI*obfuscationLevel;
		for (int i = 0; i<5; i++) {
			double xnmoins1 = xn;
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
		}	
		return xn;
	}
}
