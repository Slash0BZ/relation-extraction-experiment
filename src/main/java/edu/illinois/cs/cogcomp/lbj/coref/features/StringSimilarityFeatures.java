package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;

import edu.illinois.cs.cogcomp.lbj.coref.util.collections.MySets;

//import edu.illinois.cs.cogcomp.lbj.coref.clustering.rulers.SecondStringSoftTFIDFRuler;

/**
 * A collection of features relating to the similarity of strings.
 */
public class StringSimilarityFeatures {
    
    /** Should not need to construct this static feature library. */
    protected StringSimilarityFeatures() {
    }


    //Substrings:
    
    /** 
     * Determines whether one mention's text is a substring of the other's.
     * Uses case insensitive comparison.
     * @param ex The example whose mentions' text will be compared.
     * @param useHead if true, compare head text,
     * otherwise compare extent text.
     */
    public static boolean substring(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	return m1Text.contains(m2Text) || m2Text.contains(m1Text);	    
    }
  
    /** 
     * Determines whether one mention's text begins with
     * the text of the other mention.
     * Uses case insensitive comparison.
     * @param ex The example whose mentions' text will be compared.
     * @param useHead if true, compare head text,
     * otherwise compare extent text.
     */
    public static boolean leftSubstring(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	return m1Text.startsWith(m2Text) || m2Text.startsWith(m1Text);
    }

    /** 
     * Determines whether one mention's text ends with
     * the text of the other mention.
     * Uses case insensitive comparison.
     * @param ex The example whose mentions' text will be compared.
     * @param useHead if true, compare head text,
     * otherwise compare extent text.
     */
    public static boolean rightSubstring(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	return m1Text.endsWith(m2Text) || m2Text.endsWith(m1Text); 
    }
    
    
    //Exact Match:
    
    /**
     * Determines whether the text (either the heads or the extents)
     * of the mentions match, after dropping stop words.
     * @param ex The example whose mentions will be compared.
     * @param useHead if true, compare head text,
     * otherwise compare extent text.
     */
    public static boolean textMatchSoon(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
        String[] soonStop = {"a","an","the","this","that","these","those"};
        return StringSimilarityFeatures.stringsMatchByWords(m1Text, m2Text, soonStop);
    }
    
    /**
     * Determines whether the last word of one mention matches
     * the last word of another mention.
     * Uses case insensitive comparison.
     * @param ex The example whose mentions' text will be compared.
     * @param useHead if true, compare head text,
     * otherwise compare extent text.
     */
    public static boolean doLastWordsMatch(CExample ex, boolean useHead) {
	int i, j;
	if (useHead) {
	    i = ex.getM1().getHeadLastWordNum();
	    j = ex.getM2().getHeadLastWordNum();
	} else {
	    i = ex.getM1().getExtentLastWordNum();
	    j = ex.getM2().getExtentLastWordNum();
	}
	String m1LastWord = ex.getDoc().getWord(i);
	String m2LastWord = ex.getDoc().getWord(j);
	return ( m1LastWord.equalsIgnoreCase(m2LastWord) );
    }
    
   

    /** 
     * Determines whether any words of one mention preceding its head
     * match any words of the other mention that precede or are contained in the head.
     * @param ex The example whose mentions will be compared.
     * @return Whether a pre-head modifier matches another pre-head modifier or head word.
     */
    public static boolean prenominalModifierWordMatchAnotherOrHeadWord(
     CExample ex) {
        Mention m1 = ex.getM1();
        Mention m2 = ex.getM2();
        if (prenominalModifierWordMatchAnotherOrHeadWord(m1, m2))
            return true;
        else if (prenominalModifierWordMatchAnotherOrHeadWord(m2, m1))
            return true;
        else
            return false;
    }


    /** 
     * Determines whether a noun preceding the head of {@code m1}
     * matches a noun preceding or in the head of {@code m2}
     * No assumptions are made about the textual order
     * of {@code m1} and {@code m2}.
     * Uses POS tags to determine whether words are nouns.
     * @param m1 The first mention.
     * @param m2 The second mention.
     * @return true if any nouns from {@code m1} that precede its head
     * match any nouns from {@code m2} preceding or in its head.
     */
    public static boolean prenominalModifierWordMatchAnotherOrHeadWord(
     Mention m1, Mention m2) {
        int e1Start = m1.getExtentFirstWordNum();
        int e2Start = m2.getExtentFirstWordNum();
        int h1Start = m1.getHeadFirstWordNum();
        int h2Start = m2.getHeadFirstWordNum();
        int h2End = m2.getHeadLastWordNum();
    
        Set<String> preMods1 = PartOfSpeechTools.getLCNounsBetween(
        	m1.getDoc(), e1Start, h1Start - 1);
        Set<String> preMods2 = PartOfSpeechTools.getLCNounsBetween(
        	m2.getDoc(), e2Start, h2Start - 1);
        if (!Collections.disjoint(preMods1, preMods2))
            return true;
        Doc d2 = m2.getDoc();
        for (int i = h2Start; i <= h2End; ++i) {
            String wordI = d2.getWord(i).toLowerCase();
            String POSI = d2.getPOS(i).toLowerCase();
            if ( POSI.startsWith("n") && preMods1.contains(wordI) )
        	return true;
        }
        return false;
    }


    /**
     * Determines whether the sequence of words in one mention
     * is a subsequence of the words in another mention.
     * Uses case-insensitive comparisons.
     * @param ex The example whose mentions will be compared.
     * @param useHead Whether heads or extents should be compared.
     * @return Whether one mention's words
     * are a subsequence of another mention's.
     */
    public static boolean subsequence(CExample ex, boolean useHead) {
        String a = StringTools.getLCText(ex.getM1(), useHead);
        String b = StringTools.getLCText(ex.getM2(), useHead);
        return subsequence(a, b) || subsequence(b, a);
    }


    /**
     * Determines whether the sequence of words in {@code big}
     * is a subsequence of the words in {@code small}.
     * Words are split using whitespace ({@literal "\s+"})
     * and compared using a case-insensitive comparison.
     * @param big The bigger string.
     * @param small The smaller string.
     * @return Whether one string's words are a subsequence
     * of another strings's.
     */
    public static boolean subsequence(String big, String small) {
        String[] as = small.split("\\s+"), bs = big.split("\\s+");
        if (as.length == 0) return true;
        int aN = 0;
        String a = as[aN];
        for (String b : bs) {
            if (a.equalsIgnoreCase(b)) {
        	aN++;
            }
            if (aN >= as.length) {
        	return true; //past all words.
            } else {
        	a = as[aN];
            }
        }
        //Finished all bs and haven't eaten last a, so not substring:
        return false;
    }


    /**
     * Determines the number of capitalized words occur in exactly one mention.
     * Uses CASE-SENSITIVE comparisons.
     * @param ex The example whose mentions will be compared.
     * @return The number of capitalized words appearing in exactly one mention.
     */
    public static int getNumDiffCapitalizedWords(CExample ex) {
	boolean head = false;
        Set<String> words1 = StringTools.getCapitalizedWords(ex.getM1(), head);
        Set<String> words2 = StringTools.getCapitalizedWords(ex.getM2(), head);
    
        //TODO: Optimize?
        Set<String> inter = MySets.getIntersection(words1, words2);
        Set<String> both = MySets.getUnion(words1, words2);
    
        Set<String> diff = new HashSet<String>(both);
        diff.removeAll(inter);
    
        return diff.size();
    }


    /**
     * Determines whether two strings contain the same sequence
     * of words, after dropping {@code ignoreWords}.
     * Note: Words are split by single whitespace characters ({@literal \s})
     * rather than by one or more whitespace characters ({@literal \s+})
     * for backwards compatibility.
     * @param s1 The first string.
     * @param s2 The second string.
     * @param ignoreWords The words to be ignored.
     * The words should be supplied in the lowercase.
     */
    public static boolean stringsMatchByWords(String s1, String s2, 
     String[] ignoreWords) {
	//TODO: Try version using \\s+, but preserve this for compatibility.
	//NOTE: Using \\s instead of \\s+ for backwards compatibility.
        String[] arr1 = s1.toLowerCase().split("\\s");
        String[] arr2 = s2.toLowerCase().split("\\s");
        Set<String> drop = new HashSet<String>(Arrays.asList(ignoreWords));
        List<String> words1 = new ArrayList<String>();
        for (String word : arr1)
            if (!drop.contains(word))
        	words1.add(word);
        List<String> words2 = new ArrayList<String>();
        for (String word : arr2)
            if (!drop.contains(word))
        	words2.add(word);
        return words1.equals(words2);
    }

    
    //String Edit Distances:
    
    /** Cohen word overlap distance,
        overlap weighted by edit distance of words
        and frequency of words in doc, unnormalized. */
    /* Removed until verified
    public static double getCohenDist(CExample ex, boolean useHead) {
        //OPTIMIZE: cache ruler.
	//TODO: Verify correctness or remove.
        Doc d = ex.getDoc();
        SecondStringSoftTFIDFRuler ruler = new SecondStringSoftTFIDFRuler(d);
        String a = StringTools.getLCText(ex.getM1(), useHead);
        String b = StringTools.getLCText(ex.getM2(), useHead);
        return ruler.getDist(a, b);
    }
    */

    /** 
     * Gets the character-based edit distance,
     * normalized by the length of the longer string.
     * @param ex The example whose mentions should be compared.
     * @param useHead Whether the heads or extents should be compared.
     */
    public static double getEdit(CExample ex, boolean useHead) {
        String a = StringTools.getLCText(ex.getM1(), useHead);
        String b = StringTools.getLCText(ex.getM2(), useHead);
        int longLen = Math.max(a.length(), b.length());
        return calcLevenshteinEditDist(a, b) / (double) longLen;
    }
    
    /** 
     * Calculates the (unnormalized) Levenshtein edit distance
     * for a pair of strings.
     * Applying algorithm given in Wikipedia article on {@literal 3/24/2008}.
     * @param a One string.
     * @param b Another string.
     * @return The edit distance, as an integer.
     */
    public static int calcLevenshteinEditDist(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] d = new int[m+1][n+1];
        for (int i = 0; i <= m; ++i)
            d[i][0] = i;
        for (int j = 0; j <= n; ++j)
            d[0][j] = j;
    
        for (int i = 1; i <= m; ++i) {
            for (int j = 1; j <= n; ++j) {
        	int cost = 0; if (a.charAt(i-1) != b.charAt(j-1)) cost = 1;
        	d[i][j] = Collections.min(Arrays.asList(d[i-1][j] + 1,
        			   d[i][j-1] + 1,
        			   d[i-1][j-1] + cost));
            }
        }
        return d[m][n];
    }
}
