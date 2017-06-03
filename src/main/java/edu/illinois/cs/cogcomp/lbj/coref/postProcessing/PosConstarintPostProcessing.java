package edu.illinois.cs.cogcomp.lbj.coref.postProcessing;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbj.coref.constraints.Constraint;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

public class PosConstarintPostProcessing implements PostProcessor {
	private Constraint constraint = null;
	public PosConstarintPostProcessing() {
		
	}
	public PosConstarintPostProcessing(Constraint constraint) {
		this.constraint = constraint;
	}
	
	@Override
	public  ChainSolution<Mention> decode(Doc doc, ChainSolution<Mention> inSol) {

		ChainSolution<Mention> outSol = new ChainSolution<Mention>();
		outSol.setEdgeLabels(inSol.getEdgeLabels());
		// Copy the original Cluster
		for(Mention m: inSol.getAllMembers())
			outSol.recordExistence(m);
		
		for(Pair<Mention, Mention> edge : inSol.getEdges()){
			Mention m1 = edge.getFirst();
			Mention m2 = edge.getSecond();
			outSol.recordEquivalence(m1, m2);
		}
		
		for(Mention m: inSol.getAllMembers()){
			for(Mention a : inSol.getAllMembers()){
				if(a.compareTo(m)>=0)
					continue;
				try{
				if( !outSol.areTogether(a, m) && constraint.checkConstraint(a, m, !doc.usePredictedMentions())>0)
					outSol.recordEquivalence(a, m);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		outSol.setEdgeScores(inSol.getEdgeScores());
		return outSol;
	}
	public Constraint getConstraint() {
		return constraint;
	}
	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}
	
}
