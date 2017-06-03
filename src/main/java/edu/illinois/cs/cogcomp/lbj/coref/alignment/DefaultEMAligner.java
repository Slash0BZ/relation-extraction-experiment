package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;


/** Aligns objects based on maximizing head word overlap. */
public class DefaultEMAligner extends DefaultAligner<Mention>
 implements Serializable {
    private static final long serialVersionUID = 2L;
    

    @Override
    public int getOverlap(Mention a, Mention b) {
	return new WordOverlapChunkAligner().getOverlap(
		a.getHead(), b.getHead());
    }
}

