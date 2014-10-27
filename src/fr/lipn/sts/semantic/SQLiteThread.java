package fr.lipn.sts.semantic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class SQLiteThread extends Thread {
	private String word;
	private boolean tail=true;
	private HashMap<String, Integer> rmap;
	private Connection c;
	
	public SQLiteThread(String word, boolean tail, Connection c){
		this.word=word;
		this.tail=tail;
		this.c=c;
		rmap=new HashMap<String, Integer>();
	}
	
	public void run() {
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
	}
	
	public HashMap<String, Integer> getRS(){
		return this.rmap;
	}
}
