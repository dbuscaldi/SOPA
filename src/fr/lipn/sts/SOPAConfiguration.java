package fr.lipn.sts;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOPAConfiguration {
	public final static int PROXYGENEA1=0;
	public final static int PROXYGENEA2=1;
	public final static int PROXYGENEA3=2;
	public final static int WU_PALMER=3;
	public final static int LIN=4;
	public final static int JIANG_CONRATH=5;
	
	public static String LANG;
	public static String WN_HOME;
	public static String DBPedia_INDEX;
	public static String IR_INDEX;
	public static String NGRAMS_DB;
	public static String GoogleTF;
	
	public static int STRUCTURAL_MEASURE=PROXYGENEA3;
	public static int IC_MEASURE=JIANG_CONRATH; //measure used for IC-weighted comparison (Lin or Jiang-Conrath)
	
	/**
	 * Loads configuration from a given configuration file
	 * @param configuration_file
	 */
	public static void load(String configuration_file) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(configuration_file);
			
			Element rootEle = dom.getDocumentElement();
			NodeList nl = rootEle.getChildNodes();
			for(int i = 0 ; i < nl.getLength();i++) {
				try {
					Node n=nl.item(i);
					if(n.getNodeType()==Node.ELEMENT_NODE) {
						Element el = (Element)nl.item(i);
						//System.err.println(el.getNodeName()+" : "+n.getTextContent());
						
						if(el.getNodeName().equals("lang")){
							LANG=el.getTextContent();
						}
						if(el.getNodeName().equals("wnhome")){
							WN_HOME=el.getTextContent();
						}
						if(el.getNodeName().equals("DBPedia")){
							if(el.getAttribute("lang").equals(LANG)) {
								DBPedia_INDEX = el.getTextContent();
							}
						}
						if(el.getNodeName().equals("IRindex")){
							if(el.getAttribute("lang").equals(LANG)) {
								IR_INDEX = el.getTextContent();
							}
						}
						if(el.getNodeName().equals("Ngrams")){
							if(el.getAttribute("lang").equals(LANG)) {
								NGRAMS_DB = el.getTextContent();
							}
						}
						if(el.getNodeName().equals("GoogleTF")){
							GoogleTF = el.getTextContent();
						}
						if(el.getNodeName().equals("ssim")){
							String measure = el.getTextContent();
							if(measure.equals("pg1")) STRUCTURAL_MEASURE=PROXYGENEA1;
							else if(measure.equals("pg2")) STRUCTURAL_MEASURE=PROXYGENEA2;
							else if(measure.equals("pg3")) STRUCTURAL_MEASURE=PROXYGENEA3;
							else STRUCTURAL_MEASURE=WU_PALMER;
						}
						if(el.getNodeName().equals("ic")){
							String ic=el.getTextContent();
							if(ic.equals("jc")) IC_MEASURE=JIANG_CONRATH;
							else IC_MEASURE=LIN;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}catch(Exception pce) {
			pce.printStackTrace();
		}
	}
	
	/**
	 * Loads configuration from the standard configuration file res/config.xml
	 */
	public static void load() {
		load("res/config.xml");
	}

	public static boolean VERBOSE=false;
	
}
