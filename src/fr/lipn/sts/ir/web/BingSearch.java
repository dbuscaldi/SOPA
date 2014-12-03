package fr.lipn.sts.ir.web;
/* code adapted from
https://github.com/mark-watson/bing_search_java
*/

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class BingSearch {
  private static String [] accountKeys={"3aQwvEbZKKqgl43o0mnKz/Th4KulYdREMs8nKUzK6eU", "cTOH+E3eYNOZdM8sm2shmKEuB1RcCyApUR2jbsqmMRA"};
  private static int calls=0;
  
  //https://api.datamarket.azure.com/Bing/Search/Web?Query=%27Xbox%27&Options=%27DisableLocationDetection%2BEnableHighlighting%27
  public static String search(String query) throws Exception {
    String bingUrl = "https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web?Query=%27" + java.net.URLEncoder.encode(query) + "%27&$format=JSON";
    
    String accountKey=accountKeys[0];
    
    byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
    //byte[] accountKeyBytes = Base64.encodeBase64((accountKey).getBytes());
    String accountKeyEnc = new String(accountKeyBytes);

    URL url = new URL(bingUrl);
    URLConnection urlConnection = url.openConnection();
    String s1 = "Basic " + accountKeyEnc;
    urlConnection.setRequestProperty("Authorization", s1);
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    
    calls++;
    
    String inputLine;
    
    StringBuffer sb = new StringBuffer();
    while ((inputLine = in.readLine()) != null)
      sb.append(inputLine);
    in.close();

    return sb.toString();
  }
}
