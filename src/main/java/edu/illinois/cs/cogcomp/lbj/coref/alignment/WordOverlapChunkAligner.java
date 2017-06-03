package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;


/** Aligns objects based on head word overlap. */
public class WordOverlapChunkAligner extends DefaultAligner<Chunk>
 implements Serializable {
    private static final long serialVersionUID = 2L;
        
    /**
     * Determines the number of words shared in the heads of the mentions.
     */
    @Override
    public int getOverlap(Chunk a, Chunk b) {
	if (a.getEnd() < b.getStart() || a.getStart() > b.getEnd() )
	    return 0;  //Non-overlapping.
	int start, end;
	//The latest start:
	if (a.getStartWN() >= b.getStartWN())
	    start = a.getStartWN();
	else
	    start = b.getStartWN();

	//The earliest end:
	if (a.getEndWN() <= b.getEndWN())
	    end = a.getEndWN();
	else
	    end = b.getEndWN();

	if (end < start)
	    return 0;
	else
	    return end - start + 1;
    }

}
