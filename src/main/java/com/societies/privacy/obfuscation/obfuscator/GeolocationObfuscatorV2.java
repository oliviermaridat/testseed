/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.util.Random;

import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.obfuscation.IDataObfuscationManagerCallback;
import com.societies.privacy.obfuscation.IDataObfuscator;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class GeolocationObfuscatorV2 implements IDataObfuscator<Object> {
	private static final Logger LOG = Logger.getLogger(GeolocationObfuscatorV2.class);
	
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// Verifications
		if (!(data instanceof Geolocation)) {
			throw new Exception("It's not the right obfuscation algorithm!");
		}
		if (null == callback || null == obfuscationType) {
			throw new Exception("Wrong parameters");
		}
		
		// Algorithm
		Geolocation obfuscatedGeolocation = obfuscateLocation((Geolocation) data, obfuscationLevel);
		
		// Send to callback
		callback.obfuscationResult(obfuscatedGeolocation);
	}
	
	/**
	 * Location obfuscation algorithm
	 * @param geolocation Location to obfuscate
	 * @param obfuscationLevel Obfuscation level
	 * @return obfuscated location
	 */
	private Geolocation obfuscateLocation(Geolocation geolocation, float obfuscationLevel) {
		/* ALGORITHM
		
		*/
		
		
		// Test
//		double lat1 = sexagecimal2decimal(50, 03, 58);
//		double lon1 = sexagecimal2decimal(5, 42, 53);
//		double lat2 = sexagecimal2decimal(58, 38, 38);
//		double lon2 = sexagecimal2decimal(3, 4, 12);
//		double dist = distVincenty(lat1, lon1, lat2, lon2);
//		LOG.info("distance = "+dist);
//		double lat = sexagecimal2decimal(37, 57, 3.72030, true);
//		double lon = sexagecimal2decimal(144, 25, 29.52440);
//		double bearing = sexagecimal2decimal(306, 52, 5.37);
//		LOG.info("it is : "+lat+", "+lon+", bearing="+bearing);
//		Geolocation location = new Geolocation(lat, lon, (float) 0);
//		double distance = 54972.271;
//		Geolocation newlocation = destVincenty(location, bearing, distance);
//		LOG.info("newlocation = "+newlocation);
//		double nlat = sexagecimal2decimal(37, 39, 10.1561, true);
//		double nlon = sexagecimal2decimal(143, 55, 35.3839);
//		LOG.info("it must be : "+nlat+", "+nlon);
		
		// test
		
		
		
		// Algo 1
		//geolocation.setHorizontalAccuracy(geolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
		
		// Algo 3
//		LOG.info("Algo 3");
		Random rand = new Random();
		double theta = Math.toRadians(rand.nextDouble()*360);
		double alpha = solveXMoinsSinx(obfuscationLevel);
//		LOG.info("alpha final = "+alpha);
//		LOG.info("cos alpha/2= "+Math.cos(alpha/2));
		double d = 2*geolocation.getHorizontalAccuracy()*Math.cos(alpha/2);
//		LOG.info("d = "+d);
		Geolocation newlocation = destVincenty(geolocation, 0, d);
		newlocation.setHorizontalAccuracy(geolocation.getHorizontalAccuracy());
//		LOG.info("Original location: "+geolocation);
//		LOG.info("New location: "+newlocation);
		// /!\ We can't add 1000meters to a latitude (in degrees) like that!
//		geolocation.setLatitude(geolocation.getLatitude()+d*Math.sin(theta));
//		geolocation.setLongitude(geolocation.getLongitude()+d*Math.cos(theta));
		
		return newlocation;
	}
	
	/**
	 * Solve x-sin(x)=PI*obfuscationLevel
	 * @param obfuscationLevel
	 * @return x
	 */
	private double solveXMoinsSinx(float obfuscationLevel) {
		// -- Find x in x-sin(x)=PI*obfuscationLevel
		/* Computation algorithm
		We use Newton Method
		f(x)=x-sin(x)-pi*obfuscationLevel
		f'(x)=1-cos(x)
		xn = xnmoins - f(x)/f'(x)
		
		The difficulty is initialization, but :
		A sign study show that f is growing
		And f(PI/2)=-1.62, and f(PI)=0.9
		So, we choose a value between PI/2 and PI, for example: 2
		
		Five iterations may be enough
		*/
		double xn = 2;
		double C = Math.PI*obfuscationLevel;
		for (int i = 0; i<5; i++) {
//			LOG.info("alpha"+i+" = "+xn);
			double xnmoins1 = xn;
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
		}	
		return xn;
	}
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
	/* Vincenty Inverse Solution of Geodesics on the Ellipsoid (c) Chris Veness 2002-2010             */
	/*                                                                                                */
	/* from: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the */
	/*       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975    */
	/*       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf                                             */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

	/**
	 * Calculates geodetic distance between two points specified by latitude/longitude using 
	 * Vincenty inverse formula for ellipsoids
	 *
	 * @param   {Number} lat1, lon1: first point in decimal degrees
	 * @param   {Number} lat2, lon2: second point in decimal degrees
	 * @returns (Number} distance in metres between points
	 */
	public double distVincenty(double lat1, double lon1, double lat2, double lon2) {
		// WGS-84 ellipsoid params
		double a = 6378137;
		double b = 6356752.314245;
		double f = 1/298.257223563;  
		
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
	  
		double lambda = L;
		double lambdaP;
		double iterLimit = 100;
		double cosSqAlpha;
		double cos2SigmaM;
		double sinSigma;
		double cosSigma;
		double sigma;
		double sinLambda;
		double cosLambda;
		
		do {
		    sinLambda = Math.sin(lambda);
		    cosLambda = Math.cos(lambda);
		    sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + 
		      (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
		    if (sinSigma==0)
		    	return 0;  // co-incident points
		    cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
		    sigma = Math.atan2(sinSigma, cosSigma);
		    double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
		    cosSqAlpha = 1 - sinAlpha*sinAlpha;
		    cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
//		    if (Integer.(cos2SigmaM))
//		    	cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
		    double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
		    lambdaP = lambda;
		    lambda = L + (1-C) * f * sinAlpha *
		      (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		} while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

	  if (iterLimit==0)
		  return 0;  // formula failed to converge

	  double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
	  double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
	  double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
	  double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
	    B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
	  double s = b*A*(sigma-deltaSigma);
	  
//	  s = s.toFixed(3); // round to 1mm precision
	  
	  // note: to return initial/final bearings in addition to distance, use something like:
	  double fwdAz = Math.atan2(cosU2*sinLambda,  cosU1*sinU2-sinU1*cosU2*cosLambda);
	  double revAz = Math.atan2(cosU1*sinLambda, -sinU1*cosU2+cosU1*sinU2*cosLambda);
//	  return { distance: s, initialBearing: fwdAz.toDeg(), finalBearing: revAz.toDeg() };
	  return s;
	}
	
	/**
	 * Calculates destination point given start point lat/long, bearing & distance, 
	 * using Vincenty inverse formula for ellipsoids
	 *
	 * @param   {Number} lat1, lon1: first point in decimal degrees
	 * @param   {Number} bearing: initial bearing in decimal degrees
	 * @param   {Number} dist: distance along bearing in metres
	 * @returns (LatLon} destination point
	 */
	public Geolocation destVincenty(Geolocation location, double bearing, double dist) {
		// WGS-84 ellipsiod
		double a = 6378137;
		double b = 6356752.3142;
		double f = 1/298.257223563;  
		double s = dist;
		double alpha1 = Math.toRadians(bearing);
		double sinAlpha1 = Math.sin(alpha1);
		double cosAlpha1 = Math.cos(alpha1);

		double tanU1 = (1-f) * Math.tan(Math.toRadians(location.getLatitude()));
		double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1*cosU1;
		double sigma1 = Math.atan2(tanU1, cosAlpha1);
		double sinAlpha = cosU1 * sinAlpha1;
		double cosSqAlpha = 1 - sinAlpha*sinAlpha;
		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));

		double sigma = s / (b*A);
		double sigmaP = 2*Math.PI;
		double cos2SigmaM = 0;
		double sinSigma = 0;
		double cosSigma = 0;
		double deltaSigma = 0;
		while (Math.abs(sigma-sigmaP) > 1e-12) {
			cos2SigmaM = Math.cos(2*sigma1 + sigma);
			sinSigma = Math.sin(sigma);
			cosSigma = Math.cos(sigma);
			deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
					B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
			sigmaP = sigma;
			sigma = s / (b*A) + deltaSigma;
		}

	  double tmp = sinU1*sinSigma - cosU1*cosSigma*cosAlpha1;
	  double lat2 = Math.atan2(sinU1*cosSigma + cosU1*sinSigma*cosAlpha1, 
	      (1-f)*Math.sqrt(sinAlpha*sinAlpha + tmp*tmp));
	  double lambda = Math.atan2(sinSigma*sinAlpha1, cosU1*cosSigma - sinU1*sinSigma*cosAlpha1);
	  double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
	  double L = lambda - (1-C) * f * sinAlpha *
	      (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
	  double lon2 = (Math.toRadians(location.getLongitude())+L+3*Math.PI)%(2*Math.PI) - Math.PI;  // normalise to -180...+180

//	  double revAz = Math.atan2(sinAlpha, -tmp);  // final bearing, if required

	  // Math.toDegrees(revAz) // final bearing
	  return new Geolocation(Math.toDegrees(lat2), Math.toDegrees(lon2), (float) 0);
	}

	
	public double sexagecimal2decimal(double degree, double minute, double seconde) {
		return sexagecimal2decimal(degree, minute, seconde, false);
	}
	public double sexagecimal2decimal(double degree, double minute, double seconde, boolean neg) {
		double decimal = degree+(minute/60.0)+(seconde/3600.0);
		return neg ? -decimal : decimal;
	}

}
