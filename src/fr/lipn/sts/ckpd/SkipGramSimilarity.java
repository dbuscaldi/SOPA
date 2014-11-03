package fr.lipn.sts.ckpd;

import java.util.HashSet;

import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.measures.SimilarityMeasure;

public class SkipGramSimilarity implements SimilarityMeasure {
	
	public static double compare(ArrayCoreMap tSentence, ArrayCoreMap tSentence1){
		HashSet<SkipGram> sg1 = NGramFactory.getSkipGrams(tSentence);
		HashSet<SkipGram> sg2 = NGramFactory.getSkipGrams(tSentence1);
		
		double size1=sg1.size();
		double size2=sg1.size();
		
		HashSet<SkipGram> intersection=new HashSet<SkipGram>(sg1);
		intersection.retainAll(sg2);
		/*
		for(SkipGram s : intersection) {
			System.err.println(s.repr());
		}
		*/
		double intSize=intersection.size();
		
		return 2*intSize/(size1+size2);
		
	}
	
	public double compare(Object o1, Object o2) {
		return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
	}
}
