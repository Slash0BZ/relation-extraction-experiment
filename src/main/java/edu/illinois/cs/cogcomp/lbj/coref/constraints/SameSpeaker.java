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
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocCoNLL;

/**
 * NOT YET IMPLEMENTED: needs getSpeaker() method to access relevant info in Doc
 * 
 * given two mentions, checks whether the coref system predicts the same speaker
 * if speakers are specified for both mentions, then if speaker is identical, 
 *    mentions should be co-referent; otherwise, they should NOT corefer.
 * 
 * @author mssammon, arozovska
 *
 */

public class SameSpeaker extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "SameSpeaker";
	private final static double m_SIM_THRESHOLD = 0.8;
	
	
	public SameSpeaker() throws Exception
	{
		super();
	}
	
	/**
	 * find all mentions for which this constraint has an effect, given the index of 
	 *   a specific mention in the document
	 */
	
	public Map< Mention, Double > findAppropriate(Doc d, int mIndex, boolean useGoldStandard ) 
	{  
		Double score = 1.0; // indicates forced coref with high belief
		HashMap< Mention, Double > corefs = new HashMap< Mention, Double >();
		
		List< Mention > allMentions = getAllMentions( d, useGoldStandard );
		
		Mention m = getMention( d, mIndex, useGoldStandard );
		
    	String text = ConstraintBase.getMentionText( m ).toLowerCase();
    	if ( m_DEBUG )
    		System.err.println( "SS.findAppropriate(): text is '" + text + "'" );

    	if ( checkMention( m ) )
    	{
    		for ( Mention laterMent : allMentions ) 
    		{
				if ( laterMent.getExtentFirstWordNum() <= m.getExtentFirstWordNum() )
					continue;

    			if ( checkMention( laterMent ) )
    			{
    				String laterMentText = ConstraintBase.getMentionText( laterMent ).toLowerCase();
    				if ( m_DEBUG )
    					System.err.println( "## IPN.findAppropriate(): ment 1 is '" +
    							text + "; ment 2 is '" + laterMentText + "'" );

    				if ( Math.abs( checkConstraint( m, laterMent, useGoldStandard ) ) > m_SIM_THRESHOLD )
    					corefs.put( laterMent, score );
    		
    			}
    		}
    	}
    	
		return corefs;
	}
	  


	public double checkConstraint(Mention firstMent_, Mention secondMent_,
			boolean useGoldStandard) 
	{
		double score = 0.0;
		
    	if ( checkMention( firstMent_ ) )
    	{
    		if ( checkMention( secondMent_ ) )
    		{
    			String firstSpeaker = getSpeaker( firstMent_ );
   				String secondSpeaker = getSpeaker( secondMent_ );

    			if ( ( firstSpeaker.length() > 0 ) && 
    					( secondSpeaker.length() > 0 ) )
    			{
    				if ( m_DEBUG )
    					System.err.println( "## SameSpeaker.checkConstraint(): first speaker is '" + 
    							firstSpeaker + "'; secondSpeaker is '" + secondSpeaker + "'." ); 
    				Boolean firstIsMe = false;
    				Boolean secondIsMe = false;
    				String firstMentionText = getMentionText( firstMent_ );
    				String secondMentionText = getMentionText( secondMent_ );
    				if ( firstMentionText.equalsIgnoreCase( "i" ) ||
    						firstMentionText.equalsIgnoreCase( "me" ) ||
    						firstMentionText.equalsIgnoreCase( "my" ) )
    					firstIsMe = true;
    				
    				if ( secondMentionText.equalsIgnoreCase( "I" ) ||
    						secondMentionText.equalsIgnoreCase( "me" ) ||
    						secondMentionText.equalsIgnoreCase( "my" ) )
    					secondIsMe = true;

    				if ( firstSpeaker.equalsIgnoreCase( secondSpeaker ) )
    				{
    					if ( firstIsMe.equals( secondIsMe ) )
    						score = 1.0;
    					else
    						score = -1.0;
    				}
    				else 
    					if ( firstIsMe.equals( secondIsMe ) )
    						score = -1.0;
    			
    			}
    		}
    	}
	
		return score;
	}

	

	private String getSpeaker( Mention firstMent_ ) 
	{
		return ( ( ( DocCoNLL ) firstMent_.getDoc() ).getWhoSaid( firstMent_.getHeadFirstWordNum() ) );
	}

	public String getName() 
	{
		return m_NAME;
	}



	

	/**
	 * returns 'true' if Mention has properties indicating a person, 'false' otherwise
	 * @param m_
	 * @return
	 */
		  
	@Override
	protected boolean checkMention(Mention m_) 
	{
		String spkr = getSpeaker( m_ );
		
		if ( m_DEBUG ) 
		{
			if ( null == spkr ) {
				System.err.println( "## SameSpeaker.checkMention(): null speaker returned." );
			}
			else if ( "".equals( spkr ) )
				System.err.println( "## SameSpeaker.checkMention(): empty speaker returned." );
			else 
				System.err.println( "## SameSpeaker.checkMention(): found speaker '" + spkr + "'." );
		}
		String mentionText = getMentionText( m_ );
		
		if ( ( null != spkr ) && !( "".equals( spkr ) )
				&& ( mentionText.equalsIgnoreCase( "i" ) ||
						mentionText.equalsIgnoreCase( "me" ) ||
						mentionText.equalsIgnoreCase( "my" ) ||
						mentionText.equalsIgnoreCase( "you" ) ||
						mentionText.equalsIgnoreCase( "your" ) ) )
			return true;
		
		return false;
	}

	@Override
	protected void configure(Properties p_) 
	{
		;
	}


}
