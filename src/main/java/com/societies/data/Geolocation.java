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
	private float obfuscationLevel;
	private double shiftDirection;
	private double shiftDistance;
	
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
				+ ", horizontalAccuracy=" + horizontalAccuracy
				+ ", obfuscationLevel=" + obfuscationLevel + "]";
	}
	public String toXML() {
		return "<geolocation>\n" +
				"\t<latitude>" + latitude + "</latitude>\n" +
				"\t<longitude>" + longitude+ "</longitude>\n" +
				"\t<horizontalAccuracy>" + horizontalAccuracy + "</horizontalAccuracy>\n" +
				"\t<obfuscationLevel>" + obfuscationLevel + "</obfuscationLevel>\n" +
			"</geolocation>";
	}
	public String toJSON() {
		return "{\n" +
				"\"latitude\": \""+latitude+"\"\n" +
				"\"longitude\": \""+longitude+"\"\n" +
				"\"horizontalAccuracy\": \""+horizontalAccuracy+"\"\n" +
				"\"obfuscationLevel\": \""+obfuscationLevel+"\"\n" +
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


	public float getObfuscationLevel() {
		return obfuscationLevel;
	}


	public void setObfuscationLevel(float obfuscationLevel) {
		this.obfuscationLevel = obfuscationLevel;
	}
	

	/**
	 * @return the shiftDirection
	 */
	public double getShiftDirection() {
		return shiftDirection;
	}


	/**
	 * @param shiftDirection the shiftDirection to set
	 */
	public void setShiftDirection(double shiftDirection) {
		this.shiftDirection = shiftDirection;
	}


	/**
	 * @return the shiftDistance
	 */
	public double getShiftDistance() {
		return shiftDistance;
	}


	/**
	 * @param d the shiftDistance to set
	 */
	public void setShiftDistance(double d) {
		this.shiftDistance = d;
	}
}
