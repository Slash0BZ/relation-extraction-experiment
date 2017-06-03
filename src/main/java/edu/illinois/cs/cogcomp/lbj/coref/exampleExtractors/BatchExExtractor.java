package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.util.ArrayList;
import java.util.List;


/**
 * The base class for all example extractors that use a batch process.
 * @param <T> The type of example.
 * @author Eric Bengtson
 */
abstract public class BatchExExtractor<T> extends ExampleExtractor<T> {

    private List<T> m_exampleCache = null;
    private int m_exampleCacheNextIx = 0;

    /** 
     * Generate all (non-filtered) examples.
     * @return A list of all the examples to be extracted.
     */
    abstract protected List<T> generateAllExamples();


    //Implementation of public methods using the cache and the
    //results of generateAllExamples:

    /** 
     * Get the next example.
     * @return the next example, or null when done.
     */
    public T next() {
	return this.getNextCached();
    }

    /** 
     * Gets all the examples.
     * @return examples that would be returned by repeated calls to next().
     * Does not include the final null that next() would return.
     */
    public List<T> getExamples() {
	return new ArrayList<T>(generateAllExamples());
    }

    /**
     * Does nothing, since extractor is a batch extractor.
     */
    public void recordEquivalence() {
	//Do nothing, since batch.
    }

    //Cache implementation:

    /** 
     * Gets the next entry in the cache,
     * The cache will be populated with examples from {@code getExamples()}.
     * @return The next cached example.
     */
    protected T getNextCached() {
	if (m_exampleCache == null) {
	    m_exampleCache = this.getExamples();
	    m_exampleCacheNextIx = 0;
	}
	if ( m_exampleCache != null) {
	    if(m_exampleCacheNextIx < m_exampleCache.size() ) {
		T result = m_exampleCache.get(m_exampleCacheNextIx);
		m_exampleCacheNextIx++;
		return result;
	    } else {
		return null; //Last example.
	    }
	} else {
	    throw new RuntimeException("Bug: " + this.getClass().toString()
	    + " doesn't correctly getExamples and yet uses the cache.");
	    //TODO: Fall back to next();
	    //return this.next();
	}
    }

    /** 
     * Reset the extractor.
     * After calling this method, {@code next()}
     * should begin returning examples starting with the first example.
     */
    public void reset() {
	m_exampleCache = null;
	m_exampleCacheNextIx = 0;
    }
}
