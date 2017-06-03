package edu.illinois.cs.cogcomp.lbj.coref.postProcessing;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

public interface PostProcessor {
	public  ChainSolution<Mention> decode(Doc d, ChainSolution<Mention> result);
}
