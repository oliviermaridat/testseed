/**
 * 
 */
package com.societies.privacy.obfuscation;

import com.societies.privacy.data.ObfuscationType;


/**
 * @author olivierm
 * @date 25 août 2011
 */
public interface IDataObfuscator<E> {
	public void obfuscateData(E data, ObfuscationType obfuscationType, float obfuscationLevel, IDataObfuscationManagerCallback<E> callback) throws Exception;
}
