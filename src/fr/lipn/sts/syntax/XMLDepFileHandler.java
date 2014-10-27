package fr.lipn.sts.syntax;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLDepFileHandler extends DefaultHandler {
	private Vector <DepPair> items;
	private Stack<String> elemStack;
	boolean firstSentence=true;
	private Vector<Dependency> currentA;
	private Vector<Dependency> currentB;
	
	public XMLDepFileHandler(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser parser = spf.newSAXParser();
	    
	    items = new Vector<DepPair>();
	    
	    try {
	      parser.parse(xmlFile, this);
	    } catch (org.xml.sax.SAXParseException spe) {
	      System.out.println("SAXParser caught SAXParseException at line: " +
	        spe.getLineNumber() + " column " +
	        spe.getColumnNumber() + " details: " +
			spe.getMessage());
	    }
	  }

	  // call at document start
	  public void startDocument() throws SAXException {
		  elemStack=new Stack<String>();
	  }

	  // call at element start
	  public void startElement(String namespaceURI, String localName,
	    String qualifiedName, Attributes attrs) throws SAXException {
		 
	    String eName = localName;
	     if ("".equals(eName)) {
	       eName = qualifiedName; // namespaceAware = false
	     }
	     
	     if(eName.equals("adeps")) {
	    	 currentA = new Vector<Dependency>();
	    	 firstSentence=true;
	     }
	     if(eName.equals("bdeps")) {
	    	 currentB = new Vector<Dependency>();
	    	 firstSentence=false;
	     }
	     
	     if(eName.equals("dep")){
	    	 String label = attrs.getValue("type");
	    	 String head = attrs.getValue("from");
	    	 String target = attrs.getValue("to");
	    	 
	    	 Dependency d = new Dependency(label, head, target);
	    	 
	    	 if(firstSentence) currentA.add(d);
	    	 else currentB.add(d);
	    	 
	     }
	     
	     elemStack.push(eName);
	     
	  }

	  // call at element end
	  public void endElement(String namespaceURI, String simpleName,
	    String qualifiedName)  throws SAXException {

	    String eName = simpleName;
	    if ("".equals(eName)) {
	      eName = qualifiedName; // namespaceAware = false
	    }
	    elemStack.pop();
	    if (eName.equals("bdeps")){
	    	items.add(new DepPair(currentA, currentB));
	    }
	  }
	  
	  public Vector<DepPair> getParsedDependencies(){
		  return items;
	  } 
		  
}
