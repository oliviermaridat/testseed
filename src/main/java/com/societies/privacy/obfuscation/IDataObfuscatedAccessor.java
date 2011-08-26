/**
 * 
 */
package com.societies.privacy.obfuscation;

import com.societies.privacy.data.ObfuscationType;

/**
 * @author olivierm
 * @date 25 août 2011
 */
public interface IDataObfuscatedAccessor {
	public void getObfuscatedData(int dataId, ObfuscationType obfuscationType, float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception;
}
