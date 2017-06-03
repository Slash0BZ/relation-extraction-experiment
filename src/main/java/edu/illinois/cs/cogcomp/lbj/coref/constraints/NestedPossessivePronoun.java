package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/**
 * given two mentions, checks whether they are nested or contiguous.
 * If so, and the first is a possessive pronoun, constraint fires.
 *  
 * If conditions are met, indicates mentions CANNOT co-refer.  
 * 
 * @author mssammon, arozovska
 *
 */

public class NestedPossessivePronoun extends ConstraintBase 
{
	
	public NestedPossessivePronoun() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	private final static String m_NAME = "NestedPossessivePronoun";
	private int m_WINDOW = 5;

	@Override
	public Map<Mention, Double> findAppropriate(Doc d_, int index_, boolean useGoldStandard ) 
	{
		Map< Mention, Double > constrainedMents = new HashMap< Mention, Double > ();
		
		Mention firstMent = d_.getMention( index_ );
		
		for ( Mention secondMent: getMentions( d_, useGoldStandard ) )
		{
			double score = checkConstraint( firstMent, secondMent, useGoldStandard );

			if ( Math.abs( score ) != 0 ) 
				constrainedMents.put( secondMent, new Double( score ) );
		}
	
		return constrainedMents;
	}

	public String getName() 
	{
		return m_NAME;
	}

	public double checkConstraint(Mention firstMent_, Mention secondMent_,
			boolean useGoldStandard) 
	{
  		double score = 0.0;
  		
  		if ( ( firstMent_ != secondMent_ ) && checkMention( firstMent_ ) ) 	 		
  		{
  			if ( firstMent_.getSentNum() == secondMent_.getSentNum() )
  			{
  				int firstWordIndex = firstMent_.getHeadFirstWordNum();
  				int secondWordIndex = secondMent_.getExtentFirstWordNum();

  				
  				if ( ( secondWordIndex - firstWordIndex <= m_WINDOW ) &&
  						( secondWordIndex - firstWordIndex >= 0 ) )
  				{
  					Doc d = firstMent_.getDoc();
  					boolean isOk = true;
  					
  					for ( int i = firstWordIndex + 1; i < secondWordIndex && isOk; ++i ) 
  					{
  						if ( Arrays.binarySearch( modifierPos, d.getPOS( i ) ) < 0 )
  							isOk = false;
  					}

  					if ( isOk )
  						score = -1.0;

  				}
  			}
  		}
  		return score;
	}

	// TODO: add possessive pronoun ending? 
	@Override
	protected boolean checkMention(Mention m_) 
	{
		String text = m_.getExtent().getCleanText();
		
		if ( Arrays.binarySearch( possessivePro, text ) >= 0 )
			return true;
	
		return false;
	}

	@Override
	protected void configure(Properties p_) 
	{
		;
	}	
}	
