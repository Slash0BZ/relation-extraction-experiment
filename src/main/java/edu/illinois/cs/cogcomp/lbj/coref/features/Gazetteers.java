package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/**
 * A collection of gazetteers.
 * Each gazetteer is a set of items.  Gazetteers whose names end in CS
 * are case-sensitive; the others contain only lowercase items.
 * Any gazetteer may contain ambiguous items, which might appear
 * in multiple gazetteers.  For example, "Israel" is a male first name
 * and a country name.
 * All gazetteers will be loaded and kept in memory when any is requested.
 * @author Eric Bengtson
 */
public class Gazetteers {
    
    /** Should not need to construct this static feature collection. */
    protected Gazetteers() {
    }
    
    //Person:
    /** 
     * Gets the male first names gazetteer.
     * The gazetteer is a set of known male first names, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of male first names.
     */
    public static Set<String> getMaleFirstNames() {
	if (!gazetteersInitialized) initGazetteers();
        return maleFirstNames;
    }

    /** 
     * Gets the case-sensitive male first names gazetteer.
     * The gazetteer is a set of known male first names, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of male first names.
     */
    public static Set<String> getMaleFirstNamesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return maleFirstNamesCS;
    }
    
    
    /** 
     * Gets the female first names gazetteer.
     * The gazetteer is a set of known female first names, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of female first names.
     */
    public static Set<String> getFemaleFirstNames() {
	if (!gazetteersInitialized) initGazetteers();
        return femaleFirstNames;
    }
    
    /** 
     * Gets the case-sensitive male first names gazetteer.
     * The gazetteer is a set of known male first names, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of female first names.
     */
    public static Set<String> getFemaleFirstNamesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return femaleFirstNamesCS;
    }
    
    
    /** 
     * Gets the last names gazetteer.
     * The gazetteer is a set of known last names, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of last names.
     */
    public static Set<String> getLastNames() {
	if (!gazetteersInitialized) initGazetteers();
        return lastNames;
    }
    
    /** 
     * Gets the case-sensitive last names gazetteer.
     * The gazetteer is a set of known last names, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of last names.
     */
    public static Set<String> getLastNamesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return lastNamesCS;
    }
    
    /** 
     * Gets the honorary titles gazetteer.
     * The gazetteer is a set of honorary titles such as "mr" and "mrs",
     * in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of honorary titles like "mr" and "mrs".
     */
    public static Set<String> getHonors() {
	if (!gazetteersInitialized) initGazetteers();
        return honors;
    }
    
    
    
    //Location / GeoPolitical Entity:
    
    /** 
     * Gets the cities gazetteer.
     * The gazetteer is a set of known cities, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of city names.
     */
    public static Set<String> getCities() {
	if (!gazetteersInitialized) initGazetteers();
        return cities;
    }
    
    /** 
     * Gets the case-sensitive cities gazetteer.
     * The gazetteer is a set of known cities, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of city names.
     */
    public static Set<String> getCitiesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return citiesCS;
    }
    
    
    /** 
     * Gets the US states gazetteer.
     * The gazetteer is the set of the states in the UnitstopWordsed States,
     * in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of the states in the United States.
     */
    public static Set<String> getStates() {
	if (!gazetteersInitialized) initGazetteers();
        return states;
    }
    
    /** 
     * Gets the case-sensitive US states gazetteer.
     * The gazetteer is the set of the states in the United States,
     * case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of the states in the United States.
     */
    public static Set<String> getStatesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return statesCS;
    }
    
    
    /** 
     * Gets the countries gazetteer.
     * The gazetteer is the set of countries, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of countries.
     */
    public static Set<String> getCountries() {
	if (!gazetteersInitialized) initGazetteers();
        return countries;
    }
    
    /** 
     * Gets the case-sensitive countries gazetteer.
     * The gazetteer is the set of countries, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of countries.
     */
    public static Set<String> getCountriesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return countriesCS;
    }
    
    
    /** 
     * Gets the countries, country adjectives,
     * and country people names gazetteer.
     * The gazetteer is the set of all countries, country adjectives,
     * and country people names, in lowercase.
     * A country adjective is the adjective form of a country;
     * for example "american".
     * A country people name is the term used to describe
     * the residents of a country; for example "americans".
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of countries,
     * country adjectives, and country people names.
     */
    public static Set<String> getCountriesDemAdj() {
	if (!gazetteersInitialized) initGazetteers();
        return countriesDemAdj;
    }

    /** 
     * Gets the countries, country adjectives,
     * and country people names gazetteer.
     * The gazetteer is the set of all countries, country adjectives,
     * and people groups, case preserved.
     * A country adjective is the adjective form of a country;
     * for example "American".
     * A country people name is the term used to describe
     * the residents of a country; for example "Americans".
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of countries,
     * country adjectives, and country people names.
     */
    public static Set<String> getCountriesDemAdjCS() {
	if (!gazetteersInitialized) initGazetteers();
        return countriesDemAdjCS;
    }    
    
    
    
    //Organizations:
    
    /** 
     * Gets the political parties gazetteer.
     * The gazetteer is a set of known political parties, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of political parties.
     */
    public static Set<String> getPolParties() {
	if (!gazetteersInitialized) initGazetteers();
        return polParties;
    }
    
    
    /** 
     * Gets the corporations gazetteer.
     * The gazetteer is a set of known corporations, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of corporation names.
     */
    public static Set<String> getCorporations() {
	if (!gazetteersInitialized) initGazetteers();
        return corporations;
    }
    
    /** 
     * Gets the corporations gazetteer.
     * The gazetteer is a set of known corporations, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of corporation names.
     */
    public static Set<String> getCorporationsCS() {
	if (!gazetteersInitialized) initGazetteers();
        return corporationsCS;
    }
    
    
    /** 
     * Gets the organization identifier suffixes gazetteer.
     * The gazetteer is a set of organization identifier suffixes,
     * such as "inc", "llc", and "org", in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     *  @return A case-insensitive set of organization identifier suffixes,
     * such as "inc", "llc", and "org".
     */
    public static Set<String> getOrgClosings() {
	if (!gazetteersInitialized) initGazetteers();
        return orgClosings;
    }
    
    
    /** 
     * Gets the sports teams gazetteer.
     * The gazetteer is a set of known sports teams, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of sports team names.
     */
    public static Set<String> getSportTeams() {
	if (!gazetteersInitialized) initGazetteers();
        return sportTeams;
    }
    
    /** 
     * Gets the sports teams gazetteer.
     * The gazetteer is a set of known sports teams, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of sports team names.
     */
    public static Set<String> getSportTeamsCS() {
	if (!gazetteersInitialized) initGazetteers();
        return sportTeamsCS;
    }
    
    
    /** 
     * Gets the universities gazetteer.
     * The gazetteer is a set of known universities, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of university names.
     */
    public static Set<String> getUniversities() {
	if (!gazetteersInitialized) initGazetteers();
        return universities;
    }
    
    /** 
     * Gets the universities gazetteer.
     * The gazetteer is a set of known universities, case preserved.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-sensitive set of university names.
     */
    public static Set<String> getUniversitiesCS() {
	if (!gazetteersInitialized) initGazetteers();
        return universitiesCS;
    }

    
    
    //Linguistic:
    
    /** 
     * Gets the stop words gazetteer.
     * The gazetteer is a set of stop words such as "and" and "of",
     * in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of stop words such as "and" and "of".
     */
    public static Set<String> getStopWords() {
	if (!gazetteersInitialized) initGazetteers();
        return stopWords;
    }

    /** 
     * Gets the prepositions gazetteer.
     * The gazetteer is a set of prepositions, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of prepositions.
     */
    public static Set<String> getPrepositions() {
	if (!gazetteersInitialized) initGazetteers();
        return prepositions;
    }
    
    /** 
     * Gets the pronouns gazetteer.
     * The gazetteer is a set of pronouns, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of pronouns.
     */
    public static Set<String> getPronouns() {
	if (!gazetteersInitialized) initGazetteers();
        return pronouns;
    }
    
    /** 
     * Gets the singular nouns gazetteer.
     * The gazetteer is a set of singular nouns, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of singular nouns.
     */
    public static Set<String> getSingularNouns() {
	if (!gazetteersInitialized) initGazetteers();
        return singularNouns;
    }
    
    /** 
     * Gets the plural nouns gazetteer.
     * The gazetteer is a set of plural nouns, in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of plural nouns.
     */
    public static Set<String> getPluralNouns() {
	if (!gazetteersInitialized) initGazetteers();
        return pluralNouns;
    }
    
    /** 
     * Gets the say words gazetteer.
     * The gazetteer is a set of words synonymous with "say", in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of words synonymous with "say".
     */
    public static Set<String> getSayWords() {
	if (!gazetteersInitialized) initGazetteers();
        return sayWords;
    }
    
    /** 
     * Gets the lowercase words gazetteer.
     * The gazetteer is a set of words
     * that begin with a lowercase letter in a dictionary
     * (probably indicating that they are not proper names), in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of words
     * that begin with a lowercase letter in a dictionary
     * (probably indicating that they are not proper names).
     */
    public static Set<String> getLowercaseWords() {
	if (!gazetteersInitialized) initGazetteers();
        return lowercaseWords;
    }
    
    /** 
     * Gets the inflected words gazetteer.
     * The gazetteer is a set of all words in a dictionary,
     * including inflected forms (past forms, plural forms, etc.), in lowercase.
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of all words in a dictionary,
     * including inflected forms (past forms, plural forms, etc.).
     */
    public static Set<String> getInflectedWords() {
	if (!gazetteersInitialized) initGazetteers();
        return inflectedWords;
    }
    
    /** 
     * Gets the common words appearing more than five times gazetteer.
     * The gazetteer is a set of words appearing more than five times
     * in the ACE 2004 Corpus.
     * May include words from the test set. 
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of words appearing more than five times
     * in the ACE 2004 Corpus.
     * May include words from the test set. 
     */
    public static Set<String> getCommonWords5() {
	if (!gazetteersInitialized) initGazetteers();
        return commonWords5;
    }
    
    /** 
     * Gets the common words gazetteer.
     * The gazetteer is a set of words appearing frequently
     * in the ACE 2004 Corpus.
     * May include words from the test set. 
     * The list may include ambiguous items.
     * If the gazetteers have not been loaded, they will be loaded first.
     * @return A case-insensitive set of words appearing frequently
     * in the ACE 2004 Corpus.
     * May include words from the test set. 
     */
    public static Set<String> getCommonWords() {
	if (!gazetteersInitialized) initGazetteers();
        return commonWords;
    }
    
    public static Set<String> getVerbOfPerceptron(){
    	if (!gazetteersInitialized) initGazetteers();
    	return verbOfPerceptron;
    }
    
    public static Set<String> getChineseLastName(){
    	if (!gazetteersInitialized) initGazetteers();
    	return chineseLastName;
    }
    
    public static Set<String> getProfTitle(){
    	if (!gazetteersInitialized) initGazetteers();
    	return profTitle;
    }
    public static Set<String> getDay(){
    	if (!gazetteersInitialized) initGazetteers();
    	return days;
    }
    //Loaders and members:
    
    /**
     * Loads the gazetteers from files in the gazetteers directory located
     * in a directory on the classpath.
     */
    private static void initGazetteers() {
    	//System.out.println("InitGazetteers");
	String b 
	 = "gazetteers";
	initGazetteers_empty();
	chineseLastName = loadLinesAsSet(b+"/chineselastname.txt", true);
	days = loadLinesAsSet(b+"/days.txt", true);
	profTitle = loadLinesAsSet(b+"/prof-title.txt", false);
	gazetteersInitialized = true;
	profTitle = loadLinesAsSet(b+"/prof-title.txt", false);
	honors = loadLinesAsSet(b+"/honors.txt", true);
	verbOfPerceptron = loadLinesAsSet(b+"/verbOfPerceptron", true); 
	//if(0==0)
		//return;
	//System.err.println("Debug: Loading some gazes");
	commonWords = loadLinesAsSet(b+"/commonWords.txt", true);
	commonWords5 = loadLinesAsSet(b+"/commonWords5.txt", true);
	
	maleFirstNames = loadLinesAsSet(
		    b+"/dist.male.first_USCensus1990.lower.txt", true);
	femaleFirstNames = loadLinesAsSet(
		    b+"/dist.female.first_USCensus1990.lower.txt", true);
	lastNames = loadLinesAsSet(
		    b+"/dist.all.last_USCensus1990.lower.txt", true);
	orgClosings = loadLinesAsSet(b+"/orgClosings.txt", true);
	countries = loadLinesAsSet(b+"/countries.txt", true);
	countriesDemAdj = loadLinesAsSet(b+"/countriesWithAdjAndDem.txt", true);
	cities = loadLinesAsSet(b+"/dataenCities.txt", true);
	states = loadLinesAsSet(b+"/states.txt", true);
	polParties = loadLinesAsSet(b+"/polparties.txt", true);
	corporations = loadLinesAsSet(b+"/corporations.txt", true);
	sportTeams = loadLinesAsSet(b+"/teams.txt", true);
	universities = loadLinesAsSet(b+"/universities.txt", true);
	inflectedWords = loadLinesAsSet(b+"/2of12infNoSymbols.txt", true);
	lowercaseWords = loadLinesAsSet(b+"/lowercaseNouns.txt", true);
	singularNouns = loadLinesAsSet(b+"/singularNouns.txt", true);
	pluralNouns = loadLinesAsSet(b+"/pluralNouns.txt", true);
	sayWords = loadLinesAsSet(b+"/sayWords.txt", true);
	pronouns = loadLinesAsSet(b+"/pronouns.txt", true);
	prepositions = loadLinesAsSet(b+"/prepositions.txt", true);
	stopWords = loadLinesAsSet(b+"/stopWords.txt", true);
	days = loadLinesAsSet(b+"/days.txt", true);
	verbOfPerceptron = loadLinesAsSet(b+"/verbOfPerceptron", true);
	maleFirstNamesCS = loadLinesAsSet(
		    b+"/dist.male.first_USCensus1990.lower.txt", false);
	femaleFirstNamesCS = loadLinesAsSet(
		    b+"/dist.female.first_USCensus1990.lower.txt", false);
	lastNamesCS = loadLinesAsSet(
		    b+"/dist.all.last_USCensus1990.lower.txt", false);
	countriesCS = loadLinesAsSet(b+"/countries.txt", false);
	countriesDemAdjCS = loadLinesAsSet(b+"/countriesWithAdjAndDem.txt",
	 false);
	citiesCS = loadLinesAsSet(b+"/dataenCities.txt", false);
	profTitle = loadLinesAsSet(b+"/prof-title.txt", false);
	statesCS = loadLinesAsSet(b+"/states.txt", false);
	polPartiesCS = loadLinesAsSet(b+"/polparties.txt", false);
	corporationsCS = loadLinesAsSet(b+"/corporations.txt", false);
	sportTeamsCS = loadLinesAsSet(b+"/teams.txt", false);
	universitiesCS = loadLinesAsSet(b+"/universities.txt", false);
	chineseLastName = loadLinesAsSet(b+"/chineselastname.txt", true);
	gazetteersInitialized = true;
		//System.out.println("Finish InitGazetteers");
    }
    //TODO: Extract:
    protected static Set<String> loadLinesAsSet(String filename, boolean lower)
    {
	Set<String> result = new HashSet<String>();
	List<String> lines = (new myIO()).readLines(filename);
	for (String line : lines) {
	    if (line.length() <= 0 || line.startsWith("#"))
		continue;
	    if (lower)
		line = line.toLowerCase();
	    result.add(line);
	}
	return result;
    }
    
    private static void initGazetteers_empty() {
    	String b  = "gazetteers";
    	//System.err.println("Debug: Loading some gazes");
    	commonWords =new HashSet<String>();
    	commonWords5 = new HashSet<String>();
    	honors = loadLinesAsSet(b+"/honors.txt", true);
    	maleFirstNames = new HashSet<String>();  	//loadLinesAsSet(b+"/myMaleFirstNames.txt",true);
	    //b+"/dist.male.first_USCensus1990.lower.txt", true);
    	femaleFirstNames = new HashSet<String>();//loadLinesAsSet(b+"/myFemaleFirstNames.txt",true);
    	maleFirstNames = new HashSet<String>();
    		    //b+"/dist.male.first_USCensus1990.lower.txt", true);
    	femaleFirstNames = new HashSet<String>();
    	//	    b+"/dist.female.first_USCensus1990.lower.txt", true);
    	lastNames = new HashSet<String>();
    	orgClosings = new HashSet<String>();
    	countries = new HashSet<String>();
    	countriesDemAdj = new HashSet<String>();
    	cities = new HashSet<String>();
    	states = new HashSet<String>();
    	polParties = new HashSet<String>();
    	corporations = new HashSet<String>();
    	sportTeams = new HashSet<String>();
    	universities = new HashSet<String>();
    	inflectedWords = new HashSet<String>();
    	lowercaseWords =new HashSet<String>();
    	singularNouns =new HashSet<String>();
    	pluralNouns = new HashSet<String>();
    	sayWords = new HashSet<String>();
    	pronouns = loadLinesAsSet(b+"/pronouns.txt", true);
    	prepositions = loadLinesAsSet(b+"/prepositions.txt", true);
    	stopWords = new HashSet<String>();


    	/*maleFirstNamesCS = loadLinesAsSet(
    		    b+"/dist.male.first_USCensus1990.lower.txt", false);
    	femaleFirstNamesCS = loadLinesAsSet(
    		    b+"/dist.female.first_USCensus1990.lower.txt", false);
    	lastNamesCS = loadLinesAsSet(
    		    b+"/dist.all.last_USCensus1990.lower.txt", false);*/
    	maleFirstNamesCS = new HashSet<String>();
    	femaleFirstNamesCS = new HashSet<String>();
    	lastNamesCS = new HashSet<String>();
    	countriesCS = new HashSet<String>();
    	countriesDemAdjCS = new HashSet<String>();
    	citiesCS = new HashSet<String>();
    	statesCS = new HashSet<String>();
    	polPartiesCS = new HashSet<String>();
    	corporationsCS = new HashSet<String>();
    	sportTeamsCS = new HashSet<String>();
    	universitiesCS = new HashSet<String>();

    	gazetteersInitialized = true;
        }
    //Stored statically here for speedup:
    protected static boolean gazetteersInitialized = false;
    
    protected static Set<String> honors = null;
    protected static Set<String> maleFirstNames = null;
    protected static Set<String> femaleFirstNames = null;
    protected static Set<String> lastNames = null;
    protected static Set<String> orgClosings = null;
    protected static Set<String> countriesDemAdj = null;
    protected static Set<String> countries = null;
    protected static Set<String> cities = null;
    protected static Set<String> states = null;
    protected static Set<String> polParties = null;
    protected static Set<String> corporations = null;
    protected static Set<String> sportTeams = null;
    protected static Set<String> universities = null;
    protected static Set<String> inflectedWords = null;
    protected static Set<String> lowercaseWords = null;
    protected static Set<String> singularNouns = null;
    protected static Set<String> pluralNouns = null;
    protected static Set<String> sayWords = null;
    protected static Set<String> pronouns = null;
    protected static Set<String> prepositions = null;
    protected static Set<String> stopWords = null;
    protected static Set<String> verbOfPerceptron = null;
    protected static Set<String> commonWords = null;
    protected static Set<String> commonWords5 = null;
    protected static Set<String> chineseLastName = null;
    protected static Set<String> profTitle = null;
    protected static Set<String> days = null;

    //case-sensitive:
    protected static Set<String> maleFirstNamesCS = null;
    protected static Set<String> femaleFirstNamesCS = null;
    protected static Set<String> lastNamesCS = null;
    protected static Set<String> countriesDemAdjCS = null;
    protected static Set<String> countriesCS = null;
    protected static Set<String> citiesCS = null;
    protected static Set<String> statesCS = null;
    protected static Set<String> polPartiesCS = null;
    protected static Set<String> corporationsCS = null;
    protected static Set<String> sportTeamsCS = null;
    protected static Set<String> universitiesCS = null;
}
