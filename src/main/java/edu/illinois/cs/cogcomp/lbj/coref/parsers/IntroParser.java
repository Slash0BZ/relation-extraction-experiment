package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors.IntroExEx;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.IntroExample;


/** @author Eric Bengtson */

/** TODO: Refactor this code, its parents and siblings. */
public class IntroParser implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
    private List<Doc> m_docs;
    IntroExEx m_exExtractor;
    private List<IntroExample> m_examples;
    private int m_iD = 0;
    private int m_iX = 0;

    /** Adds no mentionDetectors, and therefore true mentions will be used. */
    public IntroParser(String fileListFN, IntroExEx extractor) {
	m_exExtractor = extractor;
	DocLoader loader = DocLoader.getDefaultLoader(fileListFN);
	this.startup(loader);
    }

    public IntroParser(DocLoader loader, IntroExEx extractor) {
    	m_exExtractor = extractor;    	
    	this.startup(loader);
    }
    
    public IntroExample next() {
	while (m_iD < m_docs.size() && m_examples.size() == 0) {
	    System.err.println("Zero Examples in doc " + m_iD);
	    this.advanceDoc();
	}
	if (m_iD >= m_docs.size()) { //Finished with last example.  Cleanup
	    this.cleanup();
	    return null;
	}
	if (m_iX >= m_examples.size()) {
	    this.advanceDoc();
	}
	IntroExample ex = this.getNextExample();
	return ex;
    }

    private IntroExample getNextExample() {
	IntroExample ex = m_examples.get(m_iX);
	m_iX++;
	return ex;
    }

    private void advanceDoc() {
	m_iD++;
	this.resetDoc();
	if (m_iD < m_docs.size())
	    m_examples = getExamples(m_iD);
	while (m_iD < m_docs.size() && m_examples.size() == 0) {
	    m_iD++;
	    if (m_iD < m_docs.size())
		m_examples = getExamples(m_iD);
	    this.resetDoc();
	}
    }

    private void resetDoc() {
	m_iX = 0;
    }

    private List<IntroExample> getExamples(int iD) {
	Doc d = m_docs.get(m_iD);
	m_exExtractor.setDoc(d);
	return m_exExtractor.getExamples();
    }

    public void reset() {
	m_iD = 0;
	m_iX = 0;
    }

    public void close() { reset(); }

    public void enqueue(Object q) {
	//Note: Does nothing.
    }

    protected void startup(DocLoader loader) {
	//List<Classifier> mds = new ArrayList<Classifier>();
	m_docs = loader.loadDocs();
	if (m_docs.size() == 0)
	    System.err.println("No Docs found.");
	m_examples = getExamples(m_iD);
    }

    protected void cleanup() {
    }


}
