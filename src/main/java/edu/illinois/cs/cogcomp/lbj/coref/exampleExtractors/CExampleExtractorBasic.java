package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.filters.CFilter;
import edu.illinois.cs.cogcomp.lbj.coref.filters.MFilter;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;


/** 
  * Extracts coreference examples from a document
  * from all ordered pairs of mentions of a document such that any filters
  * accept them.
  * If a document is not provided in the constructor, it must be set first using
  * {@code setDoc()}.
  * @author Eric Bengtson
 */
public class CExampleExtractorBasic extends DocFilteredBatchExEx<CExample>
  implements CExampleExtractor, Serializable {
  private static final long serialVersionUID = 1L;

  //Filters:
  private CFilter m_cFilter = null;
  private boolean m_bBothMentions; //If m_m2Filter non-null, should be true.
  private MFilter m_mFilter = null;
  private MFilter m_m2Filter = null;

  /* Constructors */

  /**
    * Constructs a coreference example extractor with the default settings.
   */
  public CExampleExtractorBasic() {
  }

  /**
    * Constructs a coreference example extractor
    * with a {@code CExample} filter.
    * @param cFilter A coreference example filter.
   */
  public CExampleExtractorBasic(CFilter cFilter) {
    setCFilter(cFilter);
  }

  /**
    * Constructs a coreference example extractor
    * with a {@code CExample} filter and a {@code Mention} filter.
    * @param cFilter A coreference example filter.
    * @param mFilter A mention filter.
    * @param bBothMentions Determines whether both mentions in an example
    * must be accepted by the filter for the example to be accepted.
   */
  public CExampleExtractorBasic(CFilter cFilter,
                                MFilter mFilter, boolean bBothMentions) 
  {
    this(cFilter);
    setMFilter(mFilter);
    m_bBothMentions = bBothMentions;
  }

  /**
    * Constructs a coreference example extractor
    * with a {@code CExample} filter and two {@code Mention} filters.
    * The order of the mention filters is significant.
    * @param cFilter A coreference example filter.
    * @param m1Filter A mention filter
    * for the first mention of a {@code CExample}.
    * @param m2Filter A mention filter
    * for the second mention of a {@code CExample}.
   */
  public CExampleExtractorBasic(CFilter cFilter, 
                                MFilter m1Filter, MFilter m2Filter)
  {
    this(cFilter);
    setMFilters(m1Filter, m2Filter);
  }

  /**
    * Constructs a coreference example extractor
    * that will extract examples from the given document.
    * @param doc The document whose examples will be extracted.
   */
  public CExampleExtractorBasic(Doc doc) {
    this(doc, null);
  }

  /**
    * Constructs a coreference example extractor
    * with a {@code CExample} filter
    * that will extract examples from the given document.
    * @param cFilter A coreference example filter.
    * @param doc The document whose examples will be extracted.
   */
  public CExampleExtractorBasic(Doc doc, CFilter cFilter) {
    this.setCFilter(cFilter); //Needs to happen before setDoc()
    this.setDoc(doc);
  }


  /**
    * Generates coreference examples
    * from all ordered pairs of mentions of a document such that any filters
    * accept them.
    * Uses predicted or true mentions according to the output of
    * the document's {@code getMentions()} method.
    * @return A list of examples.
   */
  protected List<CExample> generateAllExamples() {
    List<CExample> xes = new ArrayList<CExample>();
    if (m_doc == null) {
      throw new RuntimeException(
          "Using CExampleExtractor without specifying Document.");
    }
    for (int iM1 = 0; iM1 < m_doc.getMentions().size(); ++iM1) {
    	Mention m1 = m_doc.getMentions().get(iM1);
      for (int iM2 = iM1 + 1; iM2 < m_doc.getMentions().size(); ++iM2) {
  			Mention m2 = m_doc.getMentions().get(iM2);
  			if(m1.getSentNum() + 10 < m2.getSentNum())
  				continue;
  			if(m1.getID().startsWith("p") || m2.getID().startsWith("p"))
  				continue;
        CExample ex = getExample(m_doc, iM1, iM2);
        xes.add(ex);
      }
    }
    return xes;
  }



  /* Filters */

  /**
    * Sets the coreference filter.
    * Since examples are extracted in a batch,
    * filters should be set before doc is set.
    * @param cFilter The {@code CExample} filter.
   */
  protected void setCFilter(CFilter cFilter) {
    m_cFilter = cFilter;
  }

  /**
    * Sets the mention filter.
    * Since examples are extracted in a batch,
    * filters should be set before doc is set.
    * @param mFilter The filter.
   */
  protected void setMFilter(MFilter mFilter) {
    m_mFilter = mFilter;
  }

  /**
    * Sets both filters, and specify that each must accept its
    * mention for an example to be accepted.
    * Since examples are extracted in a batch,
    * filters should be set before doc is set.
    * @param m1Filter The first filter.
    * @param m2Filter The second filter.
   */
  protected void setMFilters(MFilter m1Filter, MFilter m2Filter) {
    m_mFilter = m1Filter;
    m_m2Filter = m2Filter;
    m_bBothMentions = true; //Both must be satisfied, of course.
  }

  /**
    * Determines whether all applicable filters accept the example.
    * @param ex The example.
    * @return Whether the filters accept the example.
   */
  public boolean doFiltersAccept(CExample ex) {
    if (m_cFilter != null && !m_cFilter.accept(ex))
      return false;
    if (m_mFilter != null) {
      boolean a1 = m_mFilter.accept(ex.getM1()), a2;
      if (m_m2Filter != null)
        a2 = m_m2Filter.accept(ex.getM2());
      else
        a2 = m_mFilter.accept(ex.getM2());

      //m_bBothMentions should be true if using two filters:
      if (m_bBothMentions) {
        if (!a1 || !a2) {
          return false;
        }
      } else {
        if (!a1 && !a2) {
          return false;
        }
      }
    }
    //No filter rejected, so accept:
    return true;
  }




  /* Utilities */

  /** 
    * Get a specific example.
    * @return relevant example using mention numbering based on
    * the document's getMentions().
   */
  protected CExample getExample(Doc d, int iM1, int iM2) {
    Mention m1 = d.getMentions().get(iM1);
    Mention m2 = d.getMentions().get(iM2);
    return m_doc.getCExampleFor(m1, m2);
  }
}
