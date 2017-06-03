package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;


/**
 * Generate the result in SemEval format
 * @author Kai-Wei Chang
 */
public class CoNLLScorer {	
	BufferedWriter m_keyWriter, m_predWriter;
	String m_keyFileName=null, m_predFileName=null;
	boolean trueBoundary = false;
	private boolean removeSingleton = false;
    /** Default Constructor. */
    public CoNLLScorer() {
    	this("pred");
    }
    public CoNLLScorer(String predFileName){
    	m_predFileName = predFileName;
    }
    public CoNLLScorer(String predFileName, String keyFileName){
    	m_predFileName = predFileName;
    	m_keyFileName = keyFileName;
    }
    
    /**
     * This function generate human readable corefernce annotation.
     * @param keySol: corefernece annotation in the form of ChainSolution
     * @param d: document
     * @param outWriter: File handler
     */
    public void generateCorefText(ChainSolution<Mention> keySol, Doc d, BufferedWriter outWriter) {
    	Map<Integer, String> startTag =  new HashMap<Integer, String>();
    	Map<Integer, String> endTag =  new HashMap<Integer, String>();
    	List<String> words = d.getWords();
	    List<Mention> ments = new ArrayList<Mention>(keySol.getAllMembers());
	    Map<String, Integer> EntityIdMap = new HashMap<String, Integer>();
	    int MaxEntity = 0;
	    Collections.sort(ments);
	    
	    // generate coreference tag
	    for (Mention mi : ments) {
	    	Set<Mention> backChain = keySol.getContainerFor(mi);	    	
	    	if(backChain.size() ==0)
	    		continue;
	    	if(!EntityIdMap.containsKey(mi.getEntityID())){
	    		EntityIdMap.put(mi.getEntityID(), MaxEntity);
	    		MaxEntity++;
	    	}
	    	int entityID = EntityIdMap.get(mi.getEntityID());
	    	if(mi.getExtent().getStartWN() == mi.getExtent().getEndWN()) {
	    		String tag = new String("[_"+entityID);	    		
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(startTag.containsKey(wordNum))
	    			tag = tag.concat(startTag.get(wordNum));
	    		startTag.put(wordNum, tag);
	    		
	    		tag = new String("]_"+entityID);	    			    		
	    		if(endTag.containsKey(wordNum))
	    			tag = tag.concat(endTag.get(wordNum));
	    		endTag.put(wordNum, tag);
	    	}
	    	else {
	    		// set mention start
	    		String tag = new String("[_"+entityID);	    		
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(startTag.containsKey(wordNum))
	    			tag = tag.concat(startTag.get(wordNum));
	    		startTag.put(wordNum, tag);
	    		
	    		// set mention end
	    		tag = new String("]_"+entityID);	    			    		
	    		wordNum = mi.getExtent().getEndWN();
	    		if(endTag.containsKey(wordNum))
	    			tag = tag.concat(endTag.get(wordNum));
	    		endTag.put(wordNum, tag);
	    	}
	    }
	    
		try {
			outWriter.write("#begin document " + d.getDocID() + "\n");
			int psentNum = -1;
			for(int i = 0; i < words.size(); i++){				
				if(psentNum != d.getSentNum(i)){
					outWriter.write("\n");
					psentNum = d.getSentNum(i);
				}
				
				if(startTag.containsKey(i))
					outWriter.write(startTag.get(i) + " ");
				outWriter.write(words.get(i)+ " ");
				if(endTag.containsKey(i))
					outWriter.write(endTag.get(i)+ " ");
			}
			outWriter.write("#end document\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * This function output the coreference annotation in CoNLL Shared Task format
     * @param keySol: Solution in the ChainSolution format
     * @param d: document
     * @param outWriter
     */
    private void generateCoNLLFile(ChainSolution<Mention> keySol, Doc d, BufferedWriter outWriter) {
    	Map<Integer, String> m_corefTag =  new HashMap<Integer, String>();
    	Map<Integer, String> m_mentionTag =  new HashMap<Integer, String>();
    	List<String> words = d.getWords();
    	List<Mention> ments = new ArrayList<Mention>(keySol.getAllMembers());
	    Map<String, Integer> EntityIdMap = new HashMap<String, Integer>();
	    //d.setPredEntities(keySol);
	    int MaxEntity = 0;
	    int entityID;
	    String entityTag;
	    Collections.sort(ments);
	    // generate coreference tags
	    for (Mention mi : ments) {
	    	entityTag = mi.getEntityID();
	    	if(mi.getExtent().getStartWN() == mi.getExtent().getEndWN()) {
	    		String tag = new String("("+entityTag+")");
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(m_mentionTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_mentionTag.get(wordNum));
	    		m_mentionTag.put(wordNum, tag);
	    	}
	    	else {
	    		// set mention start
	    		String tag = new String("("+entityTag);
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(m_mentionTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_mentionTag.get(wordNum));
	    		m_mentionTag.put(wordNum, tag);
	    		// set mention end
	    		tag = new String(entityTag+")");
	    		wordNum = mi.getExtent().getEndWN();
	    		if(m_mentionTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_mentionTag.get(wordNum));
	    		m_mentionTag.put(wordNum, tag);
	    	}
	    	
	    	
	    	Set<Mention> backChain = keySol.getContainerFor(mi);
	    	if(removeSingleton){
	    		if(backChain.size() ==0 || backChain.size()==1)
	    			continue;
	    		boolean flag = true;
	    		if(!trueBoundary){
	    			for(Mention mj : backChain){
	    			if(!mi.equals(mj) &&  mj.getExtent().getStart() <= mi.getExtent().getStart() 
	    						&& mj.getExtent().getEnd() >= mi.getExtent().getEnd()
	    				) {
	    					flag = false;
	    					break;
	    				}
	    		}
	    		if((!flag && !trueBoundary))
	    			continue;
	    		}
	    	}
	    	if(!EntityIdMap.containsKey(mi.getEntityID())){
	    		EntityIdMap.put(mi.getEntityID(), MaxEntity);
	    		MaxEntity++;
	    	}
	    	entityID = EntityIdMap.get(mi.getEntityID());
	    	if(mi.getExtent().getStartWN() == mi.getExtent().getEndWN()) {
	    		String tag = new String("("+entityID+")");
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(m_corefTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_corefTag.get(wordNum));
	    		m_corefTag.put(wordNum, tag);
	    	}
	    	else {
	    		// set mention start
	    		String tag = new String("("+entityID);
	    		int wordNum = mi.getExtent().getStartWN();
	    		if(m_corefTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_corefTag.get(wordNum));
	    		m_corefTag.put(wordNum, tag);
	    		// set mention end
	    		tag = new String(entityID+")");
	    		wordNum = mi.getExtent().getEndWN();
	    		if(m_corefTag.containsKey(wordNum))
	    			tag = tag.concat("|" + m_corefTag.get(wordNum));
	    		m_corefTag.put(wordNum, tag);
	    	}
	    }
	    
		try {
			outWriter.write("#begin document " + d.getDocID());
			int psentNum = -1;
			for(int i = 0; i < words.size(); i++){				
				if(psentNum != d.getSentNum(i)){
					outWriter.write("\n");
					psentNum = d.getSentNum(i);
				}
				outWriter.write(words.get(i)+"\t");
				if(m_corefTag.containsKey(i))
					outWriter.write(m_corefTag.get(i) + "\n");
				else
					outWriter.write("-\n");
			}
			outWriter.write("#end document\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Output Corefernce annotation
     * The output is in column format defined by CoNLL shared Task 12
     * @param pred: coreference annotation of a document in ChainSolution format
     * @param doc: a document
     * @return
     */
    public Score getScore( ChainSolution<Mention> pred, Doc doc) {
    	List<Doc> docs = new ArrayList<Doc>();
    	docs.add(doc);
    	List<ChainSolution<Mention>> preds = new ArrayList<ChainSolution<Mention>>();
    	preds.add(pred);
    	return getScore(preds,docs);
    }
    /**
     * Output Corefernce annotation
     * The output is in column format defined by CoNLL shared Task 12
     * @param pred: coreference annotation of a list of documents in ChainSolution format
     * @param doc: list of documents
     * @return
     */
    public Score getScore(List<ChainSolution<Mention>> preds, List<Doc> docs) {
    	try {    		
			m_predWriter = new BufferedWriter( new FileWriter(m_predFileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        int numDocs = preds.size();
        
   	    for (int iD = 0; iD < numDocs; ++iD) {
   	    	generateCoNLLFile(preds.get(iD), docs.get(iD),m_predWriter);
   	    }
   	    

   	    try {
			m_predWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
   	    try {
			if(m_keyFileName!=null){
				String line;
				String exec = "/usr/bin/perl scorer.pl all " + m_keyFileName + " " + m_predFileName + " none" + " | tee outtmp";
				System.out.println(exec);
				Process p = Runtime.getRuntime().exec(exec);
				BufferedReader input =
					new BufferedReader
					(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					System.out.println(line);
				}
				input.close();
			}
		}
		catch (Exception err) {
			err.printStackTrace();
		}
	*/
		return null;
    }
    public void setRemoveSingleton(boolean removeSingleton) {
    	this.removeSingleton = removeSingleton;
    }
    public boolean isRemoveSingleton() {
    	return removeSingleton;
    }
}
