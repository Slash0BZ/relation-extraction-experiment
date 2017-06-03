package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.features.GigaWord;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocPlainTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/**
  * Represents a Doc constructed from plain text.
  * <p>
  * To load a document from a string, construct using the no-arg constructor
  * and then call {@link #loadFromPlainText}.
  * To load the document including mention detection, see
  * {@link DocFromTextLoader}
  * </p><p>
  * To load a document given the name of a plain text file,
  * see {@link #DocPlainText(String)}.
  * To load the document including mention detection, see
  * {@link DocPlainTextLoader}.
  * </p>
  * @author Eric Bengtson
 */
public class DocPlainText extends DocTextAnnotation implements Doc {
  private static final long serialVersionUID = 1L;

  /**
    * Constructs an empty document.
    * This constructor can be used, followed by 
    * {@link #loadFromPlainText}
    * to construct a document from a text string.
   */
  public DocPlainText() {
  }

  /**
    * Constructs a document using the specified plain text file.
    * * Automatically splits sentences, determines quote levels,
    * determines part-of-speech tags, and splits words using
    * an automatic word-splitting algorithm.
    * Mentions and entities will not be set here.
    * @param filename The name of the specified file.
   */
  public DocPlainText(String filename) {
    super();
    loadFromFilename(filename);
  }
  
  public DocPlainText(String filename, boolean doPos) {
	    super();
	    loadFromFilename(filename, doPos);
	  }
  public DocPlainText(TextAnnotation ta){
	  super();
	  loadFromTA(ta);
  }

/**
    * Builds this document from the specified plain text file.
    * Automatically splits sentences, determines quote levels,
    * determines part-of-speech tags, and splits words using
    * an automatic word-splitting algorithm.
    * Mentions and entities will not be set here.
    * @param filename The name of a file containing plain text.
   */
  public void loadFromFilename(String filename) {
    //Load text and sentence split:
    String plainText = (new myIO()).readAll(filename);
    this.loadFromText(plainText, /*split*/ true, /*POS*/ false);
    String posTaggedText = loadPOSTaggerOutput();
    if (posTaggedText != null) {
      loadPOSTags(posTaggedText);
    } else {
      System.err.println(
          "Cannot use SNoW-based POS tagger in separate process."
          + " Check PATH_POS and be sure it is exported.");
      //Backoff:
      this.loadFromText(plainText, /*split*/ true, /*POS*/ true);
    }
    initTextAnnotation();
    m_baseFN = filename;
  }

  public void loadFromFilename(String filename, boolean doPOS) {
	  if(doPOS == false){
		  loadFromFilename(filename);
		  return;
	  }
	  String plainText = (new myIO()).readAll(filename);
	    this.loadFromText(plainText, /*split*/ true, true);
	  initTextAnnotation();
	  m_baseFN = filename;
  }
  /**
    * Builds the document from the given plain text,
    * automatically splitting sentences, determining quote levels,
    * determining part-of-speech tags, and splitting words by
    * an automatic word-splitting algorithm.
    * Mentions and entities will not be set here.
    * @param text The text of the document.
   */
  public void loadFromPlainText(String text) {
    loadFromText(text);
  }
  /**
    * Builds the document from the given plain text,
    * automatically splitting sentences, determining quote levels,
    * determining part-of-speech tags, and either splitting words
    * by whitespace or using a word-splitter.
    * Mentions and entities will not be set here.
    * @param text The text of the document.
    * @param doWordSplit If true, words will be split by
    * an automatic word-splitting algorithm; otherwise
    * words will be assumed to be separated by whitespace.
   */
  public void loadFromPlainText(String text, boolean doWordSplit) {
    //loadFromText(text, doWordSplit, /*POS*/true);
	setPlainText(text);
    initTextAnnotation();
    setWordsFromTA();
  }

  public void loadFromTA(TextAnnotation ta){
      setUsePredictedMentions(true);
	  setTextAnnotation(ta);
	  setWordsFromTA();
  }

  @Override
    public void write(String filename, boolean usePredictions) {
      //FIXME: Add saving ability.

    }

  @Override
  public void setGigaWord(GigaWord gw) {}

  public void setDocID(String docID){ m_docID = docID; }
}
