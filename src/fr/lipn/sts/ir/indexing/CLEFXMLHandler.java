package fr.lipn.sts.ir.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CLEFXMLHandler extends DefaultHandler {
	 /* A buffer for each XML element */
	  protected StringBuffer textBuffer = new StringBuffer();
	  protected StringBuffer titleBuffer = new StringBuffer();
	  protected StringBuffer docID = new StringBuffer();
	  
	  protected Stack<String> elemStack;
	  protected Document currentDocument;
	  protected Vector<Document> parsedDocuments;
	  
	  public CLEFXMLHandler(File xmlFile) 
	  	throws ParserConfigurationException, SAXException, IOException {
	    
		// Now let's move to the parsing stuff
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser parser = spf.newSAXParser();
	    
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
		  parsedDocuments= new Vector<Document>();
		  elemStack=new Stack<String>();
	  }

	  // call at element start
	  public void startElement(String namespaceURI, String localName,
	    String qualifiedName, Attributes attrs) throws SAXException {

	    String eName = localName;
	     if ("".equals(eName)) {
	       eName = qualifiedName; // namespaceAware = false
	     }
	     
	     elemStack.addElement(eName);
	     if(eName=="DOCID") {
	     	textBuffer.setLength(0);
	     	titleBuffer.setLength(0);
	     	docID.setLength(0);
	     	currentDocument=new Document();
	     }

	  }

	  // call when cdata found
	  public void characters(char[] text, int start, int length)
	    throws SAXException {
	  	if(elemStack.peek().equalsIgnoreCase("TITLE")){
	  		titleBuffer.append(text, start, length);
	  	} else if (elemStack.peek().equalsIgnoreCase("TITLE") || elemStack.peek().equalsIgnoreCase("TEXT")) {
	  		textBuffer.append(text, start, length);
	  	} else if (elemStack.peek().equalsIgnoreCase("DOCID")) {
	  		docID.append(text, start, length);
	  	}
	  }

	  // call at element end
	  public void endElement(String namespaceURI, String simpleName,
	    String qualifiedName)  throws SAXException {

	    String eName = simpleName;
	    if ("".equals(eName)) {
	      eName = qualifiedName; // namespaceAware = false
	    }
	    elemStack.pop();
	    if (eName.equals("DOC")){
	    	currentDocument.add(new Field("title", titleBuffer.toString(), Field.Store.YES, Field.Index.ANALYZED));
	    	currentDocument.add(new Field("text", textBuffer.toString(), Field.Store.YES, Field.Index.ANALYZED));
	    	currentDocument.add(new Field("id", this.docID.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
	    	parsedDocuments.add(currentDocument);
	    }
	  }
	  
	  public Vector<Document> getParsedDocuments() {
		  return this.parsedDocuments;
	  }
}
