package fr.lipn.sts.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.ling.TaggedWord;

public class TfIdfComparer {

	public static double compare(ArrayList<TaggedWord> tSentence, ArrayList<TaggedWord> tSentence1){
		HashMap<String, Integer> freqs1 = new HashMap<String, Integer>();
		HashMap<String, Integer> freqs2 = new HashMap<String, Integer>();
		
		HashSet<String> shared = new HashSet<String>();
		HashMap<String, Double> idfs = new HashMap<String, Double>();
		
		for(TaggedWord t : tSentence){
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
		
		for(TaggedWord t : tSentence1){
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
}
