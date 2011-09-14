/**
 * 
 */
package com.societies.privacy.obfuscation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.societies.data.Geolocation;
import com.societies.data.accessor.GeolocationDAO;
import com.societies.privacy.data.ObfuscationType;
import com.societies.privacy.data.ObfuscationTypes;
import com.societies.privacy.obfuscation.obfuscator.DataObfuscatorFactory;
import com.societies.privacy.obfuscation.obfuscator.listener.ObfuscatorListener;

/**
 * @author olivierm
 * @date 26 août 2011
 */
public class DataObfuscationManager implements IDataObfuscator<Object>, IDataObfuscatedAccessor{
//	private static final Logger LOG = Logger.getLogger(DataObfuscationManager.class);
	
	private static float defaultObfuscationLevel = 1;
	private static double defaultLatitude = 48.856666;
	private static double defaultLongitude = 2.350987;
	private static float defaultHorizontalAccuracy = 542;
	private static String defaultObfuscationAlgorithm = ObfuscationTypes.GEOLOCATION;
	
	/* -- Main -- */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine cmd = getCommandLine(args);
		float obfuscationLevel = cmd.hasOption("l") ? Float.parseFloat(cmd.getOptionValue("l")) : defaultObfuscationLevel;
		double latitude = cmd.hasOption("la") ? Double.parseDouble(cmd.getOptionValue("la")) : defaultLatitude;
		double longitude = cmd.hasOption("lo") ? Double.parseDouble(cmd.getOptionValue("lo")) : defaultLongitude;
		float horizontalAccuracy = cmd.hasOption("ha") ? Float.parseFloat(cmd.getOptionValue("ha")) : defaultHorizontalAccuracy;
		String obfuscationAlgorithm = cmd.hasOption('a') ? cmd.getOptionValue("a") : defaultObfuscationAlgorithm;
				
		DataObfuscationManager dataObfuscationManager = null;
        try {
        	dataObfuscationManager = new DataObfuscationManager();
        	
        	ObfuscationType obfuscationType = new ObfuscationType(obfuscationAlgorithm);
        	IDataObfuscationManagerCallback<Object> callback = new ObfuscatorListener();
        	
//        	LOG.info("Obfuscate a Data");
        	Geolocation geolocation = new Geolocation(latitude, longitude, horizontalAccuracy);
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("data", geolocation);
//        	map.put("obfuscationOperation", 2);
//        	map.put("theta", Math.toRadians(90));
//        	map.put("middleObfuscationLevel", 1F);
        	long start = System.currentTimeMillis();
        	
        	System.out.println("{\"results\": [\n"+geolocation.toJSON()+",");
        	dataObfuscationManager.obfuscateData(map, obfuscationType, obfuscationLevel, callback);
        	System.out.println("]}");
        	
        	long duree = System.currentTimeMillis() - start;
//        	System.out.println("Durée : "+duree+"ms");
        	
//        	LOG.info("Get an Obfuscated Data");
//        	dataObfuscationManager.getObfuscatedData(0, obfuscationType, obfuscationLevel, callback);
//        	System.out.println("]}");
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
		options.addOption("ha", true, "Horizontal accuracy (defaults to "+defaultHorizontalAccuracy+")");
		options.addOption("a", true, "Force to use an obfuscation algorithm (defaults to "+defaultObfuscationAlgorithm+")");
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
		formatter.printHelp("java -jar [...]-bin.jar [-l obfuscationLevel] [-la latitude] [-lo longitude] [-ha horizontalAccuracy] [-a obfuscationAlgorithm] ", options);
	}
	
	/* --- Methods --- */	
	/**
	 * @throws Exception 
	 * @see IDataObfuscator
	 */
	public void obfuscateData(Object data, ObfuscationType obfuscationType,
			float obfuscationLevel,
			IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// -- Verifications
		if (null == data || null == callback || null == obfuscationType) {
			throw new Exception("Wrong parameters");
		}
		// Check obfuscation level
		if (1 == obfuscationLevel) {
			callback.obfuscationResult(data);
			return;
		}
		if (0 == obfuscationLevel) {
			obfuscationLevel = 0.0000000001F;
		}
		
				
		// -- Select obfuscator
		IDataObfuscator<Object> obfuscator = DataObfuscatorFactory.getDataObfuscator(obfuscationType);
		
		// -- Obfuscate
		obfuscator.obfuscateData(data, obfuscationType, obfuscationLevel, callback);
	}
	
	public void getObfuscatedData(int dataId, ObfuscationType obfuscationType,
			float obfuscationLevel, IDataObfuscationManagerCallback<Object> callback) throws Exception {
		// -- Verifications
		if (null == callback || null == obfuscationType) {
			throw new Exception("Wrong parameters");
		}
		// Check obfuscation level
		if (0 == obfuscationLevel) {
			obfuscationLevel = 0.0000000001F;
		}
		
		// -- Retrieve data
		GeolocationDAO dao = new GeolocationDAO();
		Geolocation data = dao.findGeolocationById(dataId);
		if (null == data) {
			throw new Exception("No data");
		}
		System.out.println("{[\n"+data.toJSON()+",");
		
		// -- Algorithm
		obfuscateData(data, obfuscationType, obfuscationLevel, callback);		
	}
}
