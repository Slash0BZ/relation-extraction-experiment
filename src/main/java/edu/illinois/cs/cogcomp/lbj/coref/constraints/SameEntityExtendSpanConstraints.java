package edu.illinois.cs.cogcomp.lbj.coref.constraints;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;

public class SameEntityExtendSpanConstraints extends ConstraintBase implements Constraint{
	private final static String m_NAME = "SameEntityExtent";

	public SameEntityExtendSpanConstraints() throws Exception
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

		List<Mention> allMentions = getAllMentions( d, useGoldStandard );

		Mention m = getMention( d, mIndex, useGoldStandard );

		String text = ConstraintBase.getMentionText( m ).toLowerCase();
		if ( m_DEBUG )
			System.err.println( "IPN.findAppropriate(): text is '" + text + "'" );

		if ( checkFirstMention( m ) )
		{
			for ( Mention laterMent : allMentions ) 
			{
				if ( laterMent.compareTo( m ) <= 0)
					continue;

				if ( checkFirstMention( laterMent ) )
				{
					String laterMentText = ConstraintBase.getMentionText( laterMent ).toLowerCase();
					if ( m_DEBUG )
						System.err.println( "## IPN.findAppropriate(): ment 1 is '" +
								text + "; ment 2 is '" + laterMentText + "'" );

					if ( computeSimilarityScore( m, laterMent ) > m_SIM_THRESHOLD )
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



		if ( checkFirstMention( firstMent_ ) )
		{
//			if ( checkFirstMention( secondMent_ ) )
				return computeSimilarityScore( firstMent_, secondMent_ );
		}
		if ( checkFirstMention( secondMent_ ) ){
			    return computeSimilarityScore( firstMent_, secondMent_ );
		}
		return score;
	}

	private double computeSimilarityScore( Mention m_, Mention a_ ) 
	{
		String textM = m_.getExtent().getCleanText();
		String textA = a_.getExtent().getCleanText();
		if(textM.split(" ").length> 1 &&  textM.toLowerCase().startsWith("the"))
			textM = textM.substring(textM.indexOf(" ")+1);
		if(textA.split(" ").length> 1 &&  textA.toLowerCase().startsWith("the"))
			textA = textA.substring(textA.indexOf(" ")+1);
		if(textA.startsWith("a ") && textM.startsWith("a "))
			return 0.0;
		if(textM.split(" ").length > 1 && textA.split(" ").length > 1 &&
				m_.getDoc().getPOS(m_.getExtentFirstWordNum()).equals("CD") &&
				m_.getDoc().getPOS(a_.getExtentFirstWordNum()).equals("CD"))
			return 0.0;
			
		if(textM.equalsIgnoreCase(textA))
			return 1.0;
		else
			return 0.0;
	}

	protected boolean checkFirstMention(Mention m_) 
	{
		try {
			return checkMention(m_);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override	
	protected void configure(Properties p_) 
	{
		;
	}

	@Override
	protected boolean checkMention(Mention m_) throws Exception {
		if(m_.getEntityType().equals("MONEY") || m_.getEntityType().equals("PERCENT"))
			return false;
		if(m_.getType().equals("NAM"))
			return true;
		else
			return false;
	}
}
