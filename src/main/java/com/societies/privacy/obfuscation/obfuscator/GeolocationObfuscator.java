/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.obfuscation.IDataObfuscationManagerCallback;
import com.societies.privacy.obfuscation.IDataObfuscator;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscator implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscator.class);
	
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		if (!(data instanceof Geolocation)) {
			throw new Exception("It's not the right obfuscation algorithm!");
		}
		Geolocation obfuscatedGeolocation = (Geolocation) data;
		obfuscatedGeolocation.setHorizontalAccuracy(obfuscatedGeolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
//		LOG.info("Obfuscated location: "+obfuscatedLocation);
		System.out.println(obfuscatedGeolocation.toJSON());
	}
}
