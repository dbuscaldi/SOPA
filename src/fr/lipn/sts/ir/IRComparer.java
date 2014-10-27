package fr.lipn.sts.ir;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import fr.lipn.sts.SemanticComparer;

public class IRComparer {
	private static String index = "/tempo/indexes/AQUAINT_indexed";
	
	private final static int K=70;
	
	public static double compare(String req1, String req2){
		double ret = 0d;
		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_41);
			
			QueryParser parser;
			parser = new QueryParser(Version.LUCENE_41, "text", analyzer);
			
			req1=req1.replaceAll( "[^\\w]", " ");
			req2=req2.replaceAll( "[^\\w]", " ");
			
			Query query1 = parser.parse(req1.trim());
			Query query2 = parser.parse(req2.trim());
			
			if(SemanticComparer.VERBOSE) System.err.println("searching queries:\nQ1: "+query1.toString()+"\nQ2: "+query2.toString());
			
			TopDocs results1 = searcher.search(query1, 100);
			ScoreDoc[] hits1 = results1.scoreDocs;
			
			TopDocs results2 = searcher.search(query2, 100);
			ScoreDoc[] hits2 = results2.scoreDocs;
			
		    
		    int n_1 = results1.totalHits;
		    int n_2 = results2.totalHits;
		    
		    HashMap<String, Double> m1 = new HashMap<String, Double>();
		    HashMap<String, Double> m2 = new HashMap<String, Double>();
		    
		    //System.err.println(numTotalHits + " total matching documents");
		    if(n_1 > 0 && n_2 > 0) {
		    	for (int i = 0; i < Math.min(K, n_1); i++) {
		    		Document doc = searcher.doc(hits1[i].doc);
			        String id = doc.get("id");
			        Double score = new Double(hits1[i].score);
			        m1.put(id, score);
			        //if(SemanticComparer.VERBOSE) System.err.println("req1 doc: "+id+" : "+score);
		    	}
		    	
		    	for (int i = 0; i < Math.min(K, n_2); i++) {
		    		Document doc = searcher.doc(hits2[i].doc);
			        String id = doc.get("id");
			        Double score = new Double(hits2[i].score);
			        m2.put(id, score);
			        //if(SemanticComparer.VERBOSE) System.err.println("req2 doc: "+id+" : "+score);
		    	}
		    	
		    	HashSet<String> uniqueIDs = new HashSet<String>();
		    	uniqueIDs.addAll(m1.keySet());
		    	uniqueIDs.retainAll(m2.keySet()); //intersection
		    	//uniqueIDs.addAll(m2.keySet()); //union
		    	
		    	if(uniqueIDs.size() == 0) return 0d; //no common docs
		    	
		    	double sum=0d;
		    	for(String key : uniqueIDs) {
		    		
		    		Double v1 = m1.get(key);
		    		double s1;
		    		if(v1==null) s1=0d;
		    		else s1=v1.doubleValue();
		    		
		    		Double v2 = m2.get(key);
		    		double s2;
		    		if(v2==null) s2=0d;
		    		else s2=v2.doubleValue();
		    		
		    		if(SemanticComparer.VERBOSE) System.err.println("common doc: "+key+" s1: "+s1+" s2: "+s2);
		    		
		    		sum+=(Math.sqrt(Math.pow((s1-s2), 2)))/Math.max(s1, s2);
		    	}
		    	
		    	ret=1-(sum/(double)uniqueIDs.size());
		    }
			
		    reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	

}
