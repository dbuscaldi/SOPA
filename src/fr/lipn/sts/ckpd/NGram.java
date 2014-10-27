package fr.lipn.sts.ckpd;

import java.util.Vector;

public class NGram {
	private Vector<Term> sequence;
	private int distanceFromLongest;
	private double weight;
	
	public NGram(){
		sequence=new Vector<Term>();
	}
	
	public void add(Term term) {
		sequence.add(term);
	}
	
	public int getSize(){
		return sequence.size();
	}
	
	public boolean equals(Object anotherObj){
		NGram other = (NGram)anotherObj;
		return this.equals(other);
	}
	
	public boolean equals(NGram other){
		if(this.getSize()==other.getSize()){
			for(int i=0; i< sequence.size(); i++){
				if(sequence.get(i).equals(other.sequence.get(i))) continue;
				else return false;
			}
			return true;
		} else return false;
	}
	
	public String repr(){
		StringBuffer buf = new StringBuffer();
		for(Term t : sequence) {
			buf.append(t.repr());
			buf.append(" ");
		}
		return buf.toString().trim();
	}
	
	public int hashCode(){
		return this.repr().hashCode();
	}
	
	public boolean containedIn(NGram other){
		String r1=this.repr();
		String r2=other.repr();
		if(r2.contains(r1)) return true;
		else return false;
	}
	
	public void calculateDistance(NGram largest, NGram phrase){
		String lrgRep=largest.repr();
		String phrRep=phrase.repr();
		
		int lrgStart=phrRep.indexOf(lrgRep);
		int lrgEnd=lrgStart+lrgRep.length();
		
		int start = phrRep.indexOf(this.repr());
		int end = start+ this.repr().length();
		
		if(start >= lrgStart && end <= lrgEnd) this.distanceFromLongest=0;
		else {
			if(start > lrgEnd) this.distanceFromLongest=(start-lrgEnd);
			if(end < lrgStart) this.distanceFromLongest=(lrgStart-end);
		}
	}
	
	public void setWeights(){
		double tmpW=0.0;
		for(Term t : sequence) {
			tmpW+=t.getWeight();
		}
		this.weight=tmpW;
	}
	
	public double getWeight(){
		return this.weight;
	}
	
	public double getDistanceCoeff(){
		double eps=0.1;
		return (1.0+eps*Math.log((double)(1+this.distanceFromLongest)));
	}

}
