package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;
import edu.illinois.cs.cogcomp.lbj.coref.util.xml.XMLException;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Maps;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Pair;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbj.coref.alignment.Aligner;
import edu.illinois.cs.cogcomp.lbj.coref.alignment.DefaultEMAligner;
import edu.illinois.cs.cogcomp.lbj.coref.alignment.ExactAligner;
import edu.illinois.cs.cogcomp.lbj.coref.features.ContextFeatures;
import edu.illinois.cs.cogcomp.lbj.coref.features.GigaWord;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoad;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.ParsePhraseResult;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.ParseWordResult;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.EntityByFirstMentionComparator;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.MentionSpecificityComparator;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.GExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.Relation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

/**
 * Represents one document from a corpus, including the text, annotations of
 * coreference, relations, entities, and other relevant information. Also
 * contains methods to load input from XML files.
 * 
 * @author Eric Bengtson
 */
public abstract class DocBase implements Doc, Serializable {
	private static final long serialVersionUID = 50L;

	protected String m_baseFN; // Base File Name

	static int totalMentions = 0, goodStarts = 0, goodEnds = 0, medEnds = 0;

	private GigaWord gw = null;
	
	private boolean m_bUsePredEntities = false;
	private List<Entity> m_trueEntities;
	private List<Entity> m_predEntities;
	private ChainSolution<Mention> m_corefChains = null;

	private List<Relation> m_relations;

	private boolean m_bUsePredMentions = false;
	private List<Mention> m_trueMentions;
	private boolean m_trueMentionsSorted = false;
	private boolean m_predMentionsSorted = false;

	private List<Mention> m_predMentions;
        private List<Mention> addMents;
	private Map<Mention, Integer> m_trueMentionProsition;
	private Map<Mention, Integer> m_predMentionProsition;

	private Aligner<Mention> m_defaultAligner = null;
	private Map<Mention, Mention> m_predToTrueMention = null;

	protected Classifier m_caser = null;
	protected boolean m_bNeedsCasing = false;

	private List<List<String>> m_phrases;

	// Doc Metadata:
	protected String m_source;
	protected String m_docType;
	protected String m_version;
	protected String m_annotationAuthor;
	protected String m_encoding;

	protected String m_docID;
	protected String m_slug;
	protected String m_dateTime;
	protected String m_headline;
	protected String m_text;
	private TextAnnotation m_ta;
	private TextAnnotation[] m_tripleTA;

	private int m_textStartCharNum = 0;

	private List<String> m_words;
	private List<String> m_pos; // Corresponding to words.
	private List<Integer> m_quoteNestLevel; // corresponding to words. 

	private Map<String, Integer> m_mentWordCounts;
	private Map<String, Integer> m_docWordCounts;
	private Map<String, Integer> m_corpusWordCounts;

	// Text, not including html, and with newlines preprocessed to match
	// character start and end numbering in .apf.xml file:
	// Does include content between tags, including slug.
	private String m_countingText;

	private Map<Integer, Set<Mention>> m_trueHeadStartWordNumMentionMap;
	private Map<Integer, Set<Mention>> m_trueExtentStartWordNumMentionMap;
	private Map<Integer, Set<Mention>> m_predHeadStartWordNumMentionMap;
	private Map<Integer, Set<Mention>> m_predExtentStartWordNumMentionMap;
	// Index of character in a word -> wordIndex:
	private Map<Integer, Integer> m_charWordMap;

	private int m_nSents;

	private Map<Integer, Integer> m_wordNumSentNumMap;
	private Map<Integer, Integer> m_sentNumWordNumMap;
	// wordIndex -> Index of first character in a word:
	private Map<Integer, Integer> m_wordNumCharNumMap;

	private Map<Pair<Integer, Integer>, Pair<List<Mention>, List<Mention>>> m_sentenceMentionsPair;

	// Not initialized until use:
	private List<List<Mention>> m_mentsInSents;
	private Map<Mention, Set<Mention>> m_mentionsContaining;
	private Map<Mention, Mention> m_bestMentionMap;

	/*
	 * Memoization tool. A place for a learner to store predictions about
	 * whether an interval of words [firstWN, lastWN] is a head. Maps within the
	 * outer map shall be created on demand.
	 */
	private Map<Integer, Map<Integer, Boolean>> m_headPredictionMap;
	private Map<Pair<Mention, Mention>, CExample> m_cExMap;
	private Map<Mention, GExample> m_gExMap;

	private Boolean m_isCaseSensitive = null; // null meaning unknown

	private DocType docType = DocType.UnKnown;
	protected String domain = "unknown";

	/* Enums */

	public static enum PosSource {LBJ, FILE, SNOW, THISFILE}

	/* Constructors */

	/** Basic constructor: Not recommended. */
	public DocBase() {
		initMembersDefault();
	}

	
	/* Loading */

	protected void initMembersDefault() {
		m_predEntities = new ArrayList<Entity>();
		m_trueEntities = new ArrayList<Entity>();
		m_relations = new ArrayList<Relation>();
		m_predMentions = new ArrayList<Mention>();
		m_trueMentions = new ArrayList<Mention>();

		m_words = new ArrayList<String>();
		m_pos = new ArrayList<String>();
		m_mentWordCounts = new HashMap<String, Integer>();
		m_docWordCounts = new HashMap<String, Integer>();

		// NOTE: Currently initialized externally.
		m_corpusWordCounts = new HashMap<String, Integer>();
		m_quoteNestLevel = new ArrayList<Integer>();
		m_phrases = new ArrayList<List<String>>();
		m_trueHeadStartWordNumMentionMap = new HashMap<Integer, Set<Mention>>();
		m_trueExtentStartWordNumMentionMap = new HashMap<Integer, Set<Mention>>();
		m_predHeadStartWordNumMentionMap = new HashMap<Integer, Set<Mention>>();
		m_predExtentStartWordNumMentionMap = new HashMap<Integer, Set<Mention>>();
		m_charWordMap = new HashMap<Integer, Integer>();
		m_wordNumCharNumMap = new HashMap<Integer, Integer>();
		m_wordNumSentNumMap = new HashMap<Integer, Integer>();
		m_sentNumWordNumMap = new HashMap<Integer, Integer>();
		m_headPredictionMap = new HashMap<Integer, Map<Integer, Boolean>>();
		m_cExMap = new HashMap<Pair<Mention, Mention>, CExample>();
		m_gExMap = new HashMap<Mention, GExample>();
		m_sentenceMentionsPair = new HashMap<Pair<Integer, Integer>, Pair<List<Mention>, List<Mention>>>();
		if(Parameters.aligner ==null)
			m_defaultAligner = new DefaultEMAligner();
		else if(Parameters.aligner.equals("exact"))
			m_defaultAligner = new ExactAligner<Mention>();
		m_trueMentionProsition = new HashMap<Mention, Integer>();
		m_predMentionProsition = new HashMap<Mention, Integer>();
	}

	/**
	 * @param filename
	 *            The file containing the text of the document.
	 * @throws XMLException
	 */
	public void loadSGMText(String filename) {
		InputStream in = this.getClass().getResourceAsStream("/" + filename);
		if (in == null) {
			System.err.println("Cannot find file " + filename);
			return; // TODO: Something?
		}
		String fullText = myIO.readAll(in);
		int openTag = fullText.indexOf("<DOCNO>");
		if (openTag < 0)
			openTag = fullText.indexOf("<DOCID>");
		int closeTag = fullText.indexOf("</DOCNO>");
		if (closeTag < 0)
			closeTag = fullText.indexOf("</DOCID>");
		if (openTag < 0 || closeTag < 0) {
			System.err.println("cannot find docno tags in doc: " + filename);
		}
		m_docID = fullText.substring(openTag + 7, closeTag).trim();

		int textStart = fullText.indexOf("<TEXT>");
		if (textStart >= 0) {
			textStart += 7; // Pass tag and a newline.
			String preText = fullText.substring(0, textStart);
			preText = removeTagsAndExtraNL(preText);
			m_textStartCharNum = preText.length();
		}

		String countingText = removeTagsAndExtraNL(fullText);

		setPlainText(countingText);
	}

	protected String removeTagsAndExtraNL(String a) {
		// Replace newlines with linefeeds:
		String b = a.replaceAll("\r\n", "\n");

		// Replace tags with empty strings (.*? is non-greedy):
		Pattern p = Pattern.compile("<.*?>");
		Matcher match = p.matcher(b);
		b = match.replaceAll("");
		return b;
	}

	/**
	 * Loads text that has been preprocessed. Derives word splits, sentence
	 * splits, and POS tags from the specified string.
	 * 
	 * @param content
	 *            The text annotated with part of speech tags.
	 */
	protected void loadPOSTags(String content) {
		// Note: This function ignores newlines inside word or phrase
		// boundaries.
		// Such should not happen except because of malformed input.
		// System.out.println("Content: " + content);
		int position = 0;
		int cursor = 0; // character position in plain text.
		int sentNum = 0;
		List<Integer> sentNums = new ArrayList<Integer>();
		List<String> posTags = new ArrayList<String>();
		List<String> words = new ArrayList<String>();
		while (position < content.length()) {
			if (Character.isWhitespace(content.charAt(position))) {
				// Warning: If \r\n newlines, this might get counted twice...
				// NOTE: Changed this from Character.LINE_SEPARATOR to
				// "\n" || "\r" because the current system doesn't determine
				// the form of newlines in the file
				char thisChar = content.charAt(position);
				if (thisChar == '\n' || thisChar == '\r') {
					++sentNum;
					if (position + 1 < content.length()
							&& (content.charAt(position + 1) == '\n' || content
									.charAt(position + 1) == '\r')) {
						++position; // skip that one too.
					}
				}
				++position;
				continue;
			} else if (content.charAt(position) == '(') {
				ParseWordResult res = DocLoad.parsePOSWordPair(content,
						position);
				if (res.m_nextPosition < 0) {
					System.err.println("Error: Malformed input after char "
							+ position);
					return;
				}
				if (res.m_nextPosition <= position) { // No progress.
					System.err
							.println("No progress parsing POS tags at position"
									+ position);
					break;
				}
				position = res.m_nextPosition;
				// TODO: Fix bug pertaining to cursor's non-use?
				if (!res.m_word.equals("")) {
					sentNums.add(sentNum);
					String w = res.m_word;
					String newW = w;
					if (w.equals("-LBR-") || w.equals("-RBR-")) {
						newW = translateEscaped(w, cursor);
						w = newW;
					} else if (w.equals("\"")) {
						newW = translateEscaped(w, cursor);
						// Leave w alone, so keep unescaped as word.
					}
					int newCursor = getPlainText().indexOf(newW, cursor);
					if (newCursor <= -1) {
						throw new RuntimeException("Cannot align " + w
								+ " in text "
								+ getPlainText().substring(cursor));
					}
					cursor = newCursor + newW.length();
					words.add(w);
					posTags.add(res.m_partOfSpeech);
				} else {
					System.err.println("Empty word at position " + position);
				}
			} // End if "(", or whitespace
		} // End while.

		// FIXME: Uncomment words/sentence setters:
		setWords(words); // MODIFIED: Uncommented.
		setPOSTags(posTags);
		setSentenceNumbers(sentNums); // MODIFIED: Uncommented.
	}

	/**
	 * Loads the output of the SNoW-based POS tagger. Uses the SNoW-based POS
	 * tagger given on the command line. Should not be called until the text has
	 * been set.
	 * 
	 * @return The POS-tagged content, or null on failure. The format of the
	 *         output is as follows: {@literal (WORD POS) ... } with one line
	 *         per sentence.
	 */
	protected String loadPOSTaggerOutput() {
		// Write plain text to a temporary file:
		File tmpPlain = null;
		PrintWriter plainWriter = null;
		try {
			tmpPlain = File.createTempFile("corefLBJ", ".tmp");
			plainWriter = new PrintWriter(tmpPlain);
			// FIXME: Use sentence/word split text instead.
			for (int i = 0; i < getWords().size(); ++i) {
				plainWriter.print(getWords().get(i));
				// Write word/sentence split boundaries:
				if (i + 1 < getWords().size()) {
					if (getSentNum(i) == getSentNum(i + 1))
						plainWriter.print(" ");
					else
						plainWriter.print("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace(); // TODO: Log or supress message instead.
			if (tmpPlain != null && tmpPlain.exists())
				tmpPlain.delete();
			return null;
		} finally {
			if (plainWriter != null)
				plainWriter.close();
		}

		File tmpTagged = null;
		try {
			tmpTagged = File.createTempFile("corefLBJ", ".tmp");
		} catch (IOException e) {
			if (tmpPlain != null && tmpPlain.exists())
				tmpPlain.delete();

			// Does this exception mean file not created? Delete to be safe.
			if (tmpTagged != null && tmpTagged.exists())
				tmpTagged.delete();

			return null;
		}

		Map<String, String> env = System.getenv();
		String posPath = null;
		if (env.containsKey("PATH_POS"))
			posPath = env.get("PATH_POS");
		String cmd = "";
		if (posPath != null)
			cmd = posPath + "/";
		cmd += "tagger -i " + tmpPlain + " -o " + tmpTagged;
		try {
			Runtime rt = Runtime.getRuntime();
			Process tagger = rt.exec(cmd);
			tagger.waitFor(); // Be sure command is finished.
		} catch (IOException e) {
			e.printStackTrace();
			if (tmpTagged != null && tmpTagged.exists())
				tmpTagged.delete();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO: ?
		} finally {
			if (tmpPlain != null && tmpPlain.exists())
				tmpPlain.delete(); // Delete here and at all previous returns.
		}

		// System.err.println("About to read temp file " + tmpTagged);
		String taggedText = myIO.readAll(tmpTagged);
		// System.err.println("Read: " + taggedText);
		if (taggedText == null)
			System.err.println("Cannot load POS-tagged temp-file.");

		if (tmpTagged != null && tmpTagged.exists())
			tmpTagged.delete(); // Delete here and at all previous returns.

		return taggedText;
	}

	/**
	 * Loads text that has been preprocessed offline. Derives word splits,
	 * sentence splits, and POS tags from the specified file.
	 * 
	 * @param filename
	 *            The name of a file containing the chunked text.
	 */
	protected void loadChunkedText(String filename) {
		// Note: This function ignores newlines inside word or phrase
		// boundaries.
		// Such should not happen except because of malformed input.
		InputStream in = this.getClass().getResourceAsStream("/" + filename);
		if (in == null) {
			System.err.println("Cannot find file " + filename);
			return; // TODO: Something?
		}
		String content = myIO.readAll(in);
		// System.out.println("Content: " + content);
		int position = 0;
		int cursor = 0; // character position in plain text.
		int sentNum = 0;
		List<Integer> sentNums = new ArrayList<Integer>();
		List<String> posTags = new ArrayList<String>();
		List<String> words = new ArrayList<String>();
		while (position < content.length()) {
			if (Character.isWhitespace(content.charAt(position))) {
				// Warning: If \r\n newlines, this might get counted twice...
				// NOTE: Changed this from Character.LINE_SEPARATOR to
				// "\n" || "\r" because the current system doesn't determine
				// the form of newlines in the file
				char thisChar = content.charAt(position);
				if (thisChar == '\n' || thisChar == '\r') {
					++sentNum;
					if (position + 1 < content.length()
							&& (content.charAt(position + 1) == '\n' || content
									.charAt(position + 1) == '\r')) {
						++position; // skip that one too.
					}
				}
				++position;
				continue;
			} else if (content.charAt(position) == '(') {
				ParseWordResult res = DocLoad.parsePOSWordPair(content,
						position);
				if (res.m_nextPosition < 0) {
					System.err.println("Error: Malformed input after char "
							+ position + " in file " + filename);
					return;
				}
				position = res.m_nextPosition;
				if (!res.m_word.equals("")) {
					sentNums.add(sentNum);
					String w = res.m_word;
					String newW = w;
					if (w.equals("-LBR-") || w.equals("-RBR-")) {
						newW = translateEscaped(w, cursor);
						w = newW;
					} else if (w.equals("\"")) {
						newW = translateEscaped(w, cursor);
						// Leave w alone, so keep unescaped as word.
					}
					int newCursor = getPlainText().indexOf(newW, cursor);
					if (newCursor <= -1) {
						throw new RuntimeException("Cannot align " + w
								+ " in text "
								+ getPlainText().substring(cursor));
					}
					cursor = newCursor + newW.length();
					words.add(w);
					List<String> phrase = new ArrayList<String>();
					phrase.add(w);
					posTags.add(res.m_partOfSpeech);
					m_phrases.add(phrase);
				}
			} else if (content.charAt(position) == '[') {
				ParsePhraseResult res = DocLoad.parsePhrase(content, position);
				if (res.m_nextPosition < 0) {
					System.err.println("Error: Malformed input after char "
							+ position + " in file " + filename);
					return;
				}
				posTags.addAll(res.m_partsOfSpeech);
				for (String w : res.m_words) {
					String newW = w;
					if (w.equals("-LBR-") || w.equals("-RBR-")) {
						newW = translateEscaped(w, cursor);
						w = newW;
					} else if (w.equals("\"")) {
						newW = translateEscaped(w, cursor);
						// Leave w alone, so keep unescaped as word.
					}
					int newCursor = getPlainText().indexOf(newW, cursor);
					if (newCursor <= -1) {
						throw new RuntimeException("Cannot align " + w
								+ " in text "
								+ getPlainText().substring(cursor));
					}
					cursor = newCursor + newW.length();
					words.add(w);
					sentNums.add(sentNum);
				}
				m_phrases.add(res.m_words);
				position = res.m_nextPosition;
			}
		}
		setWords(words);
		setPOSTags(posTags);
		setSentenceNumbers(sentNums);
		// System.out.println("Finished loading chunked text");
	}

	/**
	 * Translates an escaped round, square, or curly brace escaped as
	 * {@literal -LBR-} or {@literal -RBR-}, or an escaped pair of quotes,
	 * escaped as a double quote charaacter.
	 * 
	 * @return a brace or escaped if no matching brace recognized.
	 */
	protected String translateEscaped(String escaped, int cursor) {
		int cbSpot = -1, sbSpot = -1, rbSpot = -1;
		if (escaped.equals("-LBR-")) {
			cbSpot = m_countingText.indexOf("{", cursor);
			sbSpot = m_countingText.indexOf("[", cursor);
			rbSpot = m_countingText.indexOf("(", cursor);

			if (cbSpot < 0 && sbSpot < 0 && rbSpot < 0)
				return escaped;
			if (cbSpot < 0)
				cbSpot = Integer.MAX_VALUE;
			if (sbSpot < 0)
				sbSpot = Integer.MAX_VALUE;
			if (rbSpot < 0)
				rbSpot = Integer.MAX_VALUE;

			if (cbSpot < sbSpot && cbSpot < rbSpot) {
				return "{";
			} else if (rbSpot < sbSpot) { // && implied rb <= cb
				return "(";
			} else {
				return "[";
			}
		} else if (escaped.equals("-RBR-")) {
			cbSpot = m_countingText.indexOf("}", cursor);
			sbSpot = m_countingText.indexOf("]", cursor);
			rbSpot = m_countingText.indexOf(")", cursor);

			if (cbSpot < 0 && sbSpot < 0 && rbSpot < 0)
				return escaped;
			if (cbSpot < 0)
				cbSpot = Integer.MAX_VALUE;
			if (sbSpot < 0)
				sbSpot = Integer.MAX_VALUE;
			if (rbSpot < 0)
				rbSpot = Integer.MAX_VALUE;

			if (cbSpot < sbSpot && cbSpot < rbSpot) {
				return "}";
			} else if (rbSpot < sbSpot) { // && implied rb < cb
				return ")";
			} else {
				return "]";
			}
		} else if (escaped.equals("\"")) {

			// int twoOpenSpot = m_countingText.indexOf("``", cursor);
			int twoCloseSpot = m_countingText.indexOf("\'\'", cursor);
			int oneDoubleSpot = m_countingText.indexOf("\"", cursor);
			if (
			// twoOpenSpot < 0 &&
			twoCloseSpot < 0 && oneDoubleSpot < 0) {
				return escaped;
			}

			// if (twoOpenSpot < 0) twoOpenSpot = Integer.MAX_VALUE;
			if (twoCloseSpot < 0)
				twoCloseSpot = Integer.MAX_VALUE;
			if (oneDoubleSpot < 0)
				oneDoubleSpot = Integer.MAX_VALUE;

			// if (twoOpenSpot < twoCloseSpot && twoOpenSpot < oneDoubleSpot) {
			// return "``";
			// } else
			if (twoCloseSpot < oneDoubleSpot) {
				return "\'\'";
			} else {
				return "\"";
			}
			/*
			 * //But do this instead, for compatibility with EMNLP: //Only when
			 * looking at quote levels and word counts //And maybe for mapping
			 * word numbers to character numbers. if (oneDoubleSpot < 0) {
			 * return "\'\'"; } else { return "\""; }
			 */
		} else {
			return escaped;
		}
	}

	/**
	 * Determines the location of quotes and sets them. Must be called after
	 * plain text and words are set.
	 */
	public void calcAndSetQuotes() {
		List<Integer> quoteLevels = new ArrayList<Integer>();

		// TODO: Allow nested quotes.
		int quoteNestLevel = 0;
		//Use POS instead of Words to set Quote
		for(int i=0; i< m_pos.size();i++){
			String sPos = m_pos.get(i);
			if(sPos.equals("``"))
				quoteNestLevel = 1;
			if(sPos.equals("''"))
				quoteNestLevel = 0;
			quoteLevels.add(quoteNestLevel);
		}
		setQuoteLevels(quoteLevels);
	}
	// FIXME: Chunk in java.
	/**
	 * Builds the document from the given plain text, automatically splitting
	 * sentences, determining quote levels, determining part-of-speech tags, and
	 * splitting words by an automatic word-splitting algorithm. Mentions and
	 * entities will not be set here.
	 * 
	 * @param plainText
	 *            The text of the document.
	 */
	public void loadFromText(String plainText) {
		boolean doWordSplit = true;
		boolean doPOSTag = true;
		loadFromText(plainText, doWordSplit, doPOSTag);
	}

	/**
	 * Builds the document from the given plain text, automatically splitting
	 * sentences, determining quote levels, determining part-of-speech tags, and
	 * either splitting words by whitespace or using a word-splitter. Mentions
	 * and entities will not be set here.
	 * 
	 * @param plainText
	 *            The text of the document.
	 * @param doWordSplit
	 *            If true, words will be split by an automatic word-splitting
	 *            algorithm; otherwise words will be assumed to be separated by
	 *            whitespace.
	 * @param doPOSTag
	 *            If true, POS tags will be generated by the LBJPOS algorithm.
	 *            Otherwise, no tags will be set.
	 */
	public void loadFromText(String plainText, boolean doWordSplit,
			boolean doPOSTag) {
		if (plainText == null) {
			System.err.println("Attempting to load Doc with null text.");
			return;
		}
/*		
		plainText = plainText.replace('\r', ' ');
		plainText = plainText.replace('\n', ' ');
*/		
		ArrayList<String> textTmp = new ArrayList<String>();
		textTmp.add(plainText);
		try {
			myIO.writeLines("tmpForSentenceSplitter.txt", textTmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SentenceSplitter ss = new SentenceSplitter("tmpForSentenceSplitter.txt");
		Sentence[] sents = ss.splitAll();
		
		//System.out.println("Getting Snetences: "+ sents.length);

		// Build words list:
		List<Integer> quoteLevels = new ArrayList<Integer>();
		List<Integer> sentNums = new ArrayList<Integer>();
		List<String> wordForms = new ArrayList<String>();
		List<String> posTags = new ArrayList<String>();
		LinkedVector allWords = new LinkedVector();
		int quoteLevel = 0, sentNum = 0;

		for (Sentence sent : sents) {
			LinkedVector sentWords;

			if (doWordSplit) {
				sentWords = sent.wordSplit();
			} else {
				sentWords = new LinkedVector();
				for (String wS : sent.toString().split("\\s+")) {
					sentWords.add(new Word(wS)); // TODO: Offsets?
				}
			}

			Token t = null;
			int N = sentWords.size();
			for (int i = 0; i < N; ++i) {
				Word w = (Word) sentWords.get(i);
				if (i == 0)
					t = new Token(w, null, null);
				else {
					t.next = new Token(w, t, null);
					t = (Token) t.next;
				}

				String form = w.form;
				// TODO: Handle overlapping words (in allwords and better).
				if (doWordSplit && w.previous != null
						&& w.start <= w.previous.end) {
					Word prev = (Word) w.previous;
					int overlapNum = prev.end - w.start + 1;
					form = form.substring(overlapNum);
					// System.err.println("Overlapping words: "
					// + prev.form + ", " + w.form + ": " + form);
					continue;
				}
				wordForms.add(form);

				allWords.add(w);
				sentNums.add(sentNum);

				if (form.equals("\"") || form.equals("\'\'")
						|| form.equals("\'")) {
					quoteLevel = 1 - quoteLevel; // toggle 0/1
				}
				quoteLevels.add(quoteLevel);
			} // End for words.

			if (doPOSTag) {
				sentWords = new LinkedVector(t);
				for (int i = 0; i < N; ++i) {
					t = (Token) sentWords.get(i);
					String posTag = "";
					posTags.add(posTag);
				}
			}

			sentNum++;
		} // End for sent.

		setPlainText(plainText);
		setWords(wordForms);
		if (doPOSTag)
			setPOSTags(posTags);
		setQuoteLevels(quoteLevels);
		setSentenceNumbers(sentNums);
	}

	/**
	 * Should be set before words are set.
	 * 
	 * @param text
	 *            The plain text, used for determining character counts.
	 */
	protected void setPlainText(String text) {
		if (text == null)
			System.err.println("Setting text to null!");
		m_countingText = text;
	}

	public void setWords(List<String> words) {
		boolean backwardsCompatible = true;
		setWords(words, backwardsCompatible);
	}

	/**
	 * Sets the words, aligns them with the plain text, and records statistics
	 * about them. Must be called after {@code setPlainText()} has been called.
	 * 
	 * @param words
	 *            The words (copied defensively).
	 * @param backwardsCompatible
	 *            Attempt to alter the algorithm to conform to behavior in
	 *            previous published paper.
	 */
	public void setWords(List<String> words, boolean backwardsCompatible) {
		//System.out.println(words);
		//System.out.println(m_countingText);
		if (words.size() <= 0)
			System.err.println("No words specified.");
		if (m_countingText == null) {
			throw new RuntimeException("No counting text specified.");
		}

		m_words = new ArrayList<String>(words);
		m_docWordCounts = new HashMap<String, Integer>();
		m_charWordMap = new HashMap<Integer, Integer>();
		int charNum = 0; // Cursor location.
		for (int wordNum = 0; wordNum < words.size(); ++wordNum) {
			String w = words.get(wordNum);
			int next = charNum;
			charNum = m_countingText.indexOf(w, charNum);
			if (backwardsCompatible && charNum < 0 && w.equals("\"")) {
				// System.out.println("Warning: didn't find double quote.");
				// Might have been changed from long quotes:
				charNum = m_countingText.indexOf("\'\'", next);
				if (charNum >= 0)
					w = "\'\'"; // Change back.
			}
			if (charNum < 0) {
				System.err.println("Cannot align " + w);
				System.err.println("Words were: " + words);
				System.err.println("Text was: " + m_countingText);
				break; // TODO: Continue here?
			}
			//System.out.println(wordNum + ": " + w + " at " + charNum);
			recordWordLocation(wordNum, charNum, charNum + w.length() - 1);
			Maps.addOne(m_docWordCounts, w);
			charNum += w.length(); // move cursor past word.
		}
	}

	/**
	 * Sets the POS tags. The number of POS tags must equal the number of words
	 * already set. Thus, this method must be called after {@code setWords()}
	 * 
	 * @param tags
	 *            A list of POS tags, in the same order as the words (copied
	 *            defensively).
	 * @throws IllegalArgumentException
	 *             if {@code tags.size() != words.size()}
	 */
	protected void setPOSTags(List<String> tags) {
		m_pos = new ArrayList<String>(tags);
		if (m_words.size() != m_pos.size()) {
			throw new IllegalArgumentException(
					"Different number of POS tags than words.");
		}
	}

	/**
	 * Sets the quote levels, which indicate the number of nested quotations in
	 * which each word is embedded. The number of elements in the List should
	 * equal the number of words already set. Must be called after
	 * {@code setWords()}
	 * 
	 * @param quoteLevels
	 *            A list of quote levels, in the same order as the words (copied
	 *            defensively).
	 * @throws IllegalArgumentException
	 *             if {@code quoteLevels.size() != words.size()}
	 */
	public void setQuoteLevels(List<Integer> quoteLevels) {
		m_quoteNestLevel = new ArrayList<Integer>(quoteLevels);
		if (m_words.size() != m_quoteNestLevel.size()) {
			throw new IllegalArgumentException(
					"Different number of words than quote levels.");
		}
	}

	/**
	 * Sets the sentence numbers for each word. The number of elements should
	 * equal the number of words already set. Must be called after
	 * {@code setWords()}
	 * 
	 * @param sentNums
	 *            A list of sentence numbers, in the same order as the words
	 *            (copied defensively).
	 * @throws IllegalArgumentException
	 *             if {@code sentNums.size() != words.size()} or if
	 *             {@code sentNums} is non-monotonic.
	 */
	protected void setSentenceNumbers(List<Integer> sentNums) {
		if (m_words.size() != sentNums.size()) {
			throw new IllegalArgumentException(
					"Different number of sentence numbers than words.");
		}

		m_wordNumSentNumMap.clear();
		m_sentNumWordNumMap.clear();
		int i = 0, max = 0;
		int lastSent = -1;
		for (int sentNum : sentNums) {
			m_wordNumSentNumMap.put(i, sentNum);
			if (lastSent != sentNum) {
				m_sentNumWordNumMap.put(sentNum, i);
				lastSent = sentNum;
			}
			if (sentNum < max) { // Non-monotonic; indicates error:
				throw new IllegalArgumentException(
						"Non-monotonic sentence numbers");
			}
			if (sentNum > max)
				max = sentNum;
			++i;
		}

		m_nSents = max;
	}

	/**
	 * Records the fact that a word is located at characters {@code startCN}
	 * through {@code endCN} (inclusive).
	 */
	protected void recordWordLocation(int wn, int startCN, int endCN) {
		m_wordNumCharNumMap.put(wn, startCN);
		for (int i = startCN; i <= endCN; ++i) {
			m_charWordMap.put(i, wn);
		}
	}

	/* Text */

	public String getPlainText() {
		return m_countingText; // This text is 'more plain' than m_text
	}

	/* Metadata */

	public String getDocID() {
		return m_docID;
	}

	public boolean isCaseSensitive() {
		// In absence of other info, compute by checking for any case change:
		if (m_isCaseSensitive != null)
			return m_isCaseSensitive;

		// Determine whether text could use auto-casing.
		boolean caseSensitive = false;
		// Go through the words, but skip the metadata.
		// FIXME: A better way to tell about case sensitivity
		List<String> words = m_words.subList(getTextFirstWordNum(),
				m_words.size());
		for (String word : words) {
			if (!word.toLowerCase().equals(word) && !word.equals("AMP")) {
				caseSensitive = true;
				break;
			}
		}
		m_isCaseSensitive = caseSensitive;
		return m_isCaseSensitive;
	}

	/* Sentences */

	public int getSentNum(int wordNum) {
		if (!m_wordNumSentNumMap.containsKey(wordNum)) {
			// Debug.p("Didn't find the word in the map from wordNum to sentNum"
			// + " with wordNum:" + wordNum + " In Doc:" + m_docID);
			return -1;
		}
		return m_wordNumSentNumMap.get(wordNum);
	}

	public int getSentStartNum(int sentNum) {
		if (!m_sentNumWordNumMap.containsKey(sentNum))
			return m_words.size();
			//return -1;
		return m_sentNumWordNumMap.get(sentNum);
	}

	public int getNumSentences() {
		return m_nSents;
	}

	/* Entities */

	public void setUsePredictedEntities(boolean usePred) {
		m_bUsePredEntities = usePred;
	}

	public boolean usePredictedEntities() {
		return m_bUsePredEntities;
	}

	// TODO: Decide if should never return true entities when usePred.
	public List<Entity> getEntities() {
		if (usePredictedEntities() && hasPredEntities()) {
			return getPredEntities();
		} else {
			return getTrueEntities();
		}
	}

	public List<Entity> getPredEntities() {
		return Collections.unmodifiableList(m_predEntities);
	}

	public List<Entity> getTrueEntities() {
		return Collections.unmodifiableList(m_trueEntities);
	}

	public ChainSolution<Mention> getCorefChains() {
		return m_corefChains;
	}

	/** Currently implemented slowly. */
	public Entity getEntityFor(Mention m) {
		return getEntityFor(m, getEntities());
	}

	/** Currently implemented slowly. */
	public Entity getEntityFor(Mention m, boolean usePred) {
		if (usePred)
			return getEntityFor(m, getPredEntities());
		else
			return getEntityFor(m, getTrueEntities());
	}

	/** Currently implemented slowly. */
	protected Entity getEntityFor(Mention m, List<Entity> entities) {
		// TODO: OPTIMIZE:
		for (Entity e : entities) {
			if (e.getMentions().contains(m)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Can be made public, but then need to ensure that e's mentions are all
	 * added.
	 */
	protected void addTrueEntity(Entity e) {
		m_trueEntities.add(e);
	}

	public void setPredEntities(ChainSolution<Mention> sol) {
		List<Entity> ents = new ArrayList<Entity>();
		int eNum = 1;
		int nextEID = 0;
		Set<String> eIDsUsedTrue = new HashSet<String>();
		if (m_trueEntities != null) {
			for (Entity e : m_trueEntities) {
				eIDsUsedTrue.add(e.getID());
			}
		}
		Set<String> eIDsUsedForPreds = new HashSet<String>();
		// TODO: Consider removing for speed:
		List<Set<Mention>> chains = new ArrayList<Set<Mention>>(
				sol.getSubsets());
		// Sort by smaller entity, or by mention IDs if same sized:
		Collections.sort(chains, new Comparator<Set<Mention>>() {
			public int compare(Set<Mention> a, Set<Mention> b) {
				List<Mention> al = new ArrayList<Mention>(a);
				Collections.sort(al);
				List<Mention> bl = new ArrayList<Mention>(b);
				Collections.sort(bl);
				int n = Math.min(al.size(), bl.size());
				for (int i = 0; i < n; ++i) {
					Mention am = al.get(i), bm = bl.get(i);
					int comp = am.getID().compareTo(bm.getID());
					if (comp != 0)
						return comp;
				}
				return al.size() - bl.size();
			}
		});

		for (Set<Mention> eMents : chains) {
			if (eMents.size() == 0)
				continue;
			List<Mention> mentList = new ArrayList<Mention>(eMents);
			Collections.sort(mentList);

			// Align this entity to best entity:
			// TODO: This only works if mentions are true mentions w labels.
			Set<String> eIDSet = new HashSet<String>();
			List<String> eIDList = new ArrayList<String>();
			for (Mention m : eMents) {
				eIDSet.add(m.getEntityID());
				eIDList.add(m.getEntityID());
			}
			int idCountMax = 0;
			String maxID = null;
			for (String eID : eIDSet) {
				int idCount = Collections.frequency(eIDList, eID);
				// In case of tie, pick alphabetically:
				if (idCount > idCountMax
						|| (idCount == idCountMax && eID.compareTo(maxID) <= 0)) {
					idCountMax = idCount;
					maxID = eID;
				}
			}
			if (maxID == null || maxID.equals("NONE")) {
				maxID = "" + nextEID;
				nextEID++;
			}
			if (eIDsUsedForPreds.contains(maxID)) {
				char alt = 'a';
				while (eIDsUsedForPreds.contains(maxID + alt))
					alt++;
				maxID += alt;
			}

			Mention anM = mentList.get(0);
			// TODO: Do the mentions' mention types always match
			// the entities' mention types?

			// TODO: A better way of choosing the representative name?
			List<Chunk> names = new ArrayList<Chunk>();
			for (Mention m : mentList) {
				if (m.getType().equals("NAM")) {
					names.add(m.getHead());
				}
			}

			String eID;
			if (m_trueEntities != null) {
				eID = maxID;
			} else {
				eID = m_docID + "-E" + eNum;
			}
			eIDsUsedForPreds.add(eID);

			for (Mention m : mentList) {
				m.setPredictedEntityID(eID);
				// TODO: Should we be doing this:?
				// String[] eIDParts = eID.split("-E");
				// String eIDLastPart = eIDParts[1];
				// String[] mIDParts = m.getID().split("-");
				// String mIDLastPart = "";
				// if (1 < mIDParts.length) mIDLastPart = mIDParts[1];
				// m.setID(eIDLastPart + "-" + mIDLastPart);
			}

			Entity e = new Entity(eID, anM.getEntityType(), anM.getSubtype(),
					anM.getSpecificity(), mentList, names);
			ents.add(e);
			eNum++;
		}

		m_predEntities = new ArrayList<Entity>(); // delete any old, and init.
		setUsePredictedEntities(true);
		this.addPredEntities(ents);
		m_corefChains = sol;
	}

	public boolean hasPredEntities() {
		return (m_predEntities != null && m_predEntities.size() > 0);
	}

	public boolean hasTrueEntities() {
		return (m_trueEntities != null && m_trueEntities.size() > 0);
	}

	/** Backed internally. */
	protected void addPredEntities(List<Entity> ents) {
		if (m_predEntities == null)
			m_predEntities = new ArrayList<Entity>();
		m_predEntities.addAll(ents);
	}

	/* Examples */

	public CExample getCExampleFor(Mention m1, Mention m2) {
		Pair<Mention, Mention> ms = new Pair<Mention, Mention>(m1, m2);
		CExample result = m_cExMap.get(ms);
		if (result == null) {
			result = new CExample(this, m1, m2);
			m_cExMap.put(ms, result);
		}
		return result;
	}

	public GExample getGExampleFor(Mention m) {
		GExample result = m_gExMap.get(m);
		if (result == null) {
			result = new GExample(m);
			m_gExMap.put(m, result);
		}
		return result;
	}

	/* Mentions */

	public void setUsePredictedMentions(boolean usePred) {
		m_bUsePredMentions = usePred;
	}

	public boolean usePredictedMentions() {
		return m_bUsePredMentions;
	}

	public List<Mention> getMentions() {
		if (usePredictedMentions())
			return getPredMentions();
		else
			return getTrueMentions();
	}

	public List<Mention> getPredMentions() {
		return Collections.unmodifiableList(m_predMentions);
	}

	public List<Mention> getTrueMentions() {
		return Collections.unmodifiableList(m_trueMentions);
	}

	public boolean hasPredMentions() {
		return (m_predMentions != null && m_predMentions.size() > 0);
	}

	public boolean hasTrueMentions() {
		return (m_trueMentions != null && m_trueMentions.size() > 0);
	}

	public void setPredictedMentions(Collection<Mention> ments) {
		setUsePredictedMentions(true);
		m_predMentions = new ArrayList<Mention>(ments);
		this.sortPredictedMentions();
		this.alignPredMentsToTrue();
		// TODO: Reset all memoized data that depends on the mentions.
		m_mentsInSents = null;
		m_mentionsContaining = null;
		for (Mention m : ments){
			Set<Mention> set;
			int hWord1Num = m.getHeadFirstWordNum();
			int eWord1Num = m.getExtentFirstWordNum();
			if (m_predHeadStartWordNumMentionMap.containsKey(hWord1Num))
				set = m_predHeadStartWordNumMentionMap.get(hWord1Num);
			else {
				set = new HashSet<Mention>();
				m_predHeadStartWordNumMentionMap.put(hWord1Num, set);
			}
			set.add(m);
			// FIXED Bug here: Was hWord1Num in the if below:
			if (m_predExtentStartWordNumMentionMap.containsKey(eWord1Num))
				set = m_predExtentStartWordNumMentionMap.get(eWord1Num);
			else {
				set = new HashSet<Mention>();
				m_predExtentStartWordNumMentionMap.put(eWord1Num, set);
			}
			set.add(m);
		}
	}
public void setMorePredictedMentions(Collection<Mention> ments) {
		setUsePredictedMentions(true);
		addMents = new ArrayList<Mention>(ments);
		this.sortPredictedMentions();
		this.alignPredMentsToTrue();
                	
	}
	protected void alignPredMentsToTrue() {
		m_predToTrueMention = m_defaultAligner.getAlignment(getPredMentions(),
				getTrueMentions());
	}

	protected void addTrueMention(Mention m) {

		int hWord1Num = m.getHeadFirstWordNum();
		// Fix a bug here: was int eWord1Num =
		// getWordNum(m.getExtentFirstWordNum());
		int eWord1Num = m.getExtentFirstWordNum();
		if (hWord1Num < 0 || eWord1Num < 0) {
			System.out.println(m.getDoc().getDocID());
			HashMap<Integer, Integer> tmp = (HashMap<Integer, Integer>) m_charWordMap;
			System.out.println(tmp.toString());
			System.out.println(m.getHeadFirstWordNum() + "\t" + m.getHead().getStart() + "\t" + m.getHead().getCleanText());
			System.out.println(m.getExtentFirstWordNum() + "\t" + m.getExtent().getStart() + "\t" + m.getCleanText());
			System.err.println("head or extent has invalid start");
			System.exit(0);
		}
		m_trueMentions.add(m);

		// TODO: Separate maps for pred and true?
		Set<Mention> set;
		if (m_trueHeadStartWordNumMentionMap.containsKey(hWord1Num))
			set = m_trueHeadStartWordNumMentionMap.get(hWord1Num);
		else {
			set = new HashSet<Mention>();
			m_trueHeadStartWordNumMentionMap.put(hWord1Num, set);
		}
		set.add(m);

		// FIXED Bug here: Was hWord1Num in the if below:
		if (m_trueExtentStartWordNumMentionMap.containsKey(eWord1Num))
			set = m_trueExtentStartWordNumMentionMap.get(eWord1Num);
		else {
			set = new HashSet<Mention>();
			m_trueExtentStartWordNumMentionMap.put(eWord1Num, set);
		}
		set.add(m);

		// TODO: Decide about extracting from other than heads!
		for (String word : m.getHead().getText().split("\\s")) {
			int c = 1;
			if (m_mentWordCounts.containsKey(word))
				c = m_mentWordCounts.get(word) + 1;
			m_mentWordCounts.put(word, c);
		}
	}

	public int getNumMentions() {
		if (usePredictedMentions())
			return m_predMentions.size();
		return m_trueMentions.size();
	}

	/**
	 * Sorts true mentions in natural order, which is the textual order by
	 * default.
	 * 
	 * @see Mention#compareTo(Mention)
	 */
	protected void sortTrueMentions() {
		Collections.sort(m_trueMentions);
		m_trueMentionsSorted = true;
		m_trueMentionProsition.clear();
		int i = 0;
		for (Mention m : m_trueMentions)
			m_trueMentionProsition.put(m, i++);
	}

	/**
	 * Sorts predicted mentions in natural order, which is the textual order by
	 * default.
	 * 
	 * @see Mention#compareTo(Mention)
	 */
	protected void sortPredictedMentions() {
		m_predMentionsSorted = true;
		Collections.sort(m_predMentions);
		m_predMentionProsition.clear();
		int i = 0;
		for (Mention m : m_predMentions)
			m_predMentionProsition.put(m, i++);
	}

	public int getTrueMentionPosition(Mention m) {
		if (!m_trueMentionsSorted)
			System.err.println("Warning: True mentions not sorted!");
		return m_trueMentionProsition.get(m);
	}

	public int getPredMentionPosition(Mention m) {
		if (!m_predMentionsSorted){
			System.err.println("Warning: Pred mentions not sorted!");
			sortPredictedMentions();
		}
		if(!m_predMentionProsition.containsKey(m))
			System.err.println("ERR:" +m);
		return m_predMentionProsition.get(m);
	}

	public int getMentionPosition(Mention m) {
		if (usePredictedMentions())
			return getPredMentionPosition(m);
		else
			return getTrueMentionPosition(m);
	}

	public Mention getMention(int n) {
		if (usePredictedMentions())
			return getPredMention(n);
		else
			return getTrueMention(n);
	}

	public Mention getPredMention(int n) {
		if (n < m_predMentions.size())
			return m_predMentions.get(n);
		else
			return null;
	}

	public Mention getTrueMention(int n) {
		if (!m_trueMentionsSorted)
			System.err.println("Warning: True mentions not sorted!");
		return m_trueMentions.get(n);
	}

	public Mention getTrueMentionFor(Mention pred) {
		if (m_predToTrueMention == null) {
			alignPredMentsToTrue();
		}
		return m_predToTrueMention.get(pred);
	}

	public Mention getBestMentionFor(Mention m) {
		if (m_bestMentionMap == null)
			makeBestMentionMap();
		return m_bestMentionMap.get(m);
	}

	public Set<Mention> getMentionsWithHeadStartingAt(int startWord) {
		if(usePredictedMentions()){
			if (m_predHeadStartWordNumMentionMap.containsKey(startWord))
				return m_predHeadStartWordNumMentionMap.get(startWord);
			else
				return new HashSet<Mention>();
		}
		else{
			if (m_trueHeadStartWordNumMentionMap.containsKey(startWord))
				return m_trueHeadStartWordNumMentionMap.get(startWord);
			else
				return new HashSet<Mention>();
		}
	}

	public Set<Mention> getMentionsWithExtentStartingAt(int startWord) {
		if(usePredictedMentions()){
			if (m_predExtentStartWordNumMentionMap.containsKey(startWord))
				return m_predExtentStartWordNumMentionMap.get(startWord);
			else
				return new HashSet<Mention>();
		}
		else {
			if (m_trueExtentStartWordNumMentionMap.containsKey(startWord))
				return m_trueExtentStartWordNumMentionMap.get(startWord);
			else
				return new HashSet<Mention>();
		}
	}

	public Set<Mention> getMentionsContainedIn(Mention m) {
		Set<Mention> results = new HashSet<Mention>();
		int startWord = m.getExtentFirstWordNum();
		int endWord = m.getExtentLastWordNum();
		for (int i = startWord; i <= endWord; ++i) {
			Set<Mention> startHere = getMentionsWithHeadStartingAt(i);
			for (Mention mInside : startHere) {
				int insideHeadEndWord = mInside.getHeadLastWordNum();
				if (insideHeadEndWord <= endWord)
					results.add(mInside);
			}
		}
		return results;
	}

	/**
	 * @return The Set of Mention objects whose extent is contained in (or equal
	 *         to) the extent of {@code m}. Returns predicted or true mentions
	 *         according to what getMentions() returns.
	 */
	public Set<Mention> getMentionsContaining(Mention m) {
		if (m_mentionsContaining == null) {
			buildMentionsContaining();
		}

		Set<Mention> ments = m_mentionsContaining.get(m);
		// TODO: What if null??Parameters
		return ments;
	}

	// OPTIMIZE:
	protected void buildMentionsContaining() {
		m_mentionsContaining = new HashMap<Mention, Set<Mention>>();
		for (Mention contained : getMentions()) {
			// TODO: Should we add container to containedSet just in case?
			Set<Mention> containingSet = new HashSet<Mention>();
			int startWord = contained.getExtentFirstWordNum();
			int endWord = contained.getExtentLastWordNum();
			for (Mention m : getMentions()) {
				if (m.getExtentFirstWordNum() <= startWord
						&& m.getExtentLastWordNum() >= endWord) {
					containingSet.add(m);
				}
			}
			m_mentionsContaining.put(contained, containingSet);
		}
	}

	public List<Mention> getMentionsInSent(int sentNum) {
		if (m_mentsInSents == null)
			buildMentionsInSents();
		if (sentNum < m_mentsInSents.size()) {
			return m_mentsInSents.get(sentNum);
		} else {
			System.err.println("Sentence not found.");
			return new ArrayList<Mention>();
		}
	}

	protected void buildMentionsInSents() {
		List<List<Mention>> sents = new ArrayList<List<Mention>>();
		int sN = 0;
		List<Mention> sent = new ArrayList<Mention>();
		for (Mention m : getMentions()) {
			while (m.getSentNum() > sN) { // Move to next sentence:
				sents.add(sent);
				sent = new ArrayList<Mention>();
				sN++;
			}
			sent.add(m);
		}
		while (sN < getNumSentences()) {
			sents.add(sent);
			sent = new ArrayList<Mention>();
			sN++;
		}
		m_mentsInSents = sents;
	}

	public Pair<List<Mention>, List<Mention>> getMentionsInSentences(int s1,
			int s2) {
		Pair<Integer, Integer> key = Pair.create(s1, s2);
		if (m_sentenceMentionsPair.containsKey(key)) {
			return m_sentenceMentionsPair.get(key);
		} else {
			Pair<List<Mention>, List<Mention>> result = Pair.create(
					getMentionsInSent(s1), getMentionsInSent(s2));
			m_sentenceMentionsPair.put(key, result);
			return result;
		}
	}

	/* Chunks */

	public Chunk makeChunk(int startWord, int endWord) {
		int startCharNum = this.getStartCharNum(startWord);
		int startOfLastWord = this.getStartCharNum(endWord);
		if (startOfLastWord < 0)
			System.err.println("Last word not found making chunk");
		int endCharNum = startOfLastWord + getWord(endWord).length() - 1;
		if (startCharNum < 0 || endCharNum < 0) {
			System.err.println("Invalid charNum: startCharNum=" + startCharNum
					+ "; end=" + endCharNum);
			System.err.println("sWord=" + startWord + ";eW=" + endWord);
			System.err.println("start and end words:" + getWord(startWord)
					+ " & " + getWord(endWord));
		}

		String text = this.getPlainText().substring(startCharNum,
				endCharNum + 1);
		return new Chunk(this, startCharNum, endCharNum, text);
	}

	/* Words and their attributes */

	public List<String> getWords() {
		return m_words;
	}

	public String getWord(int wordNum) {
		if(wordNum > m_words.size())
			return "$"; // the end of document
		return m_words.get(wordNum);
	}

	public List<String> getPOS() {
		return m_pos;
	}

	public String getPOS(int posNum) {
		if(posNum < m_pos.size())
			return m_pos.get(posNum);
		return 	"END";
	}

	public int getWordNum(int charNum) {
		/*
		 * FIXME: fix problems with Word-Word constructs. More specifically, fix
		 * issue where multi-mention can occupy one word.
		 */
		if (!m_charWordMap.containsKey(charNum)) {
			// Debug.p("Didn't find the word in the map from chars to word"
			// + " with start charPos:" + charNum + " In Doc:" + m_docID);
			// if (charNum + 20 < m_countingText.length()) {
			// Debug.p("string at that loc: "
			// + m_countingText.substring(charNum, charNum + 20));
			// }
			if (charNum + 1 < m_countingText.length())
				return getWordNum(charNum + 1);
			else
				return -1;
		}
		return m_charWordMap.get(charNum);
	}

	public int getTextFirstWordNum() {
		return getWordNum(m_textStartCharNum);
	}

	public int getStartCharNum(int wordNum) {
		/* FIXME: fix problems with Word-Word constructs. */
		if (!m_wordNumCharNumMap.containsKey(wordNum)) {
			System.err.println("Cannot map word " + wordNum);
			return -1;
		}
		return m_wordNumCharNumMap.get(wordNum);
	}

	public int getQuoteNestLevel(int wordNum) {
		if (wordNum >= m_quoteNestLevel.size()) {
			return 0;
		}
		return m_quoteNestLevel.get(wordNum);
	}

	/* Word Statistics */

	// FIXME: Make these return inverse frequency, not count.
	public double getInverseTrueHeadFreq(int wordNum) {
		String word = this.getWord(wordNum);
		return this.getInverseTrueHeadFreq(word);
	}

	public double getInverseTrueHeadFreq(String word) {
		if (!m_mentWordCounts.containsKey(word)) {
			/*
			 * TODO: ? System.err.println("Could not get an IDF for word " +
			 * word); new RuntimeException().printStackTrace();
			 * System.err.println(" "); System.err.println("IDF table:");
			 * System.err.println(m_mentWordCounts); System.err.println("");
			 * return Double.NaN;
			 */
			return 1.0; // assume count is 1.
		}
		return 1.0 / m_mentWordCounts.get(word);
	}

	public double getInDocInverseFreq(String word) {
		if (!m_docWordCounts.containsKey(word))
			return 1.0; // assume count is 1.
		else {
			return 1.0 / m_docWordCounts.get(word);
		}
	}

	public double getInCorpusInverseFreq(String word) {
		if (!m_corpusWordCounts.containsKey(word))
			return 1.0; // assume count is 1.
		else {
			return 1.0 / m_corpusWordCounts.get(word);
		}
	}

	public Map<String, Integer> getWholeDocCounts() {
		return new HashMap<String, Integer>(m_docWordCounts);
	}

	public void setCorpusCounts(Map<String, Integer> counts) {
		//m_corpusWordCounts = new HashMap<String, Integer>(counts);
		m_corpusWordCounts = counts;
	}
	
	public Map<String, Integer> getCorpusCounts() {
		return m_corpusWordCounts;
	}

	/* Relations */

	public int getNumRelations() {
		return m_relations.size();
	}

	public Relation getRelation(int number) {
		return (Relation) m_relations.get(number);
	}

	protected void addRelation(Relation r) {
		m_relations.add(r);
	}

	/* Memoization assistance functions */

	/**
	 * Checks to see whether a prediction has been stored for whether the closed
	 * interval [firstWN, lastWN] word sequence is a head. (Does NOT return
	 * whether it is a head)
	 */
	public boolean hasHeadPrediction(int firstWN, int lastWN) {
		// TODO: OPTIMIZE:
		if (!m_headPredictionMap.containsKey(firstWN))
			return false;
		if (!m_headPredictionMap.get(firstWN).containsKey(lastWN))
			return false;
		return true;
	}

	/*
	 * Precondition: A prediction has been stored (thus hasHeadPrediction()
	 * returns true.)
	 */
	public boolean getHeadPrediction(int firstWN, int lastWN) {
		return m_headPredictionMap.get(firstWN).get(lastWN);
	}

	public void addHeadPrediction(int firstWN, int lastWN, boolean pred) {
		Map<Integer, Boolean> innerMap;
		if (m_headPredictionMap.containsKey(firstWN))
			innerMap = m_headPredictionMap.get(firstWN);
		else
			innerMap = new HashMap<Integer, Boolean>();
		innerMap.put(lastWN, pred);
	}

	/* Output */

	public String toString() {
		String s = "Document:\n";
		if (usePredictedEntities())
			s += "Predicted ";
		else
			s += "True ";
		s += "Entities:\n" + getEntities().toString() + "\nRelations:\n"
				+ m_relations.toString();
		return s;
	}

	public String toAnnotatedString(boolean showPOS, boolean showMTypes,
			boolean showETypes, boolean showEIDs) {
		// Build start and end maps:
		Map<Pair<Integer, Integer>, Integer> predLocs = new HashMap<Pair<Integer, Integer>, Integer>();

		Map<Integer, List<String>> openBracketMap = new HashMap<Integer, List<String>>();
		Map<Integer, List<String>> closeBracketMap = new HashMap<Integer, List<String>>();
		Map<Integer, List<String>> mTypesMap = new HashMap<Integer, List<String>>();

		List<Mention> sortedPredMents = new ArrayList<Mention>(m_predMentions);
		Collections.sort(sortedPredMents);

		for (Mention m : sortedPredMents) {
			int sWord = m.getExtentFirstWordNum();
			int eWord = m.getExtentLastWordNum();
			// TODO: Are these hashable?
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(sWord, eWord);
			Maps.addOne(predLocs, p);

			List<String> openBrackets = openBracketMap.get(sWord);
			if (openBrackets == null) {
				openBrackets = new LinkedList<String>();
				openBracketMap.put(sWord, openBrackets);
			}

			List<String> closeBrackets = closeBracketMap.get(eWord);
			if (closeBrackets == null) {
				closeBrackets = new LinkedList<String>();
				closeBracketMap.put(eWord, closeBrackets);
			}

			if (m.m_isTrueMention) {
				// Correct:
				String eID = m.getEntityID();
				if (m.getPredictedEntityID() != null) {
					eID = m.getPredictedEntityID();
				}
				if(eID.equals("NONE"))
					continue;
				String openBrace = "[";
				String closeBrace = " ]";
				if (showEIDs) {
					openBrace += "_" + getShortEID(eID);
					closeBrace += "_" + getShortEID(eID);
				}
				openBrace+=" ";
				openBrackets.add(0, openBrace);

				if (showMTypes)
					closeBrace += "_" + m.getType();
				closeBrackets.add(0, closeBrace);

			} else {
				// False Positive:			
				String openBrace = "[";
				String closeBrace = " ]";
				String eID = m.getEntityID();
				if (m.getPredictedEntityID() != null) {
					eID = m.getPredictedEntityID();
				}
				if(eID.equals("NONE"))
					continue;
				if (showEIDs) {
					openBrace += "_" + getShortEID(eID);
					closeBrace += "_" + getShortEID(eID);
				}
				openBrace+=" ";
				openBrackets.add(0, openBrace);

				if (showMTypes)
					closeBrace += "_" + m.getType();
				closeBrackets.add(0, closeBrace);
			}
			// TODO: Predicted or True mTypes?
			List<String> mTypes = mTypesMap.get(eWord);
			if (mTypes == null) {
				mTypes = new LinkedList<String>();
				mTypesMap.put(eWord, mTypes);
			}
			mTypes.add(0, m.getType());
		}

		List<Mention> sortedTrueMents = new ArrayList<Mention>(m_trueMentions);
		Collections.sort(sortedTrueMents);

		for (Mention m : sortedTrueMents) {
			int sWord = m.getExtentFirstWordNum();
			int eWord = m.getExtentLastWordNum();

			List<String> openBrackets = openBracketMap.get(sWord);
			if (openBrackets == null) {
				openBrackets = new LinkedList<String>();
				openBracketMap.put(sWord, openBrackets);
			}
			List<String> closeBrackets = closeBracketMap.get(eWord);
			if (closeBrackets == null) {
				closeBrackets = new LinkedList<String>();
				closeBracketMap.put(eWord, closeBrackets);
			}

			Pair<Integer, Integer> p = new Pair<Integer, Integer>(sWord, eWord);
			if (!predLocs.containsKey(p)) {
				// False negatives:
				openBrackets.add(0, "<");
				String closeBrace = ">";
				if (showMTypes)
					closeBrace += "_" + m.getType();
				if (showEIDs) {
					String eID = m.getEntityID();
					if (m.getPredictedEntityID() != null) {
						eID = m.getPredictedEntityID();
					}
					closeBrace += "_" + getShortEID(eID);
				}
				closeBrackets.add(0, closeBrace);
			}
		}

		String s = "";
		for (int wN = 0; wN < getWords().size(); ++wN) {
			if (wN > 0)
				s += " ";

			// Start Braces:
			List<String> openBrackets = openBracketMap.get(wN);
			if (openBrackets != null) {
				for (String b : openBrackets)
					s += b;
			}

			// Word (and POS)
			s += getWord(wN);
			if (showPOS)
				s += "(" + getPOS(wN) + ")";

			// End Braces:
			List<String> closeBrackets = closeBracketMap.get(wN);
			if (closeBrackets != null) {
				for (String b : closeBrackets)
					s += b;
			}

		} // End for wN
		return s;
	} // End toAnnotatedString()

	public String toAnnotatedString(boolean showPOS) {
		return toAnnotatedString(showPOS, false, false, false);
	}

	/**
	 * @return The text of the document with the extent of each mention replaced
	 *         by the most specific mention in its entity. Uses the mentions
	 *         supplied by {@code getMentions()}.
	 */
	public String toSubstituteString() {
		Map<Integer, Mention> mentStartingAt = new HashMap<Integer, Mention>();

		for (Mention m : getMentions()) {
			int sWord = m.getExtentFirstWordNum();
			mentStartingAt.put(sWord, m);
		}

		String s = "";
		for (int wN = 0; wN < getWords().size(); ++wN) {
			if (wN > 0)
				s += " ";

			// Word:
			if (mentStartingAt.containsKey(wN)) {
				Mention m = mentStartingAt.get(wN);
				Mention best = getBestMentionFor(m);
				s += best.getExtent().getText();
				wN = m.getExtentLastWordNum(); // Note ++ in for loop.
			} else {
				s += getWord(wN);
			}

		} // End for wN
		return s;
	} // End toSubstituteString()

	protected Map<Mention, Mention> makeBestMentionMap() {
		// FIXME: Finish.
		Comparator<Mention> comp = new MentionSpecificityComparator();
		m_bestMentionMap = new HashMap<Mention, Mention>();
		for (Entity e : getPredEntities()) {
			List<Mention> ments = e.getMentions();
			Collections.sort(ments, comp);
			if (ments.size() == 0)
				continue;
			Mention best = ments.get(0);
			for (Mention m : ments) {
				m_bestMentionMap.put(m, best);
			}
		}
		return m_bestMentionMap;
	}

	public Map<Entity, Map<Integer, String>> getCoherenceInfo(boolean usePred) {
		List<Entity> ents = null;
		if (usePred)
			ents = m_predEntities;
		else
			ents = m_trueEntities;

		int minSent = Integer.MAX_VALUE, maxSent = 0;

		Map<Entity, Map<Integer, String>> entsSents = new HashMap<Entity, Map<Integer, String>>();
		for (Entity e : ents) {
			Map<Integer, String> eSents = new HashMap<Integer, String>();
			entsSents.put(e, eSents);
			List<Mention> ments = new ArrayList<Mention>(e.getMentions());
			Collections.sort(ments);
			for (Mention m : ments) {
				int sNum = m.getSentNum();
				if (sNum > maxSent)
					maxSent = sNum;
				if (sNum < minSent)
					minSent = sNum;
				String type = "";
				if (eSents.containsKey(sNum))
					type = eSents.get(sNum);

				boolean correct = true;
				// String trueEIDStrip = m.getEntityID().replaceAll("[a-z]","");
				// String predEIDStrip = e.getID().replaceAll("[a-z]","");
				// if (!trueEIDStrip.equals(predEIDStrip))
				if (!e.getID().equals(m.getEntityID()))
					correct = false;
				String l = "";

				if (m.getType().equals("NAM"))
					l = "M";
				else if (m.getType().equals("NOM"))
					l = "N";
				else if (m.getType().equals("PRE"))
					l = "R";
				else if (m.getType().equals("PRO"))
					l = "P";

				if (!correct) {
					l = l.toLowerCase();
					l += getShortEID(m.getEntityID());
				}

				type += l;
				eSents.put(sNum, type);
			}
		}
		return entsSents;
	}

	public Map<Entity, Map<Integer, String>> getCoherenceInfo() {
		return getCoherenceInfo(usePredictedEntities());
	}

	// FIXME: Note: now using lower case
	public String toCoherenceTableString(boolean usePred) {
		List<Entity> trueEnts = null, ents = null;
		if (m_trueEntities != null) {
			trueEnts = new ArrayList<Entity>(m_trueEntities);
			Collections.sort(trueEnts, new EntityByFirstMentionComparator());
		}

		if (usePred) {
			ents = m_predEntities;
			if (m_trueEntities != null) {
				ents = sortEntitiesByListOrder(ents, trueEnts);
			}
		} else {
			ents = trueEnts;
		}

		Map<Entity, Map<Integer, String>> entsSents = getCoherenceInfo(usePred);

		String r = "Entities in columns, sentences in rows.\n";
		r += "M=naMe, N=Nominal, P=Pronoun, R=pRenominal\n";

		/*
		 * for (int eN = 0; eN < ents.size(); ++ eN) if (eN > 9) r += eN + "  ";
		 * else r += eN + "   ";
		 */
		Map<Entity, Integer> colWidths = new HashMap<Entity, Integer>();
		for (Entity e : ents) {
			// Count column widths:
			String label = getShortEID(e.getID());
			int colWidth = label.length(); // Not including space b/t.
			Map<Integer, String> eSents = entsSents.get(e);
			for (int sn : eSents.keySet()) {
				String tag = eSents.get(sn);
				if (tag.length() > colWidth)
					colWidth = tag.length();
			}
			colWidths.put(e, colWidth);

			int nSpaces = colWidth + 1 - label.length();
			if (nSpaces < 0)
				nSpaces = 0;

			r += label + repeat(" ", nSpaces);
		}
		r += "\n";
		for (int i = 0; i <= m_nSents; ++i) {
			for (Entity e : ents) { // Use sorted list, not keySet().
				Map<Integer, String> eSents = entsSents.get(e);
				int colWidth = colWidths.get(e);
				String tag = "";
				if (eSents.containsKey(i)) {
					tag = eSents.get(i);
				}
				r += tag + repeat(" ", colWidth + 1 - tag.length());
			}
			r += "\n";
		}
		return r;
	}

	public String toCoherenceTableString() {
		return toCoherenceTableString(usePredictedEntities());
	}

	/*
	 * TODO: Use or remove: private void printAlignedText() { String s = ""; for
	 * (int mN = 0; mN < m_trueMentions.size(); ++mN) { Mention m =
	 * m_trueMentions.get(mN); String ext = m.getExtent().getText(); String cExt
	 * = m_countingText.substring(m.getExtentStart(), m.getExtentEnd()+1);
	 * String words = ""; int extW0Num = m.getExtentFirstWordNum(); int extWLNum
	 * = extW0Num + m.getNumExtentWords() - 1; for (int wN = extW0Num; wN <=
	 * extWLNum; ++wN) { if (wN != extW0Num) words += " "; words +=
	 * m_words.get(wN); } if (!ext.equals(cExt)) s += "***"; if
	 * (!ext.equals(words)) s += "*"; s += ext + " : " + cExt + " : " + words +
	 * "\n"; } System.out.println("Document " + m_docID); System.out.println(s);
	 * }
	 */

	protected String repeat(String s, int n) {
		// TODO: Faster (use fill with char)?
		String r = "";
		for (int i = 0; i < n; ++i) {
			r += s;
		}
		return r;
	}

	/** Does NOT modify in place (but this may change). */
	protected List<Entity> sortEntitiesByListOrder(List<Entity> ents,
			List<Entity> ordered) {
		List<Entity> r = new ArrayList<Entity>();

		Map<String, Entity> idPredEntMap = new HashMap<String, Entity>();
		for (Entity e : ents) {
			idPredEntMap.put(e.getID(), e);
		}

		for (Entity e : ordered) {
			if (idPredEntMap.containsKey(e.getID())) {
				r.add(idPredEntMap.get(e.getID()));
				char c = 'a';
				while (idPredEntMap.containsKey(e.getID() + c)) {
					r.add(idPredEntMap.get(e.getID() + c));
					c++;
				}
			}
		}
		return r;
	}

	public String getShortEID(String longID) {
		if (longID == null)
			return "";
		int b = longID.lastIndexOf("-E");
		if (b == -1)
			b = 0;
		else
			b += 2;
		return longID.substring(b);
	}

	public void save() throws IOException {
		try {
			FileOutputStream fout = new FileOutputStream(m_baseFN + ".dsr");
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(this);
		} catch (Exception e) {
			throw new IOException("Could not save:\n" + e.toString());
		}
	}

	// TODO: Deal with extension that varies by subtype.
	public void write(boolean usePredictions) {
		this.write("predictions/" + m_docID + ".txt", usePredictions);
	}

	abstract public void write(String filename, boolean usePredictions);

	/* Validation */

	/**
	 * Verify that all mentions start and end on phrase boundaries.
	 */
	/*
	 * TODO: Use or remove: private void validatePhrases() { Map<Integer,
	 * List<String>> bStartWordIndexMap = new HashMap<Integer, List<String>>();
	 * Map<Integer, List<String>> bEndWordIndexMap = new HashMap<Integer,
	 * List<String>>();
	 * 
	 * int wordNum = 0; for (List<String> phrase : m_phrases) {
	 * bStartWordIndexMap.put(wordNum, phrase); wordNum += phrase.size();
	 * bEndWordIndexMap.put(wordNum + phrase.size() - 1, phrase); }
	 * //List<String> words = ListUtils.flatten(m_phrases);
	 * 
	 * for (Mention m : m_trueMentions) { totalMentions += 1; int startWordNum =
	 * getWordNum(m.getExtentStart()); int endWordNum = startWordNum +
	 * m.getNumExtentWords() - 1; if
	 * (bStartWordIndexMap.containsKey(startWordNum)) goodStarts += 1; else { //
	 * List<String> phrase = bStartWordIndexMap.get(startWordNum); //
	 * System.out.println("Mention" + m.toString() // +
	 * " does not start on boundary."); // System.out.println("Text at wordnum "
	 * + startWordNum // + " is: " + getWord(startWordNum)); //
	 * System.out.println("Word in phrases was " // + words.get(startWordNum));
	 * } if (bEndWordIndexMap.containsKey(endWordNum)) { goodEnds += 1; medEnds
	 * += 1; // Med Ends includes good ends. } else { List<String> phrase =
	 * bStartWordIndexMap.get(startWordNum); if
	 * (bStartWordIndexMap.containsKey(startWordNum) && endWordNum <=
	 * startWordNum + phrase.size() - 1) { medEnds += 1; } //
	 * System.out.println("Mention" + m.toString() // +
	 * " does not end on boundary."); // System.out.println("Text at wordnum " +
	 * endWordNum // + " is: " + getWord(endWordNum)); //
	 * System.out.println("Word in phrases was " // + words.get(endWordNum)); //
	 * System.out.println(words); // System.exit(0); } }
	 * 
	 * }
	 */

	public static void printChunkValidity() {
		System.out.println("Accuracy of chunks:");
		double startAcc = goodStarts / (double) totalMentions;
		System.out.println(startAcc + " mentions start on boundary.");
		double endAcc = goodEnds / (double) totalMentions;
		System.out.println(endAcc + " mentions end on boundary.");
		double medAcc = medEnds / (double) totalMentions;
		System.out.println(medAcc
				+ " mentions end on boundary or within first phrase.");
	}

	public void setM_countingText(String m_countingText) {
		this.m_countingText = m_countingText;
	}

	public String getDomain() {
		return domain;
	}

	public String getM_countingText() {
		return m_countingText;
	}

	// Note that DocBase does not implement hashCode or equals.

	public void setTextAnnotation(TextAnnotation ta) {
		this.m_ta = ta;
	}

	public TextAnnotation getTextAnnotation() {
		return m_ta;
	}
	
	public void setTripleTextAnnotation(TextAnnotation[] tripleTA) {
		this.m_tripleTA = tripleTA;
	}

	public TextAnnotation[] getTripleTextAnnotation() {
		return m_tripleTA;
	}

	public void setDocType(DocType type) {
		docType = type;
	}

	public DocType getDocType() {
		return docType;
	}
	public void setBaseFilename(String fileName) {
		m_baseFN = fileName;
	}
	public String getBaseFilename() {
		return m_baseFN;
	}
	
	public void setGigaWord(GigaWord gw) {
		this.gw = gw;
	}
	
	public GigaWord getGigaWord() {
		return gw;
	}
        
        public List<Mention> getAddMents() {
                return addMents;
	}
} // End class Doc
