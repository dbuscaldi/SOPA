package fr.lipn.sts.ir.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class OpenMultilingualWordNetIndexer {

	public static void main(String[] args) throws IOException {
		 String lang="FR";
		 String filename="res/OMWN/wnindex_"+lang+".dat";
		 String dest="res/OMWN/wnindex_"+lang;
		 
		 System.err.println("Indexing to directory '" + dest + "'...");

	     Directory dir = FSDirectory.open(new File(dest));
	     
	     Analyzer analyzer;
	     if(lang.equals("ES")) analyzer = new SpanishAnalyzer(Version.LUCENE_44);
	     else if(lang.equals("IT")) analyzer = new ItalianAnalyzer(Version.LUCENE_44);
	     else analyzer = new FrenchAnalyzer(Version.LUCENE_44);
	     
	     IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
	     
		 iwc.setSimilarity(new BM25Similarity());
		 iwc.setOpenMode(OpenMode.CREATE);
		 
		 IndexWriter writer = new IndexWriter(dir, iwc);
	     
		try (BufferedReader br = new BufferedReader(new FileReader(filename)))
		{

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String[] fragments = sCurrentLine.split("\t");
				Document doc = new Document();
				doc.add(new Field("offset", fragments[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
				StringBuffer buf = new StringBuffer();
				for(int i=1; i< fragments.length; i++) {
					buf.append(fragments[i]+" ");
				}
				doc.add(new Field("lemmas", buf.toString(), Field.Store.YES, Field.Index.ANALYZED));
				System.err.println(fragments[0]+" -> "+buf.toString());
				writer.addDocument(doc);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	     writer.close();

	}

}
