package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.entityComparison.core.EntityComparison;
import edu.illinois.cs.cogcomp.lbj.coref.features.WordNetTools;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocCoNLL;

/**
 * ConstraintBase subclasses must identify pairs of Mentions in a coref Document 
 *   that are constrained to be either coreferent or non-coreferent, and associate
 *   a positive or negative score respectively.  The magnitude of the score indicates
 *   confidence in prediction, and may be used to select between competing constraints.
 *
 * Expects a config file -- default "constraints.config" -- on the classpath, which
 *   will be accessible to all constraints derived from ConstraintBase
 * @author mssammon
 *
 */

public abstract class ConstraintBase implements Constraint
{
	protected boolean m_DEBUG;
	protected String path = "/shared/experiments/hpeng7/src/Coref-Resources/";
	protected String m_configFile = path + "config/constraints.config";
	
	private EntityComparison m_neSim;
	protected final static String m_IGNORE_POSSESSIVE_KEY = "ignorePossessive";
	protected final static String m_USE_NE_SIM_KEY = "useNeSim";
	protected final static String m_USE_EXT_PROCESSING_KEY = "useExternalProcessing";
	protected final static String m_DEBUG_KEY = "debug";
	protected final static String m_INCLUDE_BAD_MENTIONS = "includeBadMentions";
	protected final static String m_NE_SIM_FILE_KEY = "neSimFile";
	protected final static String m_NE_FIRST_NAME_KEY = "useFirstNameHeuristic";
	protected boolean m_ignorePossessiveMarkers = false;
	protected boolean m_useNeSim = false;
	protected boolean m_useExternalProcessing = false;
	protected static final double m_SIM_THRESHOLD = 0.85;
	
	protected static WordNetTools m_wnTools;
	private HashSet< Mention > m_goldMentionsMatchingPredicted;
	private HashMap< String, HashMap< String, Double > > m_neSimScores;
	String m_neSimFile;

	private boolean m_useFirstNameHeuristic = false;
    private boolean m_useNeSimCache = false;
	public static String[] pronouns =
		{ "he", "her", "him", "his", "it", "its", "she", "their", "them", "they",
		"who", "whose", "us", "our", "my", "you", "your" };
	public static String[] peoplePronouns =
		{ "he", "she", "him", "her", "his", "who", "whose", "I", "my", "me" };

	public static String[] limitedPeoplePronouns =
	{ "he", "she", "him", "her", "his" };

	
	public static String[] nonPeoplePronouns = { "it", "its" };
	public static String[] pluralPronouns = { "they", "them", "their" };
	public static String[] singularPronouns =
		{ "he", "her", "him", "his", "it", "its", "she", "who" };

	public static String[] ambiguousPronouns = 
		{ "they", "them", "their", "we", "us", "our", "you", "your" };
	
	public static String[] allPeoplePronouns =
	    { "he", "she", "him", "her", "his", "who", "whose", "I", "my", "me", "you", "your" };

	public static final String[] possessivePro = {"my","our","your","his","her","their","its"};
	public static final String[] personalProSubj = {"I","we","you","he","she","they","it"};
	public static final String[] personalProObj = {"me","us","you","him","her","them","it"};

	public static final String[] modifierPos = { "JJ", "JJR", "JJS", "VBN", "VBG", "RB", "RBR", "RBS","CD" };
	public static final String[] definiteDet = { "the", "that", "this", "these", "those","our","my","her","his","its","their","your"};
	public static final String[] weakDefiniteDet = { "that", "this"  };

	static {
		Arrays.sort(pronouns);
		Arrays.sort(peoplePronouns);
		Arrays.sort(limitedPeoplePronouns);
		Arrays.sort(nonPeoplePronouns);
		Arrays.sort(pluralPronouns);
		Arrays.sort(singularPronouns);
		Arrays.sort(ambiguousPronouns);
		Arrays.sort(allPeoplePronouns);
		Arrays.sort(personalProObj);
		Arrays.sort(possessivePro);
		Arrays.sort(personalProSubj);
		Arrays.sort(modifierPos);
		Arrays.sort(definiteDet);
		Arrays.sort(weakDefiniteDet);
	}


	
	
	
	public ConstraintBase() throws Exception 
	{		
		Properties p = new Properties();
		try {
			p.load(new FileInputStream( m_configFile));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		init( p );
		configure( p );
	}

	
	public ConstraintBase( String configFileName_ ) throws Exception
	{
		Properties p = new Properties();
		try {
			p.load(new FileInputStream( configFileName_ ));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		init( p );		
		configure( p );
	}

	protected void finalize() throws Throwable
	{
		writeFile( m_neSimFile );
	}
	
	private void writeFile( String file ) throws Exception
	{
		BufferedWriter out = null;
		
	    try {
	    	out = 
	    		new BufferedWriter(
	    				new FileWriter(file));
		      // Other code that might throw exceptions
	    } catch(FileNotFoundException e) {
	    	System.err.println(
	    			"Could not open " + file);
		      // Was	n't open, so don't close it
	    	throw e;
	    } catch(Exception e) {
	    	// All other exceptions must close it
	    	try {
	    		out.close();
	    	} catch(IOException e2) {
	    		System.err.println(
	    		"out.close() unsuccessful");
	    	}
	    	throw e;
	    } finally {
	    	// Don't close it here!!!
	    }
		
	    for ( String first : m_neSimScores.keySet() )
	    {
	    	Map< String, Double > scores = m_neSimScores.get( first );
	    	
	    	for ( String second : scores.keySet() )
	    	{
	    		StringBuilder bldr = new StringBuilder();
	    		bldr.append( first );
	    		bldr.append( "\t" );
	    		bldr.append( second );
	    		bldr.append( "\t" );
	    		bldr.append( scores.get( second ).toString() );
	    		bldr.append( "\n" );
		    	out.append( bldr.toString() );
	    	}
	    }
    	try {
    		out.close();
    	} catch(IOException e2) {
    		System.err.println(
    		"out.close() unsuccessful");
    	}
	}


	protected void init( Properties p_ ) throws Exception
	{
		m_goldMentionsMatchingPredicted = new HashSet< Mention >();	
		m_neSimScores = new HashMap< String, HashMap< String, Double > >();
		
		m_ignorePossessiveMarkers = Boolean.parseBoolean( p_.getProperty( m_IGNORE_POSSESSIVE_KEY ) );
		m_useNeSim = Boolean.parseBoolean( p_.getProperty( m_USE_NE_SIM_KEY ) );
		m_useExternalProcessing = Boolean.parseBoolean( p_.getProperty( m_USE_EXT_PROCESSING_KEY ) );
		m_DEBUG = Boolean.parseBoolean( p_.getProperty( m_DEBUG_KEY ) );
		m_useFirstNameHeuristic = Boolean.parseBoolean( p_.getProperty( m_NE_FIRST_NAME_KEY ) );
		if ( null == m_wnTools )
			m_wnTools = new WordNetTools();
		
		//System.err.println( "## ConstraintBase.init(): debug set to: " + ( m_DEBUG ? "TRUE" : "FALSE" ) );
		
		if ( m_useNeSim ) {
			m_neSim = new EntityComparison();
			m_neSimFile = p_.getProperty( m_NE_SIM_FILE_KEY );
//			loadNeSimMap( m_neSimFile );			
		}
			
	}
	

	private void loadNeSimMap( String file ) throws Exception
	{
		//MsFileReader reader = null;
		InputStream ins = null;
		try {
			ins = this.getClass().getResourceAsStream(file);				
		}
		catch( Exception e )
		{
			throw e;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		boolean isMore = true;
		
		while ( isMore )
		{
			String line = reader.readLine();
			
			if ( null == line ) 
				isMore = false;
			
			else 
			{				
				String[] items = line.split( "\t" );
			
				if ( items.length != 3 ) 
					continue;
				
				HashMap< String, Double > scores = m_neSimScores.get( items[ 0 ] );
				
				if ( null == scores ) 
				{
					scores = new HashMap< String, Double >();
					m_neSimScores.put( items[ 0 ], scores );
				}

				scores.put( items[ 1 ], new Double( items[ 2 ] ) );

				if ( m_DEBUG )
					System.err.println( "## ConstraintBase.loadNeSimMap(): added entry: " +
							items[ 0 ] + ", " + items[ 1 ] + ", " + items [ 2 ] );
			}	
		}
		reader.close();
	}


	/** 
	 * read Constraint-specific properties from the all-singing, all-dancing constraint
	 *   config file
	 * @throws Exception 
	 */
	abstract protected void configure( Properties p_ ) throws Exception;
	
	/**
	 *  given the word index corresponding to a specific mention, find all other mentions
	 *    that are constrained relative to this mention
	 * 	 
	 *  return a Map from affected mentions to scores representing the strength of the constraint
	 *    for the pair formed by the mapped mention and the input mention
	 */
	
	public abstract Map<Mention, Double> findAppropriate(Doc d_, int index_, boolean useGoldStandard_ ) throws Exception;

	
	public Map< Mention, Map< Mention, Double > > findAllPairs( Doc d_, boolean useGoldStandard_ ) throws Exception 
	{
		HashMap< Mention, Map< Mention, Double > > result = 
			new HashMap< Mention, Map< Mention, Double > >();

	    List< Mention > mentions = getMentions( d_, useGoldStandard_ );


	    int maxIndex = -1;
	    int firstIndex = 0;

	    try {
	    
	    	for (int i = 0; i < mentions.size(); i++) 
	    	{
	    		maxIndex = i;
	    		Mention m = getMention(d_, i, useGoldStandard_);
	    		if ( checkMention( m ) )
	    		{
	    			Map< Mention, Double > unCorefMap = findAppropriate( d_, i, useGoldStandard_ );
	    		
	    			if ( !unCorefMap.isEmpty() ) 
	    				result.put( m, unCorefMap );

	    		}
	    	}
	    }
	    catch ( Exception e ) 
	    {
	    	System.err.println( "ERROR: ConstraintBase.findAllPairs(): start index was " +
	    			firstIndex + ", lastIndex was " + maxIndex );
	    	throw e;
	    }
	    
	    return result;
	}

	
	/**
	 *  loops over mentions in a document
	 *  for each, checks it is a non-pronoun
	 *  then calls "findAppropriate()" to get set of pronoun mentions that 
	 *     satisfy some constraint
	 *     
	 *  returns a pair of lists of mentions: first is forced coref mention pairs, 
	 *     the second is forced non-coref mention pairs.
	 * @throws Exception 
	 */
	
	public List< List< Mention[] > > constrain( Doc d_, boolean useGoldStandard_ ) throws Exception 
	{
		List<List<Mention[]>> result = new ArrayList<List<Mention[]>>();
	    List<Mention[]> forceCoref = new LinkedList<Mention[]>();
	    List<Mention[]> forceUnCoref = new LinkedList<Mention[]>();
	    List< Mention[] > badMentForceCoref = new LinkedList< Mention[] >();
	    List< Mention[] > badMentForceUnCoref = new LinkedList< Mention[] >();
	    
	    result.add(forceCoref);
	    result.add(forceUnCoref);
	    result.add( badMentForceCoref );
	    result.add( badMentForceUnCoref );

	    List< Mention > mentions = getMentions( d_, useGoldStandard_ );
	    
	    if ( m_DEBUG )
	    	System.err.println( "## ConstraintBase.constrain(): useGoldStandard is '" +
	    			( useGoldStandard_ ? "true" : "false" ) + "'; found " +
	    			mentions.size() + " mentions..." );
	    
	    int maxIndex = -1;
	    int firstIndex = 0;

	    for (int i = 0; i < mentions.size(); i++) 
	    {
    		maxIndex = i;
    		
	    	Mention m = mentions.get( i ); 
	 
	    	boolean isFirstMentGood = copyGoldMentionInfo( m, useGoldStandard_ );

	    	if ( m_DEBUG )
	    		System.err.println( "## ConstraintBase.constrain(): first mention is: "
	    			+ m.getExtent().getCleanText().toLowerCase() );

	    	
	    	if ( !( checkMention( m ) && isFirstMentGood  ) )
	    	{
	    		if ( m_DEBUG )
	    			System.err.println( "## ConstraintBase.constrain(): mention is no good. " + 
	    					"proceeding to next mention." );
	    	
	    		continue;
	    	}
	    	
	    	Map< Mention, Double > constraintMap = null;
	    	
	    	try {
	    		constraintMap = findAppropriate( d_, i, useGoldStandard_ );
	    	}
		    catch ( Exception e ) 
		    {
		    	System.err.println( "ERROR: ConstraintBase.constrain(): start index was " +
		    			firstIndex + ", lastIndex was " + maxIndex );
		    	throw e;
		    }

		    if ( null == constraintMap )
		    	continue;
		    
	    	if ( !constraintMap.isEmpty() ) 
	    		for ( Mention cM: constraintMap.keySet() ) 
	    		{
	    			boolean isSecondMentGood = copyGoldMentionInfo( cM, useGoldStandard_ );
	    			Double score = constraintMap.get( cM );
	    			
	    			if ( isFirstMentGood && isSecondMentGood ) 
	    			{
	    				if ( score > 0 )
	    					forceCoref.add( new Mention[]{ m, cM } );
	    				else if ( score < 0 )		    			  
	    					forceUnCoref.add(new Mention[]{ m, cM });
	    				else 
	    					System.err.println( "WARNING: ConstraintBase.constrain(): " +
	    						"constraint returned zero score. " );
	    			}
	    			else
	    			{
	    				if ( score > 0 )
	    					badMentForceCoref.add( new Mention[]{ m, cM } );
	    				else if ( score < 0 )		    			  
	    					badMentForceUnCoref.add(new Mention[]{ m, cM });
	    				else 
	    					System.err.println( "WARNING: ConstraintBase.constrain(): " +
	    						"constraint returned zero score. " );
	    			}
	    		}
	    }
	    
	    return result;
	}
	
	/**
	 * a filter that decides whether a mention could be a candidate for this constraint
	 * 
	 * @param m_
	 * @return 'true' if this mention is potentially affected by this constraint
	 * @throws Exception 
	 */
	
	abstract protected boolean checkMention(Mention m_ ) throws Exception; 


	/** 
	 * given a (predicted) mention from the document under consideration, 
	 *   consults this object's collection of gold-standard mentions
	 *   to see if there exists a corresponding gold standard mention
	 * if a corresponding gold standard mention exists, copy the relevant information
	 *   to the argument mention and return true
	 * otherwise, returns 'false'
	 * 
	 * @param m a 'predicted' mention under consideration
	 * @param useGoldStandard_ if this flag is set to 'true', the input mention will be
	 *     predicted, and so this operation is ignored
	 * @return 'true' if a corresponding gold mention was found for the input mention
	 */
	private boolean copyGoldMentionInfo(Mention m, boolean useGoldStandard_) 
	{
		boolean isGold = useGoldStandard_;

//		if ( !useGoldStandard_ )
			for ( Mention mGold : m_goldMentionsMatchingPredicted )
			{
				if ( mGold.equals( m ) )
				{
					m.setTrueEntityID( mGold.getTrueEntityID() );
					isGold = true;
				}
			}
	
		return isGold;
	}


	/**
	 * returns mentions, and if using predicted, stores predicted mentions 
	 *    corresponding to gold standard mentions in the specified document.  
	 * 
	 * @param d_
	 * @param useGoldStandard_
	 * @return
	 */

	protected List< Mention > getMentions( Doc d_, boolean useGoldStandard_ )
	{
		List< Mention > mentions = null; 
		List< Mention > goldMentions = d_.getTrueMentions();
		
//    	if ( !useGoldStandard_ )
//    	{
    		List< Mention > predMentions = d_.getPredMentions(); 

    		HashSet< Mention > predMentionSet = new HashSet< Mention >();
    		predMentionSet.addAll( predMentions );
    		
    		for ( Mention m : goldMentions )
    			if ( predMentionSet.contains( m ) )
    				m_goldMentionsMatchingPredicted.add( m );
       		
    		mentions = predMentions;
//    	}
//    	else
//    		mentions = goldMentions;
    	
    	return mentions;
	}
	


	protected Mention getMention( Doc d_, int index_, boolean useGoldStandard_ )
	{
		Mention m = null;
		
//		if ( useGoldStandard_ )
//			m = d_.getTrueMention( index_ );
//		else
			m = d_.getPredMention( index_ );
		
		return m;
	}
	
	protected List< Mention > getAllMentions( Doc d_, boolean useGoldStandard_ )
	{
		List< Mention > allMentions = null;
		
//		if ( useGoldStandard_ ) 
//			allMentions = d_.getTrueMentions();
//		else
			allMentions = d_.getPredMentions();
		
		return allMentions;
	}
	
	
	protected int getNumberOfMentions( Doc d_, boolean useGoldStandard_ )
	{
		int numMentions = 0;

//		if ( useGoldStandard_)
//			numMentions = d_.getTrueMentions().size();
//		else
			numMentions = d_.getPredMentions().size();
		
		return numMentions;
	}
	

	static public String getMentionText( Mention m_ )
	{
		return m_.getExtent().getCleanText();
	}

	private String removeDecorate(String s){
		String[] removeEnd = {"Corp." , "Corporation", "& Co", "corp.","corporation",
				"company","Inc.","inc.","inc","Inc","corp", "Corp","co","Co.","co."
				,"government","Government","plc.","PLC.","PLC","plc", "International",
				"international","gov."};
		String[] removeHead = {"The" , "the"};
		for(String w : removeHead){
			s = s.replaceAll("^"+w+" ", "");
		}
		for(String w : removeEnd){
			s = s.replaceAll(" "+w+"$", "");
		}
		return s;
	}
	
	/**
	 * 
	 * @param firstMent_
	 * @param firstName_
	 * @param secondMent_
	 * @param secondName_
	 * @return
	 * @throws Exception
	 */

	
	protected double computeEntityScore( Mention firstMent_, 
									     Mention secondMent_
									   ) 
	throws Exception 
	{
		double score = 0;

		String firstName = getMentionNameString( firstMent_ ).replace("", "");
		String secondName = getMentionNameString( secondMent_ );
		firstName = cleanMentionName (removeDecorate(firstName));
		secondName = cleanMentionName (removeDecorate(secondName));
		
		if ( m_DEBUG )
			System.err.println( "## ConstraintBase.computeEntityScore(): " + 
					"comparing names '" + firstName + "' and '" + secondName + "'..." );
		
		if ( firstName.equals( secondName ) )
			score = 1.0;
		
		else 
		{
			if ( m_useFirstNameHeuristic ) {
		
				if ( m_DEBUG )
					System.err.println( "## Using first name heuristic for NESim..." );
				String[] firstToks = firstName.split( " " );
				String[] secondToks = secondName.split( " " );
			
				if ( firstToks[ 0 ].equals( secondToks[ 0 ] ) )
				{
					score = 0.9;
					
					if ( m_DEBUG )
					{
						System.err.println( "ConstraintBase.computeEntityScore(): used first name heuristic (said 'YES') for names " +
								firstName + ", " + secondName + "." );
					}
				}
			}

			if ( m_useNeSim  )
			{
				String firstNeStr = buildNeSimString( getMentionEntityType( firstMent_ ), firstName );
				String secondNeStr = buildNeSimString( getMentionEntityType( secondMent_), secondName );
			
				double neScore = computeEntityStringScore( firstNeStr, secondNeStr );
				if ( m_DEBUG ) 
					System.err.println( "## ConstraintBase.computeEntityScore(): " + 
						"called NESim with strings '" + firstNeStr + "', '" + secondNeStr +
						"' and got score: " + score );
			
				if ( neScore > score )
					score = neScore;
			}
		}
		
		return score;
	}


	protected double computeEntityStringScore(String firstNeStr,
			String secondNeStr) 
	{
		Double score = null;
		HashMap< String, Double > scoreMap = null;
		
		if ( m_useNeSimCache ) 
		{
		    if ( m_neSimScores.containsKey( firstNeStr ) )
			scoreMap = m_neSimScores.get( firstNeStr );
		    else 
			{
			    scoreMap = new HashMap< String, Double > ();
			    m_neSimScores.put( firstNeStr, scoreMap );
			}
		
		    score = scoreMap.get( secondNeStr );
		}
		if ( null == score )
		{
			HashMap< String, String > comparison = m_neSim.compareNames( firstNeStr,  secondNeStr );

			score = Double.parseDouble( comparison.get( "SCORE" ) );

			if ( m_useNeSimCache )
			    scoreMap.put( secondNeStr, score );
		}
		
		return score.doubleValue();
	}


	/**
	 * given a mention and the string representing the name, build the string 
	 *   encoding the type/name[/offset] info required by NESim to compute similarity.
	 *   
	 * @param ment_
	 * @param name_
	 * @return
	 * @throws Exception
	 */
	
	protected String buildNeSimString( String type_, String name_ ) throws Exception 
	{
		String locName = name_;
		
		
		// this is a hack to avoid passing spurious NESim delimiters to NESim
		if ( name_.matches( "#" ) ) {
			locName = "zzzzzzzzzzzzzzzzz";
			String[] nameParts = name_.split( "#" );
			for ( int i = 0; i < nameParts.length; ++i )
			{
				if ( nameParts[i].length() > 0 )
				{
					locName = nameParts[i]; 
					break;
				}
			}
		}
		
		if ( ( null != type_ ) && !"NONE".equalsIgnoreCase( type_ ) )
			return ( type_ + "#" + locName );

		return locName;
	}

	
	/**
	 * get the text representation of the name; if specified by config, ignore
	 *    possessive markers (presently, "'s")
	 *    
	 * @param mention_
	 * @return
	 */
	
	protected String getMentionNameString(Mention mention_) 
	{
		return cleanMentionName( mention_.getHead().getCleanText() ); 

		
	}
	
	protected String cleanMentionName(String nameStr){
		if ( m_ignorePossessiveMarkers )
		{
			if ( nameStr.endsWith( "'s") )
				nameStr = nameStr.substring( 0, nameStr.lastIndexOf( "'s" ) );
			else if ( nameStr.endsWith( "'" ) && !(nameStr.startsWith( "'" ) ) )
				nameStr = nameStr.substring( 0, nameStr.lastIndexOf( "'" ) );
			else if ( nameStr.endsWith(" ") )
				nameStr = nameStr.substring( 0, nameStr.length()-1);
			nameStr = nameStr.replace("#", "_");
		
		}
		return nameStr;
	}



	protected String getMentionEntityType( Mention mention_ ) throws Exception 
	{
		// optionally, use CuratorManager to check for Named Entity overlapping this mention's
		// head -- verify through debugging output that token offsets for CM are valid
		
		String type = null;
		
		if ( m_useExternalProcessing )
		{
//			throw new Exception( "ERROR: IdenticalPropername.getMentionEntityType(): " + 
//					"external processing not yet enabled." );
			DocCoNLL doc = (DocCoNLL) mention_.getDoc();
			
			if ( null != doc ) 
			{
				int headIndex = mention_.getHeadFirstWordNum();
				Constituent nerMent = doc.getNameEntitySpan( headIndex );
				
				if ( null != nerMent ) {
					type = nerMent.getLabel();
					
					if ( "GPE".equals( type ) ) 
						type = "ORG";
					
					else if ( !( "PER".equalsIgnoreCase( type ) ||
								"LOC".equalsIgnoreCase( type ) ||
								"ORG".equalsIgnoreCase( type ) || 
								"MISC".equalsIgnoreCase( type ) 
								)
							)
						type = null;
						
				}
			
			}
		}
		else 
			type = mention_.getEntityType();
		
		return type;
	}
	
}
