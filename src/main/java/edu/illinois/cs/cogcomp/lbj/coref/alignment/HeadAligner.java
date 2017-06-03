package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
/**
 * Aligns objects based on exactly matching.
 * @param <T> The type of object to be aligned.
 */
public class HeadAligner<T> extends Aligner<T> implements Serializable{

	private static final long serialVersionUID = 1L;
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
	public Map<T,T> getAlignment(Collection<T> sources, Collection<T> targets) {
        Map<T,T> result = new HashMap<T,T>();
        
        Set<T> targetsLeft = new HashSet<T>(targets);
    
        for (T m : sources) {
            T target = getExactMatch(m, targetsLeft);
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
     * Finds the mention which exactly match {@code m}
     * @param m The mention to match.
     * @param targets The possible targets.
     * @return The best target for {@code m}.
     */
	 public T getExactMatch(T m, Set<T> targets) {
			for (T target : targets) {
				Mention mm = (Mention) m;
				Mention mt = (Mention) target;
				if(mm.getHead().equals(mt.getHead()))
					return target;
			}
			return null;
	 }
}
