package fr.lipn.sts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fr.lipn.sts.align.sphynx.SphynxSimilarity;
import fr.lipn.sts.align.sultan.SultanSimilarity;
import fr.lipn.sts.basic.Levenshtein;
import fr.lipn.sts.basic.SentenceLengthSimilarity;
import fr.lipn.sts.basic.TfIdfSimilarity;
import fr.lipn.sts.ckpd.NGramSimilarity;
import fr.lipn.sts.ckpd.SkipGramSimilarity;
import fr.lipn.sts.geo.BlueMarble;
import fr.lipn.sts.geo.GeographicScopeSimilarity;
import fr.lipn.sts.ir.IRSimilarity;
import fr.lipn.sts.ir.RBOSimilarity;
import fr.lipn.sts.ner.DBPediaChunkBasedAnnotator;
import fr.lipn.sts.ner.DBPediaSimilarity;
import fr.lipn.sts.ner.NERSimilarity;
import fr.lipn.sts.semantic.JWSSimilarity;
import fr.lipn.sts.semantic.SpectralDistance;
import fr.lipn.sts.semantic.proxygenea.ConceptualSimilarity;
import fr.lipn.sts.syntax.DepBasedSimilarity;
import fr.lipn.sts.tools.GoogleTFFactory;
import fr.lipn.sts.tools.WordNet;
import fr.lipn.sts.twitter.TwitterComparer;

public class SemanticComparer {
	public static boolean TRAIN_MODE=false;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws JSAPException 
	 */
		
	public static void main(String[] args) throws ClassNotFoundException, IOException, JSAPException {

		JSAP jsap = new JSAP();
        
        FlaggedOption opt2 = new FlaggedOption("inputFile")
        .setStringParser(JSAP.STRING_PARSER)
        //.setDefault("Z:/SemEval/train/STS.input.MSRvid.txt") 
        .setRequired(true) 
        .setShortFlag('f') 
        .setLongFlag("file");

        opt2.setHelp("The SemEval file containing the sentence pairs");
        jsap.registerParameter(opt2);
        
        FlaggedOption opt7 = new FlaggedOption("gsFile")
        .setStringParser(JSAP.STRING_PARSER)
        .setRequired(false) 
        .setShortFlag('g') 
        .setLongFlag("gs");

        opt7.setHelp("Gold Standard file (if specified, enables train mode)");
        jsap.registerParameter(opt7);
        
        Switch sw1 = new Switch("verbose")
        .setShortFlag('v')
        .setLongFlag("verbose");

        sw1.setHelp("Requests verbose output.");
        jsap.registerParameter(sw1);
        
        JSAPResult config = jsap.parse(args);    

        if (!config.success()) {
            
            System.err.println();

            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (java.util.Iterator errs = config.getErrorMessageIterator();
                    errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            
            System.err.println();
            System.err.println("Usage: java "
                                + SemanticComparer.class.getName());
            System.err.println("                "
                                + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }
        
        String inputfile=config.getString("inputFile"); //"Z:/SemEval/train/STS.input.MSRvid.txt";
        
        SOPAConfiguration.VERBOSE=config.getBoolean("verbose");
        
        String gsFile = config.getString("gsFile");
        if(gsFile != null) TRAIN_MODE=true;
	    
        SOPAConfiguration.load(); //Necessary to load configuration parameters related to resources
        
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    props.setProperty("ssplit.isOneSentence", "true"); //every string is one sentence
	    props.setProperty("ner.applyNumericClassifiers", "true");
	    props.setProperty("sutime.markTimeRanges", "true");
	    props.setProperty("sutime.includeRange", "true");
	    
	    // Check if it is necessary to use Spanish settings
	    if(SOPAConfiguration.LANG.equals("es")) {
	    	props.setProperty("tokenize.language", "es");
	    	props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
	    	props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
	    	props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
	    	
	    }
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    GoogleTFFactory.init(SOPAConfiguration.GoogleTF);
	    WordNet.init(SOPAConfiguration.WN_HOME);
	    BlueMarble.init();
	    
	    Vector<String> gsLabels = new Vector<String>();
	    if(TRAIN_MODE){
	    	BufferedReader gsReader = new BufferedReader(new FileReader(gsFile));
	    	String str;
	    	while((str=gsReader.readLine())!=null){
	    		gsLabels.add(str.trim());
	    	}
	    	gsReader.close();
	    }
	   
	    BufferedReader reader = new BufferedReader(new FileReader(inputfile));
	    String line;
	    
	    //DepComparer.parse(inputfile+".lorg.deps.xml");
	    
	    int i=0;
	    DBPediaChunkBasedAnnotator chunkannotator = new DBPediaChunkBasedAnnotator(SOPAConfiguration.DBPedia_INDEX);
	    DBPediaSimilarity.setAnnotator(chunkannotator);
	    
	    while((line=reader.readLine())!=null){
	    	String [] sentences = line.split("\t");
	    	
	    	//fix to set when there are points in sentences that may mislead the tagger
	    	//sentences[0] = sentences[0].replace('.', ' ');
	    	//sentences[1] = sentences[1].replace('.', ' ');
	    	sentences[0].concat(".");
	    	sentences[1].concat(".");
	    	
	    	/* code for Stanford NLPCore */
	    	Annotation ann0 = new Annotation(sentences[0]);
	    	Annotation ann1 = new Annotation(sentences[1]);
	    	
	    	pipeline.annotate(ann0);
	    	pipeline.annotate(ann1);
	    	
	    	ArrayCoreMap sent0 = (ArrayCoreMap) ann0.get(CoreAnnotations.SentencesAnnotation.class).get(0);
	    	ArrayCoreMap sent1 = (ArrayCoreMap) ann1.get(CoreAnnotations.SentencesAnnotation.class).get(0);
	    		
	    	IRSimilarity irs = new IRSimilarity();
	    	IRSimilarity irswac= new IRSimilarity("/media/expT1/index/ukwac");
	    	
		    double DBPsim=DBPediaSimilarity.compare(sentences[0], sentences[1]);
		    double NERsim=NERSimilarity.compare(sent0, sent1);
		    double sim=NGramSimilarity.compare(sent0, sent1);
		    double conceptsim=ConceptualSimilarity.compare(sent0, sent1);
		    double wnsim=JWSSimilarity.compare(sent0, sent1);
		    double depsim = DepBasedSimilarity.compare(sent0, sent1);
		    double editsim = Levenshtein.characterBasedSimilarity(sentences[0], sentences[1]);
		    double IRsim = irs.compare(sentences[0], sentences[1]);
		    double wacSim = irswac.compare(sentences[0], sentences[1]);
		    double RBOsim = RBOSimilarity.compare(sentences[0], sentences[1]); //RBO measure for IR comparison
		    double cosinesim = TfIdfSimilarity.compare(sent0, sent1);
		    double geosim = GeographicScopeSimilarity.compare(sent0, sent1);
		    double sksim=SkipGramSimilarity.compare(sent0, sent1);
		    double sphynxsim = SphynxSimilarity.compare(sent0,sent1);
		    double sultansim = SultanSimilarity.compare(sentences[0],sentences[1]);
		    double lsim = SentenceLengthSimilarity.compare(sentences[0], sentences[1]);
		    
		    //double spectsim = SpectralSimilarity.compare(sent0, sent1);
		    
		
		    if(SOPAConfiguration.VERBOSE) {
		    	System.err.println("Pair # "+(i+1));
		    	System.err.println(sentences[0]);
			    System.err.println(sentences[1]);
			    if(TRAIN_MODE) System.err.println("GS score: "+gsLabels.elementAt(i));
			    System.err.println(":");
			    System.err.println("CKPD (n-gram) similarity: "+5.0 *sim);
			    System.err.println("Conceptual (WordNet) similarity: "+5.0 *conceptsim);
			    System.err.println("Dependency-based (syntactic) similarity: "+5.0 *depsim);
			    System.err.println("Edit distance similarity: "+5.0 *editsim);
			    System.err.println("Cosine distance (tf.idf) similarity: "+5.0 *cosinesim);
			    System.err.println("NER overlap : "+5.0 *NERsim);
			    System.err.println("Conceptual (Jiang-Conrath) similarity: "+5.0 *wnsim);
			    System.err.println("IR-based similarity : "+5.0 *IRsim);
			    System.err.println("Geographic Scope similarity: "+5.0 *geosim);
			    
			    System.err.println("RBO similarity : "+RBOsim);
			    System.err.println("DBPedia similarity : "+5.0 *DBPsim);
			    System.err.println("UKWaC similarity : "+wacSim);
			    System.err.println("SkipGram similarity : "+sksim);
			    System.err.println("Sphynx similarity : "+sphynxsim);
			    System.err.println("Sultan similarity : "+sultansim);
			    System.err.println("Length similarity : "+lsim);
			    
			    //System.err.println("Spectral distance (the smaller better): "+spectsim);
			    System.err.println("--------------");
			    
		    } else {
	 
	    		if(TRAIN_MODE){
	    			System.out.print(gsLabels.elementAt(i)+" ");
	    		} else {
	    			System.out.print("0.0 ");
	    		}
	    		//System.out.println("1:"+sim+" 2:"+conceptsim+" 3:"+depsim+" 4:"+editsim+" 5:"+cosinesim+" 6:"+NERsim+" 7:"+wnsim+" 8:"+IRsim+" 9:"+geosim);
	    		System.out.println("1:"+sim+" 2:"+conceptsim+" 3:"+depsim+" 4:"+editsim+" 5:"+cosinesim+" 6:"+NERsim+" 7:"+wnsim+" 8:"+IRsim+" 9:"+geosim+" 10:"+RBOsim+ " 11:"+DBPsim+" 12:"+wacSim+" 13:"+sksim+" 14:"+sphynxsim+" 15:"+sultansim+" 16:"+lsim);
		    }
		    
		    i++;
	    }
	    reader.close();
	    
		
	}
	

}
