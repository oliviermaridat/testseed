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
	/**
	 * Data obfuscation method
	 * @param data Data to obfuscate
	 * @param obfuscationType Type of obfuscation algorithm to use with this type of data
	 * @param obfuscationLevel Obfuscation level
	 * @param callback Listener to see the result
	 * @throws Exception
	 */
	public void obfuscateData(E data, ObfuscationType obfuscationType, float obfuscationLevel, IDataObfuscationManagerCallback<E> callback) throws Exception;
}
