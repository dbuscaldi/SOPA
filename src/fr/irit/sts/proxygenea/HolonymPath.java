package fr.irit.sts.proxygenea;

import edu.mit.jwi.item.ISynsetID;
import fr.lipn.sts.tools.WordNet;

public class HolonymPath extends SynsetPath {

	public HolonymPath(ISynsetID syn){
		this.path = WordNet.getHolos(syn);
		this.syn=syn;
	}
	
}
