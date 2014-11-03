package fr.lipn.sts.syntax;

import edu.stanford.nlp.trees.TreeGraphNode;

public class Dependency {
	String label;
	DepWord head;
	DepWord dependent;
	
	public Dependency(String label, String head, String dependent){
		this.label=label;
		this.head=new DepWord(head);
		this.dependent=new DepWord(dependent);
	}
	
	public Dependency(String shortName, TreeGraphNode gov, TreeGraphNode dep) {
		this.label=shortName;
		this.head=new DepWord(gov);
		this.dependent=new DepWord(dep);
	}

	public String toString(){
		return label+"("+head.getWord()+"-"+head.getPOS()+", "+dependent.getWord()+"-"+dependent.getPOS()+")";
	}
	
	public boolean equals(Object other){
		return this.toString().equals(((Dependency)other).toString());
	}
	
	public int hashCode(){
		return this.toString().hashCode();
	}
	
}
