/**
 * 
 */
package com.societies.privacy.obfuscation.obfuscator;

import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.NewtonSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.analysis.solvers.UnivariateRealSolverFactoryImpl;
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
	
	private int OPERATION_E = 1;
	private int OPERATION_R = 2;
	private int OPERATION_S = 3;
	private int OPERATION_ES = 4;
	private int OPERATION_SE = 5;
	private int OPERATION_SR = 6;
	
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
		geolocation.setObfuscationLevel(1);
		Geolocation obfuscatedGeolocation = null;
		// --- Algo 1: enlarge radius
//		Random rand = new Random();
//		float middleObfuscationLevel = 0;
//		while((middleObfuscationLevel = rand.nextFloat()) < obfuscationLevel) {}
//		obfuscatedGeolocation = enlargeRadius(geolocation, middleObfuscationLevel);
//		obfuscatedGeolocation.setObfuscationLevel(middleObfuscationLevel);
//		System.out.println(obfuscatedGeolocation.toJSON()+",");
		// --- Algo 2: reduce radius
//		obfuscatedGeolocation = reduceRadius(geolocation, 0.5F);
		
		// --- Algo 3: shift centre
		obfuscatedGeolocation = shiftCentre(geolocation, obfuscationLevel);
		return obfuscatedGeolocation;
//		Geolocation finalObfuscatedGeolocation = shiftCentre(geolocation, obfuscatedGeolocation, obfuscationLevel);
//		finalObfuscatedGeolocation.setObfuscationLevel(obfuscationLevel);
//		return finalObfuscatedGeolocation;
	}
	
	private Geolocation enlargeRadius(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()/((float) Math.sqrt(obfuscationLevel)));
		return obfuscatedGeolocation;
	}
	
	private Geolocation reduceRadius(Geolocation geolocation, float obfuscationLevel) {
		Geolocation obfuscatedGeolocation = new Geolocation(geolocation.getLatitude(), geolocation.getLongitude(), geolocation.getHorizontalAccuracy()*((float) Math.sqrt(obfuscationLevel)));
		return obfuscatedGeolocation;
	}
	
	private Geolocation shiftCentre(Geolocation geolocation, float obfuscationLevel) {
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double alpha = solveXMoinsSinx(obfuscationLevel);
		double d = 2*geolocation.getHorizontalAccuracy()*Math.cos(alpha/2);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		Geolocation obfuscatedGeolocation = shitLatLgn(geolocation, theta, d);
		return obfuscatedGeolocation;
	}
	
	private Geolocation shiftCentre(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// Select a random theta: shift angle
		Random rand = new Random();
		double theta = rand.nextDouble()*360;
		// Resolve following system:
		/*
		 * alpha - sin(alpha) = pi*obfuscationLevel
		 * d = 2*horizontalAccuracy*cos(alpha/2)
		 */
		double gamma = solveEquation(initialLocation, middleLocation, obfuscationLevel);
		
		
//		ComposableFunction f = new ComposableFunction() {
//			
//			@Override
//			public double value(double x) throws FunctionEvaluationException {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//		};
//		UnivariateRealSolver solver = UnivariateRealSolverFactoryImpl.newInstance().newNewtonSolver();
		
		
//		LOG.info("Gamma="+gamma);
		double alpha = 2*Math.asin(middleLocation.getHorizontalAccuracy()/initialLocation.getHorizontalAccuracy()*Math.sin(gamma/2));
//		LOG.info("Alpha="+alpha);
		double d = initialLocation.getHorizontalAccuracy()*Math.cos(alpha/2)+middleLocation.getHorizontalAccuracy()*Math.cos(gamma/2);
//		LOG.info("Distance="+d);
		// Shift the geolocation center by distance d and angle theta
		/*
		 * /!\ Latitude/longitude are angles, not cartesian coordinates!
		 * new latitude != latitude+d*sin(alpha)
		 * new longitude != longitude+d*cos(alpha)
		 */
		Geolocation obfuscatedGeolocation = shitLatLgn(middleLocation, theta, d);
		return obfuscatedGeolocation;
	}
	
	/**
	 * Solve x-sin(x)=PI*obfuscationLevel with Newton Method
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
			double xnmoins1 = xn;
			xn = xnmoins1-((-C+xnmoins1-Math.sin(xnmoins1))/(1-Math.cos(xnmoins1)));
		}	
		return xn;
	}
	
	
	private double solveEquation(Geolocation initialLocation, Geolocation middleLocation, float obfuscationLevel) {
		// -- Find x in x-sin(x)=PI*obfuscationLevel
		/* Computation algorithm
		We use Newton Method
		f(x)=
		f'(x)=
		xn = xnmoins - f(x)/f'(x)
		
		The difficulty is initialization, but :
		A sign study show ...
		
		Five iterations may be enough
		 */
		double ri = initialLocation.getHorizontalAccuracy();
		double rf = middleLocation.getHorizontalAccuracy();
		double ri2 = Math.pow(ri, 2);
		double rf2 = Math.pow(rf, 2);
//		double C = 2*Math.PI*obfuscationLevel*ri*rf;
		double C = 2*Math.PI*obfuscationLevel*rf2;
		double xn = 6;
		for (int i = 0; i<10; i++) {
			double xnmoins1 = xn;
//			LOG.info("xn"+i+"="+xn);
			xn = xnmoins1
					-
					(
						(
							(ri2*
								(
									2*Math.asin(rf/ri*Math.sin(xnmoins1/2))
									-Math.sin(2*Math.asin(rf/ri*Math.sin(xnmoins1/2)))
								)
							)
							+(rf2*(xnmoins1-Math.sin(xnmoins1)))
							-C
						)
						/
						(
							(rf2/(Math.tan(xnmoins1/2)*Math.sqrt(1-Math.pow(rf/ri*Math.sin(xnmoins1/2), 2))))
							+(rf2*(1-Math.cos(xnmoins1)))
						)
					);
//			f'(x) = (
//			(ri*rf*Math.cos(xnmoins1/2)/Math.sqrt(1-Math.pow(rf/ri*Math.sin(xnmoins1/2), 2))*
//					(
//						1
//						-Math.cos(2*Math.asin(rf/ri*Math.sin(xnmoins1/2)))
//					)
//				)
//				+(rf2*(1-Math.cos(xnmoins1)))
//			);
		}
//		LOG.info("xnfinal="+xn+" ou "+Math.toDegrees(xn)+"°");
		return xn;
	}
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
	/* Vincenty Inverse Solution of Geodesics on the Ellipsoid (c) Chris Veness 2002-2010             */
	/* http://www.movable-type.co.uk/                                                                                             */
	/* from: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the */
	/*       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975    */
	/*       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	/* adapted by: Olivier Maridat (Trialog, Societies Project) 2011                                  */
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
//	  double fwdAz = Math.atan2(cosU2*sinLambda,  cosU1*sinU2-sinU1*cosU2*cosLambda);
//	  double revAz = Math.atan2(cosU1*sinLambda, -sinU1*cosU2+cosU1*sinU2*cosLambda);
//	  return { distance: s, initialBearing: fwdAz.toDeg(), finalBearing: revAz.toDeg() };
	  return s;
	}
	
	/**
	 * Calculates destination point given start point lat/long, angle (=direction) & distance of translation, 
	 * using Vincenty inverse formula for ellipsoids
	 *
	 * @param geolocation first point in decimal degrees
	 * @param direction direction of the translation
	 * @param distance distance along direction in meters
	 * @returns destination point
	 */
	public Geolocation shitLatLgn(Geolocation location, double direction, double distance) {
		// WGS-84 ellipsiod
		double a = 6378137;
		double b = 6356752.3142;
		double f = 1/298.257223563; 
		
		double alpha1 = Math.toRadians(direction);
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
		double sigma = distance / (b*A);
		double sigmaP = 2*Math.PI;
		double cos2SigmaM = 0;
		double sinSigma = 0;
		double cosSigma = 0;
		double deltaSigma = 0;
		// Iterations until |sigma-sigmaP| > 1e-12
		while (Math.abs(sigma-sigmaP) > 1e-12) {
			cos2SigmaM = Math.cos(2*sigma1 + sigma);
			sinSigma = Math.sin(sigma);
			cosSigma = Math.cos(sigma);
			deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
					B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
			sigmaP = sigma;
			sigma = distance / (b*A) + deltaSigma;
		}

	  double tmp = sinU1*sinSigma - cosU1*cosSigma*cosAlpha1;
	  double lat2 = Math.atan2(sinU1*cosSigma + cosU1*sinSigma*cosAlpha1, 
	      (1-f)*Math.sqrt(sinAlpha*sinAlpha + tmp*tmp));
	  double lambda = Math.atan2(sinSigma*sinAlpha1, cosU1*cosSigma - sinU1*sinSigma*cosAlpha1);
	  double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
	  double L = lambda - (1-C) * f * sinAlpha *
	      (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
	  double lon2 = (Math.toRadians(location.getLongitude())+L+3*Math.PI)%(2*Math.PI) - Math.PI;  // normalise to -180...+180
//	  double revAz = Math.atan2(sinAlpha, -tmp);  // final shiftAngle, if required

	  return new Geolocation(Math.toDegrees(lat2), Math.toDegrees(lon2), location.getHorizontalAccuracy());
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

	
	public double sexagecimal2decimal(double degree, double minute, double seconde) {
		return sexagecimal2decimal(degree, minute, seconde, false);
	}
	public double sexagecimal2decimal(double degree, double minute, double seconde, boolean neg) {
		double decimal = degree+(minute/60.0)+(seconde/3600.0);
		return neg ? -decimal : decimal;
	}

}
