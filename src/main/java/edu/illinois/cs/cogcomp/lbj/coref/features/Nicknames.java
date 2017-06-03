package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/**
 * Stores a two-way mapping between nicknames and full names.
 * By default, the list of nicknames is stored in the file
 * "gazetteers/nicknamesPeople.txt" relative to the classpath.
 * @author Eric Bengtson
 */
public class Nicknames extends Matches {
    protected static String DEFAULT_FILENAME = "gazetteers/nicknamesPeople.txt";
    protected static Nicknames m_instance = null;
    
    /**
     * Constructs a new nicknames mapping from the default filename
     * (See the class comment for details).
     */
    public Nicknames() {
	super(DEFAULT_FILENAME);
    }

    /**
     * Gets the single instance of Nicknames.
     * @return The single instance of Nicknames, which is cached here.
     */
    public static Nicknames get() {
	if (m_instance == null) m_instance = new Nicknames();
	return m_instance;
    }

    /**
     * A main method for testing this class.
     * @param args None needed.
     */
    public static void main(String args[]) {
	Nicknames n = new Nicknames();
	System.err.println(n);
    }

    /**
     * Loads the mapping from the given filename into {@code this}.
     * The format of the file is one mapping per line
     * in the format a:b
     * Optionally, b may be a comma-space {@literal (", ")} separated list,
     * in which case, each element of that list will be associated
     * with a, while a will be associated with b treated as a single string.
     * Lines beginning with a pound sign ({@literal #}) are ignored.
     * @param filename The name of the file, relative to the classpath.
     */
    @Override
    protected void load(String filename) {
	List<String> lines = (new myIO()).readLines(filename);

	m_aToB = new HashMap<String,String>();
	m_bToA = new HashMap<String,String>();
	for (String line : lines) {
	    if (line.startsWith("#")) continue;

	    String[] parts = line.split("\\s");
	    if (1 < parts.length) {
		String a = parts[0].toLowerCase();
		String fullB = parts[1].toLowerCase();
		m_aToB.put(a, fullB);

		String[] bs = fullB.split(", ");
		for (int i = 0; i < bs.length; ++i) {
		    m_bToA.put(bs[i], a);
		}
	    }
	}
    }
}
