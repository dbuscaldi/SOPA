package fr.lipn.sts.basic;

import fr.lipn.sts.measures.SimilarityMeasure;

public class SentenceLengthSimilarity implements SimilarityMeasure {

	@Override
	public double compare(Object o1, Object o2) {
		String text1 = (String)o1;
		String text2 = (String)o2;
		
		double diff = Math.sqrt(Math.pow(text1.length()-text2.length(), 2.0d));
		
		return diff;
	}

}
