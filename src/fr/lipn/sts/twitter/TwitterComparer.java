package fr.lipn.sts.twitter;

import java.util.HashMap;

import fr.lipn.sts.ner.DBPediaChunkBasedAnnotator;
import fr.lipn.sts.tools.GoogleTFFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterComparer {
	static boolean COSINE_SIM=true;
	static TwitterFactory tf;
	
	public static void init() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("0V84QQCRi47KizqUNd2Bdw")
		  .setOAuthConsumerSecret("kl7qJVjFOvLDsY8Wt81dZYYyaBJnt9neZz3yecZW4")
		  .setOAuthAccessToken("81116743-S0MtZtOnkvnpvTJjuhAGBA7r43XEooqwuLYRfS8wo")
		  .setOAuthAccessTokenSecret("mItCCboYVkSVgCiyhkMCdnp6ErXFmivTsCnOcLNpQNoad");
		tf = new TwitterFactory(cb.build());
	}
	
	private static double getAvgIDF(String str){
		String [] items = str.split("_");
		double w=0d;
		for(String t: items) {
			double idf = GoogleTFFactory.getIDF(t);
			w+=idf;
		}
		
		System.err.println("topic: "+str+" idf:"+w/(double)items.length);
		return w/(double)items.length;
	}
	
	public static void compare(String t1, String t2, DBPediaChunkBasedAnnotator annotator) {
		Twitter twitter = tf.getInstance();
		
		HashMap<String, Float> h1 = new HashMap<String, Float>();
		HashMap<String, Float> h2 = new HashMap<String, Float>();
		
		h1=annotator.annotateTop(t1);
		h2=annotator.annotateTop(t2);
		double thr = 3.30d;
		for(String id : h1.keySet()) {
			String topic=id.substring(29, id.length()-1);
			double idf = getAvgIDF(topic);
			
			if(idf < thr) continue;
			topic=topic.replace('_', ' ');
			
			Query query = new Query(topic);
		    QueryResult result;
			try {
				result = twitter.search(query);
				for (Status status : result.getTweets()) {
			        System.err.println("TWEET on topic "+topic+" : @" + status.getUser().getScreenName() + ":" + status.getText());
			    }
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(String id : h2.keySet()) {
			String topic=id.substring(29, id.length()-1);
			double idf = getAvgIDF(topic);
			
			if(idf < thr) continue;
			topic=topic.replace('_', ' ');
			Query query = new Query(topic);
		    QueryResult result;
			try {
				result = twitter.search(query);
				for (Status status : result.getTweets()) {
			        System.err.println("TWEET on topic "+topic+" : @" + status.getUser().getScreenName() + ":" + status.getText());
			    }
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    
	}

}
