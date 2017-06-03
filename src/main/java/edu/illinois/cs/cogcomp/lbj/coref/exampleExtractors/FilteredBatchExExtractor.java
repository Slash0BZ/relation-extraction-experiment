package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.util.ArrayList;
import java.util.List;


/** 
 * Extracts examples with a facility for getting all examples in batch
 * and filtering examples.
 * {@code next()} and {@code getExamples()} will return filtered mentions.
 * @param <T> The type of example.
 * @author Eric Bengtson
 */
abstract public class FilteredBatchExExtractor<T> extends BatchExExtractor<T> {

    /**
     * Implement this method to define the filter outputs.
     * @param ex The example.
     * @return Whether the example is accepted by the filters.
     */
    abstract protected boolean doFiltersAccept(T ex);

    /**
     * Get a list of examples accepted by the filter out of the given list.
     * @param xes A list of examples.
     * @return A list of filter-accepted examples from among given examples.
     */
    protected List<T> getFilterAccepted(List<T> xes) {
	List<T> result = new ArrayList<T>();
	for (T ex : xes) {
	    if (doFiltersAccept(ex))
		result.add(ex);
	}
	return result;
    }

    /** 
     * Get the examples.
     * @return the examples that {@code next()} will return,
     * which are filtered.
     */
    public List<T> getExamples() {
	return new ArrayList<T>(getFilterAccepted(generateAllExamples()));
    }
}
