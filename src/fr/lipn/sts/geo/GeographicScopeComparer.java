package fr.lipn.sts.geo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import fr.lipn.sts.SemanticComparer;
import fr.lipn.sts.tools.WordNet;

public class GeographicScopeComparer {
	private static double NORM_DIST=10000; //assume 10000Km as the maximal distance we can found;  we did this to align with GS scores
	
	public static HashSet<String> getLocsforNE(String ne){
		Connection c = null;
	    Statement stmt = null;
	    HashSet<String> locs = new HashSet<String>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:res/yagoLeaders.db");
	      c.setAutoCommit(false);
	      
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM leaders WHERE fullname=\""+ne+"\" OR lastname =\""+ne+"\" ;" );
	      while ( rs.next() ) {
	         String  off = rs.getString("offset");
	         locs.add("n"+off);
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(-1);
	    }
	    return locs;
	}
	
	public static double compare(ArrayList<TaggedWord> tSentence, ArrayList<TaggedWord> tSentence1, List<List<CoreLabel>> cSentence, List<List<CoreLabel>> cSentence1) {
		HashSet<String> off1= new HashSet<String>();
		HashSet<String> off2= new HashSet<String>();
		
		HashSet<ISynsetID> s1_syns = new HashSet<ISynsetID>();
		for(TaggedWord tw : tSentence){
			String text=tw.word();
			String pos =tw.tag();
			s1_syns.addAll(WordNet.getSynsets(text, pos));
		}
		HashSet<ISynsetID> s2_syns = new HashSet<ISynsetID>();
		for(TaggedWord tw : tSentence1){
			String text=tw.word();
			String pos =tw.tag();
			s2_syns.addAll(WordNet.getSynsets(text, pos));
		}
		
		
		for(ISynsetID syn : s1_syns){
			int off=syn.getOffset();
			POS p=syn.getPOS();
			String pos="n";
			if(p.equals(POS.VERB)) pos="v";
			if(p.equals(POS.ADJECTIVE)) pos="a";
			if(p.equals(POS.ADVERB)) pos="r";
			String synID=pos+String.format("%08d", off);
			if(BlueMarble.hasLocs(synID)) off1.add(synID);
		}
		
		
		for(ISynsetID syn : s2_syns){
			int off=syn.getOffset();
			POS p=syn.getPOS();
			String pos="n";
			if(p.equals(POS.VERB)) pos="v";
			if(p.equals(POS.ADJECTIVE)) pos="a";
			if(p.equals(POS.ADVERB)) pos="r";
			String synID=pos+String.format("%08d", off);
			if(BlueMarble.hasLocs(synID)) off2.add(synID);
		}
		/** now deal with Named Entities */
		for (List<CoreLabel> lcl : cSentence) {
			boolean in=false;
			StringBuffer buf = new StringBuffer();
			for (CoreLabel word : lcl) {
				String tag = word.getString(AnswerAnnotation.class);
				if(tag.equals("PERSON")){
					buf.append(word.word());
					buf.append(" ");
					in=true;
				} else {
					if(in==true) {
						off1.addAll(getLocsforNE(buf.toString().trim()));
						if(SemanticComparer.VERBOSE) System.err.println("got geoinfo for leader "+buf);
						buf.delete(0, buf.length());
					}
					in=false;
				}
	     	}
        }
		
		for (List<CoreLabel> lcl : cSentence1) {
			boolean in=false;
			StringBuffer buf = new StringBuffer();
			for (CoreLabel word : lcl) {
				String tag = word.getString(AnswerAnnotation.class);
				if(tag.equals("PERSON")){
					buf.append(word.word());
					buf.append(" ");
					in=true;
				} else {
					if(in==true) {
						off2.addAll(getLocsforNE(buf.toString().trim()));
						if(SemanticComparer.VERBOSE) System.err.println("got geoinfo for leader "+buf);
						buf.delete(0, buf.length());
					}
					in=false;
				}
	     	}
        }
		
		/*** now compute weights **/
		
		if(off1.size()==0 && off2.size()==0) {
			//no places available for both sentences: probably they are geographically not constrained
			//they are compatible from a geographical point of view
			return 1d;
		}
		
		if((off1.size() * off2.size()) ==0) {
			//one sentences contain places but the other don't
			//they are not compatible from a geographical point of view
			return 0d;
		}
		
		//in all other cases, try to establish a geographic distance
		double sumMinimalDist=0d;
		int numPairs=0;
		for(String id : off1) {
			double minDist=NORM_DIST;
			for(String id2 : off2) {
				double d =BlueMarble.getMinDistance(id, id2);
				//if(SemanticComparer.VERBOSE) System.err.println("minDistance between: "+id+" and "+id2+" : "+d);
				if (d < minDist) minDist=d;
				numPairs++;
			}
			//if(SemanticComparer.VERBOSE) System.err.println("minDist for: "+id+" : "+minDist);
			sumMinimalDist+=minDist;
		}
		
		double avgSumDist=sumMinimalDist/(double)numPairs;
		
		//System.err.println("sum of minimal distances: "+sumMinimalDist);
		
		double geoSim=1-(Math.log(1+avgSumDist)/Math.log(NORM_DIST));
		
		return geoSim;
	}
}
