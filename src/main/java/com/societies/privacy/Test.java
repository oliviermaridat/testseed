/**
 * 
 */
package com.societies.privacy;

import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import com.societies.data.Geolocation;
import com.societies.data.accessor.GeolocationDAO;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.data.ObfuscationTypes;
import com.societies.privacy.obfuscation.obfuscator.DataObfuscatorFactory;
import com.societies.privacy.obfuscation.obfuscator.listener.ObfuscatorListener;
import com.societies.utils.RandomBetween;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class Test{
	private static final Logger LOG = Logger.getLogger(Test.class);
	
	/* -- Main -- */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test test = null;
        try {
        	test = new Test();
        	
//        	RandomBetween rand = new RandomBetween();
//        	for(int i = 0; i < 20; i++) { 
//	        	float f = rand.nextFloatBetween(0, 0.0001F);
////	        	float fbetween = f*(1F-0.7F)+0.7F;
//	        	LOG.info("Number: "+f);
//        	}
        	
        	
        	double r1 = 5;
        	double r12 = Math.pow(r1, 2);
        	double r2 = 5;
        	double r22 = Math.pow(r2, 2);
        	double d = 5;
        	double d2 = Math.pow(d, 2);
        	double alpha;
        	double gamma;
        	if (d == 0 || r1 == 0 || r2 == 0) {
	        	alpha = 2*Math.PI;
	        	gamma = 0;
        	}
        	if (d > r1+r2) {
        		alpha = 0;
	        	gamma = 0;
        	}
        	else {
        		alpha = 2*Math.acos((r12+d2-r22)/(2*r1*d));
	        	gamma = 2*Math.acos((r12+d2-r22)/(2*r2*d));
        	}
		
			double a1 = r12/2*(alpha-Math.sin(alpha))+r22/2*(gamma-Math.sin(gamma));
//			double a2 = r22*Math.acos(d/r2)-d*Math.sqrt(r22-d2);
//			double a3 = r12/2*alpha+r22/2*gamma-1/2*Math.sqrt((-d+r1+r2)*(d-r1+r2)*(d+r1-r2)*(d+r1+r2));
        	
			LOG.info("Aire C1="+(Math.PI*r12)+"m², alpha="+Math.toDegrees(alpha)+"°");
			LOG.info("Aire C2="+(Math.PI*r22)+"m², gamma="+Math.toDegrees(gamma)+"°");
        	LOG.info("Aire C1 inter C2 methode 1="+a1+"m²");
//        	LOG.info("Aire C1 inter C2 methode 2="+a2+"m²");
//        	LOG.info("Aire C1 inter C2 methode 3="+a3+"m²");
        	
        	
        }
        catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/* --- Methods --- */	
}
