package fr.lipn.sts.syntax;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import edu.stanford.nlp.ling.TaggedWord;

public class DepComparer {
	private static Vector<DepPair> parsedContent;
	
	public static void parse(String xmlFile) {
		try {
			XMLDepFileHandler hldr = new XMLDepFileHandler(new File(xmlFile));
			parsedContent=hldr.getParsedDependencies();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static double getSimilarity(int order, ArrayList<TaggedWord> tSentence, ArrayList<TaggedWord> tSentence1){
		DepPair deps = parsedContent.elementAt(order);
		deps.setPOStags(tSentence, tSentence1);
		deps.setAlignments();
		return deps.getDepScore();
	}

}
