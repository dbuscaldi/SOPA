package fr.lipn.sts.syntax;

public class DepAlignment implements Comparable<DepAlignment> {
	private Dependency d;
	private Double score;
	
	public DepAlignment(Dependency d, double score){
		this.d=d;
		this.score = new Double(score);
	}

	@Override
	public int compareTo(DepAlignment o) {
		return -this.score.compareTo(o.score);
	}
	
	public Dependency getDependency(){
		return this.d;
	}
	
	public double getScoreValue(){
		return this.score.doubleValue();
	}
	
}
