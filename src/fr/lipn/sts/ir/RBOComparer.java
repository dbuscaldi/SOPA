package fr.lipn.sts.ir;

import java.io.File;
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
/**
 * This class implements the RBO measure by Webber, Moffat, Zobel (2010) "A similarity measure for indefinite rankings"
 * @author buscaldi
 *
 */
public class RBOComparer {
	private static String index = "/tempo/corpora/AQUAINT_indexed";
	
	private final static double p=0.95; //RBO parameter
	private final static int MAXHITS=100;
	
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
			
			if(SemanticComparer.VERBOSE) System.err.println("searching queries (RBO):\nQ1: "+query1.toString()+"\nQ2: "+query2.toString());
			
			TopDocs results1 = searcher.search(query1, MAXHITS);
			ScoreDoc[] hits1 = results1.scoreDocs;
			
			TopDocs results2 = searcher.search(query2, MAXHITS);
			ScoreDoc[] hits2 = results2.scoreDocs;
			
		    
		    int n_1 = results1.totalHits;
		    int n_2 = results2.totalHits;
		    
		    HashMap<Integer, HashSet<String>> q1sets = new HashMap<Integer, HashSet<String>>();
		    HashMap<Integer, HashSet<String>> q2sets = new HashMap<Integer, HashSet<String>>();
		    
		    //System.err.println(numTotalHits + " total matching documents");
		    if(n_1 > 0 && n_2 > 0) {
		    	for (int i = 0; i < Math.min(MAXHITS, n_1); i++) {
		    		Document doc = searcher.doc(hits1[i].doc);
			        String id = doc.get("id");
			        Integer k1= new Integer(i+1);
			        HashSet<String> kd= q1sets.get(new Integer(i));
			        HashSet<String> newKD = new HashSet<String>();
			        if(kd!=null){
			        	newKD.addAll(kd);
			        }
			        newKD.add(id);
		        	q1sets.put(k1, newKD);
			        
		    	}
		    	
		    	for (int i = 0; i < Math.min(MAXHITS, n_2); i++) {
		    		Document doc = searcher.doc(hits2[i].doc);
			        String id = doc.get("id");
			        Integer k2= new Integer(i+1);
			        HashSet<String> kd= q2sets.get(new Integer(i));
			        HashSet<String> newKD = new HashSet<String>();
			        if(kd!=null){
			        	newKD.addAll(kd);
			        }
			        newKD.add(id);
		        	q2sets.put(k2, newKD);
		    	}
		    	
		    	HashSet<String> uniqueIDs = new HashSet<String>();
		    	for(int i = 1; i< MAXHITS; i++) {
		    		Integer d=new Integer(i);
		    		uniqueIDs.clear();
		    		HashSet<String> m1 = q1sets.get(d);
		    		HashSet<String> m2 = q2sets.get(d);
		    		double Ad=0d;
		    		if(m1 != null && m2 != null){
		    			uniqueIDs.addAll(m1);
				    	uniqueIDs.retainAll(m2); //intersection
				    	Ad=(double)uniqueIDs.size()/d.doubleValue();
				    	//System.err.println("A"+d+" overlap: "+Ad);
		    		}
		    		double rbod=Math.pow(p, d.doubleValue()-1)*Ad;
		    		//System.err.println("RBO at "+d+": "+rbod);
		    		ret+=rbod;
		    	}
		    	
		    	ret=ret*(1-p);
		    	
		    }
			
		    reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
}
