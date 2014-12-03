package fr.lipn.sts.syntax;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.french.FrenchTreebankLanguagePack;
import edu.stanford.nlp.trees.international.spanish.SpanishTreebankLanguagePack;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.measures.SimilarityMeasure;
import fr.lipn.sts.syntax.joseph.XMLDepFileHandler;
import fr.lipn.sts.syntax.Dependency;

public class DepBasedSimilarity implements SimilarityMeasure {
	private static Vector<DepPair> parsedContent;
	
	public static void parse(String xmlFile) {
		try {
			XMLDepFileHandler hldr = new XMLDepFileHandler(new File(xmlFile));
			parsedContent=hldr.getParsedDependencies();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	/**
	 * Version that uses Joseph files
	 * @param order
	 * @param tSentence
	 * @param tSentence1
	 * @return
	 */
	public static double getSimilarity(int order, ArrayList<TaggedWord> tSentence, ArrayList<TaggedWord> tSentence1){
		DepPair deps = parsedContent.elementAt(order);
		deps.setPOStags(tSentence, tSentence1);
		deps.setAlignments();
		return deps.getDepScore();
	}
	
	private static Vector<Dependency> getDeps(Tree t, ArrayCoreMap sent) {
		Vector<Dependency> res = new Vector<Dependency>();
		
		if(SOPAConfiguration.LANG.equals("en")) {
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(t);
			Collection<TypedDependency> td = gs.typedDependenciesCollapsed();

			Object[] list = td.toArray();
			//System.out.println(list.length);
			TypedDependency typedDependency;
			for (Object object : list) {
				typedDependency = (TypedDependency) object;
				Dependency d = new Dependency(typedDependency.reln().getShortName(), typedDependency.gov(), typedDependency.dep());
				res.add(d);  
			}
		} else {
			HashMap<String, String> wdict = new HashMap<String, String>();
			for (CoreLabel word : sent.get(CoreAnnotations.TokensAnnotation.class)) {
				String text=word.word();
				String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				wdict.put(text, pos);
			}

			TreebankLanguagePack tlp;
			if(SOPAConfiguration.LANG.startsWith("es")) tlp= new SpanishTreebankLanguagePack();
			else if(SOPAConfiguration.LANG.startsWith("fr")) tlp= new FrenchTreebankLanguagePack();
			else tlp = null; //NOTE: missing Italian treebank Language Pack
			
			t.percolateHeads(tlp.typedDependencyHeadFinder());
			
			Set<edu.stanford.nlp.trees.Dependency<Label, Label, Object>> deps = t.dependencies();
			for(edu.stanford.nlp.trees.Dependency<Label, Label, Object> d : deps) {
				String relname=null;
				if(d.name()!= null) relname=((String)d.name());
				CoreLabel gl = (CoreLabel)d.governor();
				int gpos = gl.index();
				String gov = ((CoreLabel) d.governor()).word();
				String dep= ((CoreLabel) d.dependent()).word();
				CoreLabel dl = (CoreLabel)d.governor();
				int dpos = dl.index();
				
				Dependency dependency;
				if(relname!=null) { 
					dependency = new Dependency(relname, gov+"-"+gpos, dep+"-"+dpos);
				} else { //untyped dependency
					dependency = new Dependency(gov+"-"+gpos, dep+"-"+dpos);
				}
				dependency.head.setPOS(wdict.get(dependency.head.getWord()));
				dependency.dependent.setPOS(wdict.get(dependency.dependent.getWord()));
				
				res.add(dependency);
			}
			
		}
		
		return res;
	}
	
	public static double compare(ArrayCoreMap sent0, ArrayCoreMap sent1) {
		Tree tree0 = sent0.get(TreeCoreAnnotations.TreeAnnotation.class);		
		Vector<Dependency> d0 = getDeps(tree0, sent0);
		Tree tree1 = sent1.get(TreeCoreAnnotations.TreeAnnotation.class);
		Vector<Dependency> d1 = getDeps(tree1, sent1);
		//for(Dependency d : d0) System.err.println(d);
		DepPair deps = new DepPair(d0, d1);
		deps.setAlignments();
		
		return deps.getDepScore();
	}
	@Override
	public double compare(Object o1, Object o2) {
		return compare((ArrayCoreMap)o1, (ArrayCoreMap)o2);
	}

}
