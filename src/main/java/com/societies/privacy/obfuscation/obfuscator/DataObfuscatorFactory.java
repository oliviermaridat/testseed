/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.net.URI;
import java.net.URISyntaxException;

import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.obfuscation.IDataObfuscator;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class DataObfuscatorFactory {
	public static IDataObfuscator<Object> getDataObfuscator(ObfuscationType obfuscationType) {
		try {
			if (obfuscationType.getUri().equals(new URI("http://societies/data/context/geolocation"))) {
				return new GeolocationObfuscator();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("No Obfuscator to obfuscate that type of data");
	}
}
