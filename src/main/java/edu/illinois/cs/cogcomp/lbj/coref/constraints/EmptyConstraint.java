package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

/**
 * EmptyConstraint assumes every pair of mentions should be coreferent -- i.e.
 *    there is only a single entity per document.
 *    
 * Its utility is in generating the pairwise predictions for a document after
 *    clustering has been performed.
 *   
 * @author mssammon, arozovska
 *
 */

public class EmptyConstraint extends ConstraintBase implements Constraint
{
	private final static String m_NAME = "EmptyConstraint";
	
	public EmptyConstraint() throws Exception
	{
		super();
	}
	
	protected void configure( Properties p_ )
	{
		;
	}
	
	/**
	 * find all mentions for which this constraint has an effect, given the index of 
	 *   a specific mention in the document
	 */
	
	public Map< Mention, Double > findAppropriate(Doc d, int mIndex, boolean useGoldStandard ) 
	{  
		Double corefScore = 1.0; // indicates forced non-coref with high belief
//		Double uncorefScore = -1.0;
		HashMap< Mention, Double > corefs = new HashMap< Mention, Double >();
		
		List< Mention > allMentions = getAllMentions(d, useGoldStandard );
		  	
		Mention m = getMention( d, mIndex, useGoldStandard );
				
    	String text = m.getExtent().getCleanText().toLowerCase();
    	System.err.println( "EC.findAppropriate(): text is '" + text + "'" );
    	
    	for ( Mention laterMent : allMentions ) 
    	{
			if ( laterMent.getExtentFirstWordNum() > m.getExtentFirstWordNum() )
				corefs.put( laterMent, corefScore );
    	}
    	
		return corefs;
	}
	  

	public String getName() 
	{
		return m_NAME;
	}

	public double checkConstraint(Mention firstMent_, Mention secondMent_,
			boolean useGoldStandard) 
	{
		return 1.0;
	}

	@Override
	protected boolean checkMention(Mention m_) 
	{
		return true;
	}



}
