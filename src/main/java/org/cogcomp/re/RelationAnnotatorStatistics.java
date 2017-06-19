package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.main.AllTest;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationAnnotatorStatistics {

    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        int startCharOffset =
                Integer.parseInt(extentConstituent
                        .getAttribute(ACEReader.EntityHeadStartCharOffset));
        int endCharOffset =
                Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1;
        int startToken = textAnnotation.getTokenIdFromCharacterOffset(startCharOffset);
        int endToken = textAnnotation.getTokenIdFromCharacterOffset(endCharOffset);
        if (startToken >= 0 && endToken >= 0 && !(endToken - startToken < 0)) {
            Constituent cons =
                    new Constituent(extentConstituent.getLabel(), 1.0, viewName, textAnnotation,
                            startToken, endToken + 1);
            for (String attributeKey : extentConstituent.getAttributeKeys()) {
                cons.addAttribute(attributeKey, extentConstituent.getAttribute(attributeKey));
            }
            return cons;
        }
        return null;
    }

    public static void main (String[] args){
        try{
            ACEReader aceReader = new ACEReader("data/partition/eval/1", false);
            int total_count = 0;
            int total_match = 0;
            int mention_type_match = 0;
            int mention_subtype_match = 0;
            entity_type_classifier etc = new entity_type_classifier();
            entity_subtype_classifier esc = new entity_subtype_classifier();
            for (TextAnnotation ta : aceReader){
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                List<Constituent> predictedMentions = AllTest.MentionTest(ta);
                List<Constituent> predictedMentionWithTypes = new ArrayList<Constituent>();
                for (Constituent c : predictedMentions){
                    String entity_type = etc.discreteValue(c);
                    String entity_subtype = esc.discreteValue(c);
                    c.addAttribute("EntityType", entity_type);
                    c.addAttribute("EntitySubtype", entity_subtype);
                    predictedMentionWithTypes.add(c);
                    //System.out.println(getEntityHeadForConstituent(c, ta, "A").toString());
                }
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE).getConstituents()){
                    consMap.put(c,null);
                    for (Constituent pc : predictedMentionWithTypes){
                        Constituent ch = getEntityHeadForConstituent(c, ta, "TESTG");
                        Constituent pch = getEntityHeadForConstituent(pc, ta, "TESTP");
                        //if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
                        if ((ch.getStartSpan() >= pch.getEndSpan() || pch.getStartSpan() >= ch.getEndSpan()) == false){
                            consMap.put(c, pc);
                            if (!(ch.getStartSpan() == pch.getStartSpan()) || !(ch.getEndSpan() == pch.getEndSpan())){
                                System.out.println("gold: " + ch.toString());
                                System.out.println("predicted: " + pch.toString());
                            }
                            break;
                        }
                    }
                }
                List<Relation> goldRelations = ta.getView(ViewNames.MENTION_ACE).getRelations();
                total_count += goldRelations.size();
                for (Relation r : goldRelations){
                    if (consMap.get(r.getSource()) != null && consMap.get(r.getTarget()) != null){
                        total_match ++;
                        Constituent ps = consMap.get(r.getSource());
                        Constituent pt = consMap.get(r.getTarget());
                        if (ps.getAttribute("EntityType").equals(r.getSource().getAttribute("EntityType"))
                                && pt.getAttribute("EntityType").equals(r.getTarget().getAttribute("EntityType"))){
                            mention_type_match++;
                            if (ps.getAttribute("EntitySubtype").equals(r.getSource().getAttribute("EntitySubtype"))
                                    && pt.getAttribute("EntitySubtype").equals(r.getTarget().getAttribute("EntitySubtype"))){
                                mention_subtype_match ++;
                            }
                        }
                    }
                    else {
                        if (consMap.get(r.getSource()) == null){
                            //System.out.println(ta.getSentence(ta.getSentenceId(r.getSource())));
                            //System.out.println(getEntityHeadForConstituent(r.getSource(), ta, "A").toString());
                        }
                    }
                }
                //break;
            }
            System.out.println("Total count: " + total_count);
            System.out.println("Total match: " + total_match);
            System.out.println("Type match: " + mention_type_match);
            System.out.println("Subtype match: " + mention_subtype_match);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
