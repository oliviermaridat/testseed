/**
 * 
 */
package com.societies.privacy.obfuscation;

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
import com.societies.privacy.obfuscation.obfuscator.DataObfuscatorFactory;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class DataObfuscationManager implements IDataObfuscator<Object>, IDataObfuscatedAccessor{
	private static final Logger LOG = Logger.getLogger(DataObfuscationManager.class);
	private static float defaultObfuscationLevel = 1;
	private static double defaultLatitude = 48.856666;
	private static double defaultLongitude = 2.350987;
	private static float defaultHorizontalAccuracy = 542;
	private static String defaultAlgorithm = "geolocation";
	
	/* -- Main -- */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine cmd = getCommandLine(args);
		float obfuscationLevel = cmd.hasOption("l") ? Float.parseFloat(cmd.getOptionValue("l")) : defaultObfuscationLevel;
		double latitude = cmd.hasOption("la") ? Double.parseDouble(cmd.getOptionValue("la")) : defaultLatitude;
		double longitude = cmd.hasOption("lo") ? Double.parseDouble(cmd.getOptionValue("lo")) : defaultLongitude;
		float horizontalAccuracy = cmd.hasOption("h") ? Float.parseFloat(cmd.getOptionValue("h")) : defaultHorizontalAccuracy;
		String algorithm = cmd.hasOption('a') ? cmd.getOptionValue("a") : defaultAlgorithm;
				
		DataObfuscationManager dataObfuscationManager = null;
        try {
        	dataObfuscationManager = new DataObfuscationManager();
        	
        	Geolocation geolocation = new Geolocation(latitude, longitude, horizontalAccuracy);
//        	LOG.info("Geolocation to obfuscate: "+geolocation);
        	System.out.println(geolocation.toJSON());
        	ObfuscationType obfuscationType = new ObfuscationType("http://societies/data/context/geolocation");
        	IDataObfuscationManagerCallback<Object> callback = null;
        	dataObfuscationManager.obfuscateData(geolocation, obfuscationType, obfuscationLevel, callback);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
	}	
	protected static CommandLine getCommandLine(String[] args) {
		Options options = new Options();
		options.addOption("l", true, "Obfuscation level (defaults to "+defaultObfuscationLevel+")");
		options.addOption("la", true, "Latitude (defaults to "+defaultLatitude+")");
		options.addOption("lo", true, "Longitude (defaults to "+defaultLongitude+")");
		options.addOption("h", true, "Horizontal accuracy (defaults to "+defaultHorizontalAccuracy+")");
		options.addOption("a", true, "Force to use an obfuscation algorithm (defaults to "+defaultAlgorithm+")");
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			System.exit(1);
		}

		if (cmd.hasOption('h')) {
			printHelp(options);
			System.exit(0);
		}
		return cmd;
	}	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar [...]-bin.jar [-l obfuscationLevel] [-la latitude] [-lo longitude] [-h horizontalAccuracy] [-a algorithm] ", options);
	}
	
	/* --- Methods --- */	
	/**
	 * @throws Exception 
	 * @see IDataObfuscator
	 */
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel,
			IDataObfuscationManagerCallback<Object> callback) throws Exception {
		IDataObfuscator<Object> obfuscator = DataObfuscatorFactory.getDataObfuscator(obfuscationType);
		obfuscator.obfuscateData(data, obfuscationType, obfuscationLevel, callback);
	}
	
	public void getObfuscatedData(int dataId, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		GeolocationDAO dao = new GeolocationDAO();
		Geolocation data = dao.findGeolocationById(dataId);
		obfuscateData(data, obfuscationType, obfuscationLevel, callback);		
	}
}
