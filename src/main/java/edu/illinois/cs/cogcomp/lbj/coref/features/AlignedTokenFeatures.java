package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Pair;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/** 
 * Collection of feature generating functions that compare or return
 * aligned tokens. 
 */
public class AlignedTokenFeatures {

    /** No need to construct collection of features. */
    private AlignedTokenFeatures() {
    }

    /** 
     * Gets the aligned words conjoined as strings.
     * @param ex The example whose mentions will have their words aligned.
     * @return A list of aligned words in the form {@literal "wordA_&&_wordB"},
     * as computed from {@link #getWNAlignedPreModifierPairs}.
     */
    public static List<String> getWNAlignedPreModifiers(CExample ex) {
        List<String> results = new ArrayList<String>();
        List<Pair<String,String>> pairs = getWNAlignedPreModifierPairs(ex);
        for (Pair<String,String> p : pairs) {
            results.add(p.a + "_&&_" + p.b);
        }
        return results;
    }
    
    /**
     * Aligns each word {@code a} in the first mention before its head
     * to each word {@code b} in the second mention's extent
     * that is aligned to {@code a} according to {@link #aligned}.
     * @return A list of ordered pairs of aligned word strings,
     * such that the first member of each pair is from the first mention,
     * and the second member is from the second mention.
     */
    public static List<Pair<String,String>>
     getWNAlignedPreModifierPairs(CExample ex) {
        List<Pair<String,String>> result
         = new ArrayList<Pair<String,String>>();
        Doc d = ex.getDoc();
        Mention m1 = ex.getM1(), m2 = ex.getM2();
        for (int i=m1.getExtentFirstWordNum(); i<m1.getHeadFirstWordNum(); ++i){
            String a = d.getWord(i).toLowerCase();
            for (int j = m2.getExtentFirstWordNum();
             j < m2.getHeadFirstWordNum(); ++j)
            {
        	String b = d.getWord(j).toLowerCase();
        	if (AlignedTokenFeatures.aligned(a, b)) {
        	    result.add(Pair.create(a,b));
        	}
            }
        }
        return result;
    }

    /** 
     * Determines whether two strings should be aligned;
     * strings should be aligned if they share hypernyms, are both countries,
     * both cities, both first names, or both last names.
     * @param a One string.
     * @param b Another string.
     * @return Whether the two strings should be aligned.
     */
    public static boolean aligned(String a, String b) {
        if (a.equals(b)
         ||  WordNetTools.getWN().shareHypernymsPOS(a, b)
         || (Gazetteers.getCountries().contains(a)
         &&  Gazetteers.getCountries().contains(b))
         || (   Gazetteers.getCities().contains(a)
             && Gazetteers.getCities().contains(b)
            )
         || (Gazetteers.getMaleFirstNames().contains(a)
          && Gazetteers.getMaleFirstNames().contains(b))
         || (Gazetteers.getFemaleFirstNames().contains(a)
          && Gazetteers.getFemaleFirstNames().contains(b))
         || (Gazetteers.getLastNames().contains(a)
          && Gazetteers.getLastNames().contains(b))
        ) {
            return true;
        } else {
            return false;
        }
    }

    /** 
     * Extracts the relations between the aligned words returned
     * by {@code getWNAlignedPreModifierPairs()}
     * @param ex The example containing the mentions whose relationship
     * will be computed.
     * @return An array of strings, each one the name of a relation.
     */
    public static String[] getWNAlignedPairRelations(CExample ex) {
        List<Pair<String,String>> pairs = getWNAlignedPreModifierPairs(ex);
        return AlignedTokenFeatures.getWNAlignedPairRelations(pairs);
    }

    /** 
     * Computes the relations between pairs of words.
     * Possible relations are "match", "substring", "Syn", "Hyp", or "Ant",
     * or "Mismatch".
     * @param pairs The word pairs; a relation will be computed for each pair.
     * @return An array of strings, each one the name of the relation
     * of the corresponding pair of words.
     */
    public static String[] getWNAlignedPairRelations(
     List<Pair<String,String>> pairs)
    {
	String[] results = new String[pairs.size()];
	int i = 0;
        for (Pair<String,String> p : pairs) {
            if (p.a.equals(p.b))
        	results[i] = "match";
            else if (p.a.contains(p.b) || p.b.contains(p.a))
        	results[i] = "substring";
            else if (WordNetTools.getWN().areSynonyms(p.a, p.b))
        	results[i] = "Syn";
            else if (WordNetTools.getWN().areHypernyms(p.a, p.b))
        	results[i] = "Hyp";
            else if (WordNetTools.getWN().areAntonyms(p.a, p.b))
        	results[i] = "Ant";
            else
        	results[i] = "Mismatch";
	    ++i;
        }
        return results;
    }

}
