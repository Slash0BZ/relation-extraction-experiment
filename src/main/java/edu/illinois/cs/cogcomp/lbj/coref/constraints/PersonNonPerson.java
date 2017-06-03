package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.features.WordNetTools;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/**
 * given two mentions, checks whether one refers to a person and the
 *   other does not.
 *  
 * If conditions are met, indicates mentions CANNOT co-refer.  
 * 
 * @author mssammon, arozovska
 *
 */


public class PersonNonPerson extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "PersonNonPerson";
	
	private final boolean m_DEBUG = false;
	
	public PersonNonPerson() throws Exception
	{
		super();
	}
	
	/**
	 * find all mentions for which this constraint has an effect, given the index of 
	 *   a specific mention in the document
	 */
	
	public Map< Mention, Double > findAppropriate(Doc d, int mIndex, boolean useGoldStandard ) 
	{  
		Double score = -1.0; // indicates forced non-coref with high belief
		HashMap< Mention, Double > unCorefs = new HashMap< Mention, Double >();
		
		List< Mention > allMentions = getAllMentions( d, useGoldStandard );
		
		Mention m = getMention( d, mIndex, useGoldStandard );
		
    	String text = m.getExtent().getCleanText().toLowerCase();

    	if ( m_DEBUG )
    		System.err.println( "PNP.findAppropriate(): text is '" + text + "'" );
    	
    	if (Arrays.binarySearch( ambiguousPronouns, text) < 0 )  // if mention 'm' is NOT an ambiguous pronoun
    	{
        	if ( m_DEBUG )
        		System.err.println( "## PNP.findAppropriate: mention is NOT ambiguous..." );
    		
    		boolean isFirstMentPerson = isPerson( m );
    		boolean isFirstMentNonPerson = isNonPerson( m );

    		if ( isFirstMentPerson && isFirstMentNonPerson ) 
    		{
    			if ( m_DEBUG )
        			System.err.println( "## mention 1: '" + text +
        								"' has evidence for both person- and non-person-hood.");
    		}
    		else 
    		{
    			for ( Mention laterMent : allMentions ) 
    			{
    				if ( laterMent.getExtentFirstWordNum() > m.getExtentFirstWordNum() )
    				{
    					String laterMentText = laterMent.getExtent().getCleanText().toLowerCase();
    		        	if ( m_DEBUG )
    		        		System.err.println( "## PNP.findAppropriate(): ment 1 is '" +
    		        				text + "; ment 2 is '" + laterMentText + "'" );

    		    		boolean isSecondMentPerson = isPerson( laterMent );
    		    		boolean isSecondMentNonPerson = isNonPerson( laterMent );

    		    		if ( isSecondMentPerson && isSecondMentNonPerson ) 
    		    		{
    		            	if ( m_DEBUG )
    		            		System.err.println( "## mention 2: '" + laterMentText +
    		            							"' has evidence for both person- and non-person-hood.");
    		    		}
    		    		else	
    		    		{
    					
    		    			if ( isPerson( laterMent ) && isFirstMentNonPerson )
    		    			{
    		    	        	if ( m_DEBUG )
    		    	        		System.out.println( "## PNP.findAppropriate(): "+
    									"first ment is nonPerson, laterMent is person. No coref." );
    					    				
    		    				unCorefs.put( laterMent, score );
    		    			}
    		    			if ( isNonPerson( laterMent ) && isFirstMentPerson )
    		    			{
    		    	        	if ( m_DEBUG )
    		    	        		System.out.println( "## PNP.findAppropriate(): "+
    		    						"first ment is person, laterMent is nonPerson. No coref." );

    		    	        	unCorefs.put( laterMent, score );
    		    			}
    		    		}
    				}
    			}
    		}
    	}
    	
		return unCorefs;
	}
	  

	/**
	 * returns 'true' if Mention has properties indicating a person, 'false' otherwise
	 * @param m_
	 * @return
	 */
		  
	private boolean isPerson( Mention m_ )
	{
		boolean isPerson = false;
			
		String text = m_.getExtent().getCleanText().toLowerCase();
		String headText = m_.getHead().getCleanText().toLowerCase();
		    
    	if ( m_DEBUG )
    		System.err.println( "## isPerson(): text is '" + text + 
				"'; headText is '" + headText + "'" );

		if (Arrays.binarySearch( allPeoplePronouns, text) > 0) 
		{
        	if ( m_DEBUG )
        		System.err.println( "## isPerson(): found person pronoun." );
			isPerson = true; // pronoun indicates person
		}
		else if ( m_.getEntityType().equalsIgnoreCase( "PER" ) )
		{
        	if ( m_DEBUG )
        		System.err.println( "## isPerson(): found PER entity type." );
			isPerson = true;
		}
		else if ( m_wnTools.areHypernyms( headText, "person" ) )
		{
        	if ( m_DEBUG )
        		System.err.println( "## isPerson(): found person hypernym." );
			isPerson = true;
		}
		return isPerson;
	}
	
	/**
	 * returns 'true' if Mention has properties indicating a non-person, 'false' otherwise
	 * @param m_
	 * @return
	 */

	private boolean isNonPerson( Mention m_ )
	{
		boolean isNonPerson = false;
			
		String text = m_.getExtent().getCleanText().toLowerCase();
		String headText = m_.getHead().getCleanText().toLowerCase();
		    
    	if ( m_DEBUG )
    		System.out.println( "## isNonPerson(): text is '" + text + 
    				"'; headText is '" + headText + "'" );

		if (Arrays.binarySearch( nonPeoplePronouns, text) > 0) 
		{
        	if ( m_DEBUG )
        		System.out.println( "## isNonPerson(): found non-person pronoun." );
			isNonPerson = true; // pronoun indicates person
		}
		    
		else if ( m_.getEntityType().equalsIgnoreCase( "LOC" ) ||
					m_.getEntityType().equalsIgnoreCase( "ORG") 
				)
		{
        	if ( m_DEBUG )
        		System.out.println( "## isNonPerson(): found org or loc entity type." );
        	isNonPerson = true;
		}		    
		else if ( m_wnTools.areHypernyms( headText, "organization" ) ||
					m_wnTools.areHypernyms( headText, "location" )
				)
		{
        	if ( m_DEBUG )
        		System.out.println( "## isNonPerson(): found org or loc hypernym." );
			isNonPerson = true;
		}
		return isNonPerson;
	}

	public String getName() 
	{
		return m_NAME;
	}

	public double checkConstraint(Mention firstMent_, Mention laterMent_,
			boolean useGoldStandard) 
	{
		double score = 0.0;
	
    	String text = firstMent_.getExtent().getCleanText().toLowerCase();
    	if ( m_DEBUG )
    		System.err.println( "PNP.findAppropriate(): text is '" + text + "'" );
    	
    	if (Arrays.binarySearch( ambiguousPronouns, text) < 0 )  // if mention 'm' is NOT an ambiguous pronoun
    	{
        	if ( m_DEBUG )
        		System.err.println( "## PNP.findAppropriate: mention is NOT ambiguous..." );
    		
    		boolean isFirstMentPerson = isPerson( firstMent_ );
    		boolean isFirstMentNonPerson = isNonPerson(firstMent_ );

			String laterMentText = laterMent_.getExtent().getCleanText().toLowerCase();
        	if ( m_DEBUG )
        		System.err.println( "## PNP.findAppropriate(): ment 1 is '" +
					text + "; ment 2 is '" + laterMentText + "'" );

    		boolean isSecondMentPerson = isPerson( laterMent_ );
    		boolean isSecondMentNonPerson = isNonPerson( laterMent_ );

    		
    		if ( isSecondMentPerson && isSecondMentNonPerson ) 
    		{
            	if ( m_DEBUG )
            		System.err.println( "## mention 2: '" + laterMentText +
            			"' has evidence for both person- and non-person-hood.");
    		}

    		else {
			
    			if ( isSecondMentPerson && isFirstMentNonPerson )
    			{
    	        	if ( m_DEBUG )
    	        		System.err.println( "## PNP.findAppropriate(): "+
							"first ment is nonPerson, laterMent is person. No coref." );

    				score = -1.0;
    			}
    			if ( isSecondMentNonPerson && isFirstMentPerson )
    			{
    	        	if ( m_DEBUG )
    	        		System.err.println( "## PNP.findAppropriate(): "+
    						"first ment is person, laterMent is nonPerson. No coref." );
    				score = -1.0;
    			}
    		}
    	}
    	
    	return score;	
	}

	@Override
	protected boolean checkMention(Mention m_) 
	{
		return true;
	}

	@Override
	protected void configure(Properties p_) 
	{
		;
	}


}
