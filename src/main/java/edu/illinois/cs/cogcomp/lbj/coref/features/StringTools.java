package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;


/**
 * A collection of tools related to strings.
 */
public class StringTools {

    //Constructor intentionally omitted.

    /** 
     * Gets the set of capitalized words.
     * @param m The mention whose words will be retrieved.
     * @param useHead Whether words come from the head or extent.
     * @return The set of words that begin with a capital letter.
     */
    public static Set<String> getCapitalizedWords(
     Mention m, boolean useHead) {
        Set<String> result = new HashSet<String>();
        Doc d = m.getDoc();
        int start = useHead ? m.getHeadFirstWordNum() : m.getExtentFirstWordNum();
        int end = useHead ? m.getHeadLastWordNum() : m.getExtentLastWordNum();
        
        for (int i = start; i <= end; ++i) {
            String w = d.getWord(i);
            if (0 < w.length() && Character.isUpperCase(w.charAt(0)))
        	result.add(w);
        }
        return result;
    }

    /** 
     * Gets the lowercased text of a mention.
     * @param m The mention whose text to get.
     * @param useHead Whether to get the head text or the extent text.
     * @return the lowercased text of the mention.
     */
    public static String getLCText(Mention m, boolean useHead) {
        if (useHead) {
            return m.getHead().getText().toLowerCase();
        } else {
            return m.getExtent().getText().toLowerCase();
        }
    }

    /** 
     * Gets the text of a mention.
     * @param m The mention whose text to get.
     * @param useHead Whether to get the head text or the extent text.
     * @param lowercase Whether to lowercase the text.
     * @return the text of the mention.
     */
    public static String getText(Mention m, boolean useHead,
     boolean lowercase) {
	String s;
        if (useHead) {
            s = m.getHead().getText();
        } else {
            s = m.getExtent().getText();
        }
	if (lowercase) {
	    s = s.toLowerCase();
	}
	return s;
    }

}
