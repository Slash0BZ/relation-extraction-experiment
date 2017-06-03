/*
 * Created on Jun 10, 2006
 *
 */
package edu.illinois.cs.cogcomp.lbj.coref.io.loaders;

import java.util.List;

public class ParsePhraseResult {
	public String m_phraseType;
	public List<String> m_words;
	public List<String> m_partsOfSpeech;
	public int m_nextPosition;
	
	/**
	 * @param words A reference to this list (not a copy) is held in this class.
	 */
	public ParsePhraseResult(String phraseType, List<String> words, 
	 List<String> partsOfSpeech, int nextPosition) {
		m_phraseType = phraseType;
		m_words = words;
		m_partsOfSpeech = partsOfSpeech;
		m_nextPosition = nextPosition;
	}

}
