package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.ErrorAnalysis;



/**
 * Generate the result in SemEval format
 * @author Kai-Wei Chang
 */
public class OutputD3Scorer extends Scorer<ChainSolution<Mention>>{	
	BufferedWriter m_fileWriter;
	String m_outputDirectory=null;
	
    /** Default Constructor. */

    public OutputD3Scorer(String outputDirectoy){
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
		String fileName = d.getBaseFilename()+".json";
		if(m_outputDirectory != null)
			fileName = m_outputDirectory + "/"+d.getBaseFilename()+".json";
		
		File directory = new File(new File(fileName).getParent());
		if(!directory.exists())
			if(directory.mkdir()==false){
				System.err.println("Error in creating directory " + directory.getName());
			}
			else{
				System.err.println("Creating directory " + directory.getName());
			}
		
		m_fileWriter = new BufferedWriter( new FileWriter(fileName));
		m_fileWriter.write("{\n");
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
			m_fileWriter.write("\"name\" : \" " + m.getExtent().getCleanText() +"\",\n");
			m_fileWriter.write("\"text\" : \" " + ErrorAnalysis.printSentOfMentiona(d, m).replace("\"", "") +"\",\n");
			m_fileWriter.write("\"MType\" : \" " + m.getType() +"\"\n");
			m_fileWriter.write("}\n");
			if(iterator.hasNext())
				m_fileWriter.write(",\n");
		}
		m_fileWriter.write("],");
		m_fileWriter.write("\"links\" : [");
		iterator = pred.getAllMembers().iterator();
		while(iterator.hasNext()){
			Mention m = iterator.next();
			if(pred.getBestLink(m)==null)
				continue;
			m_fileWriter.write("{");
			m_fileWriter.write("\"source\" :  " + mentionIDMap.get(pred.getBestLink(m)) +",\n");
			m_fileWriter.write("\"target\" :  " + mentionIDMap.get(m) +"\n");
			//m_fileWriter.write("\"text\" : \" " + ErrorAnalysis.printSentBetweenMentions(d, m, pred.getBestLink(m)) +"\",\n");
			m_fileWriter.write("}\n");
			break;
		}
		while(iterator.hasNext()){
			Mention m = iterator.next();
			if(pred.getBestLink(m)==null)
				continue;
			m_fileWriter.write(",\n");
			m_fileWriter.write("{");
			m_fileWriter.write("\"source\" :  " + mentionIDMap.get(pred.getBestLink(m)) +",\n");
			m_fileWriter.write("\"target\" :  " + mentionIDMap.get(m) +"\n");
			//m_fileWriter.write("\"text\" : \" " + ErrorAnalysis.printSentBetweenMentions(d, m, pred.getBestLink(m)) +"\",\n");
			m_fileWriter.write("}\n");
		}
		m_fileWriter.write("]\n");
		m_fileWriter.write("}\n");
		m_fileWriter.close();
		return null;
	}
	@Override
	public Score getScore(ChainSolution<Mention> key,
			ChainSolution<Mention> pred) {
		Doc d = null;
		Iterator<Mention> iterator = pred.getAllMembers().iterator();
		if(iterator.hasNext())
			d = iterator.next().getDoc();
		try {
			getScore(key, pred, d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
