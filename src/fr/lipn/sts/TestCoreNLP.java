package fr.lipn.sts;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.ckpd.NGramComparer;
import fr.lipn.sts.ckpd.SkipGramComparer;
import fr.lipn.sts.ir.web.WebIRComparer;
import fr.lipn.sts.ner.NERComparer;
import fr.lipn.sts.tools.GoogleTFFactory;

public class TestCoreNLP {

	public static void main(String[] args) {
		SemanticComparer.VERBOSE=true;
		
		GoogleTFFactory.init("/tempo/corpora/GoogleW1T/vocab/vocab");
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment, truecase");
	    props.setProperty("ssplit.isOneSentence", "true"); //every string is one sentence
	    props.setProperty("ner.applyNumericClassifiers", "true");
	    props.setProperty("sutime.markTimeRanges", "true");
	    props.setProperty("sutime.includeRange", "true");
	    // Tokenize using Spanish settings
	    //props.setProperty("tokenize.language", "es");
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    String text1= "China stock index futures close higher -- Dec. 4";
	    String text2= "China stock index futures close lower -- Nov. 24";
	    //Annotation ann0 = new Annotation("Murder claim over Diana's death New information which has been passed to the police relating to the deaths of Princess Diana and Dodi Fayed is thought to include an allegation that they were murdered.");
    	//Annotation ann1 = new Annotation("Diana and Fayed death info received new The deaths of Princess Diana and Dodi Al Fayed are being looked at again by police after they received information.");
    	
	    Annotation ann0 = new Annotation(text1);
	    Annotation ann1 = new Annotation(text2);
    	
    	pipeline.annotate(ann0);
    	pipeline.annotate(ann1);
    	
    	ArrayCoreMap sent0 = (ArrayCoreMap) ann0.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    	ArrayCoreMap sent1 = (ArrayCoreMap) ann1.get(CoreAnnotations.SentencesAnnotation.class).get(0);
    	
    	double NERsim=NERComparer.compare(sent0, sent1);
    	double sim=NGramComparer.compare(sent0, sent1);
    	double sksim=SkipGramComparer.compare(sent0, sent1);
    	double websim = WebIRComparer.compare(text1, text2);
    	
    	System.err.println("NER sim: "+NERsim);
    	System.err.println("CKPD sim: "+sim);
    	System.err.println("Skipgram sim: "+sksim);
    	System.err.println("Websim: "+websim);
	}

}
