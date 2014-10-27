package fr.lipn.sts.geo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;


public class GeoWN {
	private static HashMap<String, Location> geoMap;
	private static String GEOWN_HOME="res/GeoWN";
	
	public static void init(){
		geoMap = new HashMap<String, Location>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(GEOWN_HOME+"/mapping.dat"));
			String line;
		    while ((line = reader.readLine()) != null) {
		    	if(!line.startsWith("#")){
		    		String [] elements = line.split("\t");
		    		String offset =elements[0];
		    		Location coord = new Location(elements[0], elements[1], elements[2], elements[3]);
		    		geoMap.put(offset, coord);
		    	}
		    }
		    reader.close();
		} catch (Exception e){
			System.err.println("Error reading GeoWN mappings:");
			e.printStackTrace();
		}
	}
	
	public static Location getCoord(String offset){
		return geoMap.get(offset);
	}
}
