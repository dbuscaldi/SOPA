package fr.lipn.sts.align.sultan;

import java.io.File;

import fr.lipn.sts.SOPAConfiguration;
import fr.lipn.sts.measures.SimilarityMeasure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class SultanSimilarity implements SimilarityMeasure {

	
	public static double compare(String req1, String req2) {
		try{
		   String sultanUrl = "http://127.0.0.1:5000/en/compare/" + java.net.URLEncoder.encode(req1) + "/"+java.net.URLEncoder.encode(req2);
		   URL url = new URL(sultanUrl);
		   URLConnection urlConnection = url.openConnection();
		   BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		   String inputLine;

								   
		   StringBuffer sb = new StringBuffer();
		   while ((inputLine = in.readLine()) != null)
				 sb.append(inputLine);
		   in.close();

		   double sim = Double.parseDouble(sb.toString());
	   	   return sim;
		} catch (Exception e){
			return 0.0;	
		}
	}

	@Override
	public double compare(Object o1, Object o2) {
		return compare((String)o1, (String)o2);
	}
	

}
