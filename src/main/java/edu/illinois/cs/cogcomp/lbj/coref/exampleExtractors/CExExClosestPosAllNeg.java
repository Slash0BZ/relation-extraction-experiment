package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/** 
 * Generates examples of coreference in the following way:
 * For each mention, create a positive example with the nearest preceding
 * coreferential mention, and create negative examples with each preceding
 * non-coreferential mention.
 * To generate examples, first set the document using {@code setDoc()}
 * and then call {@code generateAllExamples()}.
 * @author Eric Bengtson
 */
public class CExExClosestPosAllNeg extends CExampleExtractorBasic
 implements Serializable {
    private static final long serialVersionUID = 1L;
    protected boolean m_allowPosProCata = true;
    protected boolean m_allowNegProCata = true;
	protected int m_longRangeProThres = 3; // this elaborates on the previous field: how long is long?
    protected boolean m_dropHalfNeg = false;
    protected boolean m_dropHalfPredict = true;
    protected boolean m_training = false;
    protected boolean m_experimental = false;
    protected Random m_rng;

    /**
     * Default constructor.
     * Pronoun cataphora are allowed.
     */
    public CExExClosestPosAllNeg() {
	super();
	m_rng = new Random();
    }
    
    /**
     * Constructor.
     * @param allowPosProCata Whether positive examples of pronoun
     * cataphora are included.
     * @param allowNegProCata Whether negative examples that would be
     * pronoun cataphora are included.
     */
    public CExExClosestPosAllNeg(boolean allowPosProCata,
				 boolean allowNegProCata) {
	this();
	m_allowPosProCata = allowPosProCata;
	m_allowNegProCata = allowNegProCata;
    }

    /**
     * Constructor.
     * @param allowPosProCata Whether positive examples of pronoun
     * cataphora are included.
     * @param allowNegProCata Whether negative examples that would be
     * pronoun cataphora are included.
     * @param dropHalfNeg Whether to drop half of the negative examples.
     */
    public CExExClosestPosAllNeg(boolean allowPosProCata,
				 boolean allowNegProCata,
				 boolean dropHalfNeg) {
	this(allowPosProCata, allowNegProCata);
	m_dropHalfNeg = dropHalfNeg;
    }

    /**
     * Constructor.
     * @param allowPosProCata Whether positive examples of pronoun
     * cataphora are included.
     * @param allowNegProCata Whether negative examples that would be
     * pronoun cataphora are included.
     * @param dropHalfNeg Whether to drop half of the negative examples.
     * @param training Whether the classifier is training.
     */
    public CExExClosestPosAllNeg(boolean allowPosProCata,
				 boolean allowNegProCata,
				 boolean dropHalfNeg, boolean training) {
	this(allowPosProCata, allowNegProCata, dropHalfNeg);
	m_training = training;
    }

    /**
     * Constructor.
     * @param allowPosProCata Whether positive examples of pronoun
     * cataphora are included.
     * @param allowNegProCata Whether negative examples that would be
     * pronoun cataphora are included.
     * @param dropHalfNeg Whether to drop half of the negative examples.
     * @param training Whether the classifier is training.
     * @param experimental Whether to operate in experimental mode.
     */
    public CExExClosestPosAllNeg(boolean allowPosProCata,
				 boolean allowNegProCata,
				 boolean dropHalfNeg, boolean training,
				 boolean experimental) {
	this(allowPosProCata, allowNegProCata, dropHalfNeg);
	m_training = training;
	m_experimental = experimental;
    }

    /** 
     * Generates a list of all the examples in the following way:
     * For each mention, create a positive example with the nearest preceding
     * coreferential mention, and create negative examples with each preceding
     * non-coreferential mention.
     * {@code setDoc} must be called before generating examples.
     * @return A list of examples.
     */
    protected List<CExample> generateAllExamples() {
	List<CExample> xes = new ArrayList<CExample>();
	if(m_doc.usePredictedMentions()){
		System.err.println("Using Prediction Mention for Training!!");
	}
	for (int j = 1; j < m_doc.getMentions().size(); ++j) {
		Mention m2 = m_doc.getMentions().get(j);
		boolean m2Pro = m2.getType().equals("PRO");
		boolean foundTrue = false;
		for (int i = j - 1; i >= 0; --i) {
			Mention m1 = m_doc.getMentions().get(i);
			boolean m1Pro = m1.getType().equals("PRO");
			boolean bEquiv = equiv(m1, m2);
			if(m1.getSentNum() + 10 < m2.getSentNum())
				break;
			
			//remove predict nested mention
			if((m_doc.getMentionsContainedIn(m1).contains(m2) ||m_doc.getMentionsContainedIn(m2).contains(m1))
					&& (m1.getID().startsWith("p") || m2.getID().startsWith("p")))
				continue;

			if (m_experimental) {
				if (!m1.getEntityType().equals(m2.getEntityType()))
					continue;
			}

			if (m1Pro && !m2Pro) {
				if (bEquiv && !m_allowPosProCata)
					continue;
				if (!bEquiv && !m_allowNegProCata)
					continue;
			}
			if (bEquiv) {
				if (foundTrue)
					continue;
				else
					foundTrue = true;
			}

			if (m_dropHalfPredict && m1.getID().startsWith("p") && m_rng.nextBoolean())
				continue; //Drop half negatives.

			if (m_dropHalfPredict && m2.getID().startsWith("p") && m_rng.nextBoolean())
				continue; //Drop half negatives.

			if (m_dropHalfNeg && !bEquiv && m_rng.nextBoolean())
				continue; //Drop half negatives.

			CExample ex = m_doc.getCExampleFor(m1, m2);


			if (m_training) {
				//Propagate links(for salience), but not others:
				if (bEquiv)
					m2.addCorefMentsOf(m1);
			}
			xes.add(ex);
		}
	}
	return xes;
    }

    /**
     * Determines whether the mentions are the same according to entity ID.
     * @param m1 A mention.
     * @param m2 Another mention.
     * @return Whether the mentions have the same entity ID.
     */
    protected boolean equiv(Mention m1, Mention m2) {
    	return  m1.getEntityID() != null
    	&& !m1.getEntityID().equals("NONE")
	 && m1.getEntityID().equals( m2.getEntityID() );
    }
}
