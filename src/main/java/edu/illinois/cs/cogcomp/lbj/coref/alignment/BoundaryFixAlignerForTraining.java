package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;

/**
 * Aligns objects based on maximizing overlap, but allows small difference on mention boundaries.
 * We use this class to fix the annotation errors on mention boundaries. 
 * @param T The type of object to be aligned.
 */
public class BoundaryFixAlignerForTraining<T> extends Aligner<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static  String[] allowPOSHeadAndTailDifferent = new String[]{"^R.*","CD","^V.*","^J.*","^W.*","DT"};
	/**
     * Finds the best match between a mention and a target.
     * The best match is defined as the target
     * with the greatest overlap. The small differences on the boundaries are obmitted. 
     * @param m The mention to match.
     * @param targets The possible targets.
     * @return The best target for {@code m}.
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
            }
        }

        for(T m : sources){
        	// Find a mention whose boundary is similar to m
        	if(result.get(m) == m){
        		T target = getApproximateMatch(m, targetsLeft);
        		if (target == null) {
        			result.put(m, m);
        		} else {
        			result.put(m, target);
        		}
        	}
        }
        return result;
	}
	/**
     * Finds the mention which exactly match {@code m}
     * @param m The mention to match.
     * @param targets The possible targets.
     * @return The best target for {@code m}.
    1L */
	public T getExactMatch(T m, Set<T> targets) {
			for (T target : targets) {
				if(target.equals(m))
					return target;
			}
			return null;
	 }
	
	/**
     * Finds an approximate match between a mention and a target.
     * We allow some differences between boundaries to fix annotation error. 
     * @param m The mention to match.
     * @param targets The possible targets.
     * @return The best target for {@code m}.
     */
	 public T getApproximateMatch(T mt, Set<T> targets) {
		 	if (!(mt instanceof Mention))
		 		return null;
		 	Mention m = (Mention) mt;
		 	String mRemoveHeadandTail = m.getSurfaceText();
		 	if (myAux.isStringMatches(m.getDoc().getPOS(m.getExtentFirstWordNum()),allowPOSHeadAndTailDifferent , true))
		 		mRemoveHeadandTail = mRemoveHeadandTail.replaceAll("^[\\S]* ", "");
		 		if(myAux.isStringMatches(m.getDoc().getPOS(m.getExtentLastWordNum()),allowPOSHeadAndTailDifferent , true))
		 		mRemoveHeadandTail = mRemoveHeadandTail.replaceAll(" [\\S]*$", "");

			for (Mention target : (Set<Mention>)targets) {
				if(Math.abs(target.getExtentFirstWordNum() - m.getExtentFirstWordNum())<=2){
					if(target.getSurfaceText().replaceAll("'s|[\\W]", "").equals(m.getSurfaceText().replaceAll("'s|[\\W]","")))
						return (T)target;
					
				}
				String tRemoveHeadandTail = target.getSurfaceText();
				if(myAux.isStringMatches(target.getDoc().getPOS(target.getExtentFirstWordNum()),allowPOSHeadAndTailDifferent , true))
			 		tRemoveHeadandTail = tRemoveHeadandTail.replaceAll("^[\\S]* ", "");
			 	if(myAux.isStringMatches(target.getDoc().getPOS(target.getExtentLastWordNum()),allowPOSHeadAndTailDifferent , true))
			 		tRemoveHeadandTail = tRemoveHeadandTail.replaceAll(" [\\S]*$", "");
			 	
				if( Math.abs(target.getExtentFirstWordNum() - m.getExtentFirstWordNum())<=2 && 
						tRemoveHeadandTail.replaceAll("'s|[\\W]", "").equals(mRemoveHeadandTail.replaceAll("'s|[\\W]",""))){
					return (T) target;
				}
				if(m.getHead().equals(target.getHead()))
					return (T)target;
				
			}
			return null;
	 }
}
