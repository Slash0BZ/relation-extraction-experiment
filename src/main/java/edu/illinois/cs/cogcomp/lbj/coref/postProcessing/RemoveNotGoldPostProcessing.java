package edu.illinois.cs.cogcomp.lbj.coref.postProcessing;

import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.learn.LinearThresholdUnit;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

public class RemoveNotGoldPostProcessing implements PostProcessor {
	protected LinearThresholdUnit m_scorer = null;
	
	public RemoveNotGoldPostProcessing() {
		
	}
	public RemoveNotGoldPostProcessing(LinearThresholdUnit scorer) {
		m_scorer = scorer;
	}
	
	@Override
	public  ChainSolution<Mention> decode(Doc doc, ChainSolution<Mention> inSol) {

		ChainSolution<Mention> outSol = new ChainSolution<Mention>();
		List<Mention> allMents = doc.getPredMentions();
		List<Mention> trueMents = doc.getTrueMentions();
		for (int j = 0; j < allMents.size(); ++j)
			outSol.recordExistence(allMents.get(j));
		
		for (int j = 0; j < allMents.size(); ++j) {
		    Mention mj = allMents.get(j);
		    boolean flag = false;
		    for(Mention m : trueMents){
		    	if(m.getExtentFirstWordNum() == mj.getExtentFirstWordNum() && m.getExtentLastWordNum() == mj.getExtentLastWordNum())
		    		flag = true;
		    }
		    if(!flag)
		    	continue;
	    	Set<Mention> backChain = inSol.getContainerFor(mj);
	    	for(Mention mi : backChain){
	    		flag = false;
	    		for(Mention m : trueMents){
			    	if(m.getExtentFirstWordNum() == mi.getExtentFirstWordNum() && m.getExtentLastWordNum() == mi.getExtentLastWordNum())
			    		flag = true;
			    }
			    if(!flag)
			    	continue;
	    		outSol.recordEquivalence(mi, mj, "label");	    		
	    	}
	    	
		}
		return outSol;
	}
}
