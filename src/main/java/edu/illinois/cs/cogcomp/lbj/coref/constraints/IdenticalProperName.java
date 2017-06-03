package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/**
 * given two mentions, checks whether they are likely to be Named Entities, and
 *    if so, will check whether the surface strings are identical (possibly, modulo 
 *    some slight perturbations)
 *  
 * If conditions are met, indicates mentions co-refer.  
 * 
 * @author mssammon, arozovska
 *
 */

public class IdenticalProperName extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "IdenticalProperName";

	//Use this to avoid linking name entity with similar surface string
	private ModifierMismatchConstraints modifierChecker = new ModifierMismatchConstraints();
	
	public IdenticalProperName() throws Exception
	{
		super();
	}
	
	public IdenticalProperName( String configFile_ ) throws Exception
	{
		super( configFile_ );
	}
	
	public IdenticalProperName( boolean ignorePossessiveMarkers_, 
								boolean useNeSim_,
								boolean useExternalProcessing_ ) 
	throws Exception
	{
		super();

		m_ignorePossessiveMarkers = ignorePossessiveMarkers_;
		m_useNeSim = useNeSim_;
		m_useExternalProcessing = useExternalProcessing_;
	}
	
	@Override
	protected void configure(Properties p_) 
	{
		
	}

	
	/**
	 * find all mentions for which this constraint has an effect, given the index of 
	 *   a specific mention in the document
	 * @throws Exception 
	 */
	
	public Map< Mention, Double > findAppropriate(Doc d, int mIndex, boolean useGoldStandard ) throws Exception 
	{  
		if ( m_DEBUG )
		{
			long startTime = (long) System.currentTimeMillis();
			System.err.println( "IPN.findAppropriate(): start time: " + startTime );
		}
		
		Double score = 0.0; // indicates forced coref with high belief
		HashMap< Mention, Double > corefs = new HashMap< Mention, Double >();
		
		List< Mention > allMentions = getAllMentions( d, useGoldStandard );
		
		Mention m = getMention( d, mIndex, useGoldStandard );
		
		if ( null == m ) {
			throw new Exception( "ERROR: IdenticalProperName.findAppropriate(): " + 
					"found null mention at index " + mIndex + "; found " +
					allMentions.size() + " mentions total; document is:\n" +
					d.toAnnotatedString( false )
					);
		}
    	String text = m.getExtent().getCleanText().toLowerCase();
    	if ( m_DEBUG )
    		System.err.println( "IPN.findAppropriate(): text is '" + text + "'" );

    	if ( checkIsName( m ) )
    	{
    		for ( Mention laterMent : allMentions ) 
    		{
				if ( laterMent.getExtentFirstWordNum() > m.getExtentFirstWordNum() )
				{
					score = checkConstraint( m, laterMent, useGoldStandard );
					
					if ( score > m_SIM_THRESHOLD && modifierChecker.checkConstraint( m , laterMent, false)>=0)
						corefs.put( laterMent, score );
				}	
    		}
    	}
    	
    	if ( m_DEBUG )
    	{
    		long endTime = (long) System.currentTimeMillis();

			System.err.println( "IPN.findAppropriate(): end time: " + endTime );
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

	
	public double checkConstraint( Mention firstMent_, 
								   Mention secondMent_,
								   boolean useGoldStandard_ 
								   ) 
	throws Exception 
	{
		double score = 0.0;

		
		if ( m_DEBUG )
		{
			long startTime = (long) System.currentTimeMillis();

			System.err.println( "IPN.checkConstraint(): start time: " + startTime );
		}
		
		// If the mention span overlap, don't apply this constraint
		if(firstMent_.compareTo(secondMent_)<0){
			if(secondMent_.getExtentFirstWordNum() < firstMent_.getExtentLastWordNum())
				return 0.0;
		}else{
			if(firstMent_.getExtentFirstWordNum() < secondMent_.getExtentLastWordNum())
				return 0.0;
		}
		
		String firstType = firstMent_.getEntityType();

    	if ( checkIsName( firstMent_ ) )
    	{
    		String secondType = secondMent_.getEntityType();
    		
    		if ( checkIsName( secondMent_ ) && secondType.equalsIgnoreCase( firstType ) )
    		{
    			String keyPhase1 =  firstMent_.getSurfaceText();
    			boolean t = false;
    			if(firstMent_.getSurfaceText().contains(" , ")){
    				keyPhase1 =firstMent_.getSurfaceText().split(" , ")[0];
    				t = true;
    			}
    			String keyPhase2 =  secondMent_.getSurfaceText();
    			if(secondMent_.getSurfaceText().contains(" , ")){
    				keyPhase2 = secondMent_.getSurfaceText().split(" , ")[0];
    				t = true;
    			}
    			if(keyPhase1.equals(keyPhase2) && t){
    				score = 1.0;
    			}
    			
    			double initialScore = computeEntityScore( firstMent_, secondMent_ );

    			if ( initialScore > m_SIM_THRESHOLD  && modifierChecker.checkConstraint( firstMent_ , secondMent_, false)>=0){
    				/*if(!firstMent_.getSurfaceText().equalsIgnoreCase(secondMent_.getSurfaceText()))
    					System.out.println(firstMent_+ "      ||     " +secondMent_);*/
					score = 1.0;
    			}
    		}    	
    	}    	


		if ( m_DEBUG )
		{
			long endTime = (long) System.currentTimeMillis();
			System.err.println( "IPN.checkConstraint(): end time: " + endTime );
		}
		
		return score;
	}

	
	
	private boolean checkIsName( Mention ment_ ) throws Exception 
	{
		String firstType = ment_.getType();
		String text = ment_.getExtent().getCleanText();
		Doc d = ment_.getDoc();
		if ( null == ment_.getHead() )
			throw new Exception( "ERROR: IdenticalProperName.checkIsName(): " + 
					"no head specified for mention: " + ment_.toFullString() );
		// use another constraint to deal with person name entity
		if(ment_.getEntityType().equals("PER")||ment_.getEntityType().equals("MONEY") || ment_.getEntityType().equals("DATE")
				|| ment_.getEntityType().equals("PERCENT"))
			return false;
		
		if(ment_.getHeadFirstWordNum() > ment_.getExtentFirstWordNum() &&
			(d.getPOS(ment_.getHeadFirstWordNum()-1).equals("POS")||
			d.getPOS(ment_.getHeadFirstWordNum()-1).equals("PRP")))
			return false;
		
		if(ment_.getHeadLastWordNum() < ment_.getExtentLastWordNum() &&
				(d.getPOS(ment_.getHeadLastWordNum()+1).equals("IN")))
				return false;
		if ( m_DEBUG )
		{
			System.err.println( "## IPN.checkIsName(): type is '" + firstType + 
					"', text is '" + text + "'..." );
		}
		// FIXME: check that "NAM" type requirement doesn't exclude relevant mentions
		
		
		// TODO: Check if the head should equal to extent
		if ( /*(ment_.getHead() ).equals( ment_.getExtent() ) &&*/
				/*ment_.getHeadFirstWordNum() == ment_.getExtentFirstWordNum() &&*/
				( Arrays.binarySearch( pronouns, text.toLowerCase() ) < 0 ) &&
			 "NAM".equalsIgnoreCase( firstType ) && 
			 text.matches( "[A-Z].*" ) && ( text.length() > 1 )
			 && ( Arrays.binarySearch( pronouns, text ) < 0 ) 
			 )
			return true;
		
		return false;
	}

	@Override
	protected boolean checkMention(Mention m_) throws Exception 
	{
		return checkIsName( m_ );
	}





}
