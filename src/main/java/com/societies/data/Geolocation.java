/**
 * 
 */
package com.societies.data;

/**
 * @author olivierm
 * @date 25 août 2011
 */
public class Geolocation {
	protected double latitude;
	protected double longitude;
	protected float horizontalAccuracy;
	
	
	/* -- Constructor -- */
	public Geolocation(double latitude, double longitude,
			float horizontalAccuracy) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.horizontalAccuracy = horizontalAccuracy;
	}
	

	/* --- Get/set --- */
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Geolocation [latitude=" + latitude + ", longitude=" + longitude
				+ ", horizontalAccuracy=" + horizontalAccuracy + "]";
	}
	public String toXML() {
		return "<geolocation>\n" +
				"\t<latitude>" + latitude + "</latitude>\n" +
				"\t<longitude>" + longitude+ "</longitude>\n" +
				"\t<horizontalAccuracy>" + horizontalAccuracy + "</horizontalAccuracy>\n" +
			"</geolocation>";
	}
	public String toJSON() {
		return "{\n" +
				"\"latitude\": \""+latitude+"\"\n" +
				"\"longitude\": \""+longitude+"\"\n" +
				"\"horizontalAccuracy\": \""+horizontalAccuracy+"\"\n" +
				"}";
	}
	
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the horizontalAccuracy
	 */
	public float getHorizontalAccuracy() {
		return horizontalAccuracy;
	}
	/**
	 * @param horizontalAccuracy the horizontalAccuracy to set
	 */
	public void setHorizontalAccuracy(float horizontalAccuracy) {
		this.horizontalAccuracy = horizontalAccuracy;
	}
}
