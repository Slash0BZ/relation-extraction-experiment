package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.SysexMessage;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocAPF;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocCoNLL;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocPlainText;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;



/**
  * A collection of features related to the entity type of a mention
  * or the relationship between the entity types of mentions.
  * An entity type is one of "PER" (person), "ORG" (organization),
  * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
  * "FAC" (facility), or "WEA" (weapon).
 */
public class EntityTypeFeatures {

 static String[] NETypeList = new String[]{"CARDINAL","DATE","EVENT","FAC","GPE", "LAW","LOC","MONEY","NORP","ORDINAL", "ORG","PERCENT","PER","PRODUCT", "QUANTITY","TIME","WORK_OF_ART","LANGUAGE"};
 static Map<String, Set<String>> NomNERMap;
 
  private static Map<String,String> m_wneTypeBetterCache = null;

  /** No need to construct collection of static features. */
  protected EntityTypeFeatures() {
  }

  /**
    * Determines whether the entity types of two mentions match,
    * as determined by {@link #getEType}.
    * No caching is used.
    * @param ex The example containing the mentions in question.
    * @return "true", "false", or "unknown".
   */
  public static String doETypesMatch(CExample ex) {
    boolean useCache = false;
    return doETypesMatch(ex, useCache);
  }
  static boolean initNomNerMapFlag = false;
  /*private static void initNomNERMap(){
	  if(initNomNerMapFlag)
		  return;
	  initNomNerMapFlag = true;
	  NomNERMap = new HashMap<String, Set<String>>();
	  for(String s: NETypeList){
		List<String> lines = (new myIO()).readLines(Parameters.pathToNERNOMList+ s + "_commonNoun");
		Set<String> nomList = new HashSet<String>();
		for (String line : lines) {
			if (line.length() <= 0 || line.startsWith("#"))
				continue;
			String text = line.split(" ")[0];
			if(text.equals("and") || text.equals("'s"))
				continue;
			int count = Integer.parseInt(line.split(" ")[2]);
			if(count >=10)
				nomList.add(text);
		}
		NomNERMap.put(s, nomList);
	  }
	  //for(String s:NomNERMap.keySet()){
	//	  System.out.println(s);
		//  for(String m: NomNERMap.get(s))
	//		  System.out.println(m + ":"+s);
	 // }

  }*/
/*public static boolean doNomNERMatch(String head, String NER){
	  if(!initNomNerMapFlag)
			 initNomNERMap();
	  try{
	 return NomNERMap.get(NER).contains(head.toLowerCase());
	  }
	  catch (Exception c){
		  return false;
	  }
 }*/
  /**
    * Determines whether the entity types of two mentions match,
    * as determined by {@link #getEType}.
    * @param ex The example containing the mentions in question.
    * @param useCache Whether caching should be done.
    * @return "true", "false", or "unknown".
   */
  public static String doETypesMatch(CExample ex, boolean useCache) {
/*	  if(!initNomNerMapFlag)
			 initNomNERMap();*/
    String t1 = getEType(ex.getM1(), useCache);
    String t2 = getEType(ex.getM2(), useCache);
    
    String h = ex.getM2().getHead().getText().toLowerCase();
    
    //They, our, we, can refer to an organization
    if ((h.equals("our") || h.equals("we")|| (h.equals("us"))
        || h.equals("they") || h.equals("their")|| h.equals("them"))
        && ((t1.equals("PER")||t1.equals("GPE"))||t1.equals("ORG"))) 
    return "unknown";
    //TODO: This could be improved by returning "NON-x".
    if (t1.equals("unknown") || t2.equals("unknown"))
      return "unknown";
    else if (t1.equals(t2))
      return "true";
    else if((t1.equals("ORG") &&t2.equals("GPE") )||(t2.equals("ORG") &&t1.equals("GPE") ) )
    	return "unknown";
    
/*	if(Parameters.useFixNer){
	 if(ex.getM2().getType().equals("NAM") &&
    		EntityTypeFeatures.doNomNERMatch(ex.getM1().getHead().getText(), ex.getM2().getEntityType()))
			return "true";
		else if(ex.getM1().getType().equals("NAM") &&
				EntityTypeFeatures.doNomNERMatch(ex.getM2().getHead().getText(), ex.getM1().getEntityType()))
			return "true";
	}*/

   return "false";
  }

  /**
    * Determines the entity type of the mention {@code m}.
    * If the {@code m_predwnEType} caches the predicted entity type,
    * return that value; otherwise, depending on the mention type,
    * delegate to the appropriate method.  In the case of "PRE", determine
    * whether a proper noun based on case (if the document is case sensitive)
    * or else try considering it as both a "NAM" and "NOM" and if only
    * one returns non-"unknown", return that; finally, if both return a value,
    * choose the value determined assuming it is a "NAM".
    * Caching will not be done.
    * @param m The mention whose entity type should be determined.
    * @return The entity type: one of "PER" (person), "ORG" (organization),
    * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
    * "FAC" (facility), or "WEA" (weapon), or "unknown".
   */
  public static String getEType(Mention m) {
    boolean useCache = false;
    return getEType(m, useCache);
  }

  /**
    * Determines the entity type of the mention {@code m}.
    * If the {@link Mention#m_predwnEType} caches the predicted entity type,
    * return that value; otherwise, depending on the mention type,
    * delegate to the appropriate method.  In the case of "PRE", determine
    * whether a proper noun based on case (if the document is case sensitive)
    * or else try considering it as both a "NAM" and "NOM" and if only
    * one returns non-"unknown", return that; finally, if both return a value,
    * choose the value determined assuming it is a "NAM".
    * @param m The mention whose entity type should be determined.
    * @param useCache Whether caching should be done.
    * @return The entity type: one of "PER" (person), "ORG" (organization),
    * "GPE" (Geo-Political Entity), "LOC" (location), "VEH" (vehicle),
    * "FAC" (facility), or "WEA" (weapon), or "unknown".
   */
  
  public static String getEType(Mention m, boolean useCache) {
/*	  EntityDataBaseExp.initMapEntityCluster();
	  String index = m.getDoc().getDocID()+"_"+m.getExtentFirstWordNum() + "_"+ m.getExtentLastWordNum();
	  if(EntityDataBaseExp.MapFixNER.containsKey(index)){
		  return EntityDataBaseExp.MapFixNER.get(index);
	  }*/
	  if(Parameters.useGoldEntityTypes)
		  return m.getEntityType();
	  String key = m.getHead().getText();
	  if(m.getDoc() instanceof  DocCoNLL){
		  if((key.toLowerCase().equals("one") && m.getSurfaceText().toLowerCase().startsWith("one of "))||
				  (key.toLowerCase().equals("both")&& m.getSurfaceText().toLowerCase().startsWith("both of"))||
				  (key.toLowerCase().equals("all")&& m.getSurfaceText().toLowerCase().startsWith("all of"))){
			  Constituent c = new Constituent("headRep", "none", m.getDoc().getTextAnnotation(), m.getExtentFirstWordNum()+2, m.getExtentLastWordNum()+1);
			  key = WordHelpers.getWord(m.getDoc().getTextAnnotation(), DocTextAnnotation.getRobustHeadWordPosition(c));
			  m = DocTextAnnotation.genMention(-1, c, m.getDoc(), false);
		  }
		  if((key.toLowerCase().equals("both") && m.getHeadLastWordNum() != m.getExtentLastWordNum())){
			  Constituent c = new Constituent("headRep", "none", m.getDoc().getTextAnnotation(), m.getHeadLastWordNum()+1, m.getExtentLastWordNum()+1);
			  key = WordHelpers.getWord(m.getDoc().getTextAnnotation(), DocTextAnnotation.getRobustHeadWordPosition(c));
			  m = DocTextAnnotation.genMention(-1, c, m.getDoc(), false);
		  }
		  if((key.equals("and") || key.equals("or")) && (m.getHeadLastWordNum() != m.getExtentLastWordNum()) ){
			  Constituent c = new Constituent("headRep", "none", m.getDoc().getTextAnnotation(), m.getHeadLastWordNum()+1, m.getExtentLastWordNum()+1);
			  key = WordHelpers.getWord(m.getDoc().getTextAnnotation(), DocTextAnnotation.getRobustHeadWordPosition(c));
			  m = DocTextAnnotation.genMention(-1, c, m.getDoc(), false);
		  }
	  }

    if (m.getDoc() instanceof  DocCoNLL && !(m.getEntityType().equals("unknown")||m.getEntityType().equals("NONE"))){
    	return m.getEntityType();
    }
    if (useCache) {
      //If result depends on extent, change key.
      if (m_wneTypeBetterCache == null)
        m_wneTypeBetterCache = new HashMap<String,String>();
      else if (m_wneTypeBetterCache.containsKey(key))
        return m_wneTypeBetterCache.get(key);
    }

    // Need to check if the following code will change the results
      /*
    if(m.getDoc() instanceof DocAPF || m.getDoc() instanceof DocPlainText)
    {
    	TextAnnotation ta = m.getDoc().getTextAnnotation();
   		//SpanLabelView view = (SpanLabelView) ta.getView("GAZETTEERGazetteers");
        SpanLabelView view = (SpanLabelView) ta.getView(ViewNames.GAZETTEER);
    	for(Constituent  c:view.getConstituentsCoveringToken(m.getHeadFirstWordNum())){
    		if(c.getLabel().equals("Government.gz"))
    			return "GPE";
    		if(c.getLabel().equals("People.gz"))
    			return "PER";
    		if(c.getLabel().equals("Person.gz"))
    			return "PER";
    		if(c.getLabel().equals("Organizations.gz"))
    			return "ORG";
    		if(c.getLabel().equals("Locations.gz"))
    			return "LOC";
    		if(c.getLabel().equals("Weapons.gz"))
    			return "WEA";
    		if(c.getLabel().equals("Vehicles.gz"))
    			return "VEH";
    	}
        System.out.println("getEtype 2");
    	if(myAux.isAfterWordEqual(m, new String[]{"who"}, false))
    		return "PER";
    }
    */

    
    String result = "unknown";
    if (!m.m_predwnEType.equals("unknown")) {
      result = m.m_predwnEType;
    } else if (m.getType().equals("NAM")) {
      result = getNameEType(m);
    } else if (m.getType().equals("PRE")) {
      if (m.getDoc().isCaseSensitive()) {
        char h1 = key.charAt(0);
        if (Character.isUpperCase(h1))
          result = getNameEType(m);
        else
          result = getNominalEType(m);
      } else {
        String wnNAM = getNameEType(m);
        String wnNOM = getNominalEType(m);
        if ( wnNAM.equals("unknown") && !wnNOM.equals("unknown") ) {
          result = wnNOM;
        } else {
          result = wnNAM;
        }
      }
    } else if (m.getType().equals("NOM")) {
      result = getNominalEType(m);
    } else if (m.getType().equals("PRO")) {
      result = getPronounEType(m);
    } else {
      result = "unknown";
    }

    if (useCache) {
      m_wneTypeBetterCache.put(key, result);
    }
    return result;
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
    String h = m.getHead().getText().toLowerCase();
    String[] words = h.split("\\s");
    String lastWord = words[words.length-1];
    String eType = "unknown";
    if (m.getDoc() instanceof  DocCoNLL && !m.getEntityType().equals(Constants.NER_UNKNOWN)){
    	return m.getEntityType();
    }
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
    return eType;
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
	  if(m.getDoc() instanceof DocCoNLL)
		  return getNominalETypeForOntonte(m);
    String h = m.getHead().getText().toLowerCase().replace("vice ", "");
    //Order matters because e.g. political unit is-a group.
    // Check Synonyms first
    if (WordNetTools.getWN().areSynonyms(h, "attraction"))
    	return "LOC";
    if (WordNetTools.getWN().areSynonyms(h, "company"))
        return "ORG";
    else if (Parameters.useFixNer &&WordNetTools.getWN().areSynonyms(h, "group"))
        return "ORG";
    else if (WordNetTools.getWN().areSynonyms(h, "person"))
        return "PER";
    else if (WordNetTools.getWN().areSynonyms(h, "general"))
        return "PER";
    else if (WordNetTools.getWN().areSynonyms(h, "city"))
    	return "GPE";
    else if (WordNetTools.getWN().areSynonyms(h, "country"))
    	return "GPE";
    else if (WordNetTools.getWN().areSynonyms(h, "government"))
    	return "GPE";
    else if (WordNetTools.getWN().areSynonyms(h, "province"))
		return "GPE";
    else if (WordNetTools.getWN().areSynonyms(h, "political unit"))
        return "GPE";
      else if (WordNetTools.getWN().areSynonyms(h, "location"))
        return "LOC"; //TODO?
      else if (WordNetTools.getWN().areSynonyms(h, "organization"))
    	  return "LOC"; //TODO?
      else if (WordNetTools.getWN().areSynonyms(h, "weapon"))
        return "WEA";
      else if (WordNetTools.getWN().areSynonyms(h, "vehicle"))
        return "VEH";
      else if (WordNetTools.getWN().areSynonyms(h, "industrial plant")) //TODO: Better?
        return "FAC";
      else if (WordNetTools.getWN().areSynonyms(h, "facility")) //TODO: Better?
        return "FAC";
    else if (WordNetTools.getWN().areHypernyms(h, "person"))
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

  public static String getNominalETypeForOntonte(Mention m) {
	    String h = m.getHead().getText().toLowerCase().replace("vice ", "");
	    //Order matters because e.g. political unit is-a group.
		//xx
	    if(Parameters.useFixNer){
	    if(h.equals("men")||h.equals("teacher")||h.equals("teachers")||h.equals("people")||h.equals("children"))
	    	return "PER";
	    if(h.equals("gun")||h.equals("guns"))
	    	return "PRODUCT";
	    if(h.equals("issue")||h.equals("issues"))
	    	return "EVENT";
	    else if (WordNetTools.getWN().areSynonyms(h, "company"))
	        return "ORG";
	    else if (WordNetTools.getWN().areSynonyms(h, "group"))
	        return "ORG";
	    else if (WordNetTools.getWN().areSynonyms(h, "producer"))
	        return "PER";
	    else if (WordNetTools.getWN().areSynonyms(h, "person"))
	        return "PER";
	    else if (WordNetTools.getWN().areSynonyms(h, "general"))
	        return "PER";
	    else if (WordNetTools.getWN().areSynonyms(h, "city"))
	    	return "GPE";
	    else if (WordNetTools.getWN().areSynonyms(h, "country"))
	    	return "GPE";
	    else if (WordNetTools.getWN().areSynonyms(h, "government"))
	    	return "GPE";
	    else if (WordNetTools.getWN().areSynonyms(h, "province"))
			return "GPE";
	    else if (WordNetTools.getWN().areSynonyms(h, "political unit"))
	        return "GPE";
	      else if (WordNetTools.getWN().areSynonyms(h, "location"))
	        return "LOC"; //TODO?
	      else if (WordNetTools.getWN().areSynonyms(h, "organization"))
	    	  return "LOC"; //TODO?
	      else if (WordNetTools.getWN().areSynonyms(h, "weapon"))
	        return "PRODUCT";
	      else if (WordNetTools.getWN().areSynonyms(h, "vehicle"))
	        return "VEH";
	      else if (WordNetTools.getWN().areSynonyms(h, "industrial plant")) //TODO: Better?
	        return "FAC";
	      else if (WordNetTools.getWN().areSynonyms(h, "facility")) //TODO: Better?
	        return "FAC";
	    }
	    if (WordNetTools.getWN().areHypernyms(h, "person"))
		      return "PER";
	    else if (WordNetTools.getWN().areHypernyms(h, "political unit"))
	      return "GPE";
	    else if (WordNetTools.getWN().areHypernyms(h, "location"))
	      return "LOC"; //TODO?
	    
	    else if (WordNetTools.getWN().areHypernyms(h, "organization"))
	      return "ORG";
	    else if (WordNetTools.getWN().areHypernyms(h, "weapon"))
	      return "PRODUCT";
	    else if (WordNetTools.getWN().areHypernyms(h, "vehicle"))
	      return "PRODUCT";
	    else if (WordNetTools.getWN().areHypernyms(h, "food"))
		  return "PRODUCT";
	    else if (WordNetTools.getWN().areHypernyms(h, "movie"))
		      return "WORK_OF_ART";
	    else if (WordNetTools.getWN().areHypernyms(h, "film"))
		      return "WORK_OF_ART";
	    else if (WordNetTools.getWN().areHypernyms(h, "book"))
		      return "WORK_OF_ART";
	    else if (WordNetTools.getWN().areHypernyms(h, "revolusion"))
		      return "EVENT";
	    else if (WordNetTools.getWN().areHypernyms(h, "war"))
		      return "EVENT";
	    else if (WordNetTools.getWN().areHypernyms(h, "industrial plant")) //TODO: Better?
	      return "FAC";
	    else if (WordNetTools.getWN().areHypernyms(h, "building")) //TODO: Better?
		      return "FAC";
	    else if (WordNetTools.getWN().areHypernyms(h, "facility")) //TODO: Better?
	      return "FAC";
	    else if (WordNetTools.getWN().areHypernyms(h, "language")) //TODO: Better?
		      return "LANGUAGE";
	    else if (WordNetTools.getWN().areHypernyms(h, "money")) //TODO: Better?
		      return "MONEY";
	    else if (WordNetTools.getWN().areHypernyms(h, "event"))
		      return "EVENT";
	    else if (WordNetTools.getWN().areHypernyms(h, "product"))
		      return "PRODUCT";
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
        || h.equals("herself")|| h.equals("i")|| h.equals("you")
        || h.equals("your")|| h.equals("me") || h.equals("my")
        || h.equals("our") || h.equals("who")||h.equals("yourself")
        || h.equals("yourselves") || h.equals("someone") || h.equals("somebody")
        || h.equals("myself")|| h.equals("we")|| h.equals("us")
        //|| h.equals("they") || h.equals("their")|| h.equals("them")
        // They, our, we, can refer to an organization
       ) {
      return "PER";
       }
    else if(h.equals("where"))
    	return "GPE";
    return "unknown";
  }
   /*
  public static String getPronounEType(Mention m) {
    String h = m.getHead().getText().toLowerCase();
    //TODO: This could be improved by returning "NON-x".
    if(PronounResolutionFeatures.proIsHuman(m) == 'H') {*/
    /*if (h.equals("he") || h.equals("him") || h.equals("his")
        || h.equals("himself")	   
        || h.equals("she") || h.equals("her") || h.equals("hers")
        || h.equals("herself")
       ) {*/
/*      return "PER";
       }
    if(h.equals("where"))
    	return "LOC";
    return "unknown";
    
  }*/

  /* ETypes and Tokens */

  /**
    * Gets the predicted entity type, or the pronoun word if a pronoun.
    * Uses the predicted entity type computed using {@link #getEType}.
    * Uses the default mention types, which may be gold.
    * Does not cache results.
    * @param ex The example.
    * @return For each mention, the predicted entity type,
    * or, if the mention is a pronoun, the token,
    * conjoined by {@literal "&&"}.
   */
  public static String predETypeOrProWord(CExample ex) {
    boolean useCache = false;
    return predETypeOrProWord(ex, useCache);
  }

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
  public static String predETypeOrProWord(CExample ex, boolean useCache) {
    Mention m1 = ex.getM1(), m2 = ex.getM2();
    Doc d = ex.getDoc();
    String e1 =
      m1.getType().equals("PRO") ? d.getWord(m1.getHeadLastWordNum())
                                 : getEType(ex.getM1(), useCache);
    String e2 =
      m2.getType().equals("PRO") ? d.getWord(m2.getHeadLastWordNum())
                                 : getEType(ex.getM2(), useCache);
    return e1 + "&&" + e2;
  }
}

