package edu.illinois.cs.cogcomp.lbj.coref.alignment;

import java.util.Collection;
import java.util.Map;

/** Base class for aligning objects
 * @param T The type of object to be aligned.
 */
abstract public class Aligner<T> {
    /**
     * Aligns elements in {@code from} to elements in {@code to}
     * @param from The elements to be aligned.
     * @param to The target elements. 
     * @return A map representing an alignment from objects in {@code from}
     * to objects in {@code to}.
     */
    abstract public Map<T,T> getAlignment(Collection<T> from, Collection<T> to);
}
