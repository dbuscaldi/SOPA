package fr.lipn.sts.basic;

import fr.lipn.sts.measures.SimilarityMeasure;

public class SentenceLengthSimilarity implements SimilarityMeasure {

	public static double compare(String text1, String text2) {
		double diff = Math.sqrt(Math.pow(text1.length()-text2.length(), 2.0d));
		
		return diff;
	}
	
	@Override
	public double compare(Object o1, Object o2) {
		return SentenceLengthSimilarity.compare((String)o1, (String)o2);
	}

}
