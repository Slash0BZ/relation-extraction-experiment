package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.Map;


import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;

/**
 * A collection of features related to the number (singular or plural)
 * of a phrase or mention.
 */
public class NumberFeatures {

    /** 
     * Determines if the number (singular or plural) of two mentions match.
     * Number matches if {@link #getNumberStrong} match or extent texts match.
     * @param ex The example containing the mentions in question.
     * @param useDicts Determines whether lists of singular and plural words
     * should be consulted.
     * @return "t" (true: they match), "f" (false: they don't match),
     * or "u" (unknown: one or more numbers cannot be determined).
     */
    public static String doNumbersMatchStrong(CExample ex, boolean useDicts) {
        char n1 = NumberFeatures.getNumberStrong(ex.getM(1), useDicts);
        char n2 = NumberFeatures.getNumberStrong(ex.getM(2), useDicts);
        
        // Some special cases
        String ptext = ex.getM2().getText().toLowerCase();
        Mention m1 = ex.getM1();
        Mention m2 = ex.getM2();
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("PER") || EntityTypeFeatures.getEType(m1).equals("ORG")|| 
            			EntityTypeFeatures.getEType(m1).equals("GPE")))
            	return "t";
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("LOC")||(EntityTypeFeatures.getEType(m1).equals("MISC"))) )
            	return "u";
        if (n1 == n2 && n1 != 'u')
            return "t";
        if (n1 != n2 && n1 != 'u' && n2 != 'u')
            return "f";
        if (n1 == 'u' || n2 == 'u') {
            if ( ex.getM1().getExtent().getText().equals(
             ex.getM2().getExtent().getText()) )
            {
        	return "t";
            } else {
        	return "u";
            }
        }
        return "u"; //Shouldn't happen.
    }

    /** 
     * Determines the number (singular or plural) of a mention.
     * If the mention is a {@code NAM},
     * assume singular if "and" does not occur in the head; otherwise unknown.
     * If {@code NOM} or {@code PRE}, the head is looked up
     * in lists of singular and plural words; if in only one list, return
     * the appropriate number; otherwise unknown.
     * If {@code PRO}, use a table of pronouns and their number.
     * Caches the predicted number in {@link Mention#m_predNumber}.
     * @param m The mention to be examined.
     * @param useDicts Determines whether
     * extensive lists of singular and plural words should be consulted.
     * @return 's' (singular), 'p' (plural), or 'u' (unknown).
     */
    public static char getNumberStrong(Mention m, boolean useDicts) {
        if (m.m_predNumber != 'u')
            return m.m_predNumber;
    
        String e = m.getExtent().getText().toLowerCase();
        String h = m.getHead().getText().toLowerCase();
        
        if (h.equals("i") || h.equals("me") || h.equals("my") 
         || h.equals("mine") || h.equals("myself")|| h.equals("himself")
         || h.equals("he") || h.equals("she") 
         || h.equals("it") || h.equals("him") || h.equals("her") 
         || h.equals("his") || h.equals("hers") 
         || h.equals("one") || h.equals("ones")
         || h.equals("oneself")|| h.equals("herself")
         || h.equals("this") || h.equals("that")
        )
             return 's';
        if (h.equals("we") || h.equals("us") || h.equals("our") 
         || h.equals("ours") || h.equals("ourselves") || h.equals("ourself")
         || h.equals("they") || h.equals("them")
         || h.equals("their") || h.equals("theirs")
         || h.equals("themselves") || h.equals("themself")
         || h.equals("these") || h.equals("those") || h.equals("group")
         || h.equals("team")|| h.equals("lot")|| h.equals("many")|| h.equals("lots") 
         )
            return 'p';

        if (h.equals("rest") || h.equals("part")||h.equals("everyone")||h.equals("every")|| h.equals("everybody")
        		|| h.equals("first")|| h.equals("second")|| h.equals("third"))
                   return 'u';
        //TODO: More determiners.
        if (e.startsWith("a ") || e.startsWith("an ") || e.startsWith("this ") || e.startsWith("one "))
            return 's';
        if (e.startsWith("those ") || e.startsWith("these ")|| e.startsWith("both ")
         || e.startsWith("some ") || e.startsWith("two ") || e.startsWith("three ")
         || e.startsWith("four ")|| e.startsWith("five ")|| e.startsWith("hundreds ")
         || e.startsWith("thousands ")|| e.startsWith("millions "))
            return 'p';
    
        //NOTE: "The" isn't determinative of number.
    
        if (m.getType().equals("NAM")) {
            if (!h.equals("and"))
            	return 's';
            if(EntityTypeFeatures.getEType(m).equals("PER")) // Person name is Singular
            	return 's';
            else
            	return 'u'; //Hard to know if plural.
        }
        
        if (m.getType().equals("NOM") || m.getType().equals("PRE")) {
            //TODO: Head noun instead of Head NP:
            boolean mayBeSing = false, mayBePlural = false;
            if (useDicts) {
        	if (Gazetteers.getSingularNouns().contains(h))
        	    mayBeSing = true;
        	if (Gazetteers.getPluralNouns().contains(h))
        	    mayBePlural = true;
        	if (mayBeSing && !mayBePlural)
        	    return 's';
        	else if (mayBePlural && !mayBeSing)
        	    return 'p';
            } else { //Don't use dicts
        	if (m.getHead().getText().toLowerCase().endsWith("s"))
        	    return 'p';
        	else
        	    return 's';
            }
        }
            
        return 'u'; //Last resort.
    }

    //Rely on.
    //Needed for baseline:
    /**
     * Determines the number of a phrase in a simpler way,
     * for use in baseline systems.
     * If phrase is a known pronoun, number is determined by table lookup,
     * otherwise, if evidence of a personal name
     * (contains a first name or an honorary title), assume singular,
     * otherwise, if ending with "s" assume plural,
     * otherwise unknown.
     * @param phrase The phrase in question.
     * @return 's' (singular), 'p' (plural), or 'u' (unknown).
     */
    public static char getNumber(String phrase) {
        String lP = phrase.toLowerCase();
        if (lP.equals("i") || lP.equals("me") || lP.equals("my") 
         || lP.equals("mine") || lP.equals("he") || lP.equals("she") 
         || lP.equals("it") || lP.equals("him") || lP.equals("her") 
         || lP.equals("his") || lP.equals("hers"))
            return 's';
        else if (lP.equals("we") || lP.equals("us") || lP.equals("our") 
         || lP.equals("ours") || lP.equals("they") || lP.equals("them")
         || lP.equals("their") || lP.equals("theirs"))
            return 'p';
        String[] words = lP.split("\\s");
        //TODO: Verify honors are (still) always singular.
        if ( Gazetteers.getHonors().contains(words[0]) )
            return 's';
        for (String word : words) {
            if (Gazetteers.getMaleFirstNames().contains(word) ||
            Gazetteers.getFemaleFirstNames().contains(word))
        	return 's';
        }
        if (lP.endsWith("s"))
            return 'p';
        else
            return 'u';
    }
}
