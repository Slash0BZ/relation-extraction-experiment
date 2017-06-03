package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;


/**
 * given two mentions, if they are from the same sentence, and the
 *    later mention is a pronoun, and the other mention is the closest
 *    preceding "compatible" (in terms of gender and number) non-pronoun
 *    mention, indicate that the mentions are COREFERENT.
 *    
 * @author mssammon, arozovska, rizzolo
 *
 */

public class PronounToSameSentenceNonPronoun extends ConstraintBase
  implements Constraint
{
	public PronounToSameSentenceNonPronoun() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}




	private final static String m_NAME = "PronounToSameSentenceNonPronoun";

/**
 * given index of existing mention that is a non-pronoun
 *   search the doc 
 */
	  
  public Map< Mention, Double > findAppropriate(Doc d, int mIndex, boolean useGoldStandard ) 
  {  
    Mention m = getMention( d, mIndex, useGoldStandard );
    
    int sentenceIndex = m.getSentNum();
    String text = m.getExtent().getCleanText().toLowerCase();
    boolean requiresPerson = Arrays.binarySearch(peoplePronouns, text) >= 0;
    boolean requiresNonPerson =
      Arrays.binarySearch(nonPeoplePronouns, text) >= 0;
    boolean requiresPlural = Arrays.binarySearch(pluralPronouns, text) >= 0;
    boolean requiresSingular =
      Arrays.binarySearch(singularPronouns, text) >= 0;

    if (--mIndex >= 0) {
    	m = getMention( d, mIndex, useGoldStandard );
      int wordIndex = m.getHeadFirstWordNum();

      while (mIndex >= 0 && sentenceIndex == m.getSentNum()) {
        if (!m.getType().equals("PRO") && !m.getType().equals("PRE") // is a NP or NE
            && (!requiresNonPerson || !m.getEntityType().equals("PER")) // not a person if constraint says nonperson
            && (!requiresPerson || m.getEntityType().equals("PER")) // is a person if constraint says person
            && (!requiresPlural                                
                || (d.getPOS(wordIndex).equals("NNPS")
                    || d.getPOS(wordIndex).equals("NNS")))  // if plural POS, constraint is plural
            && (!requiresSingular
                || !(d.getPOS(wordIndex).equals("NNPS")
                     || d.getPOS(wordIndex).equals("NNS")))) // if not plural pos, constraint is singular
        {
        	Map< Mention, Double > ments = new HashMap< Mention, Double >();
        	ments.put( m, 1.0 );
        	return ments;
        }

        if (--mIndex < 0) break;
        m = getMention( d, mIndex, useGoldStandard );
        wordIndex = m.getHeadFirstWordNum();
      }
    }

    return null;
  }


  

  public String getName() 
  {
	return m_NAME;
  }



// TODO: check mention is earliest preceding compatible...?
  
  	public double checkConstraint(Mention firstMent_, Mention secondMent_,
		  boolean useGoldStandard) 
  	{
  		double score = 0.0;
	
  		if ( checkMention( firstMent_ ) ) 	 		
  		{
  			if ( firstMent_.getSentNum() == secondMent_.getSentNum() )
  			{
  				String text = firstMent_.getExtent().getCleanText().toLowerCase();
  				boolean requiresPerson = Arrays.binarySearch(peoplePronouns, text) >= 0;
  				boolean requiresNonPerson =
  					Arrays.binarySearch(nonPeoplePronouns, text) >= 0;
  				boolean requiresPlural = Arrays.binarySearch(pluralPronouns, text) >= 0;
  				boolean requiresSingular =
  					Arrays.binarySearch(singularPronouns, text) >= 0;

  				int wordIndex = secondMent_.getHeadFirstWordNum();

  				Doc d = secondMent_.getDoc();
  				String secondPos = d.getPOS( wordIndex );

  				if (!secondMent_.getType().equals("PRO") && 
  						!secondMent_.getType().equals("PRE") && // is a NP or NE
  						( !requiresNonPerson || !secondMent_.getEntityType().equals("PER") ) && // not a person if constraint says nonperson
  						(!requiresPerson || secondMent_.getEntityType().equals("PER")) // is a person if constraint says person
  						&& (!requiresPlural                                
  								|| (secondPos.equals("NNPS")
  										|| secondPos.equals("NNS")))  // if plural POS, constraint is plural
  						&& (!requiresSingular
  							|| !(secondPos.equals("NNPS")
  									|| secondPos.equals("NNS")))) // if not plural pos, constraint is singular
  				{
  					score = 1.0;
  				}
  			}
  		}
  		return score;
  	}




	@Override
	protected boolean checkMention(Mention m_) 
	{
		String text = m_.getExtent().getCleanText();
			
		if ( Arrays.binarySearch(limitedPeoplePronouns, text) >= 0 )
			return true;	
		
		return false;
	}
//
//	protected boolean checkMentionIsRelevantPronoun( Mention m_ )
//	{
//		
//	}
//
//	protected boolean checkMentionIsRelevantNominal( Mention m_ )
//	{
//		
//	}


	@Override
	protected void configure(Properties p_) 
	{
		;
	}

}
