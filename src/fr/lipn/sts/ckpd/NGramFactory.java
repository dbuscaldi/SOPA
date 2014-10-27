package fr.lipn.sts.ckpd;

import java.util.HashSet;
import java.util.Vector;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArrayCoreMap;

public class NGramFactory {
	
	public static HashSet<NGram> getNGramSet(ArrayCoreMap tSentence){
		Vector<CoreLabel> sentence = new Vector<CoreLabel>();
		for(CoreLabel tw : tSentence.get(CoreAnnotations.TokensAnnotation.class)){
			sentence.add(tw);
		}
		
		HashSet<NGram> ngramSet = new HashSet<NGram>();
		int maxN=sentence.size();
		for(int i=0; i< maxN; i++){
			for(int j=0; j<sentence.size(); j++) {
				NGram ng = new NGram();
				for(int k=j; k<=(j+i) && k < sentence.size(); k++){
					ng.add(new Term(sentence.get(k)));
				}
				ngramSet.add(ng);
			}
		}
		return ngramSet;
	}

	public static NGram getNGram(ArrayCoreMap tSentence) {
		NGram ng= new NGram();
		for(CoreLabel tw : tSentence.get(CoreAnnotations.TokensAnnotation.class)){
			ng.add(new Term(tw));
		}
		return ng;
	}
	
	public static HashSet<SkipGram> getSkipGrams(ArrayCoreMap tSentence){
		Vector<CoreLabel> sentence = new Vector<CoreLabel>();
		for(CoreLabel tw : tSentence.get(CoreAnnotations.TokensAnnotation.class)){
			sentence.add(tw);
		}
		
		HashSet<SkipGram> skipGramSet = new HashSet<SkipGram>();
		int maxN=sentence.size();
		for(int i=0; i< (maxN-1); i++){
			for(int j=i+1; j<maxN; j++){
				SkipGram sg = new SkipGram(sentence.elementAt(i).word(), sentence.elementAt(j).word());
				skipGramSet.add(sg);
			}
		}
		return skipGramSet;
	}

}
