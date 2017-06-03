package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/** Collection of features related to the relative location of mentions. */
public class DistanceFeatures {

    /** Construction of this static feature library should not be necessary. */
    protected DistanceFeatures() {
    }

    /** 
     * Computes the distance of the mentions of {@code ex},
     * in mention count, as a string, or "BIG" if that number is greater than
     * or equal to 10.  The result is more precisely the absolute value
     * of the difference between the indexes of the mentions in the
     * {@code List} returned by the document's getMentions() method.
     * This method runs in time proportional to the time of {@link List#indexOf}
     * on the {@code List} returned by {@link Doc#getMentions}.
     * @param ex The example whose mentions' distance is being computed.
     * @return A string containing a positive number less than 10, or "BIG".
     */
    public static String mentionDistHeads(CExample ex) {
    	/*Mention m2 = ex.getM2();
    	if(ContextFeatures.isQuoted(ex.getM1()) && !ContextFeatures.isQuoted(m2) && m2.getType().equals("PRO"))
        	return "BIG";*/
    	Doc d = ex.getDoc();
        List<Mention> ments = d.getMentions();
        int m1Num = ments.indexOf(ex.getM1());
        int m2Num = ments.indexOf(ex.getM2());
        if (m1Num < 0 || m2Num < 0) {
    	    System.err.println("BUG: Mention not found.");
    	    return "NONE";
        }
        int mDiff = Math.abs(m2Num - m1Num);
        if (mDiff < 10)
    	    return "" + mDiff;
        else
    	    return "BIG";
    }

    /**
     * Computes the distance in mentions of the mentions of {@code ex}
     * Runs in time proportional to the number of mentions.
     * The result is more precisely the absolute value
     * of the difference between the indexes of the mentions in the
     * List returned by the document's {@link Doc#getMentions} method.
     * @param ex The example whose mentions' distance is being computed.
     * @return The distance between the mentions.
     */
    public static int getNumMentsBetween(CExample ex) {
        int count = 0;
        Mention m1 = ex.getM1();
        Mention m2 = ex.getM2();
/*        // Pronoun not in the quoted will not refer to the NAM in quoted
        boolean m2isQuote = ContextFeatures.isQuoted(m2);
        boolean m1isQuote = ContextFeatures.isQuoted(m1);
        if(m1isQuote && !m2isQuote || m2.getType().equals("PRO"))
        	count+=10;*/
        
        boolean found1 = false, found2 = false;
        for (Mention m : ex.getDoc().getMentions()) {
            if ( m.equals(m1) ) {
        	found1 = true;
        	continue; //Avoid counting m1.
            } else if ( m.equals(m2) ) {
        	found2 = true;
        	return count;
            }
  /*          if(!m1isQuote && !m2isQuote && ContextFeatures.isQuoted(m))
            	continue;*/
            if ( found1 && !found2)
            	count++;
        }
        return count;
    }

    /**
     * Computes the number of mentions
     * between {@code ex.getM1()} and {@code ex.getM2()}
     * that are compatible with {@code m2}.
     * The definition of compatible will vary but includes
     * gender and number compatibility
     * and is defined by the {@link #compatible} method.
     * @param ex The example.
     * @return The number of compatible mentions between the example mentions.
     */
    public static int getNumCompatibleMentsBetween(CExample ex) {
        int count = 0;
        boolean found1 = false, found2 = false;
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	Doc d = m2.getDoc();
	boolean m2isQuote = ContextFeatures.isQuoted(m2);
    boolean m1isQuote = ContextFeatures.isQuoted(m1);
    /*if(m1isQuote && !m2isQuote || m2.getType().equals("PRO"))
    	count+=10;*/
        for (Mention m : d.getMentions()) {
            if ( m.equals(m1) ) {
        	found1 = true;
        	continue; //Avoid counting m1.
            } else if ( m.equals(m2) ) {
        	found2 = true;
        	break;
            }
/*            if(!m1isQuote && !m2isQuote && ContextFeatures.isQuoted(m))
            	continue;*/
            if ( found1 && !found2 && compatible(d, m, m2) )
        	count++;
        }
        return count;
    }

    /** 
     * Determines whether the first mention of {@code ex} is
     * the closest preceding mention compatible with
     * the second mention of {@code ex}.
     * @param ex The example containing the mentions.
     */
    public static boolean areClosestCompatible(CExample ex) {
	Doc d = ex.getDoc();
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	int h1End = m1.getHeadLastWordNum();
	int h2Start = m2.getHeadFirstWordNum();

	if ( GenderFeatures.doGendersMatch(ex, true).equals("f") )
	    return false;
	if ( NumberFeatures.doNumbersMatchStrong(ex, true).equals("f") )
	    return false;

	for (int wN = h2Start - 1; wN > h1End; --wN) {
	    Set<Mention> mentions
	    = d.getMentionsWithHeadStartingAt(wN);
	    for (Mention mCompetitor : mentions) {
		CExample competeEx = d.getCExampleFor(mCompetitor, m2);
		if ( GenderFeatures.doGendersMatch(competeEx,true).equals("t"))
		    return false;
		if ( NumberFeatures.doNumbersMatchStrong(competeEx, true)
			.equals("t") ) {
		    return false;
		}
	    }
	}
	return true;
    }


    /** 
     * Determines whether two mentions are compatible.
     * Two mentions are compatible only if they are quite likely to be
     * able to be in the same class.
     * This means that all checked attributes must be true
     * and none may be unknown or false.
     * @param d The document containing both mentions.
     * @param m1 One mention.
     * @param m2 A second mention.
     */
    public static boolean compatible(Doc d, Mention m1, Mention m2)
    {
	CExample ex = d.getCExampleFor(m1, m2);
	if (!GenderFeatures.doGendersMatchForCompatible(ex, true).equals("t"))
	    return false;
	if (!NumberFeatures.doNumbersMatchStrong(ex, true).equals("t"))
	    return false;
	return true; //Match on all checked attributes.
    }


    
    //Appositive:
    
    /**
     * Determines whether two mentions are in an appositive relationship,
     * currently approximated by checking whether only a comma intervenes
     * between their extents.
     * @param ex The example containing the mentions in question.
     */
    public static boolean areAppositives(CExample ex) {
	Doc d = ex.getDoc();
	int m1LastWN = ex.getM1().getExtentLastWordNum();
	int m2FirstWN = ex.getM2().getExtentFirstWordNum();
	if (m2FirstWN - m1LastWN == 2
		&& d.getWord(m1LastWN + 1).equals(",") ) {
	    return true;
	}
	return false; //TODO: Take into account other appositive patterns.
    }

    /**
     * Determine whether the mentions are in an appositive relationship,
     * as defined by {@literal Soon et al., 2001}.
     * Mentions are in an appositive relationship if at least one
     * is not a proper name, and either their heads or their extents
     * are separated only by a comma.
     * @param ex The example containing the mentions to examine.
     * @return Whether the mentions are in an appositive relationship.
     */
    public static boolean soonAppositive(CExample ex) {
        Doc d = ex.getDoc();
        Mention m1 = ex.getM1(), m2 = ex.getM2();
        String mType1 = m1.getType(), mType2 = m2.getType();
    
        if (!mType1.equals("NAM") && !mType2.equals("NAM"))
            return false;
    
        //Heads:
        int m1LastWN = m1.getHeadLastWordNum(); 
        int m2FirstWN = m2.getHeadFirstWordNum();
        if (m1LastWN + 2 == m2FirstWN && d.getWord(m1LastWN + 1).equals(","))
            return true;
        //Extents:
        m1LastWN = m1.getExtentLastWordNum();
        m2FirstWN = m2.getExtentFirstWordNum();
        if (m1LastWN + 2 == m2FirstWN && d.getWord(m1LastWN + 1).equals(","))
            return true;
        return false;
    }

}
