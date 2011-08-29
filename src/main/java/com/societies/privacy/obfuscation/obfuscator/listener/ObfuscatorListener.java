package com.societies.privacy.obfuscation.obfuscator.listener;

import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.privacy.obfuscation.IDataObfuscationManagerCallback;

public class ObfuscatorListener implements IDataObfuscationManagerCallback<Object> {
	private static final Logger LOG = Logger.getLogger(ObfuscatorListener.class);

	public void obfuscationResult(Object obfuscatedData) {
		if (obfuscatedData instanceof Geolocation) {
			System.out.println(((Geolocation) obfuscatedData).toJSON());
		}		
	}

	public void cancel(String msg) {
		LOG.info("Cancel:"+msg);
	}

}
