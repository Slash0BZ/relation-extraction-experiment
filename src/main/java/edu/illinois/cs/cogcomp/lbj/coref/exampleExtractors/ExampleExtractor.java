package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

/**
 * Generic example extractor.
 * @param <T> The type of the examples being extracted.
 * @author Eric Bengtson
 */
abstract public class ExampleExtractor<T> implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {

    /** 
     * Gets the next example.
     * @return The next example or null when done.
    */
    abstract public T next();

    /**
     * Does nothing.
     * @param q Ignored.
     */
    public void enqueue(Object q) {
	//Note: Does nothing.
    }

    /** 
     * After calling this method, {@code next()} should begin returning examples
     * from the first example.
     */
    public void reset() {
    }


    public void close() { reset(); }
}
