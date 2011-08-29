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

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscatorV2 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV2.class);
	
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
		// Algo 1
		//geolocation.setHorizontalAccuracy(geolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
		
		// Algo 3
		LOG.info("Algo 3");
		Random rand = new Random();
		double theta = Math.toRadians(rand.nextDouble()*360);
		double alpha = solveXMoinsSinx(obfuscationLevel);
		LOG.info("alpha final = "+alpha);
		LOG.info("cos alpha/2= "+Math.cos(alpha/2));
		double d = 2*geolocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		LOG.info("d = "+d);
		// /!\ We can't add 1000meters to a latitude (in degrees) like that!
		geolocation.setLatitude(geolocation.getLatitude()+d*Math.sin(theta));
		geolocation.setLongitude(geolocation.getLongitude()+d*Math.cos(theta));
		
		return geolocation;
	}
	
	/**
	 * Solve x-sin(x)=PI*obfuscationLevel
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
			LOG.info("alpha"+i+" = "+xn);
			double xnmoins1 = xn;
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
		}	
		return xn;
	}
}
