package fr.lipn.sts.ckpd;

import edu.stanford.nlp.ling.CoreLabel;
import fr.lipn.sts.tools.GoogleTFFactory;

public class Term {
	String text;
	String POS;
	double weight;
	
	public Term(CoreLabel tw) {
		this.text=tw.word();
		this.POS=tw.tag();
	}
	/**
	 * Creates a term from an un-labelled fragment of text
	 * @param w
	 */
	public Term(String w) {
		this.text=w;
		this.POS="UNK";
	}
	
	public String repr() {
		return text+"/"+POS;
	}
	
	public boolean equals(Object other) {
		Term t = (Term)other;
		return this.equals(t);
	}
	
	public boolean equals(Term other){
		return this.text.equals(other.text); //ignoring POS
	}
	
	public int hashCode(){
		return this.text.hashCode();
	}

	public double getWeight() {
		long nCount=GoogleTFFactory.getFrequency(this.text)+1; //+1 to avoid infinity
		this.weight = 1.0-(Math.log10((double)nCount))/(Math.log10((double)GoogleTFFactory.MAX_FREQ));
		return this.weight;
	}
}
