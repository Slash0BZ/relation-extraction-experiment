package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;


/** 
 * This interface specifies a type of ExampleExtractor
 * that extracts examples from Docs.
 * @param <T> The type of examples.
 * @author Eric Bengtson
 */
public interface DocExampleExtractor<T> extends edu.illinois.cs.cogcomp.lbjava.parse.Parser {
    
    /**
     * Get the next example.
     */
    public T next();

    //TODO: Would rather not have this here, as it implies that
    //examples can be generated batch.  However, some classes assume
    //CExampleExtractor will return examples (filtered if applicable).

    /** 
     * If implementation isn't batch, then getExamples should
     * return the output of repeated calls to next() with no intervening
     * calls.
     * @return A list of all examples.
     */
    public List<T> getExamples();

    //TODO: Move somewhere else.
    /** Does nothing unless non-batch. */
    public void recordEquivalence();

    
    /* Docs */
    
    /** 
     * Get the document.
     * @return The document.
     */
    public Doc getDoc();

    /**
     * Sets the document and resets.
     * @param doc The document.
     */
    public void setDoc(Doc doc);

    /* (non-Javadoc)
     * Added for compatibility with older edu.illinois.cs.cogcomp.lbjava libraries
     */
    public void reset();
}
