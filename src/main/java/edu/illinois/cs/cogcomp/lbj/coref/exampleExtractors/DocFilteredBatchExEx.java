package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/**
 * Extracts examples from a document using a batch process.
 * @param <T> The type of example to extract.
 * @author Eric Bengtson
 */
abstract public class DocFilteredBatchExEx<T>
 extends FilteredBatchExExtractor<T> implements DocExampleExtractor<T> {
    protected Doc m_doc = null;

    /**
     * Get the document
     * @return The document.
     */
    public Doc getDoc() {
	return m_doc;
    }

    /**
     * Sets the document and resets the extractor.
     * @param doc The document.
     */
    public void setDoc(Doc doc) {
	m_doc = doc; // Backed by input, rather than copied.
	this.reset();
    }
    
    /**
     * Indicates whether all applicable filters accept the example.
     * @param o The example.
     * @return Whether to accept the example.
     */
    public boolean doFiltersAccept(T o) {
	return true;
    }
}
