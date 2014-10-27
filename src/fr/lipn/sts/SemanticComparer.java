package fr.lipn.sts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fr.irit.sts.proxygenea.ConceptualComparer;
import fr.lipn.sts.ckpd.NGramComparer;
import fr.lipn.sts.geo.BlueMarble;
import fr.lipn.sts.geo.GeographicScopeComparer;
import fr.lipn.sts.ir.IRComparer;
import fr.lipn.sts.ir.RBOComparer;
import fr.lipn.sts.ner.DBPediaChunkBasedAnnotator;
import fr.lipn.sts.ner.DBPediaComparer;
import fr.lipn.sts.ner.NERComparer;
import fr.lipn.sts.semantic.JWSComparer;
import fr.lipn.sts.semantic.SpectralComparer;
import fr.lipn.sts.syntax.DepComparer;
import fr.lipn.sts.tools.GoogleTFFactory;
import fr.lipn.sts.tools.LevenshteinDistance;
import fr.lipn.sts.tools.TfIdfComparer;
import fr.lipn.sts.tools.WordNet;
import fr.lipn.sts.twitter.TwitterComparer;

public class SemanticComparer {
	public final static int PROXYGENEA1=0;
	public final static int PROXYGENEA2=1;
	public final static int PROXYGENEA3=2;
	public final static int WU_PALMER=3;
	public final static int LIN=4;
	public final static int JIANG_CONRATH=5;
	public static int STRUCTURAL_MEASURE=PROXYGENEA3;
	public static int IC_MEASURE=JIANG_CONRATH; //measure used for IC-weighted comparison (Lin or Jiang-Conrath)
	
	public final static int PRODUCT=0;
	public final static int GEO_MEAN=1;
	public final static int SEM_ONLY=2;
	public final static int NGRAM_ONLY=3;
	public static int COMBINATION_MODE=GEO_MEAN;
	
	public static boolean LIBSVM_OUTPUT=false;
	public static boolean TRAIN_MODE=false;
	
	public static boolean VERBOSE=false;
	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws JSAPException 
	 */
		
	public static void main(String[] args) throws ClassNotFoundException, IOException, JSAPException {

		JSAP jsap = new JSAP();
        
        FlaggedOption opt1 = new FlaggedOption("modelFile")
                                .setStringParser(JSAP.STRING_PARSER)
                                //.setDefault("Z:/tools/stanford-postagger-full-2012-03-09/models/english-bidirectional-distsim.tagger") 
                                .setRequired(true) 
                                .setShortFlag('m') 
                                .setLongFlag("model");

        opt1.setHelp("The location of the Stanford POS tagger model file");
        jsap.registerParameter(opt1);
        
        FlaggedOption opt2 = new FlaggedOption("inputFile")
        .setStringParser(JSAP.STRING_PARSER)
        //.setDefault("Z:/SemEval/train/STS.input.MSRvid.txt") 
        .setRequired(true) 
        .setShortFlag('f') 
        .setLongFlag("file");
        
        FlaggedOption opt8 = new FlaggedOption("NERmodelFile")
        .setStringParser(JSAP.STRING_PARSER)
        .setRequired(true) 
        .setShortFlag('n') 
        .setLongFlag("ner");

        opt8.setHelp("The location of the Stanford NER model file");
        //.setDefault("/tempo/shared/libs/stanford-ner-2012-07-09/classifiers/english.muc.7class.distsim.crf.ser.gz")
        jsap.registerParameter(opt8);

        opt2.setHelp("The SemEval file containing the sentence pairs");
        jsap.registerParameter(opt2);
        
        FlaggedOption opt4 = new FlaggedOption("frequencyFile")
        .setStringParser(JSAP.STRING_PARSER)
        //.setDefault("Z:/SemEval/vocab/vocab") 
        .setRequired(true) 
        .setShortFlag('d') 
        .setLongFlag("dict");

        opt4.setHelp("The Google Web1T Dictionary file");
        jsap.registerParameter(opt4);
        
        FlaggedOption opt5 = new FlaggedOption("WordNet")
        .setStringParser(JSAP.STRING_PARSER)
        //.setDefault("Z:/tools/WN3.0") 
        .setRequired(true) 
        .setShortFlag('w') 
        .setLongFlag("wn");

        opt5.setHelp("The WordNet installation directory");
        jsap.registerParameter(opt5);
        
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
        
        FlaggedOption opt3 = new FlaggedOption("similarityMeasure")
        .setStringParser(JSAP.STRING_PARSER)
        .setDefault("pg3") 
        .setRequired(false) 
        .setShortFlag('s') 
        .setLongFlag("sim");
        
        opt3.setHelp("Similarity measure: one from\n" +
        		"pg1 - ProxyGenea1\n" +
        		"pg2 - ProxyGenea2\n" +
        		"pg3 - ProxyGenea3\n" +
        		"wu - Wu/Palmer\n");
        jsap.registerParameter(opt3);
        
        FlaggedOption opt6 = new FlaggedOption("combinationMethod")
        .setStringParser(JSAP.STRING_PARSER)
        .setDefault("gm") 
        .setRequired(false) 
        .setShortFlag('c') 
        .setLongFlag("comb");
        
        opt6.setHelp("Combination Method: one from\n" +
        		"gm - Geometric Mean\n" +
        		"so - Semantic Only\n" +
        		"no - N-grams Only\n" +
        		"pr - Product" +
        		"svr - no combination: LIBSVM-formatted output for linear regression\n");
        jsap.registerParameter(opt6);
        
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
        
        String modelfile=config.getString("modelFile");// "Z:/tools/stanford-postagger-full-2012-03-09/models/english-bidirectional-distsim.tagger";
		String inputfile=config.getString("inputFile"); //"Z:/SemEval/train/STS.input.MSRvid.txt";
        String NERmodelfile=config.getString("NERmodelFile");
        
        VERBOSE=config.getBoolean("verbose");
        String dictFile=config.getString("frequencyFile");
        String wnRoot=config.getString("WordNet");
        
        String gsFile = config.getString("gsFile");
        if(gsFile != null) TRAIN_MODE=true;
        
	    String measure=config.getString("similarityMeasure");
	    List<String> availableMeasures = Arrays.asList(new String[]{"pg1", "pg2", "pg3", "wu"});
	    HashSet<String> avms = new HashSet<String>(); avms.addAll(availableMeasures);
	    if(!avms.contains(measure)){
	    	System.err.println("The specified measure does not exists, assuming pg3");
	    	measure="pg3";
	    }
	    System.err.println("setting similarity measure to "+measure);
	    if(measure.equals("pg1")) STRUCTURAL_MEASURE=PROXYGENEA1;
	    else if(measure.equals("pg2")) STRUCTURAL_MEASURE=PROXYGENEA2;
	    else if(measure.equals("pg3")) STRUCTURAL_MEASURE=PROXYGENEA3;
	    else if(measure.equals("wu")) STRUCTURAL_MEASURE=WU_PALMER;
	    
	    String combMode=config.getString("combinationMethod");
	    List<String> availableCombModes = Arrays.asList(new String[]{"gm", "pr", "so", "no", "svr"});
	    HashSet<String> avcm = new HashSet<String>(); avcm.addAll(availableCombModes);
	    if(!avcm.contains(combMode)){
	    	System.err.println("The specified combination method does not exists, assuming geometric mean");
	    	combMode="gm";
	    }
	    System.err.println("setting combination method to "+combMode);
	    if(combMode.equals("gm")) COMBINATION_MODE=GEO_MEAN;
	    else if(combMode.equals("pr")) COMBINATION_MODE=PRODUCT;
	    else if(combMode.endsWith("so")) COMBINATION_MODE=SEM_ONLY;
	    else if(combMode.endsWith("no")) COMBINATION_MODE=NGRAM_ONLY;
	    else if(combMode.endsWith("svr")) LIBSVM_OUTPUT=true;
	    
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, sentiment, parse");
	    props.setProperty("ssplit.isOneSentence", "true"); //every string is one sentence
	    props.setProperty("ner.applyNumericClassifiers", "true");
	    props.setProperty("sutime.markTimeRanges", "true");
	    props.setProperty("sutime.includeRange", "true");
	    // Tokenize using Spanish settings
	    //props.setProperty("tokenize.language", "es");
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    //MaxentTagger tagger = new MaxentTagger(modelfile );
	    //AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(NERmodelfile);
	    GoogleTFFactory.init(dictFile);
	    WordNet.init(wnRoot);
	    BlueMarble.init();
	    
	    DBPediaChunkBasedAnnotator chunkannotator = new DBPediaChunkBasedAnnotator("/tempo/indexes/DBPedia_indexed");
	    
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
	    
	    DepComparer.parse(inputfile+".lorg.deps.xml");
	    
	    int i=0;
	    while((line=reader.readLine())!=null){
	    	String [] sentences = line.split("\t");
	    	//fix to set when there are points in sentences that may mislead the tagger
	    	//sentences[0] = sentences[0].replace('.', ' ');
	    	//sentences[1] = sentences[1].replace('.', ' ');
	    	sentences[0].concat(".");
	    	sentences[1].concat(".");
	    	StringReader r0 = new StringReader(sentences[0]);
	    	StringReader r1 = new StringReader(sentences[1]);
	    	/* code for Stanford NLPCore */
	    	Annotation ann0 = new Annotation(sentences[0]);
	    	Annotation ann1 = new Annotation(sentences[1]);
	    	
	    	pipeline.annotate(ann0);
	    	pipeline.annotate(ann1);
	    	
	    	ArrayCoreMap sent0 = (ArrayCoreMap) ann0.get(CoreAnnotations.SentencesAnnotation.class).get(0);
	    	ArrayCoreMap sent1 = (ArrayCoreMap) ann1.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		    
		    double DBPsim=DBPediaComparer.compare(sentences[0], sentences[1], chunkannotator);
		    double NERsim=NERComparer.compare(sent0, sent1);
		    double sim=NGramComparer.compare(sent0, sent1);
		    double conceptsim=ConceptualComparer.compare(sent0, sent1);
		    double wnsim=JWSComparer.compare(sent0, sent1);
		    //double depsim = DepComparer.getSimilarity(i, tSentence, tSentence1);
		    double depsim=0.5;
		    double editsim = LevenshteinDistance.levenshteinSimilarity(sentences[0], sentences[1]);
		    double IRsim = IRComparer.compare(sentences[0], sentences[1]);
		    //double RBOsim = RBOComparer.compare(sentences[0], sentences[1]); //RBO measure for IR comparison
		    double cosinesim = TfIdfComparer.compare(sent0, sent1);
		    double geosim = GeographicScopeComparer.compare(sent0, sent1);
		    //double spectsim = SpectralComparer.compare(tSentence, tSentence1);
		    
		    /*
		    TwitterComparer.init();
		    TwitterComparer.compare(sentences[0], sentences[1], chunkannotator);
		    */
		    
		    //sentence lengths
		    /*double t0 = Math.log(sentences[0].length());
		    double t1 = Math.log(sentences[1].length());
		    */
		    if(VERBOSE) {
		    	System.err.println("Pair # "+(i+1));
		    	System.err.println(sentences[0]);
			    System.err.println(sentences[1]);
			    System.err.println("GS score: "+gsLabels.elementAt(i));
			    System.err.println(":");
			    System.err.println("Geographic Scope similarity: "+5.0 *geosim);
			    System.err.println("CKPD (n-gram) similarity: "+5.0 *sim);
			    System.err.println("Conceptual (WordNet) similarity: "+5.0 *conceptsim);
			    System.err.println("Conceptual (Jiang-Conrath) similarity: "+5.0 *wnsim);
			    System.err.println("Dependency-based (syntactic) similarity: "+5.0 *depsim);
			    System.err.println("Edit distance similarity: "+5.0 *editsim);
			    System.err.println("Cosine distance (tf.idf) similarity: "+5.0 *cosinesim);
			    System.err.println("NER overlap : "+5.0 *NERsim);
			    System.err.println("IR-based similarity : "+5.0 *IRsim);
			    System.err.println("DBPedia similarity : "+5.0 *DBPsim);
			    //System.err.println("Spectral distance (the smaller better): "+spectsim);
			    System.err.println("--------------");
			    
		    } else {
		    	if(!LIBSVM_OUTPUT){
		    		
		    		double res;
			    	if(COMBINATION_MODE==GEO_MEAN) res = 5.0 * Math.sqrt(conceptsim*sim);
			    	else if(COMBINATION_MODE==SEM_ONLY) res = 5.0 * conceptsim;
			    	else if(COMBINATION_MODE==NGRAM_ONLY) res = 5.0 * sim;
			    	else res = 5.0*conceptsim*sim;
			    	double conf_score=100*(1.0-Math.abs(conceptsim-sim)); //TODO: confidence score? this is based on the difference between the separate scores
			    	System.out.println(res+"\t"+conf_score);
			    	
		    	} else {
		    		if(TRAIN_MODE){
		    			System.out.print(gsLabels.elementAt(i)+" ");
		    		} else {
		    			System.out.print("0.0 ");
		    		}
		    		//System.out.println("1:"+sim+" 2:"+conceptsim+" 3:"+depsim+" 4:"+editsim+" 5:"+cosinesim+" 6:"+NERsim+" 7:"+wnsim+" 8:"+IRsim);
		    		//System.out.println("1:"+sim+" 2:"+conceptsim+" 3:"+depsim+" 4:"+editsim+" 5:"+cosinesim+" 6:"+NERsim+" 7:"+wnsim+" 8:"+IRsim+" 9:"+DBPsim+" 10:"+geosim+" 11:"+spectsim);
		    		System.out.println("1:"+sim+" 2:"+conceptsim+" 3:"+depsim+" 4:"+editsim+" 5:"+cosinesim+" 6:"+NERsim+" 7:"+wnsim+" 8:"+IRsim+" 9:"+DBPsim+" 10:"+geosim);
		    	}
		    	
		    }
		    
		    i++;
	    }
	    reader.close();
	    
		
	}

}
