package fr.lipn.sts.ir.aux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;



public class DBPediaIndexer {
	
	public static void main(String[] args) throws IOException, ParseException {
		String indexPath = "/tempo/corpora/DBPedia/indexed";
		String cat_indexPath = "/tempo/corpora/DBPedia/catindex_en";
		String fileName = "/tempo/corpora/DBPedia/abstracts/long_abstracts_en.nt";
		
		/* category index reading section */
		IndexReader ireader = IndexReader.open(FSDirectory.open(new File(cat_indexPath)));
		IndexSearcher searcher = new IndexSearcher(ireader);
		searcher.setSimilarity(new BM25Similarity());
		Analyzer sanalyzer = new WhitespaceAnalyzer(Version.LUCENE_44);
		QueryParser parser = new QueryParser(Version.LUCENE_44, "title", sanalyzer);
			
		/* global index writing section */
		Directory dir = FSDirectory.open(new File(indexPath));
	    Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_44);
	    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setSimilarity(new BM25Similarity());
	    iwc.setOpenMode(OpenMode.CREATE);
	    
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    
	    FileInputStream in = new FileInputStream(fileName);
	    //BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    
	   
		
		
	    String line="";
	    
	    int i=0;
	    
	    Document currentDoc;
	    
	    while( (line = reader.readLine()) != null) {
	    	if(line.startsWith("#")) continue;
	    	String [] elems = line.split("> ");
	    	String id = elems[0].concat(">");
	    	
	    	//System.err.println(id+" -> cats: ");
	    	String categ = getCategories(id, searcher, parser);
	    	//System.err.println(categ);	    	
	    	
	    	String head = elems[0].replaceAll("<http://dbpedia.org/resource/", "");
	    	head=head.replaceAll("_", " ");
	    	String tail = elems[2].replaceAll("@en..", "");
	    	
	    	if(!tail.startsWith("\"\"")) {
	    		currentDoc= new Document();
	    		currentDoc.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    		currentDoc.add(new Field("title", head, Field.Store.YES, Field.Index.ANALYZED));
		    	currentDoc.add(new Field("abstract", tail, Field.Store.YES, Field.Index.ANALYZED));
		    	currentDoc.add(new Field("categories", categ, Field.Store.YES, Field.Index.ANALYZED));
		    	writer.addDocument(currentDoc);
		    	System.err.print(".");
	    		//System.err.println(head+" -> "+tail);
	    	}
	    	
	    	if (i % 81 == 0) {
	    		System.err.println();
	    	}
	    	i++;
	    	//if (i> 10) break;
	    }
	    
	    
	    System.out.println("Read "+i+" lines.");
	    
	    reader.close();
	    writer.close();
	    
	    ireader.close();

	}

	private static String getCategories(String id, IndexSearcher searcher, QueryParser parser) throws ParseException, IOException {
		String res="";
		Query q = parser.parse("title:\""+id+"\"");
		
		TopDocs results = searcher.search(q, 10);
		ScoreDoc[] hits = results.scoreDocs;
		if(results.totalHits > 0) {
			Document doc = searcher.doc(hits[0].doc);
	    	res=doc.get("categories");	
		}
		
		return res;
	}

}
