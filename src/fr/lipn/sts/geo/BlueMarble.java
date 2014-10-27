package fr.lipn.sts.geo;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlueMarble {
	private static HashMap<String, HashSet<Location>> worldmap;
	
	/**
	 * synsetID must be of the form (n|v|a|r)[0-9]^8
	 * @param synsetID
	 * @return
	 */
	public static boolean hasLocs(String synsetID){
		return worldmap.containsKey(synsetID);
	}
	
	public static double getAvgDistance(String synsetID1, String synsetID2) {
		HashSet<Location> locs1 = worldmap.get(synsetID1);
		HashSet<Location> locs2 = worldmap.get(synsetID2);
		
		double distSum=0;
		int nDistances=0;
		
		for(Location j : locs1) {
			for(Location k : locs2) {
				if(locs1.equals(locs2)) nDistances++;
				else {
					distSum=distSum+j.getDistance(k);
					nDistances++;
				}
			}
		}
		
		return distSum/(double)nDistances;
		
	}
	
	public static double getMinDistance(String synsetID1, String synsetID2) {
		HashSet<Location> locs1 = worldmap.get(synsetID1);
		HashSet<Location> locs2 = worldmap.get(synsetID2);
		
		double bestDistance=20000d;
		
		for(Location j : locs1) {
			for(Location k : locs2) {
				if(locs1.equals(locs2)) return 0;
				else {
					double dist=j.getDistance(k);
					if(dist < bestDistance) bestDistance=dist;
				}
			}
		}
		
		return bestDistance;
		
	}
	
	public static void init() {
		//GeoWN.init();
		worldmap = new HashMap<String, HashSet<Location>>();
		try {
			File fXmlFile = new File("res/bluemarble_1.3.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		 
			doc.getDocumentElement().normalize();
		    
			System.err.println("Initializing Blue Marble...");
			//System.err.println("Root element :" + doc.getDocumentElement().getNodeName());
		 
			NodeList nList = doc.getElementsByTagName("synset");
		 
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				//System.err.println("\nCurrent Element :" + nNode.getNodeName());
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					String synID= eElement.getAttribute("id");
					NodeList geoRefs = eElement.getElementsByTagName("geo");
					
					//System.err.println("Synset id : " + synID);
					HashSet<Location> locs = new HashSet<Location>();
					
					for(int i=0; i < geoRefs.getLength(); i++){
						Node n = geoRefs.item(i);
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							Element en = (Element) n;
							String geoID= en.getAttribute("geonamesid");
							String lat= en.getAttribute("lat");
							String lon= en.getAttribute("lon");
							
							Location loc = new Location(geoID, lat, lon);
							locs.add(loc);
						}
					}
					
					worldmap.put(synID, locs);
		 
				}
			}
			System.err.println("done.");
			
	  	} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}
