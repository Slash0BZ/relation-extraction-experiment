package edu.illinois.cs.cogcomp.lbj.coref.postProcessing;

import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbjava.learn.LinearThresholdUnit;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

public class RemoveSingletonProcessing implements PostProcessor {
	protected LinearThresholdUnit m_scorer = null;
	
	public RemoveSingletonProcessing() {
		
	}
	public RemoveSingletonProcessing(LinearThresholdUnit scorer) {
		m_scorer = scorer;
	}
	
	@Override
	public  ChainSolution<Mention> decode(Doc doc, ChainSolution<Mention> inSol) {

		ChainSolution<Mention> outSol = new ChainSolution<Mention>();
		Set<Mention> allMents = inSol.getAllMembers();
		for (Mention mj: allMents){
			if(inSol.getContainerFor(mj).size()>1)
			outSol.recordExistence(mj);
		}
		for(Pair<Mention, Mention> edge : inSol.getEdges()){
			Mention m1 = edge.getFirst();
			Mention m2 = edge.getSecond();
			if(outSol.contains(m1) && outSol.contains(m2))
				outSol.recordEquivalence(m1, m2, "link");
		}

	/*		
		for (Mention mj: allMents) {
	    	Set<Mention> backChain = inSol.getContainerFor(mj);
	    	if(backChain.size()<=1)
	    		continue;
	    	for(Mention mi : backChain){
	    		outSol.recordEquivalence(mi, mj, "label");	    		
	    	}
		}
		outSol.setM_bestlinkDecision(inSol.getM_bestlinkDecision());*/
		outSol.setEdgeScores(inSol.getEdgeScores());
		return outSol;
	}
}
