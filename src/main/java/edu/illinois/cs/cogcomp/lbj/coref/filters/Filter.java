package edu.illinois.cs.cogcomp.lbj.coref.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Generic filter class. */
abstract public class Filter<T> {
    abstract public boolean accept(T ex);

    /** Get entries accept'ed by filter. */
    public List<T> getFiltered(Collection<T> xes) {
	List<T> newList = new ArrayList<T>();
	for (T ex : xes) {
	    if ( this.accept(ex) )
		newList.add(ex);
	}
	return newList;
    }

}
