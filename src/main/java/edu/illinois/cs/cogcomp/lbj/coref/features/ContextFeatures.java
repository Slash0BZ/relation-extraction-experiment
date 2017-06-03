package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.*;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/**
 * Collection of features related to the context of one or more mentions.
 * Especially see {@link #bothSpeakNearby}.
 */
public class ContextFeatures {

    /** Should not need to construct this collection of static features. */
    protected ContextFeatures() {
    }

    /**
     * Determine whether both mentions have a word that is synonymous for "say"
     * within {@code size} words of their extents.
     * @param ex The example containing the mentions to examine.
     * @param size The window size: the distance from the extent
     * in which a word meaning "say" will be detected.
     * @return Whether both mentions have a word meaning "say" nearby.
     */
    public static boolean bothSpeakNearby(CExample ex, int size) {
    	return isSpeaker(ex.getM1(),size) && isSpeaker(ex.getM2(),size);
	/*Doc d = ex.getDoc();
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	int numWords = d.getWords().size();
	int s = Math.max(m1.getExtentFirstWordNum() - size, 0);
	int e = Math.max(s + size - 1, 0);
	boolean found1 = contextWordInSet(Gazetteers.getSayWords(), d, s, e);
	s = Math.min(m1.getExtentLastWordNum() + 1, numWords - 1);
	e = Math.min(s + size - 1, numWords - 1);
	found1 = found1 || contextWordInSet(Gazetteers.getSayWords(), d, s, e);

	s = Math.max(m2.getExtentFirstWordNum() - size, 0);
	e = Math.max(s + size - 1, 0);
	boolean found2 = contextWordInSet(Gazetteers.getSayWords(), d, s, e);
	s = Math.min(m2.getExtentLastWordNum() + 1, numWords - 1);
	e = Math.min(s + size - 1, numWords - 1);
	found2 = found2 || contextWordInSet(Gazetteers.getSayWords(), d, s, e);
	
	return found1 && found2;*/
    }

    /**
     * Determine whether both mentions have a word that is synonymous for "say"
     * within {@code size} words of their extents.
     * @param ex The example containing the mentions to examine.
     * @param size The window size: the distance from the extent
     * in which a word meaning "say" will be detected.
     * @return Whether both mentions have a word meaning "say" nearby.
     */
    public static boolean ASpeakB(CExample ex, int size) {
	Doc d = ex.getDoc();
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	if(isQuoted(m1) && isSpeaker(m2, size)){
		int countQuote = 0;
		int countPeriod = 0;
		for(int i = m1.getExtentLastWordNum(); i < m2.getExtentFirstWordNum(); i++){
			String sWord = d.getWord(i);
			if (sWord.equals("\"") || sWord.equals("\'\'")
			          || sWord.equals("``"))
				countQuote++;
			if(countQuote > 0 && sWord.equals(".")){
				countPeriod++;
			}
		}
		if(countQuote == 1 && countPeriod < 1)
			return true;
	}
	if(isSpeaker(m1, size) && isQuoted(m2)){
		int countQuote = 0;
		int countPeriod = 0;
		for(int i = m1.getExtentLastWordNum(); i < m2.getExtentFirstWordNum(); i++){
			String sWord = d.getWord(i);
			if(countQuote == 0 && sWord.equals(".")){
				countPeriod++;
			}
			if (sWord.equals("\"") || sWord.equals("\'\'")
			          ||sWord.equals("``"))
				countQuote++;
		}
		if(countQuote == 1 && countPeriod < 1)
			return true;
	}	
	return false;
    }
    
    /**
     * Determine whether a mention has a word that is synomymous for "say"
     * @param m Mention
     * @param size The window size: the distance from the extent
     * in which a word meaning "say" will be detected.
     * @return Whether both mentions have a word meaning "say" nearby.
     */
    public static boolean isSpeaker(Mention m, int size) {
    	Doc d = m.getDoc();
    	int numWords = d.getWords().size();
    	int s = Math.max(m.getExtentFirstWordNum() - size, 0);
    	int e = m.getExtentFirstWordNum();
    	boolean found = false;
    	for (int i = e; i >= s;  --i) {
    		String sWord = d.getWord(i).toLowerCase();
    		if (sWord.equals("\"") || sWord.equals("\'\'")
			          || sWord.equals("``"))
    			break;
    	    Set<String> words = Gazetteers.getSayWords();
    		if (words.contains(sWord))
    	    	found = true;
    	}
    	s = m.getExtentLastWordNum();
    	e = Math.min(s + size - 1, numWords - 1);
    	for (int i = s; i <= e; ++i) {
    		String sWord = d.getWord(i).toLowerCase();
    		if (sWord.equals("\"") || sWord.equals("\'\'")
			          || sWord.equals("\'")|sWord.equals("``"))
    			break;
    	    Set<String> words = Gazetteers.getSayWords();
    		if (words.contains(sWord))
    	    	found = true;
    	}
    	return  found;
    }
    
    public static String nextPos(Mention m){
    	if(m.getExtentLastWordNum()+1 < m.getDoc().getWords().size())
    		return m.getDoc().getPOS(m.getExtentLastWordNum()+1);
    	return "NONE";
    }
    public static String previousPos(Mention m){
    	if(m.getExtentFirstWordNum() -1 >=0)
    		return m.getDoc().getPOS(m.getExtentFirstWordNum()-1);
    	return 
    		"NONE";
    }
    
    /**
     * Determine whether a mention is in quote
     * @param m Mention
     * @return Whether a mention is in quote.
     */
    public static boolean isQuoted(Mention m) {
    	if(m.getDoc().getQuoteNestLevel(m.getHeadFirstWordNum()) == 1)    		
    		return true;
		return false;
    }
    /* Utilities */

    /**
     * Determines whether a word in the document numbered between
     * {@code s} and {@code e} inclusive is contained in {@code words}.
     * @param words The set of words.
     * @param d The document whose words will be inspected.
     * @param s The number of the first word in the document to be inspected.
     * @param e The number of the last word in the document to be inspected.
     */
    protected static boolean contextWordInSet(
     Set<String> words, Doc d, int s, int e) {

	for (int i = s; i <= e; ++i) {
	    if (words.contains(d.getWord(i).toLowerCase()))
		return true;
	}
	return false;
    }
    

}
