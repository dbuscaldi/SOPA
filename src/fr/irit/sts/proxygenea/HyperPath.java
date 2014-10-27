package fr.irit.sts.proxygenea;

import edu.mit.jwi.item.ISynsetID;
import fr.lipn.sts.tools.WordNet;

public class HyperPath extends SynsetPath {
	
	public HyperPath(ISynsetID syn){
		this.path = WordNet.getHypernyms(syn);
		this.syn=syn;
	}
	

}
