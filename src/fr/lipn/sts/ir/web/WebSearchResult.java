package fr.lipn.sts.ir.web;

import fr.lipn.sts.basic.Levenshtein;
import fr.lipn.sts.basic.TfIdfSimilarity;

public class WebSearchResult {
	private String title;
	private String url;
	private String snippet;
	
	public WebSearchResult(String title, String snippet, String url) {
		this.title=title;
		this.snippet=snippet;
		this.url=url;
	}
	
	public boolean equals(WebSearchResult other) {
		return this.title.equals(other.title) && this.url.equals(other.url);
	}
	
	public boolean equals(Object o) {
		return this.equals((WebSearchResult)o);
	}
	
	public double getSimilarity(WebSearchResult other) {
		//TODO: calculate similarity between this search result and the other
		//nice: we can use existing similarity measures - but how to combine them?
		double score=0d;
		score=Math.sqrt(Levenshtein.characterBasedSimilarity(this.title, other.title)*Levenshtein.characterBasedSimilarity(this.url, other.url));
		score=Math.sqrt(TfIdfSimilarity.compare(this.title, other.title)*score);
		return score;
	}
	
	public String toString() {
		return this.title+" : "+this.snippet+" -> "+this.url;
	}
}
