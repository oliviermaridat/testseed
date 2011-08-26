/**
 * 
 */
package com.societies.privacy.obfuscation;

/**
 * @author olivierm
 * @date 25 août 2011
 */
public interface IDataObfuscationManagerCallback<E> {
	public void obfuscationResult(E obfuscatedData);
	public void cancel(String msg);
}
