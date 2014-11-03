package fr.lipn.sts.ir.web;

import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.lipn.sts.measures.SimilarityMeasure;

public class WebIRSimilarity implements SimilarityMeasure {
	
	private static Vector<WebSearchResult> getResultList(String req) {
		Vector<WebSearchResult> resvec = new Vector<WebSearchResult>();
		try {
			String tList=BingSearch.search(req);
			//System.err.println(tList);
			
			JSONObject results=((JSONObject)((JSONObject) JSONValue.parse(tList)).get("d"));
			
			System.err.println(results.get("results"));
			
			JSONArray rlist = (JSONArray) results.get("results");
			
			for(int i=0; i< rlist.size(); i++) {
				JSONObject item = (JSONObject) rlist.get(i);
				//String id = item.get("ID").toString();
				String title = item.get("Title").toString();
				String snippet = item.get("Description").toString();
				String url = item.get("Url").toString();
				
				WebSearchResult r = new WebSearchResult(title, snippet, url);
				resvec.add(r);
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return resvec;
	}
	
	public static double compare(String req1, String req2){
		Vector<WebSearchResult> rList1=getResultList(req1);
		Vector<WebSearchResult> rList2=getResultList(req2);
		
		double score=0d;
		
		//using Reciprocal Rank as score?
		//another idea: found how distant (semantically) is a result from the previous one, this should be included into the score computation
		int i=1;
		for(WebSearchResult r : rList1) {
			//System.err.println("result #"+i+": "+r);
			int j=1;
			for(WebSearchResult r2 : rList2) {
				//System.err.println("result #"+i+": "+r);
				if(r.equals(r2)) {
					score=score+Math.sqrt(Math.pow(((1d/(double)i)-(1d/(double)j)), 2));
					System.err.println("Match:\n#"+i+" : "+r+"\n#"+j+" : "+r2+"\n");
				}
				j++;
			}
			i++;
		}
			
		return score;
	}

	@Override
	public double compare(Object o1, Object o2) {
		return compare((String) o1, (String) o2);
	}
}
