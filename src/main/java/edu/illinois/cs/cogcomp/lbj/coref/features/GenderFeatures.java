package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;


//TODO: Distinguish between grammatical gender and entity gender in Javadoc.

/**
 * A collection of feature generating functions and utilities for
 * determining the gender of an entity.
 */
public class GenderFeatures {

    /** A Cache of genders. */
    protected static Map<String,Character> m_genderCache;

    /** Should not need to construct this static library of features. */
    protected GenderFeatures() {
    }
    
    /**
     * Determines whether the gender of two mentions match
     * as determined by {@link #getGender}.
     * Returns a boolean value and assumes true if unknown.
     * @param ex The example containing the mentions.
     * @return true if same or either is unknown, false otherwise.
     */
    public static boolean genderMatchBoolean(CExample ex) {
        boolean useWN = true;
	char g1 = getGender(ex.getM1(), useWN);
        char g2 = getGender(ex.getM2(), useWN);
        return (g1 == 'u' || g2 == 'u' || g1 == g2);
    }

    
    /**
     * Determines whether the gender of two mentions match,
     * according to {@link #getGender}.
     * Caching is not done.
     * @param useWN Whether WordNet should be used to help predict genders?
     * @return "t" (true), "f" (false), or "u" (unknown).
     */
    public static String doGendersMatch(CExample ex, boolean useWN) {
    	boolean useCache = false;
    	return doGendersMatch(ex, useWN, useCache);
    }
    
    /**
     * Determines whether the gender of two mentions match,
     * according to {@link #getGender}.
     * @param useWN Whether WordNet should be used to help predict genders?
     * @param useCache Whether to cache genders.
     * @return "t" (true), "f" (false), or "u" (unknown).
     */
    public static String doGendersMatch(CExample ex, boolean useWN,
     boolean useCache) {
        char g1 = getGender(ex.getM1(), useWN, useCache);
        char g2 = getGender(ex.getM2(), useWN, useCache);
        Mention m1 = ex.getM1();
        Mention m2 = ex.getM2();
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("PER") || EntityTypeFeatures.getEType(m1).equals("ORG")|| 
            			EntityTypeFeatures.getEType(m1).equals("GPE")))
            	return "t";
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("LOC")||(EntityTypeFeatures.getEType(m1).equals("MISC"))) )
            	return "u";
        if (g1 == 'u' || g2 == 'u')
            return "u";
        else if (g1 == 'p' && (g2=='m' || g2=='f' || g2=='p') )
            return "u";
        else if (g2 == 'p' && (g1=='m' || g1=='f' || g1=='p') )
            return "u";
        else if (g1 == g2)
            return "t";
        else
            return "f";
    }
    public static String doGendersMatchPair(CExample ex, boolean useWN,
    	     boolean useCache) {
    	        char g1 = getGender(ex.getM1(), useWN, useCache);
    	        char g2 = getGender(ex.getM2(), useWN, useCache);
    	        Mention m1 = ex.getM1();
    	        Mention m2 = ex.getM2();
    	        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
    	            	(EntityTypeFeatures.getEType(m1).equals("PER") || EntityTypeFeatures.getEType(m1).equals("ORG")|| 
    	            			EntityTypeFeatures.getEType(m1).equals("GPE")))
    	            	return "n_n";
    	        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
    	            	(EntityTypeFeatures.getEType(m1).equals("LOC")||(EntityTypeFeatures.getEType(m1).equals("MISC"))) )
    	            	return "n_n";
    	        return g1+"_"+g2;
    	        /*if (g1 == 'u' || g2 == 'u')
    	            return "u";
    	        else if (g1 == 'p' && (g2=='m' || g2=='f' || g2=='p') )
    	            return "u";
    	        else if (g2 == 'p' && (g1=='m' || g1=='f' || g1=='p') )
    	            return "u";
    	        else if (g1 == g2)
    	            return "t";
    	        else
    	            return "f";*/
    	    }

    /**
     * Determines the gender of a mention.
     * First, determines whether the mention is a proper name, common noun,
     * or pronoun, and then delegate to the corresponding method.
     * Caching will not be done.
     * @param m The mention whose gender to determine.
     * @param useWN Whether WordNet should be used to help predict genders?
     * @return 'm' (male), 'f' (female), 'n' (neuter), or 'u' (unknown).
     */
    public static char getGender(Mention m, boolean useWN) {
    	boolean useCache = false;
    	return getGender(m, useWN, useCache);
    }
    
    /**
     * Determines the gender of a mention.
     * First, determines whether the mention is a proper name,common noun,
     * or pronoun, and then delegate to the corresponding method.   
     * @param m The mention whose gender to determine.
     * @param useWN Whether WordNet should be used to help predict genders?
     * @param useCache Whether to cache genders based on the head text.
     * @return 'm' (male), 'f' (female), 'n' (neuter), 'p' (person) or 'u' (unknown).
     */
    public static char getGender(Mention m, boolean useWN,
     boolean useCache) {
        String head = m.getHead().getText();
        char g = 'u';
    
	if (m_genderCache == null) {
	    m_genderCache = new HashMap<String,Character>();
	}

	boolean put = true;
	if (useCache && m_genderCache.containsKey(head)) {
	    g = m_genderCache.get(head);
	    put = false;
        } else if (head.length() == 0) {
            g = 'u';
        } else if (m.getType().equals("NAM")) {
            g = getNameGender(m);
        } else if (m.getType().equals("PRE")) {
            if (m.getDoc().isCaseSensitive()) {
        	char h1 = head.charAt(0);
        	if (Character.isUpperCase(h1)) {
        	    g = getNameGender(m);
        	} else {
        	    g = getNominalGender(m, useWN);
        	}
            } else {
        	char gNAM = getNameGender(m);
        	char gNOM = getNominalGender(m, useWN);
        	if (gNAM == 'u' && gNOM != 'u') {
        	    g = gNOM;
        	} else {
        	    g = gNAM;
        	}
            }
        } else if (m.getType().equals("NOM")) {
            //System.out.println("Getting nominal gender of " + head);
            g = getNominalGender(m, useWN);
        } else if (m.getType().equals("PRO")) {
            g = getPronounGender(head);
        } else {
            g = 'u';
        }

        if (useCache && put)
	    m_genderCache.put(head, g);
    
        char stronger
         = GenderFeatures.getStrongerGender(g, m.m_predGender); //Prefer cached.
        return stronger;
    }

    /**
     * Determines the gender of a mention.
     * For use in reproducing published data.
     * @param ex An example.
     * @param useWN Whether WordNet should be used to help predict genders?
     * @return Whether genders match, "true", "false", or "unknown".
     */
	public static String doGendersMatchForCompatible(CExample ex, boolean useWN)
    {
    	Mention m1 = ex.getM1();
    	Mention m2 = ex.getM2();
        // Special Case They and their can be refer to People groups or Organization:
    	//if(Parameters.Debug)
    	//	System.out.println("m2: "+ PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) +" "+EntityTypeFeatures.getEType(m1) );
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
        	(EntityTypeFeatures.getEType(m1).equals("PER") || EntityTypeFeatures.getEType(m1).equals("ORG")|| 
        			EntityTypeFeatures.getEType(m1).equals("GPE")))
        	return "t";

        char g1 = getGenderForCompatible(m1);
        char g2 = getGenderForCompatible(m2);
        if (g1 == 'u' || g2 == 'u')
            return "u";
        else if (g1 == 'p' && (g2=='m' || g2=='f' || g2=='p') )
            return "u";
        else if (g2 == 'p' && (g1=='m' || g1=='f' || g1=='p') )
            return "u";
        else if (g1 == g2)
            return "t";
        else
            return "f";
    }
    
    /**
     * This is a legacy function for use as a feature for the learned classifier
     * that predicts simple coreference, used to determine whether names match.
     * @param ex An example.
     * @return Whether genders match, "true", "false", or "unknown".
     */
    public static String doGendersMatchForBasic(CExample ex) {
	//TODO: Make sure split heads don't contain punctuation.
	String m1Head = ex.getM1().getHead().getText();
	String m2Head = ex.getM2().getHead().getText();
	String[] m1Words = m1Head.split("\\s");
	String[] m2Words = m2Head.split("\\s");
	if (m1Words.length < 2 || m2Words.length < 2)
	    return "unknown";

	//Get first names:
	String firstName1 = null, firstName2 = null, honor1 = null, honor2 = null;
	if ( Gazetteers.getHonors().contains(m1Words[0].toLowerCase()) ) {
	    honor1 = m1Words[0].toLowerCase();
	    honor1 = removePunctuationFromEnds(honor1);
	    if (m1Words.length > 2) //Don't get lastname as firstname.
		firstName1 = m1Words[1].toLowerCase();
	} else {
	    firstName1 = m1Words[0].toLowerCase();
	}

	if ( Gazetteers.getHonors().contains(m2Words[0].toLowerCase()) ) {
	    honor2 = m2Words[0].toLowerCase();
	    honor2 = removePunctuationFromEnds(honor2);
	    if (m2Words.length >2)
		firstName2 = m2Words[1].toLowerCase();
	} else {
	    firstName2 = m2Words[0].toLowerCase();
	}

	//Get genders:
	char gender1 = 'u', gender2 = 'u';

	if ( firstName1 != null && Gazetteers.getMaleFirstNames().contains(firstName1) )
	    gender1 = 'm';
	else if ( firstName1 != null && Gazetteers.getFemaleFirstNames().contains(firstName1) )
	    gender1 = 'f';

	if ( firstName2 != null && Gazetteers.getMaleFirstNames().contains(firstName2) )
	    gender2 = 'm';
	else if ( firstName2 != null && Gazetteers.getFemaleFirstNames().contains(firstName2) )
	    gender2 = 'f';
	//Honors (Mr Mrs etc) take priority over names:
	if (honor1 != null && ( honor1.equals("mr") || honor1.equals("mister") || honor1.equals("mr.")) )
	    gender1 = 'm';
	if (honor2 != null && ( honor2.equals("mr") || honor2.equals("mister")|| honor2.equals("mr.") ) )
	    gender2 = 'm';

	if (honor1 != null && ( honor1.equals("ms") || honor1.equals("mrs") || honor1.equals("queen")||honor1.equals("ms.") || honor1.equals("mrs.")
		|| honor1.equals("miss") || honor1.equals("misses") ) )
	    gender1 = 'f';
	if (honor2 != null && ( honor2.equals("ms") || honor2.equals("mrs")||honor2.equals("ms.") || honor2.equals("mrs.")
		|| honor2.equals("miss") || honor2.equals("misses") ) )
	    gender2 = 'f';
	//Pronouns:
	char pro1Gender = GenderFeatures.getPronounGender(m1Words[0]);
	if (pro1Gender == 'm' || pro1Gender == 'f')
	    gender1 = pro1Gender;
	char pro2Gender = GenderFeatures.getPronounGender(m2Words[0]);
	if (pro2Gender == 'm' || pro1Gender == 'f')
	    gender2 = pro2Gender;

	//Match genders:
	if (gender1 == 'u' || gender2 == 'u')
	    return "unknown";
	else if (gender1 == gender2)
	    return "true";
	else
	    return "false";
    }
    
    
    /**
     * Gets the gender of a mention.
     * This is the legacy function for use in reproducing
     * published data.
     * @param m A mention.
     * @return The gender of the mention.
     */
    public static char getGenderForCompatible(Mention m) {
	boolean useWN = true;

	String head = m.getHead().getText();
	char g = 'u';
	if (head.length() == 0) {
	    g = 'u';
	} else if (m.getType().equals("NAM")) {
	    g = getNameGender(m);
	} else if (m.getType().equals("PRE")) {
	    if (m.getDoc().isCaseSensitive()) {
		char h1 = head.charAt(0);
		if (Character.isUpperCase(h1)) {
		    g = getNameGender(m);
		} else {
		    g = getNominalGender(m, useWN);
		}
	    } else {
		char gNAM = getNameGender(m);
		char gNOM = getNominalGender(m, useWN);
		if (gNAM == 'u' && gNOM != 'u') {
		    g = gNOM;
		} else {
		    g = gNAM;
		}
	    }
	} else if (m.getType().equals("NOM")) {
	    //System.out.println("Getting nominal gender of " + head);
	    g = getNominalGender(m, useWN);
	} else if (m.getType().equals("PRO")) {
	    g = getPronounGender(head);
	} else {
	    g = 'u';
	}
	char stronger
	    = getStrongerGender(g, m.m_predGender); //Prefer cached.
	//if(Parameters.Debug){
//		System.out.println(m+ "stronger" + stronger + " g: "+ g + " cached: " + m.m_predNumber);
	//}
	return stronger;
    }

    /**
     * Gets the gender of the mention, prioritizing
     * cities over names (resolving cases where a name
     * could either be a city or a person).
     * Used to reproduce published data. 
     * @param m The mention, assumed to be a proper name.
     * @return The gender of this name. 
     */
    public static char getNameGenderCitiesFirst(Mention m) {
	//TODO: Capitalize on Case sensitivity.
	char g = 'u';
	String head = m.getHead().getText().toLowerCase();
	String[] words = head.split("\\s");
	if (words.length == 0)
	    return 'u';
	String word0 = words[0];
	String lastWord = words[words.length-1];



	//TODO: Handle multiple honors.

	//Get first name:
	
    String firstName = null, honor = null;
    if(m.getHeadFirstWordNum() - m.getExtentFirstWordNum() >0){
    	String wordBefore = m.getDoc().getWord(m.getHeadFirstWordNum()-1).toLowerCase();
    	if ( Gazetteers.getHonors().contains(wordBefore) ) {
    		honor = wordBefore;
    		//System.out.println(m + "Honor:" + honor);
    	}
    }
	
	if ( Gazetteers.getHonors().contains(word0) ) {
	    honor = word0;
	    honor = removePunctuationFromEnds(honor);
	    if (words.length >= 3) { //Dr. First ... Last
		firstName = words[1];
	    }
	} else if (words.length >= 2) { //Probably First ... Last:
	    firstName = word0;
	} else { //Either Firstname or Lastname or not PER.
	    //Here assume firstName, and reject later if not a known name.
	    firstName = word0;
	}
	//Get gender:

	//Honors (Mr Mrs etc) take priority over names:
	if (honor != null && ( honor.equals("mr") || honor.equals("mister")  || honor.equals("king")|| honor.equals("mr."))) 
	{
	    g = 'm';
	} else if (honor != null && ( honor.equals("ms") || honor.equals("mrs") || honor.equals("queen")
			 || honor.equals("miss") || honor.equals("misses")||honor.equals("ms.") || honor.equals("mrs.") ) )
	{

	    g = 'f';
	} else if(honor != null)
		g = 'p';
	//First name gender:
	if ( firstName != null
	 && Gazetteers.getMaleFirstNames().contains(firstName) )
	{
	    g = 'm';
	} else if ( firstName != null
	 && Gazetteers.getFemaleFirstNames().contains(firstName) )
	{
	    g = 'f';
	}
	else if (Gazetteers.getLastNames().contains(lastWord) ||EntityTypeFeatures.getEType(m).equals(Constants.NER_PER ))
		g = 'p';
	
    if (Gazetteers.getCities().contains(head) || Gazetteers.getCountries().contains(head) ||(
    		!EntityTypeFeatures.getEType(m).equals(Constants.NER_PER))  &&!EntityTypeFeatures.getEType(m).equals(Constants.NER_UNKNOWN)) {
        if (g == 'm' || g == 'f' || g == 'p')
    	return 'u';
        g = 'n';
    }
    if (Gazetteers.getOrgClosings().contains(lastWord)) {
        if (g == 'm' || g == 'f' || g == 'p')
    	return 'u';
        g = 'n';
    }
	return g;
    }

    /** 
     * Determines the gender of a mention, assuming it is a proper name.
     * @return 'm'ale, 'f'emale, 'p'erson, 'n'euter, or 'u'nknown.
     */
    public static char getNameGender(Mention m) {
        //TODO: Capitalize on Case sensitivity.
    	boolean debug = false;
    	//if(m.getHead().getText().toLowerCase().equals("new york"))
    		//debug = true;
        char g = 'u';
        String head = m.getHead().getText().toLowerCase();
        String[] words = head.split("\\s");
        if (words.length == 0)
            return g;
        String word0 = words[0];
        String lastWord = words[words.length-1];
    
        //TODO: Handle multiple honors.
        //Honor can be outside the head
        String firstName = null, honor = null;
        if(m.getHeadFirstWordNum() - m.getExtentFirstWordNum() >0){
        	String wordBefore = m.getDoc().getWord(m.getHeadFirstWordNum()-1).toLowerCase();
        	if ( Gazetteers.getHonors().contains(wordBefore) ) {
        		honor = wordBefore;
        		//System.out.println(m + "Honor:" + honor);
        	}
        }
        //Get first name:
        
        if ( Gazetteers.getHonors().contains(word0) ) {
            honor = word0;
            //TODO: Get a separate punctuation splitter?
            honor = removePunctuationFromEnds(honor);
            if (words.length >= 3) { //Dr. First ... Last
        	firstName = words[1];
            }
        } else if (words.length >= 2) { //Probably First ... Last:
            firstName = word0;
        } else { //Either Firstname or Lastname or not PER.
            //Here assume firstName, and reject later if not a known name.
            firstName = word0;
        }
        
        //Get gender:
        //Honors (Mr Mrs etc) take priority over names:
        if (honor != null && ( honor.equals("mr") || honor.equals("mister")|| honor.equals("king") || honor.equals("mr."))) 
        {
            return 'm';
        } else if (honor != null && ( honor.equals("ms") || honor.equals("mrs") || honor.equals("ms.") || honor.equals("mrs.")
        		 || honor.equals("miss") || honor.equals("misses") || honor.equals("queen")))
        {
            return 'f';
        }
        
        //First name gender:
        if ( firstName != null && Gazetteers.getMaleFirstNames().contains(firstName) )
        {
            g = 'm';
        } else if ( firstName != null
         && Gazetteers.getFemaleFirstNames().contains(firstName) ) {
            g = 'f';
        } else if (honor != null || Gazetteers.getLastNames().contains(lastWord) || EntityTypeFeatures.getEType(m).equals(Constants.NER_PER)) {
            g = 'p';
        }
		if(!EntityTypeFeatures.getEType(m).equals(Constants.NER_PER)  &&!EntityTypeFeatures.getEType(m).equals(Constants.NER_UNKNOWN)) {
			g = 'n';
		}
        if(debug)
        	System.out.println(m + " : #" + g);
        if (Gazetteers.getCities().contains(head) || Gazetteers.getCountries().contains(head)){
            if (g == 'm' || g == 'f' || g == 'p')
        	return 'u';
            g = 'n';
        }

        if (Gazetteers.getOrgClosings().contains(lastWord)) {
            if (g == 'm' || g == 'f' || g == 'p')
        	return 'u';
            g = 'n';
        }
        if(debug)
        	System.out.println(m + " : #" + g);
        return g;
    }

    /** 
     * Determines the gender of a mention, assuming it is a nominal.
     * @return 'm'ale, 'f'emale, 'p'erson, 'n'euter, or 'u'nknown.
     */
    public static char getNominalGender(Mention m, boolean useWN) {
        if (!useWN)
            return 'u';
        String head = m.getHead().getText().toLowerCase();
        if(m.getHeadLastWordNum() +1 < m.getDoc().getWords().size())
        	if(m.getDoc().getWord(m.getHeadFirstWordNum()+1).equals("who"))
        		return 'p';
        //Some special cases
        if(head.startsWith("region") || head.equals("city"))
        	return 'n';
        if(head.startsWith("leader")||head.startsWith("person")||head.startsWith("people"))
        	return 'p';
        if(head.startsWith("part") || head.equals("city"))
        	return 'n';
        if (WordNetTools.getWN().areHypernyms(head, "male")) {
            return 'm';
        } else if (WordNetTools.getWN().areHypernyms(head, "female")) {
            return 'f';
        } else if (WordNetTools.getWN().areHypernyms(head, "person")) {
            return 'p';
        } else if (WordNetTools.getWN().areHypernyms(head, "artifact")
         || WordNetTools.getWN().areHypernyms(head, "location")
         || WordNetTools.getWN().areHypernyms(head, "group") //covers ORG and political entity
        ) {
            return 'n';
        } else {
            return 'u';
        }
    }

    /** 
     * Determines the gender of a mention, assuming it is a pronoun.
     * @return 'm'ale, 'f'emale, 'p'erson, 'n'euter, or 'u'nknown.
     */
    public static char getPronounGender(String word) {
        //TODO: Gender of possessive pronouns?
        String lW = word.toLowerCase();
        if (lW.equals("he") || lW.equals("him") || lW.equals("his")
         || lW.equals("himself")	   
        ) {
            return 'm';
        } else if (lW.equals("she") || lW.equals("her") || lW.equals("hers")
                || lW.equals("herself")
        ) {
            return 'f';
        } else if (lW.equals("it") || lW.equals("its") || lW.equals("itself")
                || lW.equals("anything") || lW.equals("something")
        	|| lW.equals("everything")
        	|| lW.equals("nothing")
        	|| lW.equals("which") || lW.equals("what")
        	|| lW.equals("whatever") || lW.equals("whichever")
            //Don't include because could be personal:
            //  || lW.equals("whose")
            //	|| lW.equals("these") || lW.equals("those")
            //	|| lW.equals("any") || lW.equals("each") 
            //	|| lW.equals("either") || lW.equals("neither")
            //	|| lW.equals("all") || lW.equals("most") || lW.equals("some") 
            //	|| lW.equals("several") || lW.equals("none")
            //  || lW.equals("both") || lW.equals("few") || lW.equals("many")
        ) {
            return 'n';
        } else if (lW.equals("you") || lW.equals("your") || lW.equals("yours")
                || lW.equals("i")
                || lW.equals("me") || lW.equals("my") || lW.equals("mine")
                || lW.equals("we") || lW.equals("our") || lW.equals("ours")
                || lW.equals("us")
            
                //|| lW.equals("they")|| lW.equals("their") || lW.equals("theirs")
                //|| lW.equals("them") 
        	|| lW.equals("myself") || lW.equals("ourselves")
        	|| lW.equals("themselves") || lW.equals("themself")
        	|| lW.equals("ourself") || lW.equals("oneself")
    
        	|| lW.equals("who") || lW.equals("whom") || lW.equals("whose")
        	|| lW.equals("whoever") || lW.equals("whomever")
        	|| lW.equals("anyone") || lW.equals("anybody")
        	|| lW.equals("someone") || lW.equals("somebody")
        	|| lW.equals("everyone") || lW.equals("everybody") 
        	|| lW.equals("nobody")
            //Not included because could be neuter:
            //	|| lW.equals("one") || lW.equals("one's") || lW.equals("ones")
        ) {
            return 'p';
        } else {
            return 'u';
        }
    
    }

    /**
     * Gets the stronger of the two genders. 
     * A gender is stronger than another if one is more specific than the other.
     * @param g1 One gender ('m', 'f', 'p', or 'u').
     * @param g2 Another gender ('m', 'f', 'p', or 'u').
     * @return The stronger of the two genders,
     * favoring g2 in case g1 and g2 are equal strength.
     */
    public static char getStrongerGender(char g1, char g2) {
        if ( (g1 == 'm' || g1 == 'f') && (g2 == 'p' || g2 == 'u') )
            return g1;
        else if ( (g2 == 'm' || g2 == 'f') && (g1 == 'p' || g1 == 'u') )
            return g2;
        else if ( (g1 == 'n' || g1 == 'p') && g2 == 'u' )
            return g1;
        else if ( (g2 == 'n' || g2 == 'p') && g1 == 'u' )
            return g2;
        else {
            return g2;
        }
    }


    /* Utilities */

    /**
     * Trims punctuation from string.
     * @param s A String.
     * @return The string with the leading and trailing punctuation removed.
     */
    public static String removePunctuationFromEnds(String s) {
	int first = 0;
	while (first < s.length()
	    && (s.charAt(first) == '\'' || s.charAt(first) == '"'
		|| s.charAt(first) == '(' || s.charAt(first) == ')'
		|| s.charAt(first) == ',' || s.charAt(first) == ';'
		|| s.charAt(first) == '.' || s.charAt(first) == '`'))
	    first++;

	int last = s.length() - 1;
	while (last >= first
	    && (s.charAt(last) == '\'' || s.charAt(last) == '"'
		|| s.charAt(last) == '(' || s.charAt(last) == ')'
		|| s.charAt(last) == ',' || s.charAt(last) == ';'
		|| s.charAt(last) == '.' || s.charAt(last) == '`'))
	    last--;
	String r = s.substring(first, last + 1);
	return r;
    }

}
