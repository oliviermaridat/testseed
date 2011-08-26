/**
 * 
 */
package com.societies.privacy.data;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class ObfuscationType {
	protected URI uri;

	
	/* -- Constructor -- */
	public ObfuscationType(URI uri) {
		super();
		this.uri = uri;
	}
	public ObfuscationType(String uri) {
		super();
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/* --- Get/set --- */
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObfuscationType [uri=" + uri + "]";
	}

	/**
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}	
}
