package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/** 
 * A class for loading a list of pairs of capitals of countries
 * and checking to see whether one location is the capital of a country.
 * Methods to check matches are in the superclass.
 * In general, lowercase strings should be used when storing or querying.
 */
public class Capitals extends Matches {
    
    /** The default file. */
    public static String DEFAULT_FILENAME = "gazetteers/countriesCapitals.txt";

    //Format subject to change:
    //For now, both store lowercase info only:
    //maps each country to a string representing possibly multiple capitals
    protected Map<String,String> m_countryToCapitals;
    //Maps each capital to a country (uniquely)
    protected Map<String,String> m_capitalsToCountry;

    /**
     * Default constructor.
     * Loads a list of capitals from the default file name.
     */
    public Capitals() {
	super(DEFAULT_FILENAME);
    }

    /**
     * The main method, useful for testing.
     * @param args No arguments are needed.
     */
    public static void main(String args[]) {
	Capitals cp = new Capitals();
	System.err.println(cp);
    }

    /** Loads the set of capitals. */
    protected void load(String filename) {
	List<String> lines = (new myIO()).readLines(filename);

	m_countryToCapitals = new HashMap<String,String>();
	m_capitalsToCountry = new HashMap<String,String>();
	for (String line : lines) {
	    String[] parts = line.split(":");
	    if (1 < parts.length) {
		String country = parts[0].toLowerCase();
		String fullCapitals = parts[1].toLowerCase();
		m_countryToCapitals.put(country, fullCapitals);

		String[] caps = fullCapitals.split(", ");
		for (int i = 0; i < caps.length; ++i) {
		    String strippedCap = removeParenthetical(caps[i]);
		    m_capitalsToCountry.put(strippedCap, country);
		}
	    }
	}
    }
}
