/**
 * 
 */
package com.societies.data.accessor;

import com.societies.data.Geolocation;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationDAO {
	public Geolocation findGeolocationById(int id) {
		return new Geolocation(48.856666, 2.350987, 542);
	}
}
