package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.alignment.Aligner;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.StringScore;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;


/**
 * Generate the result in SemEval format
 * @author Kai-Wei Chang
 */
public class OutputJsonScorer extends Scorer<ChainSolution<Mention>>{	
	StringWriter m_fileWriter;
	String m_outputDirectory=null;
	
    /** Default Constructor. */

    public OutputJsonScorer(String outputDirectoy){
    	m_outputDirectory = outputDirectoy;
    }
    
	/**
	{
    "nodes": [
              {
                  "tag": "http://en.wikipedia.org/wiki/Meteoroid", 
                  "id": 0, 
                  "name": "meteor"
              },
              {..}]
     "links" : [
              {
            "source": 0, 
            "target": 1
              }, {...}]
     } 
     
*/
	public Score getScore(ChainSolution<Mention> key,
			ChainSolution<Mention> pred, Doc d) throws IOException{
		
		
		m_fileWriter = new StringWriter();
		m_fileWriter.write("{\n");

		m_fileWriter.write("    \"text\": [\"");
		m_fileWriter.write(cleanString(d.toAnnotatedString(false,false,false,true)));
		m_fileWriter.write("\"],\n");
		// output node
		m_fileWriter.write("    \"nodes\": [\n");
		Map<Mention, Integer> mentionIDMap = new HashMap<Mention, Integer>(); 
		Iterator<Mention> iterator = pred.getAllMembers().iterator();
		int currentId = 0;
		while(iterator.hasNext()){
			Mention m = iterator.next();
			int id = currentId;
			if(mentionIDMap.containsKey(m))
				id = mentionIDMap.get(m);
			else
				mentionIDMap.put(m, id);
			currentId++;
			m_fileWriter.write("{");
			m_fileWriter.write("\"id\" :  " + id +",\n");
			m_fileWriter.write("\"name\" : \"" + cleanString(m.getSurfaceText()) +"\",\n");
			//m_fileWriter.write("\"span\" : \"" + m.getExtentFirstWordNum() +"_"+ m.getExtentLastWordNum() +"\",\n");
			//m_fileWriter.write("\"text\" : \"" + printSentOfMentiona(d, m) +"\",\n");
			
			String eID = m.getEntityID();
            if (m.getPredictedEntityID() != null) {
               eID = m.getPredictedEntityID();
			}
			m_fileWriter.write("\"eID\" : \"" + eID+"\",\n");
			m_fileWriter.write("\"NameEntType\" : \"" + m.getEntityType()+"\",\n");
			m_fileWriter.write("\"MType\" : \"" + m.getType() +"\"\n");
			m_fileWriter.write("}\n");
			if(iterator.hasNext())
				m_fileWriter.write(",\n");
		}
		m_fileWriter.write("],");
		m_fileWriter.write("\"links\" : [");
		
		boolean findFirst= false;
		for(Pair<Mention,Mention> link : pred.getEdges()){
			Mention m = link.getSecond();
			Mention a = link.getFirst();
				if(findFirst)
					m_fileWriter.write(",\n");
				m_fileWriter.write("{");
				//m_fileWriter.write("\"text\" : \" " + printSentBetweenMentions(d, m, pred.getBestLink(m)) +"\",\n");
				m_fileWriter.write("\"source\" :  " + mentionIDMap.get(a) +",\n");
				m_fileWriter.write("\"target\" :  " + mentionIDMap.get(m) +"\n");
				m_fileWriter.write("}\n");
				findFirst = true;
		}
		
		m_fileWriter.write("]\n");
		m_fileWriter.write("}\n");
		m_fileWriter.close();
		Score score = new StringScore(m_fileWriter.getBuffer().toString());
		return score;
	}
	@Override
	public Score getScore(ChainSolution<Mention> key,
			ChainSolution<Mention> pred) {
		Doc d = null;
		Iterator<Mention> iterator = pred.getAllMembers().iterator();
		if(iterator.hasNext())
			d = iterator.next().getDoc();
		try {
			return getScore(key, pred, d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private static String printSentOfMentiona(Doc doc, Mention m){
		int sentIndex = m.getSentNum();
		StringBuffer sent = new StringBuffer();
		int wordIndex = doc.getSentStartNum(sentIndex);
		while (wordIndex < doc.getWords().size()) {
			if (doc.getSentNum(wordIndex) > sentIndex)
				break;
			if (wordIndex == m.getExtentFirstWordNum())
				sent.append('[');
			if (wordIndex == m.getHeadFirstWordNum())
				sent.append('|');
			sent.append(doc.getWord(wordIndex) + " ");
			if (wordIndex == m.getHeadLastWordNum())
				sent.append('|');
			if (wordIndex == m
					.getExtentLastWordNum())
		sent.append(']');
			wordIndex++;
		}
		return cleanString(sent.toString());
	}
	public static String printSentBetweenMentions(Doc doc, 
		Mention m, Mention a) {
		return printSentBetweenMentions(doc, m, a, true,false);
	}
	
	public static String printSentBetweenMentions(Doc doc, 
			Mention m, Mention a, boolean breakSent, boolean pos) {
		String output= "";
		int startSent = a.getSentNum(), endSent = m.getSentNum();
		if(startSent> endSent) {
			startSent= m.getSentNum();
			endSent = a.getSentNum();
		}
		for (int sentIndex = startSent; sentIndex <= 
				endSent; sentIndex++) {
			StringBuffer sent = new StringBuffer();
			int wordIndex = doc.getSentStartNum(sentIndex);
			while (wordIndex < doc.getWords().size()) {
				if (doc.getSentNum(wordIndex) > sentIndex)
					break;
				if (wordIndex == a.getExtentFirstWordNum()
						|| wordIndex == m
								.getExtentFirstWordNum())
					sent.append('[');
				if (wordIndex == a.getHeadFirstWordNum()
						|| wordIndex == m.getHeadFirstWordNum())
					sent.append('|');
				sent.append(doc.getWord(wordIndex) + " ");
				if(pos)
					sent.append(WordHelpers.getPOS(doc.getTextAnnotation(), wordIndex) + " ");
				if (wordIndex == a.getHeadLastWordNum()
						|| wordIndex == m.getHeadLastWordNum())
					sent.append('|');
				if (wordIndex == a.getExtentLastWordNum()
						|| wordIndex == m
								.getExtentLastWordNum())
					sent.append(']');
				
				wordIndex++;
			}
			if(breakSent)
				output += sent + " ";
			else
				output += sent;
		}
		return cleanString(output);
	}
	private static String cleanString(String s){
		return s.replaceAll("\"", "``").replaceAll("'", "`").replaceAll("[^a-zA-Z_0-9.,!? ;\\\\|'`\\[\\]()<>]","");
	}
}
