package edu.illinois.cs.cogcomp.lbj.coref.features;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/**
 * Collection of features and tools related to pronouns.
 */
public class PronounFeatures {

    /** Should not need to construct this static feature library. */
    protected PronounFeatures() {
    }

    /**
     * Determines whether one mention is a relative pronoun
     * referring to the other mention.
     * @param ex The example containing the mentions.
     * @return Whether one mention is a relative pronoun referring to the other.
     */
    public static boolean relativePronounFor(CExample ex) {
	Doc d = ex.getDoc();
	String m1Head = ex.getM1().getHead().getText().toLowerCase();
	String m2Head = ex.getM2().getHead().getText().toLowerCase();
	int sh1 = ex.getM1().getHeadFirstWordNum();
	int sh2 = ex.getM2().getHeadFirstWordNum();
	//int se1 = ex.getM1().getExtentFirstWordNum();
	//int se2 = ex.getM2().getExtentFirstWordNum();
	int eh1 = ex.getM1().getHeadLastWordNum();
	int eh2 = ex.getM2().getHeadLastWordNum();
	int ee1 = ex.getM1().getExtentLastWordNum();
	int ee2 = ex.getM2().getExtentLastWordNum();
	if (matchesRelative(m1Head)) {
	    if (sh1 == ee2 + 1 || sh1 == eh2 + 1)
		return true;
	    if (sh1 == ee2 + 2 && d.getWord(ee2 + 1).equals(",")
		    || sh1 == eh2 + 2 && d.getWord(eh2 + 1).equals(","))
		return true;
	}
	if (matchesRelative(m2Head)) {
	    if (sh2 == ee1 + 1 || sh2 == eh1 + 1)
		return true;
	    if (sh2 == ee1 + 2 && d.getWord(ee1 + 1).equals(",")
		    || sh2 == eh1 + 2 && d.getWord(eh1 + 1).equals(","))
		return true;
	}
	return false;
    }

    /**
     * Determines whether the given string is a relative pronoun.
     * @param w The string, lowercased.
     * @return Whether the given string is a lowercase relative pronoun.
     */
    public static boolean matchesRelative(String w) {
	 if (w.equals("who") || w.equals("whom") || w.equals("which")
	 || w.equals("whose") || w.equals("whoever") || w.equals("whomever")
	 || w.equals("whatever") || w.equals("whichever")
	 || w.equals("that")
	)
	    return true;
	else
	    return false;
   }
    public static boolean pronounReferToOrgnize(String w){
    	if(w.equals("they") || w.equals("their") || w.equals("them") ||
        w.equals("we") || w.equals("our") || w.equals("us") ||w.equals("themselves")
        ||w.equals("ourselves"))
    		return true;
    	return false;
    }
    
    /**
     * Determines whether the given string is a pronoun.
     * @param w The string, lowercased.
     * @return Whether the given string is a lowercase pronoun.
     */
    public static boolean isPronoun(String w) {
	//Personal:
	if (w.equals("he") || w.equals("she") || w.equals("it")
	 || w.equals("him") || w.equals("her") 
	 || w.equals("his") || w.equals("hers") || w.equals("its")
	 || w.equals("they") || w.equals("them") 
	 || w.equals("their") || w.equals("theirs")
	 || w.equals("i") || w.equals("me") || w.equals("mine")
	 || w.equals("we") || w.equals("us") 
	 || w.equals("our") || w.equals("ours")
	 || w.equals("you") || w.equals("your") || w.equals("yours")
	//Indefinite
	 || w.equals("one") || w.equals("one's") || w.equals("ones")
	 || w.equals("anyone") || w.equals("anybody") || w.equals("anything")
	 || w.equals("someone") || w.equals("somebody") || w.equals("something")
	 || w.equals("everyone") || w.equals("everybody") 
	 || w.equals("everything")
	 || w.equals("nothing") || w.equals("nobody")
	 || w.equals("any") || w.equals("each") 
	 || w.equals("either") || w.equals("neither")
	 || w.equals("all") || w.equals("most") || w.equals("some") 
	 || w.equals("several") || w.equals("none")
	 || w.equals("both") || w.equals("few") || w.equals("many")
	//Reflexive
	 || w.equals("himself") || w.equals("herself") || w.equals("itself")
	 || w.equals("themselves") || w.equals("themself")
	 || w.equals("myself") || w.equals("ourselves") || w.equals("ourself")
	 || w.equals("oneself")
	//Demonstrative
	 || w.equals("this") || w.equals("that")
	 || w.equals("these") || w.equals("those")
	//Relative
	 || w.equals("who") || w.equals("whom") || w.equals("which")
	 || w.equals("whose")
	 || w.equals("whoever") || w.equals("whomever")
	 || w.equals("whatever") || w.equals("whichever")
	//Interrogative (but non-relative)
	 || w.equals("what")
	) {
	    return true;
	} else {
	    return false;
	}
    }

    
}
