/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.net.URI;
import java.net.URISyntaxException;

import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.data.ObfuscationTypes;
import com.societies.privacy.obfuscation.IDataObfuscator;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class DataObfuscatorFactory {
	public static IDataObfuscator<Object> getDataObfuscator(ObfuscationType obfuscationType) {
		try {
			if (obfuscationType.getUri().equals(new URI(ObfuscationTypes.GEOLOCATION))) {
				return new GeolocationObfuscator();
			}
			if (obfuscationType.getUri().equals(new URI(ObfuscationTypes.GEOLOCATIONV2))) {
				return new GeolocationObfuscatorV2();
			}
			if (obfuscationType.getUri().equals(new URI(ObfuscationTypes.GEOLOCATIONV3))) {
				return new GeolocationObfuscatorV3();
			}
			if (obfuscationType.getUri().equals(new URI(ObfuscationTypes.GEOLOCATIONV4))) {
				return new GeolocationObfuscatorV4();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("No Obfuscator to obfuscate that type of data");
		}
		throw new RuntimeException("No Obfuscator to obfuscate that type of data");
	}
}
