package fr.lipn.sts.ir.web;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class WebIRComparer {
	
	public static double compare(String req1, String req2){
		try {
			String rList1=BingSearch.search(req1);
			//String rList2=BingSearch.search(req2);
			System.err.println(rList1);
			
			Object obj=JSONValue.parse(rList1);
			JSONArray array=(JSONArray)obj;
			System.err.println(array.get(1));
			/*
			Object obj2=JSONValue.parse(rList2);
			JSONArray array2=(JSONArray)obj;
			*/
			return 1d;
		} catch(Exception e) {
			e.printStackTrace();
			return 0d;
		}
	}
}
