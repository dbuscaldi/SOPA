package fr.irit.sts.proxygenea;

import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SynsetID;

public class PathNode {
	String id;
	int depth;
	ISynsetID syn;
	
	public PathNode(String id, int depth){
		this.id=id;
		this.depth=depth;
		syn = new SynsetID(Integer.parseInt(id), POS.NOUN);
	}
	
	public int depth(){
		return depth;
	}
	
	public String name(){
		return id;
	}
	
	public ISynsetID getSyn(){
		return syn;
	}
}
