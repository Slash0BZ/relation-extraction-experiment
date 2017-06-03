package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.features.GigaWord;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.GExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.Relation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Pair;

/** 
 * Represents one document from a corpus, including the text, sentences, words,
 * part-of-speech tags, annotations of coreference, relations, entities,
 * mentions, and other relevant information.
 * <p>
 * The most common way to create a document is to use a DocLoader,
 * such as {@link DocFromTextLoader} or {@link DocLoader#getDefaultLoader}.
 * The advantage of such an approach is that loading of mentions
 * is done automatically (if annotation is provided in the files)
 * and/or mention prediction and typing is done automatically
 * (if mention detectors and typers are provided).
 * Alternatively, subclasses may be constructed directly.
 * </p>
 * @author Eric Bengtson
 */
public interface Doc {
	
	public enum DocSource{CoNLL,ACE04,PlainText};
	DocSource docSource= DocSource.CoNLL;
	
    public String getBaseFilename();

    /**
     * Gets the domain of the document
     * @return The plain text.
     */
    public String getDomain();
    
    /* Text */
    
    /**
     * Gets the text that is the basis for counting,
     * including the start/end characters in Chunk objects.
     * @return The plain text.
     */
    public String getPlainText();
    
    
    /**
     * Gets the {@link TextAnnotation} of the document
     * @return The {@link TextAnnotation} representation of document
     */
    public TextAnnotation getTextAnnotation();

    /**
     * Set the {@link TextAnnotation} of the document
     * @return 
     */
    public void setTextAnnotation(TextAnnotation ta);

    public TextAnnotation[] getTripleTextAnnotation();

    public void setTripleTextAnnotation(TextAnnotation[] tripleTA);
    
    /* Metadata */

    /**
     * Gets the ID for this document, as a string.
     * @return The document ID.
     */
    public String getDocID();

    public void setDocID(String docID);

    /**
     * Indicates whether the document is case sensitive.
     * @return Whether the document is case sensitive.
     */
    public boolean isCaseSensitive();

    
    
    /* Sentences */
    
    /**
     * Gets the sentence number for the specified word.
     * @param wordNum the zero-based position of the word whose
     * sentence number is desired.
     * @return The zero-based sentence number.
     */
    public int getSentNum(int wordNum);
    
    /**
     * Gets the starting position of the specified sentence. 
     * @param sentNum the index of sentence whose starting position is desired.
     * @return The zero-based starting position.
     */
    public int getSentStartNum(int sentNum);
    /** 
     * Returns the number of sentences in the document.
     * @return The number of sentences.
     */
    public int getNumSentences();

    
    
    /* Entities */

    /**
     * Sets the preference for using predicted entities or true entities.
     * @param usePred if {@code true}, prefer to use predicted entities,
     * otherwise, prefer true entities.
     */
    public void setUsePredictedEntities(boolean usePred);
   
    /** 
     * Indicates whether requests for entities will return predicted
     * entities or true entities.
     * @return Whether predicted or true entities are to be used.
     */
    public boolean usePredictedEntities();

    /** 
     * Gets the entities, in no particular order.
     * If {@link #usePredictedEntities()} and predicted
     * entities are available, return them;
     * otherwise return true entities.
     * @return An unmodifiable view of the entities.
     */
    public List<Entity> getEntities();
    
    /**
     * Gets a list of predicted entities, in no particular order.
     * @return An unmodifiable view of the predicted entities or an empty list.
     */
    public List<Entity> getPredEntities();
    
    /**
     * Gets a list of true entities, in no particular order.
     * @return An unmodifiable view of the true entities or an empty list.
     */
    public List<Entity> getTrueEntities();

    /** 
     * Gets the partition of mentions into coreferential sets.
     * @return A reference to the chain solution representing the predicted
     * partitioning of mentions into entities, or null if none has been set.
     */
    public ChainSolution<Mention> getCorefChains();

    /** 
     * Gets the entity containing {@code m}.
     * Uses entities from {@code getEntities()}.
     * @param m The mention whose entity is desired.
     * @return The entity containing {@code m}, or null if not found.
     */
    public Entity getEntityFor(Mention m);
    
    /**
     * Gets the entity containing {@code m}.
     * If {@code usePred}, returns the predicted entity,
     * else returns the true entity (if the requested type of entity
     * is not available, {@code null} will be returned).
     * @param m The mention whose entity is desired.
     * @param usePred Whether to return a predicted entity or a true entity.
     * @return The entity containing {@code m}, or null if the entity
     * of the specified type is not available.
     */
    public Entity getEntityFor(Mention m, boolean usePred);

    /** 
     * Sets the predicted entities to be those specified by {@code sol}.
     * Entity IDs are automatically created, and each mention's
     * {@code setPredictedEntityID()} method is called. 
     * Also sets usePredictedEntities to {@code true}.
     * The entities are backed internally, but the mentions are not duplicated.
     * @param sol The partition of mentions from which to derive entities.
     */
    public void setPredEntities(ChainSolution<Mention> sol);

    /**
     * Indicates whether predicted entities are available.
     * @return Whether predicted entities have been set.
     */
    public boolean hasPredEntities();

    /**
     * Indicates whether true entities are available.
     * @return Whether true entities have been set.
     */
    public boolean hasTrueEntities();

    /**
     * The type of document {Conversation, Article, or Unknown}
     * @author kchang10
     */
    public enum DocType { CONVERSATION, ARTICLE,UnKnown };
    
    public void setDocType(DocType type);
    
    public DocType getDocType();
    /* Examples */

    /** 
     * Returns the unique {@code CExample} for the given pair of mentions
     * in the given order.
     * Doc is the head of a collection of related examples;
     * as such, it needs to return the same {@code CExample} any time
     * an inference-based classifier is used.
     * @param m1 The first mention.
     * @param m2 The second mention.
     * @return The unique {@code CExample} referring to
     * the ordered pair {@code m1, m2}.
     */
    public CExample getCExampleFor(Mention m1, Mention m2);

    /** 
     * Returns the unique {@code GExample} for the given pair of mentions
     * in the given order.
     * Doc is the head of a collection of related examples;
     * as such, it needs to return the same {@code GExample} any time
     * an inference-based classifier is used.
     * @param m The mention.
     * @return The unique {@code GExample} referring to
     * the ordered pair {@code m1, m2}.
     */
    public GExample getGExampleFor(Mention m);


    
    /* Mentions */

    public int getNumMentions();

    /**
     * Sets the preference for using predicted mentions or true mentions.
     * @param usePred if {@code true}, prefer to use predicted mentions,
     * otherwise, prefer true mentions.
     */
    public void setUsePredictedMentions(boolean usePred);

    /** 
     * Indicates whether requests for mentions will return predicted
     * mentions or true mentions.
     * @return Whether predicted or true mentions are to be used.
     */
    public boolean usePredictedMentions();

    public Mention getMention(int n);
    public Mention getTrueMention(int n);
    public Mention getPredMention(int n);

    /**
     *  Return the position of the mention in the text
     *  @return Integer i, if the mention is the i-th mention in the text 
     */
    public int getMentionPosition(Mention m);
    public int getTrueMentionPosition(Mention m);
    public int getPredMentionPosition(Mention m);
    
    /** 
     * Gets the mentions of the document, sorted (typically in document order).
     * Returns predicted mentions or true mentions
     * depending on the result of {@code usePredictedMentions()}.
     * @return mentions sorted by their natural ordering
     * (usually document ordering).
     */
    public List<Mention> getMentions();

    /**
     * Gets a sorted list of predicted mentions.
     * @return sorted predicted mentions, or an empty list if none available.
     */
    public List<Mention> getPredMentions();

    /**
     * Indicates whether predicted mentions have been set.
     * @return Whether predicted mentions have been set.
     */
    public boolean hasPredMentions();

    /**
     * Indicates whether true mentions have been set.
     * @return Whether true mentions have been set.
     */
    public boolean hasTrueMentions();
    
    /** 
     * Gets a sorted list of true mentions.
     * @return sorted true mentions, or an empty list if none available.
     */
    public List<Mention> getTrueMentions();

    /** 
     * Sets the predicted mentions and records a preference for using them.
     * @param ments The predicted mentions (copied defensively).
     */
    public void setPredictedMentions(Collection<Mention> ments);

    /** 
     * Gets the true mention aligned with the specified mention.
     * @param pred A predicted mention.
     * @return The true mention aligned with {@code pred}.
     */
    public Mention getTrueMentionFor(Mention pred);

    /** 
     * Gets the canonical mention of the entity containing {@code m}.
     * @param m A mention.
     * @return The canonical mention for {@code m}.
     */
    public Mention getBestMentionFor(Mention m);

    /**
     * Returns the set of mentions whose heads start
     * at the specified word number, or an empty set if none are found.
     * May be backed internally or not: no guarantees are made (yet).
     * @param startWord A word number.
     * @return The set of mentions whose heads start at {@code startWord}.
     */
    public Set<Mention> getMentionsWithHeadStartingAt(int startWord);

    /**
     * Returns the set of mentions whose extents start
     * at the specified word number, or an empty set if none are found.
     * May be backed internally or not: no guarantees are made (yet).
     * @param startWord A word number.
     * @return The set of mentions
     * whose extents start at {@code startWord}.
     */
    public Set<Mention> getMentionsWithExtentStartingAt(int startWord);

    /** 
     * Gets the set of mentions whose head is entirely contained within
     * a specified mention's extent,
     * including the specified mention itself.
     * Returns predicted or true mentions
     * according to the result of {@code getMentions()}.
     * @param m The specified mention.
     * @return The set of mentions contained in {@code m}.
     */
    public Set<Mention> getMentionsContainedIn(Mention m);

    /** 
     * Gets the set of mentions whose extents entirely contain
     * a specified mention's extent,
     * including the specified mention itself.
     * Returns predicted or true mentions
     * according to the result of {@code getMentions()}.
     * @param m The specified mention.
     * @return The set of mentions containing {@code m}.
     */
    public Set<Mention> getMentionsContaining(Mention m);

    /** 
     * Gets a list of the mentions in a specified sentence in order.
     * Returns true or predicted mentions according to the value
     * of {@code usePredictedMentions()}.
     * @param sentNum The number of the specified sentence.
     * @return A List of the mentions in the specified sentence,
     * in the order that they appear in the sentence.  
     */
    public List<Mention> getMentionsInSent(int sentNum);
    
    /**
     * Gets a pair of lists of mentions, one for each of the two
     * specified sentences.
     * Gets all the mentions in the specified sentences.
     * @param s1 The number of the first sentence.
     * @param s2 The number of the second sentence.
     * @return A pair of lists of mentions, one for each sentence.
     */
    public Pair<List<Mention>,List<Mention>> getMentionsInSentences(
     int s1, int s2);

    
    
    /* Chunks */
    
    /* TODO: Should this be in Doc interface? */
    /**
     * Create a chunk spanning the specified words in this document.
     * @param startWord The position of the first word in desired chunk.
     * @param endWord The position of the last word in the desired chunk.
     * @return The desired chunk.
     */
    public Chunk makeChunk(int startWord, int endWord);
    
    
    
    /* Words and their attributes */
    
    /**
     * Gets a list of the surface forms of the words of the document.
     * @return A list of Strings of words, in the order they appear. 
     */
    public List<String> getWords();
    
    /**
     * Gets the specified word.
     * @param wordNum The position of the specified word
     * (as an index into a {@code List}).
     * @return The {@code wordNum}th word as a string.
     */
    public String getWord(int wordNum);
    
    /**
     * Gets a list of the Part-Of-Speech tags for the words of the document.
     * The tag set is that output by the LBJ POS tagger.
     * @return A list of Part-Of-Speech tags corresponding to the words
     * of the document.
     * @see edu.illinois.cs.cogcomp.lbj.pos.POSTagger
     */
    public List<String> getPOS();
    
    /**
     * Gets the Part-Of-Speech tag for the word at the {@code posNum}
     * position in the document.
     * @param posNum The position of the word whose POS tag should be returned.
     * @return The Part-Of-Speech tag for the desired word position.
     * @see edu.illinois.cs.cogcomp.lbj.pos.POSTagger
     */
    public String getPOS(int posNum);
    
    /**
     * Determines the word number (zero-based) 
     * of the word at {@code charNum},
     * or if no word is at charNum, return the word number of the closest
     * word appearing after charNum, or if no such word exists, return -1.
     * @param charNum The character number.
     * @return The word number corresponding to the specified character number.
     */
    public int getWordNum(int charNum);
    
    /**
     * Gets the word number of the first word in the main text of the document
     * (as distinguished from headlines and metadata that may be included in
     * the plain text.)
     * @return The word number of the first word in the main text.
     */
    public int getTextFirstWordNum();
    
    /**
     * Gets the zero-based position of the first character of a word. 
     * @param wordNum The zero-based position of the word in the document.
     * @return The zero-based position of the first character in the word
     * within into the plain text,
     * or {@code -1} if {@code wordNum} is invalid.
     */
    public int getStartCharNum(int wordNum);
    
    /**
     * Indicates the number of nested quotes the specified word is in.
     * 0 is the base level of the text.
     * @param wordNum The position of the specified word.
     * @return The number of nested quotes.
     */
    public int getQuoteNestLevel(int wordNum);
    
    /**
     * Get the Name Entity Span of a given prosition
     * @param headPosition: The position of the specified word.
     * @return The {@link Constituent} indicates the NER span.
     */
    public Constituent getNameEntitySpan(int headPosition); 
    
    /* Word Statistics */
    
    /**
     * Gets the inverse true head frequency
     * of the word at the specified position.
     * @param wordNum The position in the document of the specified word.
     * @return The inverse true head frequency of the specified word,
     * or 1.0 if the word is not in a true head.
     * @see #getInverseTrueHeadFreq(String)
     */
    public double getInverseTrueHeadFreq(int wordNum);
    
    /**
     * Gets the inverse of the number of occurrences of the specified word
     * in the heads of the true mentions in the document.
     * @param word The specified word.
     * @return The inverse true head frequency of the specified word,
     * or 1.0 if the word is not found in any heads.
     */    
    public double getInverseTrueHeadFreq(String word);
    
    /**
     * Gets the inverse of the number of occurrences of the specified word
     * in the document.
     * Not normalized.
     * @param word The specified word.
     * @return The inverse of the number of times the word occurs in the
     * document, or 1.0 if the word does not occur.
     */
    public double getInDocInverseFreq(String word);

    /**
     * Gets the inverse of the number of occurrences of the specified word
     * in the corpus.
     * Not normalized.
     * @param word The specified word.
     * @return The inverse of the number of times the word occurs in the
     * corpus, or 1.0 if the word does not occur.
     */
    public double getInCorpusInverseFreq(String word);

    /**
     * Sets the corpus counts for the words in the corpus.
     * Makes a copy of the map, which may be slow or space consuming.
     * @param counts A map from words to counts of words in the corpus.
     */
    public void setCorpusCounts(Map<String,Integer> counts);
    
    /**
     * Gets the counts for the words in the document.
     * Returns a copy, which may be slow or space consuming.
     * @return A map from words to counts of words in the document.
     */
    public Map<String,Integer> getWholeDocCounts();


    
    
    
    /* Relations */
   
    /**
     * Gets the specified relation.  Relations are not yet emphasized.
     * @param number the number of the desired relation.
     * @return The desired relation.
     */
    public Relation getRelation(int number);
    
    /**
     * Gets the number of relations.
     * @return The number of relations.
     */
    public int getNumRelations();









    /* Output */

    /**
     * Gets the document as a string annotated with mention boundaries,
     * with square brackets for true mentions, asterisks for false alarms,
     * and triangle brackets for missed mentions,
     * and optionally annotated
     * with Part-Of-Speech tags, mention types, entity types, and entity IDs.
     * Predicted Entity IDs will be shown if available. 
     * @param showPOS Whether the Part-Of-Speech tags should be shown.
     * @param showMTypes Whether mention types should be shown.
     * @param showETypes Whether entity types should be shown.
     * @param showEIDs Whether entity IDs should be shown.
     * @return The text of the document, annotated.
     */
    public String toAnnotatedString(boolean showPOS, boolean showMTypes,
	    boolean showETypes, boolean showEIDs);
    
    /**
     * Gets the document as a string annotated with mention boundaries,
     * with square brackets for true mentions, asterisks for false alarms,
     * and triangle brackets for missed mentions,
     * and optionally annotated with Part-Of-Speech tags.
     * @param showPOS Whether the Part-Of-Speech tags should be shown.
     * @return The text of the document, annotated.
     */
    public String toAnnotatedString(boolean showPOS);


    /**
     * Gets the document as a string where each mention
     * has been replaced by the most specific mention coreferential with it.
     * @return The doc as a string with each mention
     * represented by its most specific coreferential mention.
     */
    public String toSubstituteString();

    /**
     * Gets a grid indicating the mention type for each combination
     * of entities and sentences.
     * If a mention is predicted to belong to its true entity,
     * its mention type will be uppercase;
     * but if it is predicted to be in the wrong entity
     * (due to coreference mistake) its mention type will be lowercase
     * and the mention's entity ID will be appended after its mention type.
     * @param usePred Whether predicted entities should be used.
     * @return A map from entities to a map from sentence numbers to strings,
     * representing the grid described above.
     */
    public Map<Entity,Map<Integer,String>> getCoherenceInfo(boolean usePred);
    
    /**
     * Gets the coherence info using the value of
     * {@code usePredictedEntities()} to determine whether
     * to use predicted entities.
     * @return Coherence info as described in the one parameter version
     * of this method.
     */
    public Map<Entity,Map<Integer,String>> getCoherenceInfo();

    /**
     * Gets the coherence grid represented as a string,
     * laid out in a grid.
     * @param usePred
     * @return A coherence grid as a string.
     * @see #getCoherenceInfo()
     */
    public String toCoherenceTableString(boolean usePred);

    /**
     * Gets the coherence grid represented as a string,
     * laid out in a grid.
     * Predicted entities will be used as determined by the value of
     * {@code usePredictedEntities()}.
     * @return A coherence grid as a string.
     * @see #getCoherenceInfo()
     */
    public String toCoherenceTableString();

    /**
     * Writes the document to a file using serialization.
     * @throws IOException
     */
    public void save() throws IOException;

    /**
     * Writes this Doc in the appropriate format. 
     * @param usePredictions Whether predicted mentions and entities
     * should be written.
     */
    public void write(boolean usePredictions);
    
    /** 
     * Writes this Doc in the appropriate format.
     * @param filename The name of the target file.
     * @param usePredictions Whether predicted mentions and entities
     * should be written.
     */
    public void write(String filename, boolean usePredictions);

	public void setGigaWord(GigaWord gw);
	
	public GigaWord getGigaWord();

    //Note that a document is typically only hashable and equals by identity.
}
