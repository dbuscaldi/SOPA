package fr.lipn.sts.semantic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.util.ArrayCoreMap;
import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.SemanticComparer;

public class SpectralComparer {
	
	private static Connection c=null;
	
	public static int getFrequency(String word) {
		try{
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT hits FROM unigrams WHERE id='"+word+"';");
			while ( rs.next() ) {
		         int off = rs.getInt(1);
		         return off;
		    }
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return 0;
	}
	
	public static HashMap<String, Integer> getBigrams(String word, boolean tail) {
		HashMap<String, Integer> rmap = new HashMap<String, Integer>();
		try{
			Statement stmt = c.createStatement();
			String text;
			
			if(tail) text= "SELECT id2, hits FROM bigrams WHERE id1='"+word+"';";
			else text="SELECT id1, hits FROM bigrams WHERE id2='"+word+"';";
			
			ResultSet rs = stmt.executeQuery(text);
			while ( rs.next() ) {
		         String w = rs.getString(1);
		         Integer cnt = new Integer(rs.getInt(2));
		         rmap.put(w, cnt);

		    }
			rs.close();
			stmt.close();
		
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return rmap;
	}
	
	private static double getDistance(HashMap<String, Integer> m1, HashMap<String, Integer> m2, double freq1, double freq2) {
		HashSet<String> shtails = new HashSet<String>();
		shtails.addAll(m1.keySet());
		shtails.retainAll(m2.keySet());
		double bc=0d;
		for(String s : shtails) {
			bc+=Math.sqrt((m1.get(s).doubleValue()/freq1) * (m2.get(s).doubleValue()/freq2)); //Math.sqrt(a*b) is Bhattacharyya coefficient
		}
		return -Math.log(bc);
	}
	
	public static double getSimilarity(String w1, String w2){
		double sim=0d;
		
		double freq1=(double)getFrequency(w1);
		double freq2=(double)getFrequency(w2);
		
		//thread version
		SQLiteThread t1=new SQLiteThread(w1, true, c);
		SQLiteThread t2=new SQLiteThread(w2, true, c);
		SQLiteThread t3=new SQLiteThread(w1, false, c);
		SQLiteThread t4=new SQLiteThread(w2, false, c);
		
		t1.run(); t2.run(); t3.run(); t4.run();
		while(t1.isAlive() && t2.isAlive() && t3.isAlive() && t4.isAlive()) {
			//wait for synchronization
		}
		HashMap<String, Integer> m1_tails=t1.getRS();
		HashMap<String, Integer> m2_tails=t2.getRS();
		
		sim+=getDistance(m1_tails, m2_tails, freq1, freq2);
		
		HashMap<String, Integer> m1_heads=t3.getRS();
		HashMap<String, Integer> m2_heads=t4.getRS();
		
		sim+=getDistance(m1_heads, m2_heads, freq1, freq2);
		
		/*
		//standard version
		HashMap<String, Integer> m1_tails=getBigrams(w1, true);
		HashMap<String, Integer> m2_tails=getBigrams(w2, true);
		
		sim+=getDistance(m1_tails, m2_tails, freq1, freq2);
		
		HashMap<String, Integer> m1_heads=getBigrams(w1, false);
		HashMap<String, Integer> m2_heads=getBigrams(w2, false);
		
		sim+=getDistance(m1_heads, m2_heads, freq1, freq2);
		*/
		return (sim/2);
	}
	/**
	 * Note: this returns a distance, not a similarity! The smaller, the more similar are the two sentences
	 * @param tSentence
	 * @param tSentence1
	 * @return
	 */
	public static double compare(ArrayCoreMap tSentence, ArrayCoreMap tSentence1){
		double res = 0d;
		try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:"+SOPAConfiguration.NGRAMS_DB);
		      c.setAutoCommit(false);
		      
		      
		} catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(-1);
		}  
	    
		double sumDist=0;
		int ncomp=0;
		//Compare only on words that are not shared between the two sentences
		HashSet<String> sharedWords = new HashSet<String>();
		HashSet<String> sW1 = new HashSet<String>();
		for (CoreLabel word : tSentence.get(CoreAnnotations.TokensAnnotation.class)) {
			sharedWords.add(word.word().toLowerCase());
		}
		for (CoreLabel word : tSentence1.get(CoreAnnotations.TokensAnnotation.class)) {
			sW1.add(word.word().toLowerCase());
		}
		sharedWords.retainAll(sW1);
		
		
		for (CoreLabel tw : tSentence.get(CoreAnnotations.TokensAnnotation.class)) {
			String w=tw.word().toLowerCase();
			char pos=tw.get(CoreAnnotations.PartOfSpeechAnnotation.class).toLowerCase().charAt(0);
			if(!sharedWords.contains(w)) {
				double minDist=10; //NOTE: is 10 enough? -- think about the penalization for not aligned words
				String bestMatch="None.";
				if(pos=='n' || pos=='v' || pos=='r' || pos=='j') {
					for (CoreLabel tw1 : tSentence.get(CoreAnnotations.TokensAnnotation.class)) {
						String w1=tw1.word().toLowerCase();
						if(!sharedWords.contains(w1)) {
							char pos1=tw1.get(CoreAnnotations.PartOfSpeechAnnotation.class).toLowerCase().charAt(0);
							if(!w.equals(w1) && pos==pos1){
								double d = getSimilarity(tw.word(), tw1.word());
								//if(SemanticComparer.VERBOSE) System.err.println("[Spectral] Similarity between "+tw.word()+" and "+tw1.word()+" : "+d);
								if(d<minDist) {
									minDist=d;
									bestMatch=w1;
								}
							} 
							
						}
					}
					if(SemanticComparer.VERBOSE) System.err.println("[Spectral] Best alignment for "+tw.word()+" : "+bestMatch+" ,score: "+minDist);
					sumDist+=minDist;
					ncomp++;
				}
			}
		}
		res=sumDist/((double)ncomp+(double)sharedWords.size()+1);
		
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	   return res;

	    
	    
	}
}
