package edu.illinois.cs.cogcomp.lbj.coref.features;

import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;

/** Collection of feature generators
 * and tools for computing properties of names.
 */
public class NameFeatures {

    /** Should not need to construct this static feature library. */
    protected NameFeatures() {
    }

    /** 
     * Determines whether the first names match.
     * Match is case-insensitive.
     * @param ex The example whose mentions will be compared.
     * @return "t" (true: they match), "f" (false: they don't match), or
     * "u" (unknown: first names not found).
     */
    public static String doFirstNamesMatch(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	String[] m1Words = m1Text.split("\\s+");
	String[] m2Words = m2Text.split("\\s+");
	if (m1Words.length < 2 || m2Words.length < 2)
	    return "u";
	String firstName1, firstName2;
	if ( Gazetteers.getHonors().contains(m1Words[0].toLowerCase()) ) {
	    if (m1Words.length <= 2) //Don't get last name as first name.
		return "u";
	    else
		firstName1 = m1Words[1];
	} else {
	    firstName1 = m1Words[0];
	}

	if ( Gazetteers.getHonors().contains(m2Words[0].toLowerCase()) ) {
	    if (m2Words.length <=2)
		return "u";
	    else
		firstName2 = m2Words[1];
	} else {
	    firstName2 = m2Words[0];
	}

	if ( firstName1.equalsIgnoreCase(firstName2) )
	    return "t";
	else
	    return "f";
    }

    /** 
     * Determines whether the Honorary Titles (like Mr or Mrs) match.
     * Assumes that if any honorary title occurs, it is the first word
     * of the head.
     * @param ex The example whose mentions will be compared.
     * @return "t" (true: they match), "f" (false: they don't match), or
     * "u" (unknown: honorary titles not found).
     */
    public static String doHonoraryTitlesMatch(CExample ex, boolean useHead) {
	String m1Text = StringTools.getLCText(ex.getM1(), useHead);
	String m2Text = StringTools.getLCText(ex.getM2(), useHead);
	String[] m1Words = m1Text.split("\\s+");
	String[] m2Words = m2Text.split("\\s+");
	String m1FirstWord = m1Words[0];
	String m2FirstWord = m2Words[0];
	if ( Gazetteers.getHonors().contains(m1FirstWord)
		&& Gazetteers.getHonors().contains(m2FirstWord) ) {
	    if ( m1FirstWord.equals(m2FirstWord) ) {
		return "t";
	    } else {
		return "f";
	    }
	} else {
	    return "u";
	}
    }
}
