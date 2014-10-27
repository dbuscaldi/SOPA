package fr.lipn.sts.ner;

import java.util.HashMap;
import java.util.HashSet;

import fr.lipn.sts.SemanticComparer;

public class DBPediaComparer {
	static boolean COSINE_SIM=true;
	
	public static double compare(String t1, String t2, DBPediaChunkBasedAnnotator annotator) {
		HashMap<String, Float> h1 = new HashMap<String, Float>();
		HashMap<String, Float> h2 = new HashMap<String, Float>();
		
		h1=annotator.annotate(t1);
		h2=annotator.annotate(t2);
		
		HashSet<String> shared = new HashSet<String>();
		
		shared.addAll(h1.keySet());
		shared.retainAll(h2.keySet());
		
		if (shared.size()==0) return 0d;
		
		if(SemanticComparer.VERBOSE) {
			System.err.println("Shared DBPedia entities : ");
			for(String e : shared) {
				System.err.println(e);
			}
		}
		if(COSINE_SIM) {
			double num = 0.0;
			for(String s : shared){
				double a = h1.get(s).doubleValue();
				double b = h2.get(s).doubleValue();
				num+=(a*b);
			}
			
			double d1 = 0.0;
			for(String s : h1.keySet()){
				double a = h1.get(s).doubleValue();
				d1+=Math.pow(a, 2.0d);
			}
			
			double d2 = 0.0;
			for(String s : h2.keySet()){
				double a = h2.get(s).doubleValue();
				d2+=Math.pow(a, 2.0d);
			}
			
			double den = Math.sqrt(d1)*Math.sqrt(d2);
			
			return (num/den);
		} else {
			//similar to IR score
			double sum=0d;
	    	for(String key : shared) {
	    		
	    		Float v1 = h1.get(key);
	    		double s1;
	    		if(v1==null) s1=0d;
	    		else s1=v1.doubleValue();
	    		
	    		Float v2 = h2.get(key);
	    		double s2;
	    		if(v2==null) s2=0d;
	    		else s2=v2.doubleValue();
	    		
	    		sum+=(Math.sqrt(Math.pow((s1-s2), 2)))/Math.max(s1, s2);
	    	}
	    	
	    	return 1-(sum/(double)shared.size());
	    	
		}
	}
/*
	public static double compare(String t1, String t2, DBPediaChunkBasedAnnotator annotator) {
		HashSet<String> s1=new HashSet<String>();
		HashSet<String> s2=new HashSet<String>();
		
		s1.addAll(annotator.annotate(t1));
		s2.addAll(annotator.annotate(t2));
		
		int overlap_A=0;
		int totalsize_A=0;
		//now calculate overlap (for each category)
		HashSet<String> v1 = new HashSet<String>();
		v1.addAll(s1);
		totalsize_A+=v1.size();
		v1.retainAll(s2);
		overlap_A+=v1.size();
		if(SemanticComparer.VERBOSE) System.err.println("Shared DBPedia entities : "+v1.toString());
		
		double score_A= (double)overlap_A/(double)totalsize_A;
		
		int overlap_B=0;
		int totalsize_B=0;
		v1.clear();
		v1.addAll(s2);
		totalsize_B+=v1.size();
		v1.retainAll(s1);
		overlap_B+=v1.size();
		
		double score_B= (double)overlap_B/(double)totalsize_B;
		
		if(totalsize_A > 0 && totalsize_B > 0) return (double)(2*overlap_A)/(double)(totalsize_A+totalsize_B); //Math.max(score_A, score_B);
		else return 0d;
	}
*/
}
