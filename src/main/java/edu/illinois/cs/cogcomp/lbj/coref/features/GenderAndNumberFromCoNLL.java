package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;


//TODO: Distinguish between grammatical gender and entity gender in Javadoc.

/**
 * A collection of feature generating functions and utilities for
 * determining the gender of an entity.\t+
 */
public class GenderAndNumberFromCoNLL {

    /** A Cache of genders. */
    protected static Map<String,Character> m_genderCache;
    protected static Map<String,Character> m_numberCache;
    //protected static Map<String,Character> m_numberCache;
    protected static Map<String,Character> m_genderCoNLL;
    //protected static Map<String,Character> m_numberCoNLL;
    /** Should not need to construct this static library of features. */
    protected GenderAndNumberFromCoNLL() {
    	initCoNLLGenderAndNumber();
    }

    //Initialize cache
    private static void initCoNLLGenderAndNumber() {
    	//System.err.println("start loading gender file");
    	m_genderCoNLL = new HashMap<String, Character>();
    	//m_numberCoNLL = new HashMap<String, Character>();
    	String filename = "gender.data";    	
    	List<String> lines = (new myIO()).readLines(filename);
    	for (String line : lines) {
    		String[] parts = line.split("\\s+");
    		String[] words = Arrays.copyOfRange(parts, 0, parts.length -4);
    		String phase = StringUtils.join(words, " ");
    		int male = Integer.valueOf(parts[parts.length -4]);
    		int female = Integer.valueOf(parts[parts.length -3]);
    		int neutral = Integer.valueOf(parts[parts.length -2]);
    		int plural = Integer.valueOf(parts[parts.length -1]);
    		int total = male + female + neutral;
    		double maleRate = (total>0)?(double) male / (double)(total):0;
    		double femaleRate = (total>0)?(double) female / (double)total:0;
    		double pluralRate = (double)plural / (plural + total);
    		
    		// Only consider the phase appear more than 5 times
    		if(total >= 50){
    				// if it is a plural noun, we cannot decide gender from the giving gender information files.
    				if(pluralRate > 0.8)
    					m_genderCoNLL.put(phase, 'p');
    				//m: male, f: female, n: notHuman, s: singlePerson, p: plural u: unknown 
    				else if(maleRate > 0.8 && femaleRate < 0.15 && pluralRate < 0.3)
    					m_genderCoNLL.put(phase, 'm');
    				else if(femaleRate > 0.8 && maleRate < 0.15 && pluralRate < 0.3)
    					m_genderCoNLL.put(phase, 'f');
    				else if(maleRate + femaleRate > 0.8 && pluralRate < 0.3)
    					m_genderCoNLL.put(phase, 's');
    				else if(maleRate + femaleRate < 0.5 && pluralRate < 0.3)
    					m_genderCoNLL.put(phase, 'n');
    		}
    	}
    	//System.err.println("finish loading gender file");
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
        //System.out.println(ex.getM1().getText()+ " " + ex.getM2().getText() + g1 + " " + g2);
        if (g1 == 'u' || g2 == 'u')
            return "u";
        else if (g1 == 'p' && (g2=='m' || g2=='f' || g2=='p') )
            return "u";
        else if (g2 == 'p' && (g1=='m' || g1=='f' || g1=='p') )
            return "u";
        else if (g1 == g2){
        	//System.out.println("true");
            return "t";
        }
        else{
        	//System.out.println("false!");
            return "f";
        }
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
     * @return 'm' (male), 'f' (female), 'n' (neuter), or 'u' (unknown).
     */
    public static char getNumber(Mention m, boolean useWN,
     boolean useCache){
    	if (m_genderCache == null ||m_numberCache == null) {
    		initCoNLLGenderAndNumber();
    	    m_numberCache = new HashMap<String,Character>();
    	    m_genderCache = new HashMap<String,Character>();
    	}
    	if(m.getHead().getText().toLowerCase().equals("and"))
    		return 'p';
    	String extent = m.getExtent().getText();
    	if (useCache && m_numberCache.containsKey(extent))
    		return m_numberCache.get(extent);
    	
    	
    	char n = NumberFeatures.getNumberStrong(m, useWN);
    	if(n == 'u'){
    		n = queryInfo(m, n);
    		if(n != 'p')
    			n = 's';
    	}
    	
    	if (useCache)
    		m_numberCache.put(extent, n);
    	return n;
    }
    public static String doNumbersMatch(CExample ex, boolean useDicts) {
        String ptext = ex.getM2().getText().toLowerCase();
        Mention m1 = ex.getM1();
        Mention m2 = ex.getM2();
        char n1 = getNumber(m1, useDicts, true);
        char n2 = getNumber(m2, useDicts, true);
        // Some special cases
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("PER") || EntityTypeFeatures.getEType(m1).equals("ORG")|| 
            			EntityTypeFeatures.getEType(m1).equals("GPE")))
            	return "t";
        if(PronounFeatures.pronounReferToOrgnize(m2.getExtent().getText().toLowerCase()) && 
            	(EntityTypeFeatures.getEType(m1).equals("LOC")||(EntityTypeFeatures.getEType(m1).equals("MISC"))) )
            	return "u";
        if (n1 == n2 && n1 != 'u')
            return "t";
        if (n1 != n2 && n1 != 'u' && n2 != 'u')
            return "f";
        if (n1 == 'u' || n2 == 'u') {
            if ( ex.getM1().getExtent().getText().equals(
             ex.getM2().getExtent().getText()) )
            {
        	return "t";
            } else {
        	return "u";
            }
        }
        return "u"; //Shouldn't happen.
    }
    public static char getGender(Mention m, boolean useWN,
     boolean useCache) {
        String head = m.getHead().getText();
        String extent = m.getExtent().getText();
        char g = 'u';
        if(m.getHead().equals("us"))
        	g = 'p';
        if (m_genderCache == null || m_numberCache == null) {
        	initCoNLLGenderAndNumber();
        	m_numberCache = new HashMap<String,Character>();
        	m_genderCache = new HashMap<String,Character>();
        }

        if (useCache && m_genderCache.containsKey(extent))
        	return m_genderCache.get(extent);
        if (g == 'u' && !(head.length() == 0)) {
        	if(m.getType().equals("PRO"))
        		g = GenderFeatures.getPronounGender(head);
        	if(myAux.isMentionSurfaceContainsWords(m, new String[]{"men","boy","boys","son","sons","king","kings","uncle","uncles","fathers","father"}, false))
        		g = 'm';
        	if(myAux.isMentionSurfaceContainsWords(m, new String[]{"women","girl","girls","daughter","daughters","queen","queens","aunt","aunts","mothers","mother"}, false))
        		g = 'f';	
        	// Some special nominal gender (fix the gender error for a plurl noun
        	//g = getStrictGender(m, useWN);
        }
        if (g == 'u' /*&& m.getType().equals("NAM") && EntityTypeFeatures.getEType(m).equals("PER")*/) {
        	g = queryInfo(m, g);
        	if(g == 'p')
        		g = 'u';
        	if(g == 's')
        		g = 'p';

        }
        if (useCache)
        	m_genderCache.put(extent, g);

        char stronger = GenderFeatures.getStrongerGender(g, m.m_predGender); //Prefer cached.
        return stronger;
    }

    private static char queryInfo(Mention m, char g) {
    	boolean shortExtent = (m.getExtent().getWords().size() < 5);
		String head = m.getHead().getText();
		String extent = m.getExtent().getText();
		String headQuerry = head.toLowerCase().replaceAll("[0-9]+","#");
		String headQuerryNoFirst = headQuerry.replaceAll("^\\S* ","! ");
		String headQuerryNoLast = headQuerry.replaceAll(" \\S*$", " !");
		String extentQuerry = extent.toLowerCase().replaceAll("[0-9]+","#");
		String extentQuerryNoFirst = extentQuerry.replaceAll("^\\S* ","! ");
		String extentQuerryNoLast = extentQuerry.replaceAll(" \\S*$", " !");
		if(shortExtent && m_genderCoNLL.containsKey(extentQuerry))
			g = m_genderCoNLL.get(extentQuerry);
		else if(m_genderCoNLL.containsKey(headQuerry))
			g = m_genderCoNLL.get(headQuerry);
		else if(m_genderCoNLL.containsKey(headQuerryNoFirst))
			g = m_genderCoNLL.get(headQuerryNoFirst);
		else if(m_genderCoNLL.containsKey(headQuerryNoLast))
			g = m_genderCoNLL.get(headQuerryNoLast);
		else if(m_genderCoNLL.containsKey(extentQuerry))
			g = m_genderCoNLL.get(extentQuerry);
		else if (m_genderCoNLL.containsKey(extentQuerryNoFirst))
			g = m_genderCoNLL.get(extentQuerryNoFirst);
		else if (m_genderCoNLL.containsKey(extentQuerryNoLast))
			g = m_genderCoNLL.get(extentQuerryNoLast);
		return g;
	}
    
    
}
