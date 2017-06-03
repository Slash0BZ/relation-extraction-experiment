package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;

/**
 * given two mentions, check that they are definite descriptions (using determiner
 *   and mention type) and that if so, they are identical. 
 *   
 * If conditions are met, indicates mentions co-refer.  
 * 
 * @author mssammon, arozovska
 *
 */

public class IdenticalDetNom extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "IdenticalDetNom";
	private final static double m_SIM_THRESHOLD = 0.8;
	private final boolean m_DEBUG = false;
	private boolean m_filterWeakDefiniteDet = false;
	static protected String m_FILTER_WEAK_DEF_DET = "filterWeakDefDet";
	//String[] weekCorefHead = {"time","judge","president","service","money"};
	String[] weekCorefHead ={};
	public IdenticalDetNom() throws Exception
	{
		super();
	}
	
	public IdenticalDetNom( String configFileName_ ) throws Exception 
	{
		super( configFileName_ );
	}
	
	
	protected void configure( Properties p_ )
	{
		String str =  p_.getProperty( m_FILTER_WEAK_DEF_DET );
		m_filterWeakDefiniteDet = Boolean.parseBoolean( str );
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
		
    	String text = m.getExtent().getCleanText().toLowerCase();
    	if ( m_DEBUG )
    		System.err.println( "INOM.findAppropriate(): text is '" + text + "'" );
    	
    	
    	String firstType = m.getType();

    	if ( "NOM".equalsIgnoreCase( firstType ) )
    	{
    		for ( Mention laterMent : allMentions ) 
    		{
				if ( laterMent.getExtentFirstWordNum() <= m.getExtentFirstWordNum() )
					continue;

    			String secondType = laterMent.getType();
    			
    			if ( secondType.equalsIgnoreCase( firstType ) )
    			{
    				String laterMentText = laterMent.getExtent().getCleanText().toLowerCase();
    				if ( m_DEBUG )
    					System.err.println( "## IPN.findAppropriate(): ment 1 is '" +    					
    							text + "; ment 2 is '" + laterMentText + "'" );

    				if ( computeEntityScore( text, laterMentText, m, laterMent ) > m_SIM_THRESHOLD )
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
		String firstType = firstMent_.getEntityType();
		//if(Math.abs(firstMent_.getSentNum()-secondMent_.getSentNum())>5)
		//	return 0.0;
		
    	if ( checkMention( firstMent_ )  && checkMention( secondMent_ ))
    	{
    		String secondType = secondMent_.getEntityType();
    		
    		if ( secondType.equalsIgnoreCase( firstType ) )
    		{
    			String firstMentClean = cleanMentionName (firstMent_.getExtent().getCleanText().split(" , w")[0]);
    			String secondMentClean = cleanMentionName (secondMent_.getExtent().getCleanText().split(" , w")[0]);
    			double locScore = computeEntityScore( firstMentClean, secondMentClean,firstMent_, secondMent_);
    			if ( locScore > m_SIM_THRESHOLD ){
    				if (checkMentionPair( firstMent_, secondMent_ )){
    					score = 1.0;
    				}
    			}
    		
    		}
    	}
		return score;
	}

	
	
	private boolean checkMentionPair(Mention firstMent_, Mention secondMent_) {
		Doc doc = firstMent_.getDoc();
		if(firstMent_.compareTo( secondMent_ ) >0){
			// Swap
			Mention tmp = firstMent_;
			firstMent_ = secondMent_;
			secondMent_ = tmp;
		}
		//if(Math.abs(firstMent_.getSentNum()-secondMent_.getSentNum())>=10)
			//return false;
		for(int idx = doc.getMentionPosition(firstMent_)+1; idx < doc.getMentionPosition(secondMent_); idx++){
			Mention mentBetween = doc.getMention(idx);
			if(mentBetween.getHead().getCleanText().equalsIgnoreCase(firstMent_.getHead().getCleanText()))
				return false;
		}
		return true;
	}

	private double computeEntityScore( String text_, String text2_, Mention firstMent_, Mention secondMent_ ) 
	{
		double score = 0.0;
		// ignore the determiner
		String firstWord = text_.split(" ")[0].toLowerCase();
		boolean differentPossessive = false;
		if ( Arrays.binarySearch( possessivePro, text_.split(" ")[0].toLowerCase() ) >= 0 &&
				Arrays.binarySearch( possessivePro, text2_.split(" ")[0].toLowerCase() ) >= 0 &&
				!text_.split(" ")[0].toLowerCase().equals(text2_.split(" ")[0].toLowerCase())
				)
			differentPossessive = true;
				
		if ( Arrays.binarySearch( definiteDet, firstWord ) >= 0 ||
			firstWord.toLowerCase().equals("a")||firstWord.toLowerCase().equals("an"))
		{
			
			if(text_.indexOf(" ") !=-1)
				text_ = text_.substring(text_.indexOf(" ")+1);
		}
		if(firstMent_.getDoc().getPOS(firstMent_.getExtentFirstWordNum()).equals("CD")){
			//System.out.print(text_ + "  " + text_.substring(text_.indexOf(" ")) + "::");
			text_ = text_.substring(text_.indexOf(" ")+1);
			//System.out.println(text_);
		}
		if(text_.split(" ").length>1 && Arrays.binarySearch(modifierPos, firstMent_.getDoc().getPOS(firstMent_.getExtentFirstWordNum()+1))>=0){
			//System.out.print(text_ + "  " + text_.substring(text_.indexOf(" ")) + "::");
			text_ = text_.substring(text_.indexOf(" ")+1);
			//System.out.println(text_);
		}
		firstWord = text2_.split(" ")[0].toLowerCase();
		if ( Arrays.binarySearch( definiteDet, firstWord ) >= 0 )
		{
			if(text2_.indexOf(" ") !=-1)
				text2_ = text2_.substring(text2_.indexOf(" ")+1);
		}
		
		if(text2_.split(" ").length>1 && Arrays.binarySearch(modifierPos, secondMent_.getDoc().getPOS(secondMent_.getExtentFirstWordNum()+1))>=0){
			//System.out.print(text2_ + "  ");
			text2_ = text2_.substring(text2_.indexOf(" ")+1);
			//System.out.println(text2_);
		}
		
		if ( text_.equalsIgnoreCase( text2_ ) )
			score = 1.0;
		if(differentPossessive)
			score = 0.0;
		
		return score;
	}

	@Override
	protected boolean checkMention(Mention m_) 
	{
		boolean isGood = false;
		
		if ( "NOM".equalsIgnoreCase( m_.getType() ) )
		{
			Doc d = m_.getDoc();
			int firstIndex = m_.getExtentFirstWordNum();
			String firstWord = d.getWord( firstIndex ).toLowerCase();

			// Possessive 
			if(d.getPOS(m_.getExtentFirstWordNum()).equals("PRP$") || 
					(m_.getExtentFirstWordNum() < m_.getHeadFirstWordNum() && d.getPOS(m_.getHeadFirstWordNum()-1).equals("POS")))
						isGood = true;
			
			if ( Arrays.binarySearch( definiteDet, firstWord ) >= 0 || firstWord.toLowerCase().equals("a") ||firstWord.toLowerCase().equals("an")||d.getPOS(m_.getExtentFirstWordNum()).equals("CD") )
			{
				isGood = true;
				
				if(myAux.isStringContains(m_.getHead().getCleanText(), weekCorefHead, false))
					isGood = false;
				if ( m_.getExtentLastWordNum() == firstIndex ) // assumes inclusive indexes
					if ( m_filterWeakDefiniteDet && 
							Arrays.binarySearch( weakDefiniteDet, firstWord ) >= 0 )
						isGood = false;
			}
		}
		
		return isGood;
	}


}
