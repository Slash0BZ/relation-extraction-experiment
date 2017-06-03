package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.*;
import java.util.*;

import edu.brandeis.cs.steele.wn.FileBackedDictionary;
import edu.brandeis.cs.steele.wn.DictionaryDatabase;
import edu.brandeis.cs.steele.wn.PointerTarget;
import edu.brandeis.cs.steele.wn.PointerType;
import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.Word;
import edu.brandeis.cs.steele.wn.Synset;
import edu.brandeis.cs.steele.wn.POS;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

import edu.illinois.cs.cogcomp.lbj.coref.util.collections.MySets;

import edu.illinois.cs.cogcomp.lbj.coref.io.WNFileManager;

/**
 * A collection of methods for dealing with WordNet,
 * with functionality to look up words in WordNet and determine
 * the synonyms, hypernyms, and antonyms.
 * Since WordNet can be slow, also caches lookups.
 * Provides facility for loading and saving from a file,
 * enabling cache persistence.
 */
public class WordNetTools 
 //implements Serializable {
    implements Externalizable {
    //Serialization management:
    private static String m_packageName = "edu/illinois/cs/cogcomp/lbj/coref/features";
    private static String m_className = "WordNetTools";
    private static final String m_dsrBase = "WordNetTools.dsr";
    private static String m_dsrName = null;
    private static WordNetTools m_wn = null;
    
    transient private DictionaryDatabase m_dict = null;
    transient private Map<String,IndexWord> wordCache;
    private Map<String,Boolean> shareSynCache;
    private Map<String,Boolean> shareHypPOSCache;
    private Map<String,Boolean> shareHypCache;
    private Map<String,Boolean> areSynCache;
    private Map<String,Boolean> areHypCache;
    private Map<String,Boolean> areAntCache;
    
    private static final long serialVersionUID = 56L;

    /**
     * Constructs a new WordNetTools object with empty caches.
     * @see #getWN() 
     */
    public WordNetTools() {
	this.startup();
	shareSynCache = new HashMap<String,Boolean>();
	shareHypPOSCache = new HashMap<String,Boolean>();
	shareHypCache = new HashMap<String,Boolean>();
	areSynCache = new HashMap<String,Boolean>();
	areHypCache = new HashMap<String,Boolean>();
	areAntCache = new HashMap<String,Boolean>();
    }

    /**
     * Load the underlying WordNet FileBackedDictionary.
     * @throws RuntimeException if the database cannot be loaded.
     */
    private void startup() {
	try {
	    //System.err.println("Debug: Loading WN Dict");
	    //String dbName = (new myIO()).findFile("wordnet/wordnetdata");
	    //WAS: m_dict = new FileBackedDictionary(dbName);
	    WNFileManager fm = new WNFileManager("wordnet/wordnetdata");
	    m_dict = new FileBackedDictionary(fm);

	    wordCache = new WeakHashMap<String,IndexWord>();
	    //System.err.println("Debug: Done Loading WN Dict");
	} catch (Exception e) {
	    throw new RuntimeException("Cannot load wordnet database", e);
	}
    }

    
    
    /* Primary Features */
    
    /**
     * Determines whether two phrases are synonyms using WordNet.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns or noun phrases.
     * If any sense of {@code w1} is synonymous with any sense of {@code w2},
     * or vice versa, returns true.
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether two phrases are synonyms.
     */
    public boolean areSynonyms(String w1, String w2) {
	String j = this.join(w1, w2);
	if (areSynCache.containsKey(j))
	    return areSynCache.get(j);
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    areSynCache.put(j, false);
	    return false;
	}
	if ( overlap(getSynonyms(entry1), getSenseWords(entry2)) ) { 
	    areSynCache.put(j,true);
	    return true;
	}
	if ( overlap(getSynonyms(entry2), getSenseWords(entry1)) ) { 
	    areSynCache.put(j,true);
	    return true;
	}
	//Then must not be Synonyms.
	areSynCache.put(j, false);
	return false;
    }

    /**
     * Determines whether two phrases are antonyms using WordNet.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns or noun phrases.
     * If any sense of {@code w1} is an antonym of any sense of {@code w2},
     * or vice versa, returns true.
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether two phrases are antonyms.
     */
    public boolean areAntonyms(String w1, String w2) {
	String j = this.join(w1, w2);
	if (areAntCache.containsKey(j))
	    return areAntCache.get(j);
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    areAntCache.put(j,false);
	    return false;
	}
	//Is 2 in 1's Antonym set?
	if ( overlap(getAntonyms(entry1), getSenseWords(entry2)) ) { 
	    areAntCache.put(j,true);
	    return true;
	}
	//Is 2 in 1's Antonym set?
	if ( overlap(getAntonyms(entry2), getSenseWords(entry1)) ) { 
	    areAntCache.put(j,true);
	    return true;
	}
	//Then must not be Antonyms.
	areAntCache.put(j,false);
	return false;
    }

    /**
     * Determines whether one phrase is the hypernym of another using WordNet.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns or noun phrases.
     * If any sense of {@code w1} is a hypernym of any sense of {@code w2},
     * or vice versa, returns true.
     * Not restricted to direct hypernyms (other ancestors or descendants may
     * appear along the hypernym path between the two phrases).
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether either phrase is the hypernym of the other.
     */
    public boolean areHypernyms(String w1, String w2) {
	String j = this.join(w1, w2);
	if (areHypCache.containsKey(j)) {
	    return areHypCache.get(j);
	}
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    areHypCache.put(j,false);
	    return false;
	}
	if ( overlap(getAllHypernyms(entry1), getSenseWords(entry2)) ) { 
	    areHypCache.put(j,true);
	    return true;
	}
	if ( overlap(getAllHypernyms(entry2), getSenseWords(entry1)) ) { 
	    areHypCache.put(j,true);
	    return true;
	}
	//Then must not be Hypernyms.
	areHypCache.put(j,false);
	return false;
    }

    /**
     * Determines whether two phrases share hypernyms using WordNet.
     * Since entries in WordNet would generally share a very high-level
     * hypernym such as "entity", entries near the root of the hypernym
     * tree are not considered matches.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns or noun phrases.
     * If any sense of {@code w1} is the hypernym of {@code w2},
     * or vice versa, returns true.
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether the phrases share a hypernym.
     */
    public boolean shareHypernyms(String w1, String w2) {
	String j = this.join(w1, w2);
	if (shareHypCache.containsKey(j))
	    return shareHypCache.get(j);
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    shareHypCache.put(j,false);
	    return false;
	}

	Set<Word> shared = MySets.getIntersection(
	 getHypernyms(entry1), getHypernyms(entry2));

	if (shared.size() > 0) {
	    shareHypCache.put(j,true);
	    return true;
	} else {
	    shareHypCache.put(j,false);
	    return false;
	}
    }

    
    /**
     * Determines whether two phrases share hypernyms using WordNet.
     * Since entries in WordNet would generally share a very high-level
     * hypernym such as "entity", entries near the root of the hypernym
     * tree are not considered matches.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns, except that if
     * no such entry is found, they are assumed to be adjectives.
     * If any sense of {@code w1} is the hypernym of {@code w2},
     * or vice versa, returns true.
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether the phrases share a hypernym.
     */
    public boolean shareHypernymsPOS(String w1, String w2) {
	String j = this.join(w1, w2);
	if (shareHypPOSCache.containsKey(j))
	    return shareHypPOSCache.get(j);
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    shareHypPOSCache.put(j,false);
	    return false;
	}
	
	Set<Word> shared = MySets.getIntersection(
		 getHypernyms(entry1), getHypernyms(entry2));
	if (shared.size() > 0) { //Any common hypernyms?
	    shareHypPOSCache.put(j, true);
	    return true;
	} else {
	    shareHypPOSCache.put(j, false);
	    return false;
	}
    }

    
    /**
     * Determines whether any sense of one phrase has the same synonym
     * as any sense of another phrase.
     * This method is case-sensitive to the extent that the underlying
     * database is.  Input strings are assumed to be nouns or noun phrases.
     * @param w1 A string.
     * @param w2 Another string.
     * @return Whether two phrases share a synonym.
     */
    public boolean shareSynonymns(String w1, String w2) {
	String j = this.join(w1, w2);
	if (shareSynCache.containsKey(j))
	    return shareSynCache.get(j);
	IndexWord entry1 = getIndexNoun(w1), entry2 = getIndexNoun(w2);
	if (entry1 == null || entry2 == null) {
	    shareSynCache.put(j,false);
	    return false;
	}
	Set<Word> syns1 = getSynonyms(entry1);
	Set<Word> syns2 = getSynonyms(entry2);
	syns1.retainAll(syns2);
	if (syns1.size() > 0) { //Any common synonyms?
	    shareSynCache.put(j,true);
	    return true;
	} else {
	    shareSynCache.put(j,false);
	    return false;
	}
    }

    
    
    /* Get sets of related items */
    
    
    /**
     * Gets all synonyms for all senses of a word.
     * @param word An entry in WordNet.
     * @return The set of Words synonymous with {@code word}.
     */
    public Set<Word> getSynonyms(IndexWord word) {
	Set<Word> words = new HashSet<Word>();
	for (Synset sense : word.getSenses()) 
	    for (Word w : sense.getWords())
		words.add(w); //Add synonym
	return words;
    }
    
    /**
     * Looks up word and gets all synonyms for all senses of a word.
     * @param word Any string, assumed to be a noun.
     * @return The phrases synonymous with {@code word}.
     */
    public String[] getSynonymStrings(String word) {
	List<String> result = new ArrayList<String>();
	IndexWord entry = getIndexNoun(word);
	if (entry == null)
	    return result.toArray(new String[0]);
	Set<Word> syns = getSynonyms(entry);
	for (Word w : syns) {
	    result.add(w.getLemma());
	}
	return result.toArray(new String[0]);
    }

    /**
     * Gets all antonyms for all senses of a word.
     * @param word An entry in WordNet.
     * @return The set of Words that are antonyms of {@code word}.
     */
    public Set<Word> getAntonyms(IndexWord word) {
	Set<Word> words = new HashSet<Word>();
	for (Synset sense : word.getSenses()) {
	    for (PointerTarget pt : sense.getTargetsList(PointerType.ANTONYM)) {
		Word w = this.getWord(pt);
		if (w != null)
		    words.add(w);
	    }
	}
	return words;
    }

    /**
     * Gets all hypernyms of all senses of a word.
     * @param word An entry in WordNet.
     * @return The set of Words that are hypernyms of {@code word}.
     */
    public Set<Word> getAllHypernyms(IndexWord word) {
	Set<Word> words = new HashSet<Word>();
	for (Synset sense : word.getSenses())  {
	    words.addAll(getAllHypernyms(sense));
	}
	return words;
    }

    /**
     * Gets all hypernyms of a word.
     * @param word A pointer target entry in WordNet,
     * which may be a {@code Word}
     * or {@code Synset} (in which case the first element will be used).
     * @return The set of Words that are hypernyms of {@code word}.
     */
    public Set<Word> getAllHypernyms(PointerTarget word) {
	Set<Word> words = new HashSet<Word>();
	List<PointerTarget> targets = new ArrayList<PointerTarget>();
	try {
	    targets = word.getTargetsList(PointerType.HYPERNYM);
	} catch (ClassCastException e) { //Known problem.
	    //Ignore.
	    //System.err.println("Problem processing " + word);
	}
	for (PointerTarget pt : targets) {
	    Word w = this.getWord(pt);
	    if (w != null) {
		words.add(w);
		Set<Word> allH = getAllHypernyms(pt);
		words.addAll(allH);
	    }
	}
	return words;
    }
    
    /**
     * Looks up word and gets all hypernyms for all senses of a word.
     * @param word Any string, assumed to be a noun.
     * @return The hypernyms of {@code word}.
     */
    public String[] getHypernymStrings(String word) {
	List<String> result = new ArrayList<String>();
	IndexWord entry = getIndexNoun(word);
	if (entry == null)
	    return result.toArray(new String[0]);
	Set<Word> hyps = getAllHypernyms(entry);
	for (Word w : hyps) {
	    result.add(w.getLemma());
	}
	return result.toArray(new String[0]);
    }

    /**
     * Gets hypernyms of all senses of a word. Does not include top level
     * entries such as "entity", "abstraction", "physical entity", "object",
     * "whole", "artifact", or "group", since practically every entry
     * would have one of these as a hypernym.
     * @param word An entry in WordNet.
     * @return The set of Words that are hypernyms of {@code word}.
     * @see #getAllHypernyms(IndexWord)
     */
    public Set<Word> getHypernyms(IndexWord word) {
	Set<Word> words = new HashSet<Word>();
	if (word == null) {
	    System.err.println("Word null in getHypernyms");
	    return words;
	}
	String[] arr = new String[]{"entity", "abstraction",
	"physical entity", "object", "whole", "artifact", "group"};
	Set<String> stopwords = new HashSet<String>(Arrays.asList(arr));

	for (Synset sense : word.getSenses())  {
	    List<PointerTarget> targets = new ArrayList<PointerTarget>();
	    try {
		targets = sense.getTargetsList(PointerType.HYPERNYM);
	    } catch (ClassCastException e) { //Known problem.
		//Ignore.
	    }
	    for (PointerTarget pt : targets) {
		if (pt instanceof Synset) {
		    Synset ss = (Synset) pt;
		    boolean skip = false;
		    for (Word w : ss.getWords()) {
			if (stopwords.contains(w.getLemma())) {
			    skip = true;
			    break;
			}
		    }
		    if (skip)
			break;
		}
		Word w = this.getWord(pt);
		if (w != null) {
		    words.add(w);
		}
	    }
	}
	return words;
    }
   



    /* Lookup */

    /**
     * Translates a pointer target to a {@code Word}.
     * If {@code p} is a Synset, the first entry is returned.
     * @param p A pointer target representing a word or synset.
     * @return The word, the first word of a synset, or null.
     */
    private Word getWord(PointerTarget p) {
	if (p instanceof Word) {
	    return (Word) p;
	} else if (p instanceof Synset) {
	    Synset ss = (Synset) p;
	    if (ss.getWords().length > 0)
		return ss.getWord(0);
	} else {
	    System.err.println("PT not Word or Synset");
	}
	return null;
    }

    /**
     * Looks up the {@code IndexWord} of a string in WordNet.
     * The string is assumed to be a noun (or noun phrase),
     * but if no such entry is found, it is assumed to be an adjective.
     * @param word The word or phrase to be looked up.
     * @return The IndexWord entry, or null.
     */
    private IndexWord getIndexWord(String word) {
	IndexWord result = getIndexWord(word, "NOUN");
	if (result == null)
	    result = getIndexWord(word, "ADJ");
	return result;
    }

    /**
     * Looks up the {@code IndexWord} of a string in WordNet.
     * The string is assumed to be a noun (or noun phrase).
     * @param word The word or phrase to be looked up.
     * @return The IndexWord entry, or null.
     */
    private IndexWord getIndexNoun(String word) {
	return getIndexWord(word, "NOUN");
    }

    /**
     * Looks up the {@code IndexWord} of a string in WordNet.
     * If the desired word is not found, the word will be backed-off
     * by attempting to add "s" or "es", and finally by using the
     * lookupBaseForm method (which result will only be accepted if at
     * least the length of {@code word} minus 2).
     * This method is cached based on the word (ignoring the POS),
     * so a given {@code word} string will always return the same
     * IndexWord, even if {@code pos} differs. 
     * @param word The word or phrase to be looked up.
     * @param pos The part of speech of the desired entry, "NOUN" or "ADJ".
     * @return The IndexWord entry, or null.
     */
    private IndexWord getIndexWord(String word, String pos) {
	if (wordCache.containsKey(word))
	    return wordCache.get(word);   

	IndexWord result = lookupIndexWordSafe(word, pos);
	if (result != null) {
	    wordCache.put(word, result);
	    return result;
	}

	if (word.endsWith("s")) {
	    String lemma = word.substring(0, word.length()-1);
	    result = lookupIndexWordSafe(lemma, pos);
	}
	if ( result == null && word.endsWith("es") ) {
	    String lemma = word.substring(0, word.length()-2);
	    result = lookupIndexWordSafe(lemma, pos);
	}
	if (result != null) {
	    wordCache.put(word, result);
	    return result;
	}

	//System.out.println(word + "-s and -es not found, trying irregulars");
	String baseForm = null;
	if (pos.equals("NOUN")) {
	    baseForm = m_dict.lookupBaseForm(POS.NOUN, word);
	} else if (pos.equals("ADJ")) {
	    baseForm = m_dict.lookupBaseForm(POS.ADJ, word);
	}
	if (baseForm != null) {
	    if (baseForm.length() >= word.length() - 2) {
		result = lookupIndexWordSafe(baseForm, pos);
		if (!baseForm.equalsIgnoreCase(word)) {
		    //System.out.println("Found baseform: "
		    // + baseForm + " of " + word);
		}
	    }
	} 

	wordCache.put(word, result);
	return result;
    }

    /**
     * Looks up the {@code IndexWord} of a string in WordNet.
     * The string is assumed to be a noun (or noun phrase).
     * If the entry is not found, a shorter entry will NOT
     * be returned.
     * @param word The word or phrase to be looked up.
     * @return The IndexWord entry, or null.
     */
    protected IndexWord lookupIndexNounSafe(String word) {
	return lookupIndexWordSafe(word, "NOUN");
    }

    /**
     * Looks up the {@code IndexWord} of a string in WordNet.
     * This method is not cached.
     * so a given {@code word} string will always return the same
     * IndexWord, even if {@code pos} differs. 
     * @param word The word or phrase to be looked up.
     * If punctuation or number, it will be ignored.
     * @param pos The part of speech of the desired entry,
     * either "NOUN" or "ADJ".
     * @return The IndexWord entry, or null.
     */
    protected IndexWord lookupIndexWordSafe(String word, String pos) {
	if (word.length() == 0)
	    return null;
	//if (!hasLetters(word))
	//    return null;
	if (word.startsWith("\'") && word.length() <= 2)
	    return null;

	IndexWord result = null;
	try {
	    if (pos.equals("NOUN")) {
		result = m_dict.lookupIndexWord(POS.NOUN, word);
	    } else if (pos.equals("ADJ")) {
		result = m_dict.lookupIndexWord(POS.ADJ, word);
	    } else {
		System.err.println(
		 "Unrecognized POS when looking up IndexWord");
	    }
	} catch (ClassCastException e) { //Known problem.
	    //Ignore.
	    //e.printStackTrace();
	} catch (NoSuchElementException e) { //POS IMPLIED problem.
	    //Ignore (probably punctuation or number)
	    //NOTE: The underlying JWordNet may generate a text
	    //message (to standard out or err) indicating
	    //that there is a problem parsing 18 IMPLIED
	    //(Part of the copyright notice.
	    //System.err.println(word + " (" + pos + ")");
	}
	//It is a known problem (for us) that lookupIndexWord will backoff
	//to a prefix if the whole string isn't found.
	if (result != null && !result.getLemma().equalsIgnoreCase(word)) {
	    //System.err.println("Warning: found " + result.getLemma()
	    //+ " instead of " + word + ".  Ignoring.");
	    result = null;
	}
	return result;
    }

    /**
     * Determines whether a phrase has any letters.
     * @param phrase any text.
     * @return Whether the phrase contains any letters.
     */
    private boolean hasLetters(String phrase) {
	for (int i = 0; i < phrase.length(); ++i) {
	    if (Character.isLetter(phrase.charAt(i)))
		return true;
	}
	return false;
    }
    
    /**
     * Gets the {@code Word} for each sense of {@code word}.
     * @param word The desired entry.
     * @return The senses of {@code word} as {@code Word}s.
     */
    public List<Word> getSenseWords(IndexWord word) {
	/* Assumes first entry in Synset is always this Word,
	 * rather than a synonym.
	 */
	List<Word> words = new ArrayList<Word>();
	for ( Synset sense : word.getSenses() )
	    words.add(this.getWord(sense));
	return words;
	
    }
    
    
    
    /* Utilities */

    private <T> boolean overlap(Set<T> a, Collection<T> b) {
	Set<T> inter = new HashSet<T>(b);
	inter.retainAll(a);
	return inter.size() > 0;
    }

    private String join(String a, String b) {
	return a + "_&_" + b;
    }
    
    
    
    /* Input / Output */

    /**
     * Writes the synsets of {@code word} to {@code System.out}.
     * @param word The noun or noun phrase.
     */
    public void printSynsets(String word) {
	printSynsets(m_dict.lookupIndexWord(POS.NOUN, word));
    }
    
    /**
     * Writes the synsets of {@code word} to {@code System.out}.
     * @param word The word or phrase.
     */
    public void printSynsets(IndexWord word) {
	for (Synset sense : word.getSenses()) {
	    System.out.println("Sense: " + sense.getLongDescription());
	    for (Word senseWord : sense.getWords()) {
		System.out.println(senseWord.getLemma());	
	    }
	    System.out.println("");
	}
    }

    //Serialization protocol:
    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
    }
    private void readObject(ObjectInputStream in) throws IOException,
     ClassNotFoundException {
	in.defaultReadObject();
	this.startup();
    }

    @SuppressWarnings("unchecked")
    /** 
     * Reads the object using the Externalization protocol.
     * If this fails, it initializes the object to the default state
     * (Resetting the cache).
     */
    public void readExternal(ObjectInput in) {
	//System.err.println("Debug: readExternal");
	try {
	    shareSynCache = (Map<String,Boolean>) in.readObject();
	    shareHypPOSCache = (Map<String,Boolean>) in.readObject();
	    shareHypCache = (Map<String,Boolean>) in.readObject();
	    areSynCache = (Map<String,Boolean>) in.readObject();
	    areHypCache = (Map<String,Boolean>) in.readObject();
	    areAntCache = (Map<String,Boolean>) in.readObject();
	} catch (Exception e) {
	    //System.err.println("Could not read externalized WN input.");
	    //e.printStackTrace();
	    shareSynCache = new HashMap<String,Boolean>();
	    shareHypPOSCache = new HashMap<String,Boolean>();
	    shareHypCache = new HashMap<String,Boolean>();
	    areSynCache = new HashMap<String,Boolean>();
	    areHypCache = new HashMap<String,Boolean>();
	    areAntCache = new HashMap<String,Boolean>();
	}
	//System.err.println("Debug: Done readExternal");
	this.startup();
    }

    /**
     * Writes the object using the externalization protocol.
     */
    public void writeExternal(ObjectOutput out) {
	//System.err.println("Debug: writeExternal");
	try {
	    out.writeObject(shareSynCache);
	    out.writeObject(shareHypPOSCache);
	    out.writeObject(shareHypCache);
	    out.writeObject(areSynCache);
	    out.writeObject(areHypCache);
	    out.writeObject(areAntCache);
	} catch (Exception e) {
	    //System.err.println("Could not write externalized WN input.");
	    //e.printStackTrace();
	}
	//System.err.println("Debug: Done writeExternal");
    }

    /**
     * Load the WN from a precomputed location.
     */
    private static WordNetTools loadWN() {
        String filename = getFQDSRName();
	if (filename == null) {
	    //System.err.println("Cannot compute WN Filename.");
	    return new WordNetTools();
	}
	File fdsr = new File(filename);
        
        if (!fdsr.exists()) {
	   //TODO: Message if no serialized available?
           //System.err.println("WN serialized file does not exist.");
	   //System.err.println("  Tried " + filename);
           //System.err.println("Not loading cached WordNetTools.");
           return new WordNetTools();
        }
       
        //System.err.println("Debug: Loading WN dsr");
        try {
            FileInputStream fin = new FileInputStream(fdsr);
            ObjectInputStream oin = new ObjectInputStream(fin);
	    WordNetTools wn = (WordNetTools) oin.readObject();
            return wn;
	}
        catch (Exception e) {
            //Do nothing.
            //System.err.println("Cannot load serialized WN.");
            //e.printStackTrace();
        }
	//System.err.println("Returning non-cached WordNetTools.");
        return new WordNetTools();
    }

    /**
     * Saves the WordNetTools with its associated caches.
     */
    public static void saveWN() {
	String filename = getFQDSRName();
	if (filename == null) {
	    System.err.println("Cannot find a location to save WN.");
	    System.err.println("  Considered: " + filename);
	    System.err.println("Not saving WordNet.");
	    return;
        }
        try {    
            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(getWN());
            System.err.println("Saved WN to " + filename);
        }
        catch (Exception e) {
            System.err.println("Could not save Wordnet to " + filename
	     + "because:\n" + e.toString());
        }
    }
    
    /**
     * Determines the location where the serialization file
     * should be loaded or saved.
     * @return The fully qualified filename of the serialization file.
     * @see #loadWN
     * @see #saveWN
     */
    protected static String getFQDSRName() {
      if (m_dsrName != null)
	  return m_dsrName;
      try {
	String fqClass = m_packageName +"/" + m_className + ".class";
	fqClass = (new myIO()).findFile(fqClass);
	if (fqClass == null) {
	    //System.err.println("Cannot find WordNet library.");
	    return "";
	}
	fqClass = new File(fqClass).getAbsolutePath();
	if (fqClass == null) {
	    System.err.println("Cannot get absolute path for wn file.");
	    return "";
	}
	if (!fqClass.contains(m_className)) {
	    System.err.println("File didn't contain filename!");
	    return "";
	}
	fqClass = fqClass.substring(0,
				    fqClass.indexOf(m_className) - 1);
	m_dsrName = fqClass + File.separator + m_dsrBase;
	return m_dsrName;
      } catch (FileNotFoundException e) {
	return m_dsrName;
      }
    }

    /**
     * Gets the singleton WordNetTools, with its associated caches.
     * @return The WordNetTools object.
     */
    public static WordNetTools getWN() {
	if (m_wn == null) m_wn = loadWN();
	return m_wn;
    }
}
