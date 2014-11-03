package fr.lipn.sts.basic;

import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.measures.SimilarityMeasure;
import fr.lipn.sts.tools.GoogleTFFactory;

public class TfIdfSimilarity implements SimilarityMeasure {

	public static double compare(ArrayCoreMap tSentence, ArrayCoreMap tSentence1){
		HashMap<String, Integer> freqs1 = new HashMap<String, Integer>();
		HashMap<String, Integer> freqs2 = new HashMap<String, Integer>();
		
		HashSet<String> shared = new HashSet<String>();
		HashMap<String, Double> idfs = new HashMap<String, Double>();
		
		for (CoreLabel t : tSentence.get(CoreAnnotations.TokensAnnotation.class)) {
			String w = t.word();
			Integer freq = freqs1.get(w);
			if(freq == null) freq=new Integer(1);
			else freq = new Integer(freq.intValue()+1);
			freqs1.put(w, freq);
			Double idf = idfs.get(w);
			if(idf==null){
				idf = GoogleTFFactory.getIDF(w);
				idfs.put(w, idf);
			}
		}
		
		for (CoreLabel t : tSentence1.get(CoreAnnotations.TokensAnnotation.class)) {
			String w = t.word();
			Integer freq = freqs2.get(w);
			if(freq == null) freq=new Integer(1);
			else freq = new Integer(freq.intValue()+1);
			freqs2.put(w, freq);
			Double idf = idfs.get(w);
			if(idf==null){
				idf = GoogleTFFactory.getIDF(w);
				idfs.put(w, idf);
			}
		}
		
		shared.addAll(freqs1.keySet());
		shared.retainAll(freqs2.keySet());
		
		double num = 0.0;
		for(String s : shared){
			double a = freqs1.get(s).doubleValue()*idfs.get(s);
			double b = freqs2.get(s).doubleValue()*idfs.get(s);
			num+=(a*b);
		}
		
		double d1 = 0.0;
		for(String s : freqs1.keySet()){
			double a = freqs1.get(s).doubleValue()*idfs.get(s);
			d1+=Math.pow(a, 2.0d);
		}
		
		double d2 = 0.0;
		for(String s : freqs2.keySet()){
			double a = freqs2.get(s).doubleValue()*idfs.get(s);
			d2+=Math.pow(a, 2.0d);
		}
		
		double den = Math.sqrt(d1)*Math.sqrt(d2);
		
		return (num/den);
	}
	
	/**
	 * This method uses a simple regular expression ("[\\w]+") to transform a string into an array of strings
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double compare(String s1, String s2){
		HashMap<String, Integer> freqs1 = new HashMap<String, Integer>();
		HashMap<String, Integer> freqs2 = new HashMap<String, Integer>();
		
		HashSet<String> shared = new HashSet<String>();
		HashMap<String, Double> idfs = new HashMap<String, Double>();
		
		String [] tSentence = s1.split("[\\w]+");
    	String [] tSentence1 = s2.split("[\\w]+");
    	
		for(String w : tSentence){
			Integer freq = freqs1.get(w);
			if(freq == null) freq=new Integer(1);
			else freq = new Integer(freq.intValue()+1);
			freqs1.put(w, freq);
			Double idf = idfs.get(w);
			if(idf==null){
				idf = GoogleTFFactory.getIDF(w);
				idfs.put(w, idf);
			}
		}
		
		for(String w : tSentence1){
			Integer freq = freqs2.get(w);
			if(freq == null) freq=new Integer(1);
			else freq = new Integer(freq.intValue()+1);
			freqs2.put(w, freq);
			Double idf = idfs.get(w);
			if(idf==null){
				idf = GoogleTFFactory.getIDF(w);
				idfs.put(w, idf);
			}
		}
		
		shared.addAll(freqs1.keySet());
		shared.retainAll(freqs2.keySet());
		
		double num = 0.0;
		for(String s : shared){
			double a = freqs1.get(s).doubleValue()*idfs.get(s);
			double b = freqs2.get(s).doubleValue()*idfs.get(s);
			num+=(a*b);
		}
		
		double d1 = 0.0;
		for(String s : freqs1.keySet()){
			double a = freqs1.get(s).doubleValue()*idfs.get(s);
			d1+=Math.pow(a, 2.0d);
		}
		
		double d2 = 0.0;
		for(String s : freqs2.keySet()){
			double a = freqs2.get(s).doubleValue()*idfs.get(s);
			d2+=Math.pow(a, 2.0d);
		}
		
		double den = Math.sqrt(d1)*Math.sqrt(d2);
		
		return (num/den);
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
