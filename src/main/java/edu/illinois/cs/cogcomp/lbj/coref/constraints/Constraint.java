package edu.illinois.cs.cogcomp.lbj.coref.constraints;

import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;


public interface Constraint
{
	/**
	 * Given a document, this method compiles a list of mention pairs which
	 * this constraint will force to be coreferent and another list of
	 * mention pairs that it will force to be non-coreferent.  The two lists
	 * are themselves returned in a list.
	 *
	 * @param d  The document to constrain.
	 * @param useGoldStandard use gold standard mentions from Document
	 * @return A list of two lists as described above.
	 * @throws Exception 
	 **/
	
	public List<List<Mention[]>> constrain( Doc d_, boolean useGoldStandard ) throws Exception;
	  
	  
	/**
	 * given a document and the index of one mention, find all other mentions
	 *    that form the second half of pairs affected by this constraint 
	 * @param d_
	 * @param mIndex_
	 * @return
	 * @throws Exception 
	 */
	
	public Map< Mention, Double > findAppropriate( Doc d_, int mIndex_, boolean useGoldStandard ) throws Exception;

	
	/**
	 * identify all pairs of mentions affected by this constraint
	 * @param d_
	 * @param useGoldStandard
	 * @return
	 * @throws Exception
	 */
	
	public Map< Mention, Map< Mention, Double > > findAllPairs( Doc d_, boolean useGoldStandard ) throws Exception;

	/**
	 * for a given pair of mentions, identify whether the constraint affects them
	 * 
	 * a positive score indicates they should be coreferent; a negative score that they
	 *    should not co-refer; and a zero score means they are not affected by this constraint
	 * @param firstMent_
	 * @param secondMent_
	 * @param useGoldStandard
	 * @return
	 * @throws Exception
	 */
	
	public double checkConstraint( Mention firstMent_, Mention secondMent_, boolean useGoldStandard ) throws Exception;
	
	/**
	 * return the name of this constraint
	 * @return
	 */
	
	public String getName();

}
