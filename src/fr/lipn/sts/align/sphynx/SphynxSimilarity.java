package fr.lipn.sts.align.sphynx;

import java.util.HashSet;

import edu.mit.jwi.item.ISynsetID;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.measures.SimilarityMeasure;

public class SphynxSimilarity implements SimilarityMeasure {

	public static double compare(ArrayCoreMap s1, ArrayCoreMap s2) {
		String [] seq1 = new String [s1.get(CoreAnnotations.TokensAnnotation.class).size()];
		String [] seq2 = new String [s2.get(CoreAnnotations.TokensAnnotation.class).size()];
		int i=0;
		for (CoreLabel word : s1.get(CoreAnnotations.TokensAnnotation.class)) {
			String text=word.word();
			seq1[i]=text;
			i++;
		}
		
		i=0;
		for (CoreLabel word : s2.get(CoreAnnotations.TokensAnnotation.class)) {
			String text=word.word();
			seq2[i]=text;
			i++;
		}
		
		WordSequenceAligner aligner = new WordSequenceAligner();
		
		Alignment a =aligner.align(seq1, seq2);
		AlignmentScores scores = new AlignmentScores(a);
		
		if(SOPAConfiguration.VERBOSE) System.err.println("[Sphynx]: "+scores.toString());
		
		return (double)(1.0f-scores.getWordErrorRate()); //WER=1 means that the two sentences are completely dissimilar
		
	}
	
	@Override
	public double compare(Object o1, Object o2) {
		return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
	}

}
