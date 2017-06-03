package edu.illinois.cs.cogcomp.lbj.coref.features;

import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;

/**
 * Collection of features that determine the relationship of two mentions
 * using WordNet.  Determines whether mentions are synonyms, antonyms,
 * hypernyms, or whether they share a hypernym.
 * Relies on WordNetTools, which wraps JWordNet, which ultimately
 * accesses a local-file based WordNet library.
 */
public class WordNetFeatures {

    /**
     * Determines whether one mention is the synonym of another mention.
     * This method is case sensitive to the extent that the WordNet library is.
     * @param ex The example containing the mentions being compared.
     * @param useHead Whether the heads or extents of the mentions
     * should be compared.
     * @return Whether one mention is the synonym of the other mention.
     */
    public static boolean areSynonyms(CExample ex, boolean useHead) {
	boolean lowercase = false;
	String m1Text = StringTools.getText(ex.getM1(), useHead, lowercase);
	String m2Text = StringTools.getText(ex.getM2(), useHead, lowercase);
        return WordNetTools.getWN().areSynonyms(m1Text, m2Text);
    }

    /**
     * Determines whether one mention is the antonym of another mention.
     * @param ex The example containing the mentions being compared.
     * @param useHead Whether the heads or extents of the mentions
     * should be compared.
     * @return Whether one mention is the antonym of the other mention.
     */
    public static boolean areAntonyms(CExample ex, boolean useHead) {
	boolean lowercase = false;
	String m1Text = StringTools.getText(ex.getM1(), useHead, lowercase);
	String m2Text = StringTools.getText(ex.getM2(), useHead, lowercase);
        return WordNetTools.getWN().areAntonyms(m1Text, m2Text);
    }

    /**
     * Determines whether one mention is the hypernym of another mention.
     * This method is case sensitive to the extent that the WordNet library is.
     * @param ex The example containing the mentions being compared.
     * @param useHead Whether the heads or extents of the mentions
     * should be compared.
     * @return Whether one mention is the hypernym of the other mention.
     */
    public static boolean areHypernyms(CExample ex, boolean useHead) {
	boolean lowercase = false;
	String m1Text = StringTools.getText(ex.getM1(), useHead, lowercase);
	String m2Text = StringTools.getText(ex.getM2(), useHead, lowercase);
        return WordNetTools.getWN().areHypernyms(m1Text, m2Text);
    }

    /**
     * Determines whether two mentions have the same hypernym.
     * This method is case sensitive to the extent that the WordNet library is.
     * @param ex The example containing the mentions being compared.
     * @param useHead Whether the heads or extents of the mentions
     * should be compared.
     * @return Whether two mention share a hypernym.
     */
    public static boolean doShareHypernyms(CExample ex, boolean useHead) {
	boolean lowercase = false;
	String m1Text = StringTools.getText(ex.getM1(), useHead, lowercase);
	String m2Text = StringTools.getText(ex.getM2(), useHead, lowercase);
        return WordNetTools.getWN().shareHypernyms(m1Text, m2Text);
    }
}
