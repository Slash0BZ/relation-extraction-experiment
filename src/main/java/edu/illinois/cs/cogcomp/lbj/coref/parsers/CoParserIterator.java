package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors.CExExClosestPosAllNeg;
import edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors.CExampleExtractor;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Maps;


//TODO: Refactor the edu.illinois.cs.cogcomp.lbjava Parser related code out
//and delegate to CExampleExtractorBasic instead.

/* If no mentionDetector(s) specified, true mentions will be used. */
/**
  * Extracts coreference examples for use in training an LBJ classifier.
  * The examples are extracted from a corpus of documents specified
  * either by providing a file name containing a list of document filenames
  * or by providing a document loader.
  * From each document, examples are extracted according to the specified
  * example extractor.
  * See the various constructors for details.
  * To extract examples, repeatedly call the next method until it returns null.
  * @author Eric Bengtson
 */
public class CoParserIterator implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {

  //Members:
  private String[] m_filename;
  private CExampleExtractor m_cExExtractor;
  private List<CExample> m_examples;
  private int m_iD = 0;
  private int m_iX = 0;
  private Map<String,Integer> CorpusWordCount = null;
  DocLoader loader = null;

  //Constructors:

  /**
    * Constructs a Parser that extracts coreference examples from a corpus
    * loaded using the default document loader,
    * and examples extracted using the default example extractor.
    * The default example extractor is currently
    * {@link CExExClosestPosAllNeg}, which loads examples as follows:
    * For each mention, creates a positive example with the nearest preceding
    * coreferential mention, and creates negative examples with each preceding
    * non-coreferential mention.
    * Does not include any cataphoric examples (examples where
    * a pronoun precedes a non-pronoun).
    * @param fileListFN The classpath-relative filename of the corpus file,
    * containing a list of document filenames, one per line.
    * Each filename should be specified relative to
    * a location in the classpath.
   */
  public CoParserIterator(String fileListFN) {
    this(fileListFN, 
         new CExExClosestPosAllNeg(false, false, false, true)
        );
  }

  public CoParserIterator(DocLoader loader, CExampleExtractor extractor, String CorpusWordCount) {
	    m_cExExtractor = extractor;
	    this.loader = loader; 
	    loader.setM_preWordCount(CorpusWordCount);
	    this.startup();
	  }
  /**
    * Constructs a Parser that extracts coreference examples from a corpus,
    * with documents loaded by a specified document loader
    * and coreference examples extracted from each document
    * using the specified example extractor.
    * @param loader A document loader that loads a corpus of documents.
    * @param extractor An coreference example extractor.
   */
  public CoParserIterator(DocLoader loader, CExampleExtractor extractor) {
    m_cExExtractor = extractor;
    this.loader = loader; 
    this.startup();
  }

  /**
    * Constructs a Parser that extracts coreference examples
    * from a corpus loaded using the default document loader
    * as specified by {@link DocLoader#getDefaultLoader},
    * and examples extracted using the specified example extractor.
    * @param fileListFN The classpath-relative filename of the corpus file,
    * containing a list of document filenames, one per line.
    * Each filename should be specified relative to
    * a location in the classpath.
    * @param extractor An coreference example extractor.
   */
  public CoParserIterator(String fileListFN, CExampleExtractor extractor) {
    m_cExExtractor = extractor;
    DocLoader loader = DocLoader.getDefaultLoader(fileListFN);
    this.startup();
  }



  //edu.illinois.cs.cogcomp.lbjava.parse.Parser methods:

  /**
    * Gets the next coreference example, or null if no more examples remain.
    * @return The next coreference example or null if none remain.
   */
  public CExample next() {
    //Ensure doc has next example, or past last doc.
    while (m_iD < m_filename.length && m_iX >= m_examples.size()) {
      if (m_iX == 0) {
        System.err.println("Zero CExamples in doc " + m_iD);
      }
      this.advanceDoc();
    }
    //If past last doc, we are done.
    if (m_iD >= m_filename.length) { //Finished with last example.  Cleanup
      this.cleanup();
      return null;
    }
    return this.getNextExample();
  }

  /**
    * Resets the parser to the first document in the corpus and resets
    * the example extractor.
    * It is not necessary to call this method before the first call to next.
   */
  public void reset() {
    m_iD = 0;
    m_cExExtractor.reset();
    this.resetDoc();
  }


  public void close() { m_cExExtractor.close(); reset(); }

  /**
    * Does nothing
    * @param q An arbitrary object.
   */
  public void enqueue(Object q) {
    //Note: Does nothing.
  }



  //Private methods:

  /**
    * Gets an example from the cache and prepares for the next example.
    * Call only after {@code m_examples} is initialized and when {@code m_iX}
    * is less than the size of {@code m_examples}
    * @return The next example (but never null).
   */
  private CExample getNextExample() {
    CExample ex = m_examples.get(m_iX);
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
    if (m_iD < m_filename.length)
      this.resetDoc();
  }

  /**
    * Resets the document, including caching the examples from the
    * example extractor.
    * Safe to call even if document is empty or does not exist.
   */
  private void resetDoc() {
    m_iX = 0;
    if (m_iD < m_filename.length)
      m_examples = getExamples();
    else
      m_examples = new ArrayList<CExample>();
  }

  /**
    * Load all examples from the example extractor.
    * This includes calling setDoc on the example extractor
    * to set the doc to the current document
    * (as indicated by {@code m_iD}).
    * Should not be called if the document does not exist.
    * @return A list of the examples, in the order returned by the
    * example extractor.
   */
  private List<CExample> getExamples() {
    //Doc d = m_docs.get(m_iD);
    String filenameBase = m_filename[m_iD];
    //System.err.println("Loading " + filenameBase);
    Doc d = loader.loadDoc(filenameBase);
    d.setCorpusCounts(CorpusWordCount);
    m_cExExtractor.setDoc(d);
    return m_cExExtractor.getExamples();
  }



  //Protected startup and cleanup methods:

  /**
    * Prepares the parser, by loading documents and resetting the doc.
    * @param loader The loader from which to get the documents.
   */
  protected void startup() {
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
