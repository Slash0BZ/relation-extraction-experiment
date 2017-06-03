package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;


/**
 * A collection of features that involve part of speech tags.
 */
public class PartOfSpeechTools {

    /**
     * Retrieve the set of nouns in a document of the words
     * between the word at position {@code s} and the word at
     * position {@code e}.
     * Nouns will be words whose POS tag starts with "n"
     * (case insensitive).
     * @param d The document whose nouns will be retrieved.
     * @param s The word number of the first word to be examined.
     * @param e The word number of the last word to be examined.
     * @return The set of nouns from a range of words in a document.
     */
    public static Set<String> getLCNounsBetween(Doc d, int s, int e) {
        Set<String> nouns = new HashSet<String>();
        for (int i = s; i <= e; ++i) {
            if ( d.getPOS(i).toLowerCase().startsWith("n") ) 
        	nouns.add( d.getWord(i).toLowerCase() );
        }
        return nouns;
    }

    /**
     * Retrieve the set of nouns in a document of the words
     * between the word at position {@code s} and the word at
     * position {@code e}.
     * Nouns will be words whose POS tag starts with "n"
     * (case insensitive).
     * @param d The document whose nouns will be retrieved.
     * @param s The word number of the first word to be examined.
     * @param e The word number of the last word to be examined.
     * @return The set of nouns from a range of words in a document.
     */
    public static Set<String> getLCNounAdjsBetween(Doc d, int s, int e) {
        Set<String> words = new HashSet<String>();
        for (int i = s; i <= e; ++i) {
            String pos = d.getPOS(i).toLowerCase();
            if ( pos.startsWith("n")  || pos.startsWith("j") ) 
        	words.add( d.getWord(i).toLowerCase() );
        }
        return words;
    }

}
