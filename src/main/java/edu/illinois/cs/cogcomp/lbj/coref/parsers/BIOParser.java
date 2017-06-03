package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.BIOExample;

/**
 * Extracts examples of mention chunks, one per word, for training
 * a mention detection classifier.
 * Each example represents one word, and indicate whether the word
 * begins, is inside, or ends a head and/or extent of a mention.
 * The examples are extracted from a corpus of documents specified
 * by providing a document loader.
 * To extract examples, repeatedly call the next method until it returns null.
 * @author Eric Bengtson
 */
public class BIOParser implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
    public List<Doc> m_docs;
    public List<BIOExample> m_examples;
    protected int m_iD = 0;
    protected int m_iX = 0;

    private int m_numExamplesProcessed = 0;

    
    /**
     * Constructs a Parser that extracts examples from a corpus,
     * with documents loaded by a specified document loader.
     * @param loader A document loader that loads a corpus of documents.
     */
    public BIOParser(DocLoader loader) {
	this.startup(loader);
    }
    
    public BIOParser() {}
    
    
    //edu.illinois.cs.cogcomp.lbjava.parse.Parser methods:

    /**
     * Gets the next example, or null if no more examples remain.
     * @return The next example or null if none remain.
     */
    public BIOExample next() {
	//Ensure doc has next example, or past last doc.
	while (m_iD < m_docs.size() && m_iX >= m_examples.size()) {
	    if (m_iX == 0)
		System.err.println("Zero BIOExamples in doc " + m_iD);
	    this.advanceDoc();
	}
	//If past last doc, we are done.
	if (m_iD >= m_docs.size()) {
	    this.cleanup();
	    return null; // No more elements remain.
	}

	m_numExamplesProcessed++;
	return getNextExample(); //Advances m_iX.
    }

    /**
     * Resets the parser to the first document in the corpus.
     * It is not necessary to call this method before the first call to next.
     */
    public void reset() {
	m_iD = 0;
	this.resetDoc();
	m_numExamplesProcessed = 0;
    }

    public void close() { reset(); }
    
    /**
     * Does nothing
     * @param q An arbitrary object.
     */
    public void enqueue(Object q) {
	//Note: Does nothing.
    }
    
    
    //Main example generating code:

    public List<BIOExample> getBIOExamples(Doc d) {
	List<String> words = d.getWords();
	char[] bioH = new char[words.size()];
	char[] bioE = new char[words.size()];
	char[] bracketsE = new char[words.size()];
	int nPos = 0, nNeg = 0;

	/** * Find all candidate chunks ** */
	/* First add every candidate as a false candidate */
	nNeg = words.size(); //Will be reduced by nPos
	for (int s = 0; s < words.size(); ++s) {
	    bioH[s] = 'o'; //outside
	    bioE[s] = 'o'; //outside
	    bracketsE[s] = 'n'; //none
	}

	/* Next add positive examples */
	//Only examples with same mentionType are counted (unless "ALL")
	for (int iM = 0; iM < d.getTrueMentions().size(); ++iM) {
	    Mention m = d.getTrueMentions().get(iM);
	    int hWordStart = m.getHeadFirstWordNum();
	    int hWordEnd = m.getHeadLastWordNum();
	    bioH[hWordStart] = 'b';
	    for (int wN = hWordStart + 1; wN <= hWordEnd; ++wN) {
		bioH[wN] = 'i';
	    }
	    int eWordStart = m.getExtentFirstWordNum();
	    int eWordEnd = m.getExtentLastWordNum();
	    bioE[eWordStart] = 'b';
	    for (int wN = eWordStart + 1; wN <= eWordEnd; ++wN) {
		bioE[wN] = 'i';
	    }

	    if (bracketsE[eWordStart] == 'e') bracketsE[eWordStart] = 'b';
	    else if (bracketsE[eWordStart] == 'n') bracketsE[eWordStart] = 's';

	    if (bracketsE[eWordEnd] == 's') bracketsE[eWordEnd] = 'b';
	    else if (bracketsE[eWordEnd] == 'n') bracketsE[eWordEnd] = 'e';
	}
	nPos += d.getTrueMentions().size();
	nNeg -= d.getTrueMentions().size(); //Every new pos wasn't really a neg.

	/** Build examples list: **/
	List<BIOExample> xes = new ArrayList<BIOExample>();
	int nWords = d.getWords().size();
	BIOExample prevEx = null;
	for (int wN = d.getTextFirstWordNum(); wN < nWords; ++wN) {
	    BIOExample ex = new BIOExample(d, wN, bioH[wN], bioE[wN], prevEx);
	    if (bracketsE[wN] == 'b') { //If both, treat as s but add an e
		ex.m_bracketE = 's';
		BIOExample ex2
		 = new BIOExample(d, wN, bioH[wN], bioE[wN], prevEx);
		ex2.m_bracketE = 'e';
		xes.add(ex2);
	    } else {
		ex.m_bracketE = bracketsE[wN];
	    }
	    xes.add(ex);
	    /*
	    if (bioH[wN] != 'o') {
		//more pos samples to increase recall:
		xes.add(ex);
	    }
	    */
	    prevEx = ex;
	}
	return xes;
    }
    
    
    
    //Private methods:
    
    /**
     * Gets an example from the cache and prepares for the next example.
     * Call only after {@code m_examples} is initialized and when {@code m_iX}
     * is less than the size of {@code m_examples}
     * @return The next example (but never null).
     */
    private BIOExample getNextExample() {
	BIOExample ex = m_examples.get(m_iX);
	m_iX++;
	return ex;
    }
    
    /**
     * Prepares to extract examples from the next document
     * (including resetting the document).
     * Safe to call even when no additional documents remain.
     */
    private void advanceDoc() {
	m_iD++;
	if (m_iD < m_docs.size())
	    this.resetDoc();
    }
    
    /**
     * Resets the document, including caching the examples from the
     * example extractor.
     * Safe to call even if document is empty or does not exist.
     */
    private void resetDoc() {
	m_iX = 0;
	if (m_iD < m_docs.size())
	    m_examples = getBIOExamples(m_docs.get(m_iD));
	else
	    m_examples = new ArrayList<BIOExample>();
    }
    
    //Protected startup and cleanup methods:

    /**
     * Prepares the parser, by loading documents and resetting the doc.
     * @param loader The loader from which to get the documents.
     */
    protected void startup(DocLoader loader) {
	    m_docs = loader.loadDocs();
	if (m_iD < m_docs.size())
	    m_examples = getBIOExamples(m_docs.get(m_iD));
	else
	    m_examples = new ArrayList<BIOExample>();
    }

    /**
     * Called immediately before next returns null.
     * Currently does nothing, but can be used to save caches or
     * record statistics. 
     */
    protected void cleanup() {
	//Uncomment this line to enable saving of WordNet
	//as serialized file to speed results when building
	//multiple classifiers sequentially as separate java processes:

	//WordNetTools.saveWN();
    }
}
