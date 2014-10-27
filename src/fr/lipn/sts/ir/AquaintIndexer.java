package fr.lipn.sts.ir;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class AquaintIndexer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String usage = "java fr.lipn.sts.ir.AquaintIndexer"
                + " [-index INDEX_PATH] -docs DOCS_PATH [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
   String indexPath = "index";
   String docsPath = null;
   
   for(String arg : args){
   	System.err.println(arg);
   }
   
   boolean create = true;
   for(int i=0;i<args.length;i++) {
     if ("-index".equals(args[i])) {
       indexPath = args[i+1];
       i++;
     } else if ("-docs".equals(args[i])) {
       docsPath = args[i+1];
       i++;
     } else if ("-update".equals(args[i])) {
       create = false;
     }
   }

   if (docsPath == null) {
     System.err.println("Usage: " + usage);
     System.exit(1);
   }

   final File docDir = new File(docsPath);
   if (!docDir.exists() || !docDir.canRead()) {
     System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
     System.exit(1);
   }
   
   Date start = new Date();
   try {
     System.out.println("Indexing to directory '" + indexPath + "'...");

     Directory dir = FSDirectory.open(new File(indexPath));
     Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_41);
     IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_41, analyzer);
	 iwc.setSimilarity(new BM25Similarity());

     if (create) {
       // Create a new index in the directory, removing any
       // previously indexed documents:
       iwc.setOpenMode(OpenMode.CREATE);
     } else {
       // Add new documents to an existing index:
       iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
     }
	    
     IndexWriter writer = new IndexWriter(dir, iwc);
     indexDocs(writer, docDir);

     // NOTE: if you want to maximize search performance,
     // you can optionally call forceMerge here.  This can be
     // a terribly costly operation, so generally it's only
     // worth it when your index is relatively static (ie
     // you're done adding documents to it):
     //
     // writer.forceMerge(1);

     writer.close();

     Date end = new Date();
     System.out.println(end.getTime() - start.getTime() + " total milliseconds");

   } catch (IOException e) {
     System.out.println(" caught a " + e.getClass() +
      "\n with message: " + e.getMessage());
   }
 }

 /**
  * Indexes the given file using the given writer, or if a directory is given,
  * recurses over files and directories found under the given directory.
  * 
  * NOTE: This method indexes one document per input file.  This is slow.  For good
  * throughput, put multiple documents into your input file(s).  An example of this is
  * in the benchmark module, which can create "line doc" files, one document per line,
  * using the
  * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
  * >WriteLineDocTask</a>.
  *  
  * @param writer Writer to the index where the given file/dir info will be stored
  * @param file The file to index, or the directory to recurse into to find files to index
  * @throws IOException
  */
 static void indexDocs(IndexWriter writer, File file)
   throws IOException {
   // do not try to index files that cannot be read
   if (file.canRead()) {
     if (file.isDirectory()) {
       String[] files = file.list();
       // an IO error could occur
       if (files != null) {
         for (String f : files) {
           indexDocs(writer, new File(file, f));
         }
       }
     } else {
   	if(file.getName().endsWith(".xml")) {
   		System.out.println("indexing " + file);
           try {
           		AquaintXMLHandler hdlr = new AquaintXMLHandler(file);
           		Vector<Document> docs=hdlr.getParsedDocuments();
           		for(Document doc : docs) writer.addDocument(doc);
           } catch (Exception e) {
           	e.printStackTrace();
           } 
	      }
	    }
   }

  }

}
