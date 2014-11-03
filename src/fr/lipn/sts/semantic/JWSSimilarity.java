package fr.lipn.sts.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.mit.jwi.item.ISynsetID;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.SemanticComparer;
import fr.lipn.sts.measures.SimilarityMeasure;
import fr.lipn.sts.semantic.proxygenea.ConceptualSimilarity;
import fr.lipn.sts.semantic.proxygenea.HyperPath;
import fr.lipn.sts.semantic.proxygenea.PathNode;
import fr.lipn.sts.semantic.proxygenea.SynsetPath;
import fr.lipn.sts.tools.GoogleTFFactory;
import fr.lipn.sts.tools.WordNet;

public class JWSSimilarity implements SimilarityMeasure {
	
	public static double compare(ArrayCoreMap tSentence, ArrayCoreMap tSentence1) {
		HashMap<String, HashSet<HyperPath>> aSenses = new HashMap<String, HashSet<HyperPath>>();
		for (CoreLabel word : tSentence.get(CoreAnnotations.TokensAnnotation.class)) {
			HashSet<ISynsetID> s1_syns = new HashSet<ISynsetID>();
			String text=word.word();
			String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			s1_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HyperPath> paths_1= new HashSet<HyperPath>();
			for(ISynsetID syn : s1_syns){
				HyperPath sp = new HyperPath(syn);
				paths_1.add(sp);
			}
			aSenses.put(text, paths_1);
		}
		
		HashMap<String, HashSet<HyperPath>> bSenses = new HashMap<String, HashSet<HyperPath>>();
		for (CoreLabel word : tSentence1.get(CoreAnnotations.TokensAnnotation.class)) {
			HashSet<ISynsetID> s2_syns = new HashSet<ISynsetID>();
			String text=word.word();
			String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			s2_syns.addAll(WordNet.getNounSynsets(text, pos));
			HashSet<HyperPath> paths_2= new HashSet<HyperPath>();
			for(ISynsetID syn : s2_syns){
				HyperPath sp = new HyperPath(syn);
				paths_2.add(sp);
			}
			bSenses.put(text, paths_2);
		}
		
		double m1Sim = maxSim(aSenses, bSenses);
		double m2Sim = maxSim(bSenses, aSenses);
		
		if(SemanticComparer.VERBOSE) System.err.println("JWS weight: "+(0.5d * (m1Sim+m2Sim)));
		
		return 0.5d * (m1Sim+m2Sim);
	}

	private static double maxSim(HashMap<String, HashSet<HyperPath>> aSenses,
			HashMap<String, HashSet<HyperPath>> bSenses) {
		//retain maximum similarity for each word in S1
		double sumWeight=0d;
		double den=0d;
		
		String targetSim="";
		for(String w : aSenses.keySet()){
			double maxSim=0d;
			for(HyperPath p1 : aSenses.get(w)) {
				for(String target : bSenses.keySet()){
					HashSet<HyperPath> paths2 = bSenses.get(target);
					for(HyperPath p2 : paths2){
						if(p1.comparableTo(p2)){
							double w0 = compareIC(p1,p2);
							if (w0 > maxSim) {
								maxSim=w0;
								targetSim=target;
							}
						}	
					}
				}
			}
			
			double w_idf = GoogleTFFactory.getIDF(w);
			sumWeight+=maxSim*w_idf; //Mihalcea et al.
			den+=w_idf;
			if(maxSim > 0) {
				if(SemanticComparer.VERBOSE) System.err.println("JWS: best weight for "+w+" : "+maxSim+" <-> ("+targetSim+")");
			}
		}
		//Mihalcea et al.
		
		sumWeight=sumWeight/den;
		
		return sumWeight;
	}
	
	private static double compareIC(SynsetPath p1, SynsetPath p2){
		double ret =0d;
		PathNode lcs = ConceptualSimilarity.leastCommonSubsumer(p1, p2);
		if(lcs==null) {
			if(SemanticComparer.VERBOSE) {
				System.err.println("LCS error");
				p1.print(System.err);
				p2.print(System.err);
			}
			return 0d; 
		}

		double cd = WordNet.getIC(lcs.getSyn());
		double l1 = WordNet.getIC(p1.getSyn());
		double l2 = WordNet.getIC(p2.getSyn());
		/*System.err.println(lcs.getSyn()+" : "+cd);
		System.err.println(p1.getSyn()+" : "+l1);
		System.err.println(p2.getSyn()+" : "+l2);*/
		switch(SOPAConfiguration.IC_MEASURE){
			case SOPAConfiguration.LIN:
				ret= ((cd*cd)/(l1+l2)); break; 
			case SOPAConfiguration.JIANG_CONRATH:
				ret = (1.0d/Math.abs(1+l1+l2-2*cd)); break;
		}
		//System.err.println("JWS score: "+ret);
		return ret;
	}

	@Override
	public double compare(Object o1, Object o2) {
		return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
	}
}
