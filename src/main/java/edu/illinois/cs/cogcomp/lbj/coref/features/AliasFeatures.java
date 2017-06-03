package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc.DocSource;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;



/** 
 * Collection of feature generating functions that relate to aliases.
 * For example features are provided to determine whether one mention
 * is an initialism or acronym of the other,
 * and to find the initials of a string.
 */
public class AliasFeatures {

    /** No need to construct collection of features. */
    protected AliasFeatures() {
    }

    /** 
     * Determines whether the mentions are aliases.
     * If personal names, and their last head words match, yes.
     * Or if non-pronouns with the same entity type (or either/both "unknown")
     * and their heads have matching initials, yes.
     * (Note that at exactly one mention should be longer than one word
     * for the mentions to be initials.)
     * Assumes mention types have been set (either predicted or true.)
     * Words are split by non-alphanum characters.
     * @param ex The example
     * @param useGoldETypes if true, uses mention's given entity types;
     * if false, gets entity types using {@code EntityTypeFeatures}.
     * @return Whether the mentions are aliases.
     */
    public static boolean areSoonAliasBetter(CExample ex,
     boolean useGoldETypes) {
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	if(ex.getDoc().docSource == DocSource.ACE04)
		useGoldETypes= false;
	String eType1, eType2;
	if (useGoldETypes) {
	    eType1 = ex.getM1().getEntityType();
	    eType2 = ex.getM2().getEntityType();
	} else {
	    eType1 = EntityTypeFeatures.getEType(m1);
	    eType2 = EntityTypeFeatures.getEType(m2);
	}

	String mType1 = m1.getType();
	String mType2 = m2.getType();
	String m1Head = m1.getHead().getText();
	String m2Head = m2.getHead().getText();
	String[] m1Words = m1Head.split("\\s");
	String[] m2Words = m2Head.split("\\s");

	if (mType1.equals("PRO") || mType2.equals("PRO"))
	    return false;

	if (eType1.equals("PER") && eType2.equals("PER")
		&& mType1.equals("NAM") && mType2.equals("NAM"))
	{
	    String lastWord1 = m1Words[m1Words.length-1];
	    String lastWord2 = m2Words[m2Words.length-1];
	    if ( lastWord1.equalsIgnoreCase(lastWord2) )
		return true;
	}
	//Allow PER to be initials also:
	if (eType1.equals(eType2) || eType1.equals("unknown")
		|| eType2.equals("unknown"))
	{
	    return doInitialsMatchBetter(ex);
	}
	return false;
    }


    /**
     * Determine whether the mentions are aliases, without using
     * entity types.
     * For use in baseline system.
     * @param ex The example.
     * @return Whether the mentions are aliases.
     */
    public static boolean noETypeAlias(CExample ex)  {
	String mType1 = ex.getM1().getType();
	String mType2 = ex.getM2().getType();
	String m1Head = ex.getM1().getHead().getText();
	String m2Head = ex.getM2().getHead().getText();
	String[] m1Words = m1Head.split("\\s");
	String[] m2Words = m2Head.split("\\s");

	if (mType1.equals("NAM") && mType2.equals("NAM")) {
	    String finalWord1 = m1Words[m1Words.length-1].toLowerCase();
	    String finalWord2 = m2Words[m2Words.length-1].toLowerCase();
	    if ( finalWord1.equals(finalWord2) )
		return true;
	    //TODO: Deal with punctuation.
	    String initialized, shorter;
	    if (m1Words.length < m2Words.length) {
		initialized = AliasFeatures.getSoonInitials(
		 m2Head, Gazetteers.getOrgClosings());
		shorter = m1Head;
	    } else {
		initialized = AliasFeatures.getSoonInitials(
		 m1Head, Gazetteers.getOrgClosings());
		shorter = m2Head;
	    }
	    shorter = shorter.replaceAll("[., ]", "");
	    return ( shorter.equalsIgnoreCase(initialized) );
	}
	return false;
    }

    /**
     * Checks whether two mentions initials match.
     * If exactly one mention has one word, it is treated as initials
     * and the other mention's head is compared with it
     * using {@link #doInitialsMatchBetter(String, String[])}.
     * Words are split on non-alphanum, non-period characters.
     * @param ex The example
     * @return true when one mention is the initials of another mention.
     */
    public static boolean doInitialsMatchBetter(CExample ex) {
	String[] m1Words = ex.getM1().getHead().getText().split("[^a-zA-Z0-9.]+");
	String[] m2Words = ex.getM2().getHead().getText().split("[^a-zA-Z0-9.]+");
	if (m1Words.length == 1 && m2Words.length == 1)
	    return false;
	if (m1Words.length > 1 && m2Words.length > 1)
	    return false;
	if (m1Words.length == 1)
	    return doInitialsMatchBetter(m1Words[0], m2Words);
	else if (m2Words.length == 1)
	    return doInitialsMatchBetter(m2Words[0], m1Words);
	else
	    return false; //Should not happen.
    }

    /**
     * Determines whether {@code initials} is the initials
     * corresponding to {@code words}.
     * Non-word characters are ignored,
     * and any word that is {@code ignorable} is optional.
     * @param initials A string representing the initials of some phrase.
     * @param words The words whose initials will be computed and compared.
     * @return Whether the initials for {@code words}
     * match {@code initials}.
     */
    public static boolean doInitialsMatchBetter(String initials,
	    String[] words) {
	int i = 0;
	char[] inits
	= initials.toLowerCase().replaceAll("\\W", "").toCharArray();
	for (String w : words) {
	    w = w.toLowerCase();
	    if (ignorable(w)) {
		if (i < inits.length && inits[i] == w.charAt(0))
		    ++i;
		continue;
	    } else if (i >= inits.length) {
		return false;
	    }
	    if (w.charAt(0) != inits[i]
	                             && Character.isLetterOrDigit(w.charAt(0)))
		return false;
	    ++i;
	}
	if (i == inits.length)
	    return true;
	else
	    return false; //TODO: Maybe more inits should be ignored now?
    }
    
    /**
     * Determines whether a word can be ignored when computing initials
     * or determining whether initials match.
     * Currently, stop words and organizational suffixes are ignorable.
     * @param w A lowercase string.
     * @return true when {@code w} is ignorable.
     */
    public static boolean ignorable(String w) {
	if (Gazetteers.orgClosings.contains(w)) return true;
	if (Gazetteers.stopWords.contains(w)) return true;
	return false;
    }
    
    /** 
     * Determines whether two mentions are aliases,
     * as computed by the two parameter form of this method,
     * and using gold Entity Types.
     * @param ex The example whose mentions will be checked for relatedness.
     * @return Whether the mentions are aliases.
     */
    public static boolean areSoonAlias(CExample ex) {
	boolean useGoldEType = true;
        return areSoonAlias(ex, useGoldEType);
    }

    /** 
     * Determines whether two mentions are aliases, as defined in
     * Soon et al., Computational Linguistics, 2001.
     * For proper names of people or Geo-Political Entities (GPEs),
     * returns true when the last words of the heads are equal (ignoring case)
     * For organizations, determines whether the mention having less words
     * is an initialism of the mention having more words (ignoring punctuation
     * in the shorter).
     * Caching will not be used.
     * @param ex The example whose mentions will be checked for relatedness.
     * @param useGoldEType If true, use the gold Entity Type,
     * otherwise, predict it.
     * @return Whether the mentions are aliases as defined above.
     */
    public static boolean areSoonAlias(CExample ex, boolean useGoldEType) {
	boolean useCache = false;
	return AliasFeatures.areSoonAlias(ex, useGoldEType, useCache);
    }

    /** 
     * Determines whether two mentions are aliases, as defined in
     * Soon et al., Computational Linguistics, 2001.
     * For proper names of people or GeoPolitical Entities (GPEs),
     * returns true when the last words of the heads are equal (ignoring case)
     * For organizations, determines whether the mention having less words
     * is an initialism of the mention having more words (ignoring punctuation
     * in the shorter).
     * @param ex The example whose mentions will be checked for relatedness.
     * @param useGoldEType If true, use the gold Entity Type,
     * otherwise, predict it.
     * @param useCache Whether to use the cached values for entity type as
     * determined by {@link EntityTypeFeatures#getEType(Mention, boolean)}.
     * @return Whether the mentions are aliases as defined above.
     */
    public static boolean areSoonAlias(CExample ex, boolean useGoldEType,
     boolean useCache) {
    	if(ex.getDoc().docSource == DocSource.ACE04)
    		useGoldEType= false;
        String eType1, eType2;
        if (useGoldEType) {
            eType1 = ex.getM1().getEntityType();
            if(eType1 ==null)
            	eType1 = EntityTypeFeatures.getEType(ex.getM1(), useCache);
            eType2 = ex.getM2().getEntityType();
            if(eType2 ==null)
            	eType2 = EntityTypeFeatures.getEType(ex.getM1(), useCache);
        } else {
            eType1 = EntityTypeFeatures.getEType(ex.getM1(), useCache);
            eType2 = EntityTypeFeatures.getEType(ex.getM2(), useCache);
        }
        
        String mType1 = ex.getM1().getType();
        String mType2 = ex.getM2().getType();
        String m1Head = ex.getM1().getHead().getText();
        String m2Head = ex.getM2().getHead().getText();
        String[] m1Words = m1Head.split("\\s");
        String[] m2Words = m2Head.split("\\s");
    
        if (mType1.equals("NAM") && mType2.equals("NAM")
         && eType1.equals(eType2)
         && (eType1.equals("PER") || eType1.equals("GPE"))
        ) {
            String lastWord1 = m1Words[m1Words.length-1].toLowerCase();
            String lastWord2 = m2Words[m2Words.length-1].toLowerCase();
            return ( lastWord1.equals(lastWord2) );
        } else if ( (eType1.equals("ORG") || eType1.equals("unknown"))
         && (eType2.equals("ORG") || eType2.equals("unknown")) ) {
            Set<String> orgClosings = Gazetteers.getOrgClosings();
            //TODO: Deal with punctuation.
            String initials, shorter;
            if (m1Words.length < m2Words.length) {
        	initials = AliasFeatures.getSoonInitials(m2Head, orgClosings);
        	shorter = m1Head;
            } else {
        	initials = AliasFeatures.getSoonInitials(m1Head, orgClosings);
        	shorter = m2Head;
            }
            shorter = shorter.replaceAll("[., ]", "");
            return ( shorter.equalsIgnoreCase(initials) );
        }
        return false;
    }

    /** 
     * Determines whether the heads of two mentions have the same
     * initials.  Convert both mentions to initials (if multiword),
     * and determine whether the initials are equal (ignoring case).
     * Note that if both mentions are single word, this method
     * returns true iff the mentions have the same spelling.
     * Uses {@code getInitials(String s)}.
     * @param ex The example whose mentions will be compared.
     * @return Whether the initials match.
     */
    public static boolean doInitialsMatch(CExample ex) {
	String m1Head = ex.getM1().getHead().getText();
	String m2Head = ex.getM2().getHead().getText();
	String[] m1Words = m1Head.split("\\s");
	String[] m2Words = m2Head.split("\\s");
	String initials1 = getInitials(m1Words);
	String initials2 = getInitials(m2Words);

	if (m1Words.length == 1) {
	    if (m2Words.length == 1) {
		if ( m1Head.equalsIgnoreCase(m2Head) )
		    return true; //Both perhaps acronyms, and definitely same.
	    } else if ( m1Head.equalsIgnoreCase(initials2) ) {
		return true;
	    }
	} else if (m2Words.length == 1) { //And m1Words.length != 1
	    if ( m2Head.equalsIgnoreCase(initials1) )
		return true;
	}
	return false;
    }
    
    /** 
     * Computes the initials of the specified string.
     * Computes initials by returning a string
     * containing the first letter of each non-stop word, except that
     * {@code suffixes} are excluded.
     * Uses smartcase (i.e. only words beginning with
     * an uppercase character are included when {@code s} is
     * not all lowercase),
     * Splits {@code s} on whitespace.
     * @param s The String whose initials will be computed.
     * @return The initials of the given string.
     * @see #getSoonInitials(String[], Set, boolean)
     */
    public static String getSoonInitials(String s, Set<String> suffixes) {
        String[] parts = s.split("\\s");
        boolean useCase = !s.equals(s.toLowerCase());
        return getSoonInitials(parts, suffixes, useCase);
    }

    /**
     * Computes the initials of {@code parts}, by returning a string
     * containing the first letter of each non-stop word, except that
     * {@code suffixes} are excluded and if {@code useCase} is
     * true only words beginning with an uppercase character are included.
     * @param parts An array of strings corresponding to words in a phrase.
     * @param suffixes A Set of Strings not to be used to form initials in the
     * result.
     * @param useCase When true, only include initials from words beginning
     * with an uppercase letter. (Result will still be lowercase).
     * @return A lowercase String containing the sequence of initials with no
     * additional spaces or punctuation.
     */
    public static String getSoonInitials(String[] parts, Set<String> suffixes,
     boolean useCase) {
        String s = "";
        for (String part : parts) {
            if (part.length() > 0 && !suffixes.contains(part)) {
        	char c = part.charAt(0);
        	if (useCase) {
        	    if (Character.isUpperCase(c)) {
        		s += c;
        	    }
        	} else {
        	    part = part.toLowerCase();
        	    if (!part.equals("a") && !part.equals("an")
        	     && !part.equals("the") && !part.equals("of") 
        	     && !part.equals("and") && !part.equals("or") 
        	     && !part.equals("but") && Character.isLetterOrDigit(c))
        		s += c;
        	}
            }
        }
        //TODO: Would like to have this (improved) version as an option.
        //TODO: Ensure comment consistency.
        //return s.toLowerCase();
        return s;
    }

    /** Computes the initials of {@code s}. */
    public static String getInitials(String s) {
        String[] parts = s.split("\\s");
        return getInitials(parts);
    }

    /** 
     * Computes the initials of parts, including a character
     * for each non-stop word.
     * @param parts The words to compute initials on.
     * @return A lowercase String containing the sequence of initials,
     * with no additional spaces or punctuation.
     */
    public static String getInitials(String[] parts) {
        String s = "";
        for (String part : parts) {
            part = part.toLowerCase();
            if (part.length() > 0 && !part.equals("a") && !part.equals("an")
            && !part.equals("the") && !part.equals("of") && !part.equals("and") 
            && !part.equals("or") && !part.equals("but")
            )
        	s += part.charAt(0);
        }
        return s;
    }

    
    /**  
     * Determines whether mentions have the same last name, if people.
     * Assumes mention types have been set (either predicted or true).
     * Words are split by non-alphanum characters.
     * @param ex The example
     * @param useGoldETypes if true, uses mention's given entity types;
     * if false, gets entity types using {@code EntityTypeFeatures}.
     * @return Whether the mentions have the same last name.
     */
    public static boolean doLastNamesMatch(CExample ex,
     boolean useGoldETypes) {
	Mention m1 = ex.getM1(), m2 = ex.getM2();
	if(ex.getDoc().docSource == DocSource.ACE04)
		useGoldETypes= false;
	String eType1, eType2;
	if (useGoldETypes) {
	    eType1 = ex.getM1().getEntityType();
	    eType2 = ex.getM2().getEntityType();
	} else {
	    eType1 = EntityTypeFeatures.getEType(m1);
	    eType2 = EntityTypeFeatures.getEType(m2);
	}

	String mType1 = m1.getType();
	String mType2 = m2.getType();
	String m1Head = m1.getHead().getText();
	String m2Head = m2.getHead().getText();
	String[] m1Words = m1Head.split("\\s");
	String[] m2Words = m2Head.split("\\s");

	if (mType1.equals("PRO") || mType2.equals("PRO"))
	    return false;

	if (eType1.equals("PER") && eType2.equals("PER")
		&& mType1.equals("NAM") && mType2.equals("NAM"))
	{
	    String lastWord1 = m1Words[m1Words.length-1];
	    String lastWord2 = m2Words[m2Words.length-1];
	    if ( lastWord1.equalsIgnoreCase(lastWord2) )
		return true;
	}
	return false;
    }
}
