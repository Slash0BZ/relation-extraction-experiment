package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Aligns objects based on maximizing overlap.
 * @param <T> The type of object to be aligned.
 */
public abstract class DefaultAligner<T> extends Aligner<T>
implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default Constructor.
     */
    public DefaultAligner() {
	super();
    }

    /** Aligns each source element to some target element
     * or to itself if no alignment can be found.
     * Alignment is 1-to-1 (each target element is linked to
     * by at most one source element).
     * @param sources A collection of source elements.
     * @param targets A collection of target elements.
     * @return A map that aligns each source element
     * with either a target element or itself.
     */
    @Override
    public Map<T,T> getAlignment(
	    Collection<T> sources, Collection<T> targets) {
        
        Map<T,T> result = new HashMap<T,T>();
    
        Set<T> targetsLeft = new HashSet<T>(targets);
    
        for (T m : sources) {
            T target = getBestMatch(m, targetsLeft);
            if (target == null) {
        	result.put(m, m);
            } else {
        	result.put(m, target);
        	targetsLeft.remove(target);
            }
        }
    
        return result;
    }
    
    /**
     * Finds the best match between a mention and a target.
     * The best match is defined as the target
     * with the greatest overlap.
     * @param m The mention to match.
     * @param targets The possible targets.
     * @return The best target for {@code m}.
     */
    public T getBestMatch(T m, Set<T> targets) {
	int bestOverlap = 0;
	T best = null;
	for (T target : targets) {
	    int overlap = getOverlap(m, target);
	    if (overlap > bestOverlap) {
		bestOverlap = overlap;
		best = target;
	    }
	}
	return best;
    }
    
    abstract public int getOverlap(T a, T b);

}
