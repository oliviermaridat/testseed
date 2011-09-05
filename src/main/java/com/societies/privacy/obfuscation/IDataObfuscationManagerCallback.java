/**
 * 
 */
package com.societies.privacy.obfuscation;

/**
 * @author olivierm
 * @date 25 août 2011
 */
public interface IDataObfuscationManagerCallback<E> {
	/**
	 * See the result of the obfuscation
	 * @param obfuscatedData Obfuscated data
	 */
	public void obfuscationResult(E obfuscatedData);
	/**
	 * See cancel message
	 * @param msg Message
	 */
	public void cancel(String msg);
}
