package fr.lipn.sts.ner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.measures.SimilarityMeasure;
import fr.lipn.sts.tools.GoogleTFFactory;

public class NERSimilarity implements SimilarityMeasure {

	public static double compare(ArrayCoreMap cSentence, ArrayCoreMap cSentence1) {
		HashMap<String, HashSet<String>> s1Map = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> s2Map = new HashMap<String, HashSet<String>>();
		
		int size_A=0, size_B=0;
		double weighted_sizeA=0d, weighted_sizeB=0d;
		
		for (CoreLabel word : cSentence.get(CoreAnnotations.TokensAnnotation.class)) {
			String tag = word.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			if(!tag.equals("O")){
				HashSet<String> tv = s1Map.get(tag);
				if(tv==null) tv=new HashSet<String>();
				tv.add(word.word());
				s1Map.put(tag, tv);
				size_A++;
				weighted_sizeA+=GoogleTFFactory.getIDF(word.word());
			}
	    }
		
		for (CoreLabel word : cSentence1.get(CoreAnnotations.TokensAnnotation.class)) {
			String tag = word.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			if(!tag.equals("O")){
				HashSet<String> tv = s2Map.get(tag);
				if(tv==null) tv=new HashSet<String>();
				tv.add(word.word());
				s2Map.put(tag, tv);
				size_B++;
				weighted_sizeB+=GoogleTFFactory.getIDF(word.word());
			}
     	}
        
		
		if(s1Map.size()==0 && s1Map.size()==0) return 1.0d; //NOTE: should return 0 instead? TEST IT
		
		int overlap=0;
		double weighted_overlap=0d;

		//now calculate overlap (for each category)
		Set<String> shLabels = new HashSet<String>(s1Map.keySet());
		shLabels.retainAll(s2Map.keySet());
		for(String k : shLabels) {
			Set<String> shVals = new HashSet<String>(s1Map.get(k));
			shVals.retainAll(s2Map.get(k));
			overlap+=shVals.size();
			for(String s : shVals) {
				weighted_overlap+=GoogleTFFactory.getIDF(s);
			}
			
			if(SOPAConfiguration.VERBOSE) System.err.println("Shared NERs for category "+k+" : "+shVals.size());
		}
		
		/* returns a weighted (idf) Dice coefficient as similarity between the two sets of NEs */
		if(size_A > 0 && size_B > 0) return (double)(2*weighted_overlap)/(double)(weighted_sizeA+weighted_sizeB);
		else return 0d;
		
		/* old version 
		if(size_A > 0 && size_B > 0) return (double)(2*overlap)/(double)(size_A+size_B); //Dice coefficient
		else return 0d;*/
	}

	@Override
	public double compare(Object o1, Object o2) {
		return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
	}

}
