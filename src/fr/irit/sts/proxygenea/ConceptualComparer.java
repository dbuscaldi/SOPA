package fr.irit.sts.proxygenea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.mit.jwi.item.ISynsetID;
import edu.stanford.nlp.ling.TaggedWord;
import fr.lipn.sts.SemanticComparer;
import fr.lipn.sts.syntax.DepWord;
import fr.lipn.sts.tools.LevenshteinDistance;
import fr.lipn.sts.tools.WordNet;

public class ConceptualComparer {

	public static double compare(ArrayList<TaggedWord> tSentence,
			ArrayList<TaggedWord> tSentence1) {
		
		HashMap<String, HashSet<HyperPath>> aSenses = new HashMap<String, HashSet<HyperPath>>();
		for(TaggedWord tw : tSentence){
			HashSet<ISynsetID> s1_syns = new HashSet<ISynsetID>();
			String text=tw.word();
			String pos =tw.tag();
			s1_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HyperPath> paths_1= new HashSet<HyperPath>();
			for(ISynsetID syn : s1_syns){
				HyperPath sp = new HyperPath(syn);
				paths_1.add(sp);
			}
			aSenses.put(text, paths_1);
		}
		
		HashMap<String, HashSet<HyperPath>> bSenses = new HashMap<String, HashSet<HyperPath>>();
		for(TaggedWord tw : tSentence1){
			HashSet<ISynsetID> s2_syns = new HashSet<ISynsetID>();
			String text=tw.word();
			String pos =tw.tag();
			s2_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HyperPath> paths_2= new HashSet<HyperPath>();
			for(ISynsetID syn : s2_syns){
				HyperPath sp = new HyperPath(syn);
				paths_2.add(sp);
			}
			bSenses.put(text, paths_2);
		}
		
		//retain maximum similarity for each word
		float sumWeight=0;
		int cnt=0;
		String targetSim="";
		for(String w : aSenses.keySet()){
			float maxSim=0f;
			for(HyperPath p1 : aSenses.get(w)) {
				for(String target : bSenses.keySet()){
					HashSet<HyperPath> paths2 = bSenses.get(target);
					for(HyperPath p2 : paths2){
						if(p1.comparableTo(p2)){
							float w0 = compare(p1,p2);
							if (w0 > maxSim) {
								maxSim=w0;
								targetSim=target;
							}
						}	
					}
				}
			}
			
			sumWeight+=maxSim;
			if(maxSim > 0) {
				if(SemanticComparer.VERBOSE) System.err.println("best weight for "+w+" : "+maxSim+" <-> ("+targetSim+")");
					cnt++;
			}
		}
		
		if(sumWeight > 0) sumWeight=sumWeight/(float)cnt; //NOTE: added if to avoid NaN
		
		return sumWeight;
	}
	
	public static double compareHolonyms(ArrayList<TaggedWord> tSentence,
			ArrayList<TaggedWord> tSentence1) {
		
		HashMap<String, HashSet<HolonymPath>> aSenses = new HashMap<String, HashSet<HolonymPath>>();
		for(TaggedWord tw : tSentence){
			HashSet<ISynsetID> s1_syns = new HashSet<ISynsetID>();
			String text=tw.word();
			String pos =tw.tag();
			s1_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HolonymPath> paths_1= new HashSet<HolonymPath>();
			for(ISynsetID syn : s1_syns){
				HolonymPath sp = new HolonymPath(syn);
				paths_1.add(sp);
			}
			aSenses.put(text, paths_1);
		}
		
		HashMap<String, HashSet<HolonymPath>> bSenses = new HashMap<String, HashSet<HolonymPath>>();
		for(TaggedWord tw : tSentence1){
			HashSet<ISynsetID> s2_syns = new HashSet<ISynsetID>();
			String text=tw.word();
			String pos =tw.tag();
			s2_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HolonymPath> paths_2= new HashSet<HolonymPath>();
			for(ISynsetID syn : s2_syns){
				HolonymPath sp = new HolonymPath(syn);
				paths_2.add(sp);
			}
			bSenses.put(text, paths_2);
		}
		
		//retain maximum similarity for each word
		float sumWeight=0;
		int cnt=0;
		String targetSim="";
		for(String w : aSenses.keySet()){
			float maxSim=0f;
			for(HolonymPath p1 : aSenses.get(w)) {
				for(String target : bSenses.keySet()){
					HashSet<HolonymPath> paths2 = bSenses.get(target);
					for(HolonymPath p2 : paths2){
						p1.print(System.err);
						p2.print(System.err);
						if(p1.comparableTo(p2)){
							float w0 = compare(p1,p2);
							if (w0 > maxSim) {
								maxSim=w0;
								targetSim=target;
							}
						}	
					}
				}
			}
			
			sumWeight+=maxSim;
			if(maxSim > 0) {
				if(SemanticComparer.VERBOSE) System.err.println("best holonym weight for "+w+" : "+maxSim+" <-> ("+targetSim+")");
					cnt++;
			}
		}
		sumWeight=sumWeight/(float)cnt;
		
		return sumWeight;
	}
	
	public static PathNode leastCommonSubsumer(SynsetPath p1, SynsetPath p2){
		PathNode cur=null;
		Vector<String> ra = p1.reversePath();
		Vector<String> rb = p2.reversePath();
		for(int i=0; i< Math.min(ra.size(), rb.size()); i++){
			if(ra.get(i).equals(rb.get(i))) {
				cur=new PathNode(ra.get(i), (i+1));
			}
			else break;
		}
		return cur;
	}
	/**
	 * Compares two DepWords (includes antonym check)
	 * @param a
	 * @param b
	 * @return
	 */
	public static float compare(DepWord a, DepWord b){
		HashSet<ISynsetID> s1_syns = new HashSet<ISynsetID>();
		String text=a.getWord();
		String pos =a.getPOS();
		s1_syns.addAll(WordNet.getNounSynsets(text, pos));
		
		HashSet<ISynsetID> s2_syns = new HashSet<ISynsetID>();
		String btext = b.getWord();
		String bpos = b.getPOS();
		s2_syns.addAll(WordNet.getNounSynsets(btext, bpos));
		
		float maxSim=0f;
		if(s1_syns.size() > 0 && s2_syns.size() > 0) {
			//check antonyms
			for(ISynsetID isid : s1_syns){
				for(ISynsetID isid2 : s2_syns){
					if(WordNet.checkAntonym(isid, isid2)) {
						return 0; //antonyms have 0 similarity TODO: check if this is useful or not
					}
				}
			}
			HashSet<HyperPath> paths_1= new HashSet<HyperPath>();
			for(ISynsetID syn : s1_syns){
				HyperPath sp = new HyperPath(syn);
				paths_1.add(sp);
			}
			
			HashSet<HyperPath> paths_2= new HashSet<HyperPath>();
			for(ISynsetID syn : s2_syns){
				HyperPath sp = new HyperPath(syn);
				paths_2.add(sp);
			}
			
			
			for(HyperPath p1 : paths_1) {
				for(HyperPath p2 : paths_2){
					if(p1.comparableTo(p2)){
						float w0 = compare(p1,p2);
						if (w0 > maxSim) {
							maxSim=w0;
						}
					}
				}
			}
			
		}
		
		if(maxSim == 0) return LevenshteinDistance.levenshteinSimilarity(text, btext);
		else return maxSim;
		
	}
	
	private static float compare(SynsetPath p1, SynsetPath p2){
		float ret =0f;
		PathNode lcs = leastCommonSubsumer(p1, p2);
		if(lcs==null) {
			if(SemanticComparer.VERBOSE) {
				System.err.println("LCS error");
				p1.print(System.err);
				p2.print(System.err);
			}
			return 0f;
		}
		float cd = (float)lcs.depth();
		float l1 = (float)p1.size();
		float l2 = (float)p2.size();
		
		switch(SemanticComparer.STRUCTURAL_MEASURE){
			case SemanticComparer.PROXYGENEA1:
				ret= (cd*cd)/(l1*l2); break; 
			case SemanticComparer.PROXYGENEA2:
				ret = cd/(l1+l2-cd); break;
			case SemanticComparer.PROXYGENEA3:
				ret = 1f/(1f+l1+l2-2*cd); break;
			case SemanticComparer.WU_PALMER:
				ret= (2*cd)/(l1+l2); break;
		}
		return ret;
	}
	

	

}
