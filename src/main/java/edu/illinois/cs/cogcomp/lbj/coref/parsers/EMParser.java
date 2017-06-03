package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/** 
 * Extracts mentions for use in training an LBJ classifier.
 * The mentions are extracted from a corpus of documents specified
 * either by providing a file name containing a list of document filenames.
 * Gets all the mentions in the specified documents,
 * @author Eric Bengtson
 */
public class EMParser implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
    private List<Doc> m_docs;
    private List<Mention> m_examples;
    private int m_iD = 0;
    private int m_iX = 0;

    /** 
     * Constructs a Parser that extracts mentions
     * for use in training an LBJ classifier.
     * The mentions are extracted from a corpus of documents specified
     * either by providing a file name containing a list of document filenames.
     * Gets all the mentions in the specified documents.
     */
    public EMParser(String fileListFN) {
	this.startup(DocLoader.getDefaultLoader(fileListFN));
    }
    
    public EMParser() {
    }
    
    //edu.illinois.cs.cogcomp.lbjava.parse.Parser methods:
    
    /**
     * Gets the next mention, or null if no more mentions remain.
     * @return The next mention or null if none remain.
     */
    public Mention next() {
	while (m_iD < m_docs.size() && m_iX >= m_examples.size()) {
	    if (m_iX == 0) {
		System.err.println("No examples in doc " + m_iD);
	    }
	    this.advanceDoc();
	}
	if (m_iD >= m_docs.size()) { //Finished with last example.
	    this.cleanup();
	    return null;
	}
	return this.getNextExample();
    }
    
    /**
     * Resets the parser to the first document in the corpus and resets
     * the position within the (first) document.
     * It is not necessary to call this method before the first call to next.
     */
    public void reset() {
	m_iD = 0;
	resetDoc();
    }

    public void close() { reset(); }
    
    /**
     * Does nothing
     * @param q An arbitrary object.
     */
    public void enqueue(Object q) {
	//Note: Does nothing.
    }
    

    
    /**
     * Gets the current mention from the cache and prepares for the next example.
     * Call only after {@code m_examples} is initialized and when {@code m_iX}
     * is less than the size of {@code m_examples}
     * @return The current mention (but never null).
     */
    private Mention getNextExample() {
	Mention ex = m_examples.get(m_iX);
	m_iX++;
	return ex;
    }

    /**
     * Prepares to extract mentions from the next document
     * (including resetting the document).
     * Safe to call even when no additional documents remain.
     */
    private void advanceDoc() {
	m_iD++;
	if (m_iD < m_docs.size())
	    this.resetDoc();
    }

    /**
     * Resets the document, including caching the mentions from the
     * current document.
     * Safe to call even if document is empty or does not exist.
     */
    private void resetDoc() {
	m_iX = 0;
	if (m_iD < m_docs.size())
	    m_examples = getExamples(m_iD);
	else
	    m_examples = new ArrayList<Mention>();
    }

    /**
     * Load all mentions from the current document using
     * {@link Doc#getMentions}.
     * Should not be called if the document does not exist.
     * @return A list of mentions as retrieved using {@link Doc#getMentions}.
     */
    private List<Mention> getExamples(int iD) {
	Doc d = m_docs.get(m_iD);
	return d.getMentions();
    }


    /**
     * Prepares the parser, by loading documents and resetting.
     * @param loader The loader from which to get the documents.
     */
    protected void startup(DocLoader loader) {
	m_docs = loader.loadDocs();
	reset();
    }
    
    
    /**
     * Called immediately before next returns null.
     * Currently does nothing, but can be used to save caches or
     * record statistics. 
     */
    protected void cleanup() {
	//Does nothing.
    }
}
