package edu.illinois.cs.cogcomp.lbj.coref.features;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CaseExample;


/** Features related to case. */
public class Case {

    /** Construction of this static feature library should not be necessary. */
    protected Case() {
    }
    
    /** 
     * Given a word in a document,
     * returns that word in the case that {@code caser} predicts it should be.
     * @param d A document giving the context for the word.
     * @param wordNum The position in the document of the word in question.
     * @param caser A classifier that takes a {@link CaseExample}
     * returns "allUpper", "firstCap", or "other".
     * @return The specified word in the appropriate case.
     */
    public static String getCasedWord(Doc d, int wordNum, Classifier caser) {
	String casedWord = d.getWords().get(wordNum).toLowerCase();
	String predCase = caser.discreteValue(new CaseExample(d, wordNum));
	if (predCase.equals("allUpper")) {
	    casedWord = casedWord.toUpperCase();
	} else if (predCase.equals("firstCap")) {
	    casedWord = casedWord.substring(0,1).toUpperCase()
			 + casedWord.substring(1);
	} else if (predCase.equals("other")) {
	    casedWord = casedWord.substring(0,1)
			 + casedWord.substring(1).toUpperCase();
	}
	return casedWord;
    }
}
