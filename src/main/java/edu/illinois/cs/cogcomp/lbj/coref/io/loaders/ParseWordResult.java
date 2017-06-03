/*
 * Created on Jun 10, 2006
 *
 */
package edu.illinois.cs.cogcomp.lbj.coref.io.loaders;

public class ParseWordResult {
	public String m_word;
	public String m_partOfSpeech;
	public int m_nextPosition;
	
	
	public ParseWordResult(String word, String partOfSpeech, int nextPosition) {
		m_word = word;
		m_partOfSpeech = partOfSpeech;
		m_nextPosition = nextPosition;
	}

}
