package fr.lipn.sts.ir.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

public class UkWaCIndexer {
	public static void main(String[] args) throws IOException {
		String filename="/media/expT1/corpora/WaCky/en/ukwac/cleaned_pre.pos.corpus";
		String dest="/media/expT1/index/ukwac";
		 
		System.err.println("Indexing to directory '" + dest + "'...");

	    Directory dir = FSDirectory.open(new File(dest));
	     
	    Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_41);
	     
	    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_41, analyzer);
	     
		iwc.setSimilarity(new BM25Similarity());
		iwc.setOpenMode(OpenMode.CREATE);
		 
		IndexWriter writer = new IndexWriter(dir, iwc);
	     
		try (BufferedReader br = new BufferedReader(new FileReader(filename)))
		{

			String sCurrentLine;
			
			Document doc=null;
			long n=0;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.startsWith("CURRENT URL ")) {
					doc=new Document();
					String title = sCurrentLine.substring(12);
					doc.add(new Field("id", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
				} else {
					if(doc!=null) {
						doc.add(new Field("text", sCurrentLine, Field.Store.YES, Field.Index.ANALYZED));
						writer.addDocument(doc);
						n+=1;
						if (n%1000 == 0) {
							System.err.print(".");
						}
						if (n%80000 == 0) {
							System.err.println();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	     writer.close();
	     System.err.println("Done.");
	}
}
