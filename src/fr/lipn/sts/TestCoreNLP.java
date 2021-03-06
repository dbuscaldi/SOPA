package fr.lipn.sts;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.ArrayCoreMap;
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
import fr.lipn.sts.ir.web.WebIRSimilarity;
import fr.lipn.sts.ner.DBPediaChunkBasedAnnotator;
import fr.lipn.sts.ner.DBPediaSimilarity;
import fr.lipn.sts.ner.NERSimilarity;
import fr.lipn.sts.semantic.JWSSimilarity;
import fr.lipn.sts.semantic.proxygenea.ConceptualSimilarity;
import fr.lipn.sts.syntax.DepBasedSimilarity;
import fr.lipn.sts.tools.GoogleTFFactory;
import fr.lipn.sts.tools.WordNet;

public class TestCoreNLP {

	public static void main(String[] args) {
		SOPAConfiguration.VERBOSE=true;
		SOPAConfiguration.load("res/config.xml");
		GoogleTFFactory.init(SOPAConfiguration.GoogleTF);
		WordNet.init();
		BlueMarble.init();
		
		Properties props = new Properties();
	    //props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment, truecase");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    props.setProperty("ssplit.isOneSentence", "true"); //every string is one sentence
	    props.setProperty("ner.applyNumericClassifiers", "true");
	    props.setProperty("sutime.markTimeRanges", "true");
	    props.setProperty("sutime.includeRange", "true");
	    
	    if(SOPAConfiguration.LANG.equals("es")) {
	    	props.setProperty("tokenize.language", "es");
	    	props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
	    	props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
	    	props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
	    	
	    }
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    //String text1= "China stock index futures close higher -- Dec. 4";
	    //String text2= "China stock index futures close lower -- Nov. 24";
	    
	    //String text1= "El volcamiento de un bus de turismo, ocurrido en el kilómetro 8 de la vía Bogotá-Choachí, dejó un saldo de 38 personas lesionadas, de las cuales 30 tuvieron que ser remitidas a hospitales.";
	    //String text2= "De las personas que iban en el bus y fueron valoradas en el sitio del accidente, 8 no ameritaron traslado a centro asistencial; el resto de los heridos fueron llevados a hospitales.";
	    
	    String text1="alto huallaga is located northeast of the capital lima.";
	    String text2="puerto cabezas is located 557 km northeast of managua, nicaragua.";
	    
	    //String text1="A bull standing in the field.";
	    //String text2="A brown horse stands in a lush green field.";
	    
	    DBPediaChunkBasedAnnotator chunkannotator = new DBPediaChunkBasedAnnotator(SOPAConfiguration.DBPedia_INDEX);
    	DBPediaSimilarity.setAnnotator(chunkannotator);
    	
	    Annotation ann0 = new Annotation(text1);
	    Annotation ann1 = new Annotation(text2);
    	
    	pipeline.annotate(ann0);
    	pipeline.annotate(ann1);
    	
    	ArrayCoreMap sent0 = (ArrayCoreMap) ann0.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    	ArrayCoreMap sent1 = (ArrayCoreMap) ann1.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    	
    	IRSimilarity irs = new IRSimilarity();
    	
    	double NERsim=NERSimilarity.compare(sent0, sent1);
    	double sim=NGramSimilarity.compare(sent0, sent1);
    	double sksim=SkipGramSimilarity.compare(sent0, sent1);
    	//double websim = WebIRSimilarity.compare(text1, text2);
    	double conceptsim=ConceptualSimilarity.compare(sent0, sent1);
	    double wnsim=JWSSimilarity.compare(sent0, sent1);
	    double depsim = DepBasedSimilarity.compare(sent0, sent1);
	    double editsim = Levenshtein.characterBasedSimilarity(text1, text2);
	    //double editsim1 = Levenshtein.wordBasedSimilarity(text1, text2);
	    double IRsim = irs.compare(text1, text2);
	    double sphynxsim = SphynxSimilarity.compare(sent0,sent1);
	    double sultansim = SultanSimilarity.compare(text1,text2);
	    
	    double RBOsim = RBOSimilarity.compare(text1, text2); //RBO measure for IR comparison
	    double cosinesim = TfIdfSimilarity.compare(sent0, sent1);
	    double geosim = GeographicScopeSimilarity.compare(sent0, sent1);
	    double lsim = SentenceLengthSimilarity.compare(text1, text2);
	    //double spectsim = SpectralSimilarity.compare(sent0, sent1);
	    double DBPsim=DBPediaSimilarity.compare(text1, text2);
	    
    	System.err.println("NER sim: "+NERsim);
    	System.err.println("CKPD sim: "+sim);
    	System.err.println("Skipgram sim: "+sksim);
    	//System.err.println("Websim: "+websim);
    	System.err.println("Conceptual Similarity: "+conceptsim);
    	System.err.println("WN sim (Mihalcea): "+wnsim);
    	System.err.println("Syntactic Dependences similarity: "+depsim);
    	System.err.println("Edit distance similarity(chars): "+editsim);
    	//System.err.println("Edit distance similarity(words): "+editsim1);
    	System.err.println("IR similarity: "+IRsim);
    	System.err.println("Sphynx similarity: "+sphynxsim);
    	System.err.println("sultan similarity: "+sultansim);
    	System.err.println("Geo similarity: "+geosim);
    	System.err.println("Cosine similarity: "+cosinesim);
    	System.err.println("RBO similarity: "+RBOsim);
    	System.err.println("Length difference: "+lsim);
    	System.err.println("DBPedia similarity: "+DBPsim);
	}

}
