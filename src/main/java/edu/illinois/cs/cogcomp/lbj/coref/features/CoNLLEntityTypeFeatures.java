package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocCoNLL;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;



/**
  * A collection of features related to the entity type of a mention
  * or the relationship between the entity types of mentions.
  * An entity type is one of "PER" (person), "ORG" (organization),
  * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
  * "FAC" (facility), or "WEA" (weapon).
 */
public class CoNLLEntityTypeFeatures {
  /** No need to construct collection of static features. */
  protected CoNLLEntityTypeFeatures() {
  }

  /**
    * Determines whether the entity types of two mentions match,
    * as determined by {@link #getEType}.
    * No caching is used.
    * @param ex The example containing the mentions in question.
    * @return "true", "false", or "unknown".
   */

  
  public static String doETypesMatch(CExample ex, boolean useCache, boolean useNER) {
	  	if(useNER){
	  		String t1 = ex.getM1().getEntityType();
	  		String t2 = ex.getM2().getEntityType();
	  		if (t1.equals("unknown") || t2.equals("unknown"))
	  			return "unknown";
	  		else if (t1.equals(t2))
	  			return "true";
/*			if(Parameters.useFixNer){
	  		 if(EntityTypeFeatures.doNomNERMatch(ex.getM1().getHead().getText(), ex.getM2().getEntityType()))
	  			return "true";
	  		else if(EntityTypeFeatures.doNomNERMatch(ex.getM2().getHead().getText(), ex.getM1().getEntityType()))
	  			return "true";
			}*/
	  			return "false";
	  	}
	  	else
	  		return EntityTypeFeatures.doETypesMatch(ex,useCache);
	  }
  
  // The span of ET needs to exactly the same as the span of mention
  public static String doNERExactMatch(CExample ex) {
	  		Mention m1 = ex.getM1();
	  		Mention m2 = ex.getM2();
	  		String t1 = ex.getM1().getEntityType();
	  		String t2 = ex.getM2().getEntityType();
	  		if(m1.getDoc() instanceof DocCoNLL){
	  		View nerView = ((DocCoNLL)m1.getDoc()).getTextAnnotation().getView(ViewNames.NER);
			List<Constituent> consCoverM1 = nerView.getConstituentsCoveringSpan(m1.getExtent().getStartWN(), m1.getExtent().getEndWN()+1);			
			List<Constituent> consCoverM2 = nerView.getConstituentsCoveringSpan(m2.getExtent().getStartWN(), m2.getExtent().getEndWN()+1);
			for(Constituent c : consCoverM1)
				if(c.getStartSpan() == m1.getExtent().getStartWN() && c.getEndSpan() -1 ==m1.getExtent().getEndWN())
					t1 = c.getLabel();
			for(Constituent c : consCoverM2)
				if(c.getStartSpan() == m2.getExtent().getStartWN() && c.getEndSpan() -1 ==m2.getExtent().getEndWN())
					t2 = c.getLabel();
	  		if (t1.equals("unknown") || t2.equals("unknown"))
	  			return "unknown";
	  		else if (t1.equals(t2))
	  			return "true";
/*			if(Parameters.useFixNer){
	  		if(EntityTypeFeatures.doNomNERMatch(ex.getM1().getHead().getText(), ex.getM2().getEntityType()))
	  			return "true";
	  		else if(EntityTypeFeatures.doNomNERMatch(ex.getM2().getHead().getText(), ex.getM1().getEntityType()))
	  			return "true";
			}*/
	  			return "false";
	  		}
	  		else 
	  			return EntityTypeFeatures.doETypesMatch(ex,true);
  
  }
  
  // Return true if both the mentions contains NER spans with the same label 
  public static String doNERLooseMatch(CExample ex) {
		Mention m1 = ex.getM1();
		Mention m2 = ex.getM2();
		String t1 = ex.getM1().getEntityType();
		String t2 = ex.getM2().getEntityType();
		if(t1.equals(t2))
			return "true";
		if(m1.getDoc() instanceof DocCoNLL){
		View nerView = ((DocCoNLL)m1.getDoc()).getTextAnnotation().getView(ViewNames.NER);
		List<Constituent> consCoverM1 = nerView.getConstituentsCoveringSpan(m1.getExtent().getStartWN(), m1.getExtent().getEndWN()+1);			
		List<Constituent> consCoverM2 = nerView.getConstituentsCoveringSpan(m2.getExtent().getStartWN(), m2.getExtent().getEndWN()+1);		
		for(Constituent c1 : consCoverM1)
			for(Constituent c2 : consCoverM2)
				if(c1.getLabel().equals(c2.getLabel()))
					return "true";
		if (t1.equals("unknown") || t2.equals("unknown"))
  			return "unknown";
/*			if(Parameters.useFixNer){
		 if(ex.getM2().getType().equals("NAM") &&
				EntityTypeFeatures.doNomNERMatch(ex.getM1().getHead().getText(), ex.getM2().getEntityType()))
  			return "true";
  		else if(ex.getM1().getType().equals("NAM") &&
  				EntityTypeFeatures.doNomNERMatch(ex.getM2().getHead().getText(), ex.getM1().getEntityType()))
  			return "true";
			}*/
			return "false";
		}
		else
			return EntityTypeFeatures.doETypesMatch(ex,true);
}
  
  /**
    * Determines the entity type of a mention, assuming that the mention
    * is a proper name (ACE mention type "NAM").
    * Checks Various Gazetteers, including lists of first names,
    * last names, honors, cities, states, countries, corporations,
    * sports teams, and universities.
    * In case of a conflict, "unknown" is returned.
    * @param m A mention assumed to be a "NAM".
    * @return The entity type: one of "PER" (person), "ORG" (organization),
    * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
    * "FAC" (facility), or "WEA" (weapon), or "unknown".
   */
  public static String getNameEType(Mention m) {
	  return m.getEntityType();
	  //##HACK##//
	  /*
    String h = m.getHead().getText().toLowerCase();
    String[] words = h.split("\\s");
    String lastWord = words[words.length-1];
    String eType = "unknown";
    //TODO: More gazetteers:
    if (Gazetteers.getMaleFirstNames().contains(words[0])
        || Gazetteers.getFemaleFirstNames().contains(words[0])
        || Gazetteers.getHonors().contains(words[0]) //Assuming all personal honors.
       ) {
      eType = "PER";
       }
    if (Gazetteers.getLastNames().contains(lastWord)) {
      if (eType.equals("PER"))
        return "PER"; //Twice so confident.
      eType = "PER";
    }
    if ( Gazetteers.getCities().contains(h) || Gazetteers.getCountries().contains(h)
         || Gazetteers.getStates().contains(h)) {
      if (!eType.equals("unknown"))
        return "unknown";
      eType = "GPE";
         }
    if (Gazetteers.getOrgClosings().contains(lastWord) || Gazetteers.getPolParties().contains(h)
        || Gazetteers.getCorporations().contains(h) || Gazetteers.getSportTeams().contains(h)
        || Gazetteers.getUniversities().contains(h)) {
      if (!eType.equals("unknown"))
        return "unknown";
      eType = "ORG";
        } 
    return eType;*/
  }

  /**
    * Determines the Entity Type of a mention, assuming that the mention
    * is a common name phrase (ACE mention type "NOM").
    * Checks whether a hypernym of the phrase is one of
    * "person", "political unit", "location", "organization",
    * "weapon", "vehicle", "industrial plant", or "facility", 
    * and returns the appropriate entity type. 
    * @param m A mention assumed to be a "NOM".
    * @return The entity type: one of "PER" (person), "ORG" (organization),
    * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
    * "FAC" (facility), or "WEA" (weapon), or "unknown".
   */
  public static String getNominalEType(Mention m) {
    String h = m.getHead().getText().toLowerCase();
    //Order matters because e.g. political unit is-a group.
    if (WordNetTools.getWN().areHypernyms(h, "person"))
      return "PER";
    else if (WordNetTools.getWN().areHypernyms(h, "political unit"))
      return "GPE";
    else if (WordNetTools.getWN().areHypernyms(h, "location"))
      return "LOC"; //TODO?
    else if (WordNetTools.getWN().areHypernyms(h, "organization"))
      return "ORG";
    else if (WordNetTools.getWN().areHypernyms(h, "weapon"))
      return "WEA";
    else if (WordNetTools.getWN().areHypernyms(h, "vehicle"))
      return "VEH";
    else if (WordNetTools.getWN().areHypernyms(h, "industrial plant")) //TODO: Better?
      return "FAC";
    else if (WordNetTools.getWN().areHypernyms(h, "facility")) //TODO: Better?
      return "FAC";
    else
      return "unknown";
  }

  /** Determine the entity type of the mention, assuming it is a pronoun.
    * Does not attempt to distinguish amongst non-personal pronouns.
    * @param m A mention assumed to be a "PRO".
    * @return "PER" or "unknown".
   */
  public static String getPronounEType(Mention m) {
    String h = m.getHead().getText().toLowerCase();
    //TODO: This could be improved by returning "NON-x".
    if (h.equals("he") || h.equals("him") || h.equals("his")
        || h.equals("himself")	   
        || h.equals("she") || h.equals("her") || h.equals("hers")
        || h.equals("herself")
       ) {
      return "PER";
       }
    return "unknown";
  }

  /* ETypes and Tokens */

  /**
    * Gets the predicted entity type, or the pronoun word if a pronoun.
    * Uses the predicted entity type computed using {@link #getEType}.
    * Uses the default mention types, which may be gold.
    * @param ex The example.
    * @param useCache Whether caching should be done.
    * @return For each mention, the predicted entity type,
    * or, if the mention is a pronoun, the token,
    * conjoined by {@literal "&&"}.
   */
  
  public static String predETypeOrProWord(CExample ex, boolean useCache, boolean useGold) {
	  if(useGold){
		  Mention m1 = ex.getM1(), m2 = ex.getM2();
		  Doc d = m1.getDoc();
		  String e1 =
		  m1.getType().equals("PRO") ? d.getWord(m1.getHeadLastWordNum())
                  : m1.getEntityType();
		  String e2 =
			  m2.getType().equals("PRO") ? d.getWord(m2.getHeadLastWordNum())
                  :  m2.getEntityType();
		  return e1 + "&&" + e2;  
	  }
	  else
		  return EntityTypeFeatures.predETypeOrProWord(ex,useCache);
	  }
}

