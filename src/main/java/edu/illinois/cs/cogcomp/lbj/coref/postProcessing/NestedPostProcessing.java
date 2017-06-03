package edu.illinois.cs.cogcomp.lbj.coref.postProcessing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.features.NumberFeatures;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class NestedPostProcessing implements PostProcessor {

	public NestedPostProcessing() {
		
	}

	@Override
	public  ChainSolution<Mention> decode(Doc doc, ChainSolution<Mention> inSol) {		
		ChainSolution<Mention> outSol = new ChainSolution<Mention>();
		Set<Mention> allMents = inSol.getAllMembers();
		Set<Mention> discardMention = new HashSet<Mention>();
		//System.out.println(inSol);
		//System.out.println("=========");
		int veryLarge = 100000;
		for (Mention mj : allMents) {
	    	Set<Mention> backChain = inSol.getContainerFor(mj);
	    	/*if(mj.getExtent().getText().contains(" and ")){
	    		for(int i=j; i< allMents.size(); ++i){
	    			Mention mi = allMents.get(i);
	    			//if(mi.getType().equals("PRO") && NumberFeatures.getNumberStrong(mi, true)== 'p')
	    				//outSol.recordEquivalence(mi, mj, "label");
	    		}
	    		continue;
	    	}*/
	    	for(Mention mi : backChain){
	    		if(!mi.equals(mj)
	    				&& mi.getExtentFirstWordNum() <= mj.getExtentFirstWordNum() 
	    				&& mi.getExtentLastWordNum() >= mj.getExtentLastWordNum() 
	    				&& !mi.getExtent().getText().contains(" and ")
	    				&& !mi.getExtent().getText().contains(" or ")){
	    			discardMention.add(mj);
	    			break;
	    		}
	    	}
		}
		for(Mention mj: allMents){
			if(discardMention.contains(mj))
				continue;
			outSol.recordExistence(mj);
			
		}
		for(Pair<Mention,Mention> link : inSol.getEdges()){
			Mention first = link.getFirst();
			Mention second = link.getSecond();
			if(discardMention.contains(first) || discardMention.contains(second))
				continue;
			outSol.recordEquivalence(first, second);
		}
		return outSol;
	}

}
