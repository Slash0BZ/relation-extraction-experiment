package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors.IntroExEx;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.IntroExample;


/** @author Eric Bengtson */

/** TODO: Refactor this code, its parents and siblings. */
public class IntroParserIterator implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
	private String[] m_filename;
	IntroExEx m_exExtractor;
	private List<IntroExample> m_examples;
	private int m_iD = 0;
	private int m_iX = 0;
	private Map<String,Integer> CorpusWordCount = null;
	DocLoader loader = null;

	public IntroParserIterator(DocLoader loader, IntroExEx extractor, String CorpusWordCount) {
		    m_exExtractor = extractor;
		    this.loader = loader; 
		    loader.setM_preWordCount(CorpusWordCount);
		    this.startup(loader);
		  }
	/** Adds no mentionDetectors, and therefore true mentions will be used. */
	public IntroParserIterator(String fileListFN, IntroExEx extractor) {
		m_exExtractor = extractor;
		loader = DocLoader.getDefaultLoader(fileListFN);
		this.startup(loader);
	}

	public IntroParserIterator(DocLoader loader, IntroExEx extractor) {
		m_exExtractor = extractor;    	
		this.startup(loader);
	}

	public IntroExample next() {
		while (m_iD < m_filename.length && m_examples.size() == 0) {
			System.err.println("Zero Examples in doc " + m_iD);
			this.advanceDoc();
		}
		if (m_iD >= m_filename.length) { //Finished with last example.  Cleanup
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
		m_iX = 0;
		if (m_iD < m_filename.length)
			m_examples = getExamples(m_iD);
		while (m_iD < m_filename.length && m_examples.size() == 0) {
			m_iD++;
			if (m_iD < m_filename.length)
				m_examples = getExamples(m_iD);
			this.resetDoc();
		}
	}

	private void resetDoc() {
		m_iX = 0;
	    if (m_iD < m_filename.length)
	        m_examples = getExamples(m_iD);
	      else
	        m_examples = new ArrayList<IntroExample>();
	}

	private List<IntroExample> getExamples(int iD) {
		String filenameBase = m_filename[m_iD];
		//System.err.println("Loading " + filenameBase);
		Doc d = loader.loadDoc(filenameBase);
		//	Doc d = m_docs.get(m_iD);
		m_exExtractor.setDoc(d);
		return m_exExtractor.getExamples();
	}

	public void reset() {
		m_iD = 0;
		m_iX = 0;
		this.resetDoc();
	}

	public void close() { reset(); }

	public void enqueue(Object q) {
		//Note: Does nothing.
	}

	protected void startup(DocLoader loader) {
		if(Parameters.PreWordCount == null)
			System.err.println("WARNING!!! THE WORDCOUNT DOESN'T SET");
		else{
			try {
				ObjectInputStream oos = new ObjectInputStream(new FileInputStream(Parameters.PreWordCount));
				CorpusWordCount =(Map<String,Integer>)oos.readObject();
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_filename = loader.getFilenames();
		m_iD = 0;
		resetDoc(); //Loads examples.
		if (m_examples.size() == 0)
			System.err.println("Doc " + m_iD + " has no examples.");
	}
	//List<Classifier> mds = new ArrayList<Classifier>();
	//	m_docs = loader.loadDocs();
	//	if (m_docs.size() == 0)
	//    System.err.println("No Docs found.");
	//	m_examples = getExamples(m_iD);
	//    }

	protected void cleanup() {
	}


}
