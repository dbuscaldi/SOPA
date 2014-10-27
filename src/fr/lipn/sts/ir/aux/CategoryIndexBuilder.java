package fr.lipn.sts.ir.aux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class CategoryIndexBuilder {
	private static String catFile_en= "/tempo/corpora/DBPedia/categories/article_categories_en.nt";
	private static String oldID;
	private static StringBuffer categs = new StringBuffer();
	private static Pattern p;
	private static Document currentDoc;
	private static int nlines=0;
	
	public static void main(String[] args) throws IOException {
		
		String indexPath = "/tempo/corpora/DBPedia/catindex_en";
		
		Directory dir = FSDirectory.open(new File(indexPath));
	    Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_44);
	    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setSimilarity(new BM25Similarity());
	    iwc.setOpenMode(OpenMode.CREATE);
	    
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    
		BufferedReader br = new BufferedReader(new FileReader(catFile_en));
		String line;
		oldID="";
		
		p= Pattern.compile("(<http://dbpedia.org/resource/Category:)(.+?)(>)");
		
		while ((line = br.readLine()) != null) {
			if(!line.startsWith("#")){
				String [] elems = line.split("> ");
				String id = elems[0].concat(">");
				if(!id.equals(oldID)) {
					flush(writer);
					categs.delete(0, categs.length());
					oldID=id;
				}
				
				String cat = elems[2].concat(">");
				cat= cat.replaceAll("\\(", "_");
				cat= cat.replaceAll("\\)", "_");
				
				Matcher matcher = p.matcher(cat);
				matcher.find();
				//String cleanCat = (matcher.group(2)).replace('_', ' ');
				String cleanCat = matcher.group(2);
				
				//System.err.println(cleanCat);
				categs.append(cleanCat+" ");
			}
		}
		br.close();
		
		writer.close();

	}

	private static void flush(IndexWriter writer) throws IOException {
		//System.err.println(oldID+" ---- "+categs.toString().trim());
		System.err.print(".");
		if (nlines % 81 == 0) {
    		System.err.println();
    	}
    	nlines++;
    	
		currentDoc= new Document();
		currentDoc.add(new Field("title", oldID, Field.Store.YES, Field.Index.NOT_ANALYZED));
    	currentDoc.add(new Field("categories", categs.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    	writer.addDocument(currentDoc);
	}

}
