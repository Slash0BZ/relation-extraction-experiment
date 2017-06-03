package edu.illinois.cs.cogcomp.lbj.coref.filters;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;

/** 
 * Filters EntityMentions.
 */
abstract public class MFilter extends Filter<Mention> {
    /**
     * Determines whether a mention should be accepted by the filter.
     * @param m The mention in question.
     * @return Whether the mention should be accepted by the filter.
     */
    abstract public boolean accept(Mention m);
}
