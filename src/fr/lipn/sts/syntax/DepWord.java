package fr.lipn.sts.syntax;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.TreeGraphNode;

public class DepWord {
	private String word;
	private int position;
	private String POS;
	
	/**
	 * Joseph-based DepWord constructor
	 * @param seq
	 */
	public DepWord(String seq){
		
		String [] items = seq.split("-");
		
		try{
			this.word=items[0];
			this.position=Integer.parseInt(items[1]);
		} catch (Exception e) {
			//format error: word is multi-word
			int lastitem=items.length-1;
			String tmp_li=(items[lastitem].replace('\'', ' ')).trim(); //to avoid the #number' format
			this.position=Integer.parseInt(tmp_li);
			StringBuffer tmpWord = new StringBuffer();
			for(int i=0; i<lastitem; i++){
				tmpWord.append(items[i]);
				if(i<lastitem-1) tmpWord.append("-");
			}
			this.word=tmpWord.toString();
		}
		if(this.word.equals("ROOT")) POS="NN";
		else POS=null;
		
		//if(this.word.equals("") || this.word==null) System.err.println("error-seq: "+seq);
		
	}
	
	public DepWord(TreeGraphNode n) {
		this.word=n.nodeString();
		this.position=0;
		CoreLabel label=n.label();
		this.POS=label.tag();
		
		if(this.word.equals("ROOT")) this.POS="NN";
	}
	
	public boolean isRoot(){
		return (position==0) && word.equals("ROOT");
	}
	
	public String getWord(){
		return word;
	}
	
	public int getPosition(){
		return position;
	}
	
	public String getPOS(){
		return this.POS;
	}
	public void setPOS(String pos){
		this.POS=pos;
	}
	
	public String toString(){
		return this.word+"-"+this.POS;
	}
}
