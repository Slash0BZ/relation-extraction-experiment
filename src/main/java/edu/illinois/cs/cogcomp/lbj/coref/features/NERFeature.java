package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;



/**
  * A collection of features related to the entity type of a mention
  * or the relationship between the entity types of mentions.
  * An entity type is one of "PER" (person), "ORG" (organization),
  * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
  * "FAC" (facility), or "WEA" (weapon).
 */
public class NERFeature {
  /** No need to construct collection of static features. */
  protected NERFeature() {
  }
  
  // The NER need to be exactly the same
  public static String doNERExactMatch(CExample ex) {
	  		Mention m1 = ex.getM1();
	  		Mention m2 = ex.getM2();
	  		String t1 = getHeadNER(m1);
	  		String t2 = getHeadNER(m2);
	  		
	  		if (t1.equals(Constants.NER_UNKNOWN) || t2.equals(Constants.NER_UNKNOWN))
	  			return "unknown";
	  		else if (t1.equals(t2))
	  			return "true";
	  		else
	  			return "false";
  }

  //  
  public static String doNERLooseMatch(CExample ex) {
	  Mention m1 = ex.getM1();
		Mention m2 = ex.getM2();
		String t1 = getHeadNER(m1);
		String t2 = getHeadNER(m2);
		if (t1.equals(Constants.NER_UNKNOWN) && t2.equals(Constants.NER_UNKNOWN))
			return "unknown";
		else if (t1.equals(t2) || t1.equals(Constants.NER_UNKNOWN) || t2.equals(Constants.NER_UNKNOWN))
			return "true";
		else
			return "false";
  }
  

  public static String getHeadNER(Mention m)
	{
	  //return EntityTypeFeatures.getEType(m);
	  if(m.getDoc().getTextAnnotation() ==null|| !m.getDoc().getTextAnnotation().hasView(ViewNames.NER_CONLL)) {
		  System.out.println("Returning with no ner view");
		  return Constants.NER_UNKNOWN;
	  }
	  
	  	View nerView = m.getDoc().getTextAnnotation().getView(ViewNames.NER_CONLL);
	  	if (nerView != null) {
			System.out.println("NER view checked");
		}
		List<Constituent> consCoverM = nerView.getConstituentsCoveringSpan(m.getHead().getStartWN(), m.getHead().getEndWN()+1);
		String result = Constants.NER_UNKNOWN;
		if(consCoverM.size()>0)
		 result = consCoverM.get(0).getLabel();
		//System.out.println(m + " " + result);
		if(result.contains("PER"))
			result = Constants.NER_PER;
		if (result.equals(Constants.NER_UNKNOWN)) {
		    if (m.getType().equals("NAM")) {
		      result = EntityTypeFeatures.getNameEType(m);
		    } else if (m.getType().equals("PRE")) {
		      if (m.getDoc().isCaseSensitive()) {
		        char h1 = m.getHead().getText().charAt(0);
		        if (Character.isUpperCase(h1))
		          result = EntityTypeFeatures.getNameEType(m);
		        else
		          result = EntityTypeFeatures.getNominalEType(m);
		      } else {
		        String wnNAM = EntityTypeFeatures.getNameEType(m);
		        String wnNOM = EntityTypeFeatures.getNominalEType(m);
		        if ( wnNAM.equals("unknown") && !wnNOM.equals("unknown") ) {
		          result = wnNOM;
		        } else {
		          result = wnNAM;
		        }
		      }
		    } else if (m.getType().equals("NOM")) {
		      result = EntityTypeFeatures.getNominalEType(m);
		    } else if (m.getType().equals("PRO")) {
		      result = EntityTypeFeatures.getPronounEType(m);
		    } else {
		      result = Constants.NER_UNKNOWN;
		    }
		}
		return result;
	}
}

