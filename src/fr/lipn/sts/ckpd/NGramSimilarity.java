package fr.lipn.sts.ckpd;

import java.util.HashSet;

import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.measures.SimilarityMeasure;

public class NGramSimilarity implements SimilarityMeasure {

	public static double compare(ArrayCoreMap tSentence, ArrayCoreMap tSentence1){
		HashSet<NGram> set0 = NGramFactory.getNGramSet(tSentence);
		HashSet<NGram> set1 = NGramFactory.getNGramSet(tSentence1);
		
		NGram ngram0 = NGramFactory.getNGram(tSentence);
		NGram ngram1 = NGramFactory.getNGram(tSentence);
		
		return calc(set0, ngram0, set1, ngram1);
	}
	
	public static double compare(String s1, String s2){
		HashSet<NGram> set0 = NGramFactory.getNGramSet(s1);
		HashSet<NGram> set1 = NGramFactory.getNGramSet(s2);
		
		NGram ngram0 = NGramFactory.getNGram(s1);
		NGram ngram1 = NGramFactory.getNGram(s2);
		
		return calc(set0, ngram0, set1, ngram1);
		
	}
	
	private static double calc(HashSet<NGram> set0, NGram ngram0, HashSet<NGram> set1, NGram ngram1) {
		double simValue=0.0;
		
		NGram longestSent;
		if(ngram0.getSize() > ngram1.getSize()) longestSent=ngram0;
		else longestSent=ngram1;
		
		HashSet<NGram> intSet= new HashSet<NGram>(set0);
	    intSet.retainAll(set1);
	    HashSet<NGram> coveringSet = new HashSet<NGram>();
	    for(NGram n : intSet) {
	    	HashSet<NGram> rest = new HashSet<NGram>(intSet);
	    	rest.remove(n);
	    	boolean flag=true;
	    	for(NGram o : rest){
	    		if(n.containedIn(o)) {
	    			flag=false; break;
	    		}
	    	}
	    	if(flag) coveringSet.add(n);
	    }
	    /*
	    System.err.println("covering set:");
	    for(NGram ng : coveringSet){
    		System.err.println(ng.repr());
    	}
	    System.err.println("-------------------------------");
	    */
	    NGram longestNG=new NGram();
	    for(NGram ng : coveringSet){
	    	if(ng.getSize()> longestNG.getSize()) longestNG=ng;
	    }
	    
	    longestSent.setWeights();
	    //prepare weights
	    for(NGram ng : coveringSet){
	    	ng.calculateDistance(longestNG, longestSent);
	    	ng.setWeights();
	    }
	    
	    double ngWSum=0.0;
	    for(NGram ng : coveringSet){
	    	double ngw=ng.getWeight()/ng.getDistanceCoeff();
	    	//System.err.println(ngw+" weight for: "+ng.repr());
	    	ngWSum+=ngw;
	    }
	    //System.err.println("longestSent weight"+longestSent.getWeight());
	    simValue=ngWSum/longestSent.getWeight();
	    
	    return simValue;
	}

	@Override
	public double compare(Object o1, Object o2) {
		if(o1.getClass().equals(String.class)) {
			return compare((String)o1, (String)o2);
		} else {
			return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
		}
	}
}
