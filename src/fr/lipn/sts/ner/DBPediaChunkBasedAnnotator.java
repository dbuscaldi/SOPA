package fr.lipn.sts.ner;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class DBPediaChunkBasedAnnotator {
	private String termIndexPath;
	private StanfordCoreNLP pipeline;
	
	private static int MAX_ANNOTS=5;

	public DBPediaChunkBasedAnnotator(String termIndexPath) {
		this.termIndexPath=termIndexPath;
		 Properties props = new Properties();
		 props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		 //props.setProperty("ssplit.isOneSentence", "true"); //every string is one sentence
		 
		 pipeline = new StanfordCoreNLP(props);
	}
	
	public HashMap<String, Float> annotateTop(String document) { //returns only top annotation
		HashMap<String, Float> ret = new HashMap<String, Float>();
		
		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(termIndexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_44);
			
			Vector<String> fragments = new Vector<String>();
			
			Annotation ann = new Annotation(document);
			pipeline.annotate(ann);
			
			for(CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree parse = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
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
			
			Vector<String> fragments = new Vector<String>();
			
			Annotation ann = new Annotation(document);
			pipeline.annotate(ann);
			
			for(CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree parse = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
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
