/*
  * Created on Apr 14, 2006
 */
package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.filters.CFilter;
import edu.illinois.cs.cogcomp.lbj.coref.filters.MFilter;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/** FIXME: This code should inherit from an onlineCExample class */
public class CExExtractorUntilTrue extends CExampleExtractorBasic
  implements Serializable {
  private static final long serialVersionUID = 1L;
  //The paradigm of this code expects that advance() will
  //always be called at the beginning of every getNext().
  //However, recordEquivalence() may have been called, in which case
  //m2 may have been moved.
  //Thus, between calls to getNext(), the m1 and m2 positions are
  //not meaningful and may not point to valid examples.
  private int m_iM1 = 1, m_iM2 = 1; //next to be used.
  private int m_nNegEx = 0, m_nPosEx = 0;
  private boolean m_bTraining;

  /** @param training Indicates whether the system is being trained:
    When training=true, the system expects Entity IDs to be valid and
    recordEquivalence not to be called;
    When training=false, recordEquivalence must be called, and nothing
    is presumed about Entity IDs.
   */
  public CExExtractorUntilTrue(boolean training) {
    m_bTraining = training;
  }

  public CExExtractorUntilTrue(CFilter filter, boolean training) {
    super(filter);
    m_bTraining = training;
  }

  public CExExtractorUntilTrue(Doc doc, CFilter filter, boolean training) {
    super(doc, filter);
    m_bTraining = training;
  }

  /** @param bBothMentions if true, requires that both mentions be
    accepted by the filter if the example is to be generated.
   */
  public CExExtractorUntilTrue(CFilter cFilter,
                               MFilter mFilter, boolean bBothMentions, boolean training)
  {
    super(cFilter, mFilter, bBothMentions);
    m_bTraining = training;
  }

  /** The order of the MFilters matters here. */
  public CExExtractorUntilTrue(CFilter cFilter,
                               MFilter m1Filter, MFilter m2Filter,
                               boolean training)
  {
    super(cFilter, m1Filter, m2Filter);
    m_bTraining = training;
  }

  /* Called by setDoc() after doc is set. */
  @Override
    public void reset() {
      super.reset();
      m_iM1 = 1; //To be advanced still
      m_iM2 = 1;
      m_nNegEx = 0; m_nPosEx = 0;
    }

  /** WARNING: Resets state. */
  public List<CExample> getExamples() {
    if (m_doc == null) {
      throw new RuntimeException(
          "Using ExampleExtractor without specifying Document.");
    }
    this.reset();
    List<CExample> xes = new ArrayList<CExample>();
    CExample ex = this.getNext();
    while (ex != null) {
      xes.add(ex);
      ex = this.getNext();
    }
    return xes;
  }


  //TODO: This code is tricky and deserves a checking
  /** When bTraining is false, you must manually call recordEquivalence()
    afterwards if the predictor predicts positive.
NOTE: This code expects ID to be set to NONE at eval time.
   */
  public CExample getNext() {
    if (m_doc == null) {
      throw new RuntimeException(
          "Using CExampleExtractor without specifying Document.");
    }

    //NOTE: A bug existed here until 9/21/07
    this.advance(); //Pre-advance.  Avoids problems with recordEquiv
    //TODO: Eliminate the if?:
    //TODO: Add mFilter in if:
    while ( m_iM2 < this.m_doc.getMentions().size()
            && !doFiltersAccept( this.getExample(m_doc, m_iM1, m_iM2) )
            //&& !m_filter.accept( this.getExample(m_doc, m_iM1, m_iM2) )
          ) {
      this.advance();
    }
    if (m_iM2 == this.m_doc.getMentions().size())
      return finished();

    Mention m1 = m_doc.getMentions().get(m_iM1);
    Mention m2 = m_doc.getMentions().get(m_iM2);
    CExample result = m_doc.getCExampleFor(m1, m2);
    if (  m_bTraining && m1.getEntityID() != null
          && !m1.getEntityID().equals("NONE")
          && m1.getEntityID().equals( m2.getEntityID() )  ) {
      this.advanceToNextM2();
      m_nPosEx++;
    } else {
      m_nNegEx++;
    }

    if (result == null) {
      return finished();
    } else {
      return result;
    }
  }

  private CExample finished() {
    //clean up and return null:
    //System.out.println("Negative examples: " + m_nNegEx);
    //System.out.println("Positive examples: " + m_nPosEx);
    return null;
  }

  public void recordEquivalence() {
    this.advanceToNextM2();
  }

  /** Move to the next example (if necessary, moving to the next m2) */
  protected void advance() {
    m_iM1--;
    if (m_iM1 < 0) {
      this.advanceToNextM2();
      //In this case, we're currently advancing, so we
      //need to have things set up coherently, so:
      m_iM1--;
    }
  }

  /** Move the slower moving example. */
  protected void advanceToNextM2() {
    m_iM2++;
    m_iM1 = m_iM2;//To be advanced.
    //System.err.println("DEBUG: Advancing to next M2");
  }
}
