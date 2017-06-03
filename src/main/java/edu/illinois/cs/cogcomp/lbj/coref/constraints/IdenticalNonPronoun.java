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
 * given two mentions, check that they are non-pronouns 
 *   and that if so, they are identical. 
 *   
 * If conditions are met, indicates mentions co-refer.  
 *   
 * @author mssammon, arozovska
 *
 */

public class IdenticalNonPronoun extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "IdenticalNonPronoun";
	private final static double m_SIM_THRESHOLD = 0.8;
	private final boolean m_DEBUG = false;
	
	public IdenticalNonPronoun() throws Exception
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
    		System.err.println( "INP.findAppropriate(): text is '" + text + "'" );
    	
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

    				if ( computeSimilarityScore( text, laterMentText ) > m_SIM_THRESHOLD )
    					corefs.put( laterMent, score );
    		
    			}
    		}
    	}
    	
		return corefs;
	}
	  


	/**
	 * returns 'true' if Mention has properties indicating a person, 'false' otherwise
	 * @param m_
	 * @return
	 */
		  
	public String getName() 
	{
		return m_NAME;
	}

	
	public double checkConstraint(Mention firstMent_, Mention secondMent_,
			boolean useGoldStandard) 
	{
		double score = 0.0;
		
    	if ( checkMention( firstMent_ ) )
    		if ( checkMention( secondMent_ ) )
    			score = computeSimilarityScore( ConstraintBase.getMentionText( firstMent_ ).toLowerCase(), 
    											ConstraintBase.getMentionText( secondMent_ ).toLowerCase() );
 	
		return score;
	}

	
	
	private double computeSimilarityScore( String text_, String text2_ ) 
	{
		double score = 0.0;

		if ( text_.equalsIgnoreCase( text2_ ) )
			score = 1.0;
		
		return score;
	}

	@Override
	protected boolean checkMention(Mention m_) 
	{
		if ( !"PRO".equalsIgnoreCase( m_.getType() ) )
		{
			String text = ConstraintBase.getMentionText( m_ );
			if ( Arrays.binarySearch( pronouns, text ) < 0 )
				return true;
		}	
		return false;
	}

	@Override
	protected void configure(Properties p_) {
		// TODO Auto-generated method stub
		
	}


}
