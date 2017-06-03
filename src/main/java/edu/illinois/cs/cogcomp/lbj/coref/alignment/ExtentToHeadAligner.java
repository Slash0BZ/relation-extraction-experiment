package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;


/** Aligns objects by number of words overlap, then
 *  by closeness of last words.
 *  Imposes the restriction that to align, a pair of chunks must be nested.
 */
public class ExtentToHeadAligner extends WordOverlapChunkAligner
 implements Serializable {
    private static final long serialVersionUID = 1L;

    public Chunk getBestMatch(Chunk m, Set<Chunk> in) {
	int bestOverlapSize = 1; //Minimum of 1 word overlap.
	int bestEndToEndDist = Integer.MAX_VALUE;
	Chunk bestM = null;
	for (Chunk mIn : in) {
	    int overlapSize = getOverlap(m, mIn);
	    int dist = getEndToEndDist(m, mIn);
	    if (overlapSize > bestOverlapSize) {
		bestOverlapSize = overlapSize;
		bestM = mIn;
		
		bestEndToEndDist = Integer.MAX_VALUE; //reset
		
		if (dist < bestEndToEndDist) {
		    bestEndToEndDist = dist;    
		}
	    } else if (overlapSize == bestOverlapSize) {
		if (dist < bestEndToEndDist) {
		    bestEndToEndDist = dist;
		    bestM = mIn;
		}
	    }
	}
	return bestM;
    }

    /** @return the number of words overlapping, or 0 if the chunks are not
     *  nested.
     */
    public int getOverlap(Chunk a, Chunk b) {
	if (!nestedBInA(a, b))
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

    public boolean nestedBInA(Chunk a, Chunk b){
	if (a.getStart() <= b.getStart() && b.getEnd() <= a.getEnd())
	    return true; //a encompasses b.
	else
	    return false;
    }
    
    /** Gets the distance, in words, between the last word in {@code a}
	and the last word in {@code b}.  The distance is the absolute
	value of the difference between the word locations.
    */
    protected int getEndToEndDist(Chunk a, Chunk b) {
	return Math.abs(b.getEndWN() - a.getEndWN());
    }

}
