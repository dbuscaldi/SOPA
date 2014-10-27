package fr.lipn.sts.ckpd;

public class SkipGram {
	protected String head;
	protected String tail;
	
	public SkipGram(String word1, String word2) {
		this.head=word1;
		this.tail=word2;
	}

	public boolean equals(Object anotherObj){
		SkipGram other = (SkipGram)anotherObj;
		return this.equals(other);
	}
	
	public boolean equals(SkipGram other){
		return this.head.equals(other.head) && this.tail.equals(other.tail);
	}
	
	public String repr() {
		return this.head+":"+this.tail;
	}
	
	public int hashCode() {
		return this.repr().hashCode();
	}
}
