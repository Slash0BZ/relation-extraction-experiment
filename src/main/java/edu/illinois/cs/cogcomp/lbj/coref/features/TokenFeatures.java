package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;

/** 
 * Collection of feature generating functions that return tokens (word strings)
 * in or around the mentions of a CExample.
 */
public class TokenFeatures {
	/** If the threshold is set larger than 1, 
		the token will never be set as rare */
	private static double threshold = 0.4;
    /** Should not need to construct this static feature collection. */
    protected TokenFeatures() {
    }
    private static Map<String, Integer> s_goldHeadLastWordCount = null;
    
    /**
     * Get the pairs of words preceding the heads,
     * by conjoining the ordered pair of words with "_AND_".
     * Rare words are replaced with "_RARE_",
     * and if both words are rare the string is "_Rare_Duplicate". 
     * @param ex The example whose mentions will be processed.
     * @return An array of strings containing conjoined pairs of words.
     */
    public static String[] preWordPairs(CExample ex) {
	List<String> results = new ArrayList<String>();
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	Doc d = ex.getDoc();
	int s1 = m1.getExtentFirstWordNum();
	int e1 = m1.getHeadFirstWordNum() - 1;
	for (int i = s1; i <= e1; ++i) {
	    String w1 = d.getWord(i);
	    boolean r1 = (d.getInCorpusInverseFreq(w1) > getThreshold());
	    String rw1 = w1;
	    if (r1) rw1 = "_RARE_";
	    
	    int s2 = m2.getExtentFirstWordNum();
	    int e2 = m2.getHeadFirstWordNum() - 1;
	    for (int j=s2; j <= e2; ++j) {
		String w2 = d.getWord(j);
		boolean r2 = (d.getInCorpusInverseFreq(w2) > getThreshold());
		String rw2 = w2;
		if (r2) rw2 = "_RARE_";
		if (r1 && r2 && w1.equals(w2)) {
		    results.add("_Rare_Duplicate_");
		} else {
		    results.add(rw1 + "_AND_" + rw2);
		}
	    }
	}
	return (String[]) results.toArray();
    }
    
    /** 
     * Gets the set of all words that are contained in both mentions.
     * @param ex The example whose mentions are processed.
     * @param useHead Should the heads or the extents of the mentions be used?
     * @return An array view of the set of all shared words.
     */
    public static String[] getSharedWords(CExample ex, boolean useHead) {
	Set<String> results = new HashSet<String>();
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	String[] m1Words = m1Text.split("\\s+");
	String[] m2Words = m2Text.split("\\s+");

	Set<String> words = new HashSet<String>();
	for (int i = 0; i < m1Words.length; ++i) {
	    String w = m1Words[i].toLowerCase();
	    if (!Gazetteers.getStopWords().contains(w))
		words.add(w);
	}
	for (int i = 0; i < m2Words.length; ++i) {
	    String word2 = m2Words[i].toLowerCase();
	    if (words.contains(word2))
		results.add(word2);
	}
	return (String[]) results.toArray();
    }
 
    /**
     * Gets the last word of each mention, conjoined by "_AND_".
     * If either word is rare, "_RARE_" is substituted.
     * If both words are rare, the result is "_Rare_Duplicate".
     * @param ex The example whose words are retrieved.
     * @param useHead Whether the last word of the head or the extent
     * should be retrieved.
     * @return The string containing the last word of each mention,
     * conjoined with "_AND_".
     */
    public static String lastWordPair(CExample ex, boolean useHead) {
	
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	Doc d = ex.getDoc();
	
	int i;
	if (useHead) i = m1.getHeadLastWordNum();
	else i = m1.getExtentLastWordNum();
	
	String w1 = d.getWord(i);
	boolean r1 = (d.getInCorpusInverseFreq(w1) > getThreshold());
	String rw1 = w1;
	if (r1) rw1 = "_RARE_";
	
	int j;
	if (useHead) j = m2.getHeadLastWordNum();
	else j = m2.getExtentLastWordNum();
	
	String w2 = d.getWord(j);
	boolean r2 = (d.getInCorpusInverseFreq(w2) > getThreshold());
	String rw2 = w2;
	if (r2) rw2 = "_RARE_";

	if (r1 && r2 && w1.equals(w2)) {
	    return "_Rare_Duplicate_";
	} else {
	   if(rw1.compareTo(rw2) > 0)
			return rw1 + "_AND_" + rw2;
		else
			return rw2 + "_AND_" + rw1;
	}
    }

    /**
     * Gets the mention types of both mentions, conjoined by {@literal "&&"},
     * except that if the second mention is a pronoun, the last word
     * of its head is substituted for its mention type. 
     * @param ex The example whose types are retrieved.
     * @return A string containing the mention types
     * conjoined by {@literal "&&"}, except that if the second mention
     * is a pronoun, the last word of its head replaces its type. 
     */
    public static String mTypeProWord(CExample ex) {
        Mention m1 = ex.getM1(), m2 = ex.getM2();
        Doc d = ex.getDoc();
    
        String w1 = m1.getType();
    
        int j = m2.getHeadLastWordNum();
        String w2 = m2.getType();
        if (w2.equals("PRO"))
	    w2 = d.getWord(j);
    
        return w1 + "&&" + w2;
    }

    /**
     * Gets the last word of each mention's head, where each word
     * by conjoining the ordered pair of words with "_AND_".
     * Rare words are replaced with "_RARE_",
     * and if both words are rare the string is "_Rare_Duplicate". 
     * @param ex The example whose mentions will be processed.
     * @return The conjoined pairs of words.
     */
    public static String getLastHeadWordPair(CExample ex) {
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	Doc d = ex.getDoc();

	int i = m1.getHeadLastWordNum();

	String w1 = d.getWord(i);
	boolean r1 = (d.getInCorpusInverseFreq(w1) > getThreshold());
	String rw1 = w1;
	if (r1) rw1 = "_RARE_";

	int j = m2.getHeadLastWordNum();

	String w2 = d.getWord(j);
	boolean r2 = (d.getInCorpusInverseFreq(w2) > getThreshold());
	String rw2 = w2;
	if (r2) rw2 = "_RARE_";

	if (r1 && r2 && w1.equals(w2)) {
	    return "_Rare_Duplicate_";
	} else {
	    if(rw1.compareTo(rw2) > 0)
			return rw1 + "_AND_" + rw2;
		else
			return rw2 + "_AND_" + rw1;
	}
    }

	/**
	 * If the threshold is set to larger than 1,
	 * the token will never be set to rare.
	 * @param threshold
	 */
	public static void setThreshold(double threshold) {
		TokenFeatures.threshold = threshold;
	}

	public static double getThreshold() {
		return threshold;
	}
	public static String getLastHeadWordPositivePair(CExample ex) {
    	if(s_goldHeadLastWordCount == null){
    		s_goldHeadLastWordCount = (Map<String, Integer>) myAux.loadObject(Parameters.pathToGoldHeadLastWordPairCount);
    	}
    	Mention m1 = ex.getM1(), m2 = ex.getM2();
    	Doc d = ex.getDoc();

    	int i = m1.getHeadLastWordNum();
    	int j = m2.getHeadLastWordNum();
    	
    	String w1 = d.getWord(i);
    	String w2 = d.getWord(j);
    	String wordPair = w1 + "_AND_" + w2;
    	if(w1.compareTo(w2)<1)
			wordPair =  w2 + "_AND_" + w1;
    	if(s_goldHeadLastWordCount.containsKey(wordPair)){
    		return wordPair;
    	}
    	else
    		return "_NOTAPPEAR_";
    }
}
