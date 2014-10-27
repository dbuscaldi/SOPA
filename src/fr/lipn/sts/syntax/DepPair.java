package fr.lipn.sts.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.stanford.nlp.ling.TaggedWord;
import fr.irit.sts.proxygenea.ConceptualComparer;
import fr.lipn.sts.SemanticComparer;
import fr.lipn.sts.tools.GoogleTFFactory;
import fr.lipn.sts.tools.LevenshteinDistance;

public class DepPair {
	Vector<Dependency> d1;
	Vector<Dependency> d2;
	
	HashMap<Dependency, DepAlignment> alMapD1; //maps d1 deps to its best alignment
	HashMap<Dependency, DepAlignment> alMapD2; //maps d2 deps """"
	
	public DepPair(Vector<Dependency> d1, Vector<Dependency> d2){
		
		//allowedLSet=new HashSet<String>();
		//for(String s : allowedLabelList) allowedLSet.add(s);
		
		this.d1=d1; this.d2=d2;
		alMapD1 = new HashMap<Dependency, DepAlignment>();
		alMapD2 = new HashMap<Dependency, DepAlignment>();
	}
	
	private String findPOS(ArrayList<TaggedWord> tSentence, int position, String word){
		if(word.equals("ROOT")) return "NN";
		try {
			TaggedWord tw = tSentence.get(position);
			if(word.startsWith(tw.word())){
				return tw.tag();
			} else {
				int lp=position-1;
				tw = tSentence.get(lp);
				if(word.startsWith(tw.word())){
					return tw.tag();
				} else {
					int rp=position+1;
					tw = tSentence.get(rp);
					if(word.startsWith(tw.word())){
						return tw.tag();
					}
				}
			}
		} catch (Exception e){
			if(position < 0){
				int i=0;
				TaggedWord tw = tSentence.get(i);
				while(!word.startsWith(tw.word()) && i < tSentence.size()-1){
					i++;
					tw=tSentence.get(i);
				}
				if(i< tSentence.size()) return tw.tag();
				else return "NN";
			}
			
			if(position >= tSentence.size()) {
				int i=tSentence.size()-1;
				TaggedWord tw = tSentence.get(i);
				while(!word.startsWith(tw.word()) && i > 0){
					i--;
					tw=tSentence.get(i);
				}
				if(i >= 0) return tw.tag();
				else return "NN";
			}
		}
		//if everything else fails, return NN
		return "NN";
	}
	
	public void setPOStags(ArrayList<TaggedWord> tSentence,
			ArrayList<TaggedWord> tSentence1) {
		
		for(Dependency d : d1) {
			int hp = d.head.getPosition()-1;
			d.head.setPOS(findPOS(tSentence, hp, d.head.getWord()));
			int tp = d.dependent.getPosition()-1;
			d.dependent.setPOS(findPOS(tSentence, tp, d.dependent.getWord()));
			
			//System.err.println(d.toString());
		}
		//System.err.println("-----");
		for(Dependency d : d2) {
			int hp = d.head.getPosition()-1;
			d.head.setPOS(findPOS(tSentence1, hp, d.head.getWord()));
			int tp = d.dependent.getPosition()-1;
			d.dependent.setPOS(findPOS(tSentence1, tp, d.dependent.getWord()));
			
			//System.err.println(d.toString());
		}
		
	}
	
	private DepAlignment getBestDep(Dependency d, Vector<Dependency> dd2){
		Vector<DepAlignment> tmpVA = new Vector<DepAlignment>();
		String label = d.label;
		DepWord head = d.head;
		DepWord tail = d.dependent;
		
		double headW=GoogleTFFactory.getIC(head.getWord());
		double tailW=GoogleTFFactory.getIC(tail.getWord());
		
		for(Dependency dd : dd2){
			String dlabel = dd.label;
			DepWord dhead = dd.head;
			DepWord dtail = dd.dependent;
			
			double dheadW=GoogleTFFactory.getIC(dhead.getWord());
			double dtailW=GoogleTFFactory.getIC(dtail.getWord());
			
			double hw = Math.max(headW, dheadW);
			double dw = Math.max(tailW, dtailW);
			
			Double ld = new Double(LevenshteinDistance.levenshteinSimilarity(label, dlabel));
			Double hsim = new Double(hw*ConceptualComparer.compare(head, dhead));
			Double tailsim = new Double(dw*ConceptualComparer.compare(tail, dtail));
			//System.err.println("H:Comparing "+head.toString()+" and "+dhead.toString()+" : "+hsim);
			//System.err.println("T:Comparing "+tail.toString()+" and "+dtail.toString()+" : "+tailsim);
			Double score;
			if((label.equals("not") || dlabel.equals("not")) && !label.equals(dlabel)) {
				//add penalization if strong similarity on head and tail
				if(hsim > 0.5 && tailsim > 0.5) score = 0d;
				else score = ld * ((hsim + tailsim)/2);
			} else {
				score = ld * ((hsim + tailsim)/2);
			}
			//other possible formulations: 2hsim*tailsim/(hsim+tailsim) ; Math.sqrt(hsim*tailsim);
			//Double score = ld * ((2*hsim*tailsim)/(hsim+tailsim));
			
			tmpVA.add(new DepAlignment(dd, score.doubleValue()));
		}

		if(tmpVA.size() == 0) return null; //no deps aligned
		Collections.sort(tmpVA);
		if(tmpVA.get(0).getScoreValue() > 0.1) {
			return tmpVA.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * This method sets the alignments for each dependency
	 */
	public void setAlignments(){
		
		for(Dependency d : d1) {
			DepAlignment match = getBestDep(d, this.d2);
			if(match != null) alMapD1.put(d, match);
			//if(SemanticComparer.VERBOSE) System.err.println("Dep1: "+d.toString()+" aligned: "+match.getDependency().toString()+" score: "+match.getScoreValue());
			
		}
		
		//now try to align deps in d2 who haven't been aligned
		for(Dependency u : d2){
			DepAlignment match = getBestDep(u, this.d1);
			if(match != null) alMapD2.put(u, match);
			//if(SemanticComparer.VERBOSE) System.err.println("Dep2: "+u.toString()+" aligned: "+match.getDependency().toString()+" score: "+match.getScoreValue());
		}
	}
	
	/**
	 * Calculates the score of the dependencies
	 * @return
	 */
	public double getDepScore(){
		//calculate the best score out of two
		double sum1=0d, sum2=0d;
		double N1 = (double) d1.size();
		double N2 = (double) d2.size();
		
		for(Dependency k : alMapD1.keySet()){
			if(SemanticComparer.VERBOSE) System.err.println("S1 Alignment: "+k.toString()+" <-> "+alMapD1.get(k).getDependency().toString()+" score: "+alMapD1.get(k).getScoreValue());
			sum1+=alMapD1.get(k).getScoreValue();
		}
		double score_1=sum1/N1;
		if(SemanticComparer.VERBOSE) System.err.println("S1 Score: "+score_1+"\t(N:"+N1+")");
		
		for(Dependency u : alMapD2.keySet()){
			if(SemanticComparer.VERBOSE) System.err.println("S2 Alignment: "+u.toString()+" <-> "+alMapD2.get(u).getDependency().toString()+" score: "+alMapD2.get(u).getScoreValue());
			sum2+=alMapD2.get(u).getScoreValue();
		}
		double score_2=sum2/N2;
		if(SemanticComparer.VERBOSE) System.err.println("S2 Score: "+score_2+"\t(N:"+N2+")");
		
		return Math.max(score_1, score_2);
	}
	

}
