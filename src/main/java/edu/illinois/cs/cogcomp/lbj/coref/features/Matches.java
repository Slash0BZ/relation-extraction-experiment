package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/**
 * Stores pairings of equivalent strings,
 * loaded from a file. 
 */
public class Matches {

    //Format subject to change:
    //For now, both store lowercased info only:
    protected Map<String,String> m_aToB;
    protected Map<String,String> m_bToA;

    /** Creates an empty matching. */
    public Matches() {
    }

    /**
     * Creates the mapping and loads it from the specified file.
     * @param filename The file specifying the mapping,
     * in the format specified by the {@code load} method.
     */
    public Matches(String filename) {
	load(filename);
    }

    /** 
     * Determines whether a pair of strings is a match.
     * This method is symmetric: {@code doMatch(s,t) == doMatch(t,s)}.
     * For now, this method is not case sensitive.
     * @param s One string
     * @param t Another string.
     * @return Whether the pair of strings is a match.
     */
    public boolean doMatch(String s, String t) {
	String lS = s.toLowerCase();
	String lT = t.toLowerCase();
	if (m_aToB.containsKey(lS) && m_aToB.get(lS).equals(lT)) {
	    return true;
	} else if (m_bToA.containsKey(lT) && m_bToA.get(lT).equals(lS)) {
	    return true;
	} else if (m_aToB.containsKey(lT) && m_aToB.get(lT).equals(lS)) {
	    return true;
	} else if (m_bToA.containsKey(lS) && m_bToA.get(lS).equals(lT)) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Converts this mapping to a string in an
     * unspecified format.  This format is likely
     * to be similar to the file format of the input,
     * except that both orders a to b and b to a
     * may be displayed.
     * @return A string representing the mapping.
     */
    public String toString() {
	String s = "A to B:\n";
	for (String a : m_aToB.keySet()) {
	    s += a + " : " + m_aToB.get(a) + "\n";
	}

	s += "\nB To A:\n";
	for (String b : m_bToA.keySet()) {
	    s += b + " : " + m_bToA.get(b) + "\n";
	}
	return s;
    }

    /**
     * Loads the matchings into {@code this} from a file.
     * The format of each line of the file is {@literal a:b}
     * Optionally, b may be a comma-space {@literal (", ")} separated list,
     * in which case, each element of that list will be associated
     * with a, while a will be associated with b treated as a single string.
     * @param filename The name of the file, which must be relative
     * to the classpath.
     */
    protected void load(String filename) {
	List<String> lines = (new myIO()).readLines(filename);

	m_aToB = new HashMap<String,String>();
	m_bToA = new HashMap<String,String>();
	for (String line : lines) {
	    String[] parts = line.split(":");
	    if (1 < parts.length) {
		String a = parts[0].toLowerCase();
		String fullB = parts[1].toLowerCase();
		m_aToB.put(a, fullB);

		String[] bs = fullB.split(", ");
		for (int i = 0; i < bs.length; ++i) {
		    String strippedB = removeParenthetical(bs[i]);
		    m_bToA.put(strippedB, a);
		}
	    }
	}
    }
    
    /**
     * Removes all parenthetical phrases not containing any nested parentheses. 
     * @param full The full string.
     * @return The string with parentheticals removed.
     */
    protected String removeParenthetical(String full) {
	//return full;
	return full.replaceAll("\\([^)(].*\\)","");
    }
}
