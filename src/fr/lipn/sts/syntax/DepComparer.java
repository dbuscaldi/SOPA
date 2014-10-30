package fr.lipn.sts.syntax;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.syntax.joseph.XMLDepFileHandler;

public class DepComparer {
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
	
	private static Vector<Dependency> getDeps(Tree t) {
		Vector<Dependency> res = new Vector<Dependency>();
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(t);
		Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
		//System.out.println(td);

		Object[] list = td.toArray();
		System.out.println(list.length);
		TypedDependency typedDependency;
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			System.out.println("Dependency Name"+typedDependency.dep().nodeString()+ " :: "+ "Node"+typedDependency.reln());
			Dependency d = new Dependency(typedDependency.reln().getShortName(), typedDependency.gov().nodeString(), typedDependency.dep().nodeString());
			res.add(d);  
		}
		
		return res;
	}
	
	public static double compare(ArrayCoreMap sent0, ArrayCoreMap sent1) {
		Tree tree0 = sent0.get(TreeCoreAnnotations.TreeAnnotation.class);		
		Vector<Dependency> d0 = getDeps(tree0);
		Tree tree1 = sent1.get(TreeCoreAnnotations.TreeAnnotation.class);
		Vector<Dependency> d1 = getDeps(tree1);
		
		DepPair deps = new DepPair(d0, d1);
		
		return deps.getDepScore();
	}

}
