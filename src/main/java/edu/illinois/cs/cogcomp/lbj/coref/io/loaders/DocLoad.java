package edu.illinois.cs.cogcomp.lbj.coref.io.loaders;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

public class DocLoad {

    public static List<String> loadWords(String filename) {
	List<String> resultWords = new ArrayList<String>();
	String splitText = (new myIO()).readAll(filename);
	String[] wordsArray = splitText.split("\\s"); // Whitespace
	for (int i = 0; i < wordsArray.length; i++) {
	    if (wordsArray[i].length() == 0)
		continue;
	    String sWord = wordsArray[i];
	    if (sWord.equals("-LBR-"))
		sWord = "(";
	    else if (sWord.equals("-RBR-"))
		sWord = ")";
	    resultWords.add(sWord);
	}
	return resultWords;
    }

    /**
     * @param startPos
     *                Index of the opening bracket '[' of this [PhraseType
     *                (POS Word) ... (POS Word) ] sequence.
     * @return ParsePhraseResult("",null,-1) on malformed input. Otherwise,
     *         the nextPosition is set to the character after ']'.
     */
    public static ParsePhraseResult parsePhrase(String content, int startPos) {
	int afterPhraseType = content.indexOf(" ", startPos + 1);
	if (afterPhraseType < 0)
	    return new ParsePhraseResult("", null, null, -1);
	String phraseType = content.substring(startPos + 1, afterPhraseType);

	List<String> words = new ArrayList<String>();
	List<String> parts = new ArrayList<String>();
	int nextPos = afterPhraseType + 1;
	while (nextPos < content.length() && content.charAt(nextPos) != ']') {
	    if (Character.isWhitespace(content.charAt(nextPos))) {
		++nextPos;
		continue;
	    } else if (content.charAt(nextPos) == '(') {
		ParseWordResult res = parsePOSWordPair(content, nextPos);
		if (res.m_nextPosition < 0)
		    return new ParsePhraseResult("", null, null, -1);
		if (!res.m_word.equals("")) {
		    words.add(res.m_word);
		    parts.add(res.m_partOfSpeech);
		}
		// Otherwise ignore it.
		nextPos = res.m_nextPosition;
	    }
	}
	// Double check that there was indeed a closing bracket:
	if (nextPos >= content.length())
	    return new ParsePhraseResult("", null, null, -1);

	return new ParsePhraseResult(phraseType, words, parts, nextPos + 1);
    }


    /**
         * @param position
         *                Index of the opening paren '(' of this (POS Word)
         *                pair.
         * @return ParseWordResult("","",-1) on malformed input.
         */
    public static ParseWordResult parsePOSWordPair(
		  String content, int position) {
	int afterPOS = content.indexOf(" ", position + 1); // Pass paren
	if (afterPOS < 0)
	    return new ParseWordResult("", "", -1); // Malformed input
	String partOfSpeech = content.substring(position + 1, afterPOS);
	int afterWord = content.indexOf(")", afterPOS + 1);
	while (afterWord > 0 && afterWord + 1 < content.length()
	 && content.charAt(afterWord + 1) != ' '
	 && content.charAt(afterWord + 1) != ']'
	 && content.charAt(afterWord + 1) != '\n'
	) {
	    afterWord = content.indexOf(")", afterWord + 1);
	}
	if (afterWord < 0)
	    return new ParseWordResult("", "", -1); // Malformed input
	String word = content.substring(afterPOS + 1, afterWord);

	//Note: Once escaped here; now escape in the load function,
	//Where full text is available to determine which type of
	//bracket is represented.
	//if (word.length() > 0 && word.charAt(0) == '-') {
	    //if (word.equals("-LBR-"))
	//	word = "(";
	 //   else if (word.equals("-RBR-"))
	//	word = ")";
	//}

	return new ParseWordResult(word, partOfSpeech, afterWord + 1);
    }

}
