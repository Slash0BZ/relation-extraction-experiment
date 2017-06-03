package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/**
 * A collection of tools related to strings.
 */
public class SentStructureFeatures {

	/*
     * Determine whether the mentions are in the same sentence.,
     * @param ex The example containing the mentions to examine.
     * @return Whether the mentions are in the same sentence.
     */
	public static boolean inSameSentence(CExample ex){
		Mention m1 = ex.getM1();
		Mention m2 = ex.getM2();
		if(m1.getSentNum() == m2.getSentNum())
			return true;
		else
			return false;
	}
	public static String firstMentionPosition(CExample ex){
		return String.valueOf(MentionPosition(ex.getM1()));
	}
	public static String secondMentionPosition(CExample ex){
		return String.valueOf(MentionPosition(ex.getM2()));
	}
	public static int MentionPosition(Mention m){
		int SentStartNum = m.getDoc().getSentStartNum(m.getSentNum());
		int SentEndNum = SentEndNum = m.getDoc().getSentStartNum(m.getSentNum()+1);
		int MentionStartNum = m.getExtentFirstWordNum();
		/*if(m.getSentNum()+ 1  < m.getDoc().getNumSentences() )
			SentEndNum = m.getDoc().getSentStartNum(m.getSentNum()+1);
		else
			SentEndNum = m.getDoc().getWords().size();*/
		double PositionRatio = (double)(MentionStartNum - SentStartNum) / (SentEndNum - SentStartNum);
		return (int)(PositionRatio*6);
		
		
	}
	
}
