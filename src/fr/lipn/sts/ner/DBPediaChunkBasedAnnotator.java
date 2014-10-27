package fr.lipn.sts.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;

public class DBPediaChunkBasedAnnotator {
	private String termIndexPath;
	private LexicalizedParser parser;
	
	private static int MAX_ANNOTS=5;

	public DBPediaChunkBasedAnnotator(String termIndexPath) {
		this.termIndexPath=termIndexPath;
		 parser = LexicalizedParser.loadModel("lib/englishPCFG.ser.gz");
	}
	
	public HashMap<String, Float> annotateTop(String document) { //returns only top annotation
		HashMap<String, Float> ret = new HashMap<String, Float>();
		
		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(termIndexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_44);
			
			Reader r = new BufferedReader(new StringReader(document));
			Vector<String> fragments = new Vector<String>();
			
			for(List<HasWord> sentence : new DocumentPreprocessor(r)) {
				Tree parse = parser.apply(sentence);
				for(Tree p : parse){
					if(p.label().value().equals("NP") && p.isPrePreTerminal()) {
						//p.pennPrint();
						StringBuffer tmpstr = new StringBuffer();
						for(Tree l : p.getLeaves()){
							
							tmpstr.append(l.label().toString());
							tmpstr.append(" ");
						}
						fragments.add(tmpstr.toString().trim());
						//System.err.println("Chunk found: "+tmpstr);
					}
					
				}
			}
			
			
			for(String fragment :  fragments) {
				
				if(fragment.length()==0) continue;
				//System.err.println("Annotating: "+fragment);
						
				QueryParser parser = new QueryParser(Version.LUCENE_44, "title", analyzer);
				Query query = parser.parse(fragment);
				
				TopDocs results = searcher.search(query, 20);
			    ScoreDoc[] hits = results.scoreDocs;
			    
			    int numTotalHits = results.totalHits;
			    //System.err.println(numTotalHits + " total matching articles");
			    
			    if(numTotalHits > 0) {
				    hits = searcher.search(query, numTotalHits).scoreDocs;
				    for(int i=0; i< Math.min(numTotalHits, 1); i++){
				    	Document doc = searcher.doc(hits[i].doc);
				    	String id = doc.get("id");
				    	float score = hits[i].score;
				    	//String categories = doc.get("categories");
				    	ret.put(id, new Float(score));
				    	//System.err.println(id);
				    }
			    }
								 
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public HashMap<String, Float> annotate(String document) {
		HashMap<String, Float> ret = new HashMap<String, Float>();
		
		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(termIndexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_44);
			
			Reader r = new BufferedReader(new StringReader(document));
			Vector<String> fragments = new Vector<String>();
			
			for(List<HasWord> sentence : new DocumentPreprocessor(r)) {
				Tree parse = parser.apply(sentence);
				for(Tree p : parse){
					if(p.label().value().equals("NP") && p.isPrePreTerminal()) {
						//p.pennPrint();
						StringBuffer tmpstr = new StringBuffer();
						for(Tree l : p.getLeaves()){
							
							tmpstr.append(l.label().toString());
							tmpstr.append(" ");
						}
						fragments.add(tmpstr.toString().trim());
						//System.err.println("Chunk found: "+tmpstr);
					}
					
				}
			}
			
			
			for(String fragment :  fragments) {
				
				if(fragment.length()==0) continue;
				//System.err.println("Annotating: "+fragment);
				try {		
					QueryParser parser = new QueryParser(Version.LUCENE_44, "title", analyzer);
					Query query = parser.parse(fragment);
					
					TopDocs results = searcher.search(query, 20);
				    ScoreDoc[] hits = results.scoreDocs;
				    
				    int numTotalHits = results.totalHits;
				    //System.err.println(numTotalHits + " total matching articles");
				    
				    if(numTotalHits > 0) {
					    hits = searcher.search(query, numTotalHits).scoreDocs;
					    for(int i=0; i< Math.min(numTotalHits, MAX_ANNOTS); i++){
					    	Document doc = searcher.doc(hits[i].doc);
					    	String id = doc.get("id");
					    	float score = hits[i].score;
					    	//String categories = doc.get("categories");
					    	ret.put(id, new Float(score));
					    	//System.err.println(id);
					    }
				    }
				} catch(ParseException pe) {
					continue;
				}
								 
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
