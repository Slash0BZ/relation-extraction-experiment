package org.cogcomp.re;
import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.lbj.coref.main.AllTest;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
//import edu.illinois.cs.cogcomp.pos.*;
import edu.illinois.cs.cogcomp.edison.annotators.*;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;

import java.util.*;
import java.lang.*;

public class PredictedMentionReader implements Parser{
    private List<Relation> relations;
    private int currentRelationIndex;
    public String getOppoName(String name){
        if (name.equals("Family") || name.equals("Lasting-Personal") || name.equals("Near") || name.equals("Business")){
            return name;
        }
        return name + "_OP";
    }
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

    public boolean skipTypes(String type){
        if (type.equals("Ownership") || type.equals("Ownership_OP")
                || type.equals("Student-Alum") || type.equals("Student-Alum_OP")
                || type.equals("Artifact") || type.equals("Artifact_OP")){
            return true;
        }
        return false;
    }
    /*
    public PredictedMentionReader(String path){
        relations = new ArrayList<Relation>();
        try {
            ACEReader aceReader = new ACEReader(path, false);
            PredictedMentionAnnotator predictedMentionAnnotator = new PredictedMentionAnnotator();
            int total_gold_relation_count = 0;
            int total_predicted_relation_count = 0;
            int total_entity_type_match = 0;
            int total_entity_subtype_match = 0;
            int total_entity_mention_match = 0;
            int total_gold_mention_count = 0;
            int total_predicted_mention_count = 0;
            int total_correct_mention_count = 0;
            int total_correct_mention_with_type = 0;
            for (TextAnnotation ta : aceReader){
                predictedMentionAnnotator.addView(ta);
                View predictedView = ta.getView("PREDICTED_MENTION_EXTRACTOR");
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                List<Constituent> gold_mentions = entityView.getConstituents();
                List<Constituent> predicted_mentions = predictedView.getConstituents();
                for (Constituent gm : gold_mentions){
                    Constituent gm_head = getEntityHeadForConstituent(gm, ta, "TestGoldHeads");
                    for (Constituent pm : predicted_mentions){
                        Constituent pm_head = getEntityHeadForConstituent(pm, ta, "TestPredictedHeads");
                        if (pm_head.getStartSpan() == gm_head.getStartSpan() && pm_head.getEndSpan() == gm_head.getEndSpan()){
                            total_correct_mention_count ++;
                            if (pm.getAttribute("EntityType").equals(gm.getAttribute("EntityType"))){
                                total_correct_mention_with_type ++;
                            }
                            break;
                        }
                    }
                }
                total_gold_mention_count += gold_mentions.size();
                total_predicted_mention_count += predicted_mentions.size();
                List<Relation> gold_relations = entityView.getRelations();
                total_gold_relation_count += gold_relations.size();
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence = ta.getSentence(i);
                    List<Constituent> in_cur_sentence = predictedView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < in_cur_sentence.size(); j++){
                        for (int k = 0; k < in_cur_sentence.size(); k++){
                            if (j == k){
                                continue;
                            }
                            Constituent source = in_cur_sentence.get(j);
                            Constituent target = in_cur_sentence.get(k);
                            boolean found_tag = false;
                            for (Relation r : gold_relations){
                                Constituent gold_source_head = getEntityHeadForConstituent(r.getSource(), ta, "EntityGoldHeads");
                                Constituent gold_target_head = getEntityHeadForConstituent(r.getTarget(), ta, "EntityGoldHeads");
                                Constituent predicted_source_head = getEntityHeadForConstituent(source, ta, "EntityPredictedHeads");
                                Constituent predicted_target_head = getEntityHeadForConstituent(target, ta, "EntityPredictedHeads");
                                if (gold_source_head.getStartSpan() == predicted_source_head.getStartSpan()
                                        && gold_source_head.getEndSpan() == predicted_source_head.getEndSpan()
                                        && gold_target_head.getStartSpan() == predicted_target_head.getStartSpan()
                                        && gold_target_head.getEndSpan() == predicted_target_head.getEndSpan()){

                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), source, target, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    Relation newOpRelation = new Relation(r.getAttribute("RelationSubtype") + "_OP", target, source, 1.0f);
                                    newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newOpRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype") + "_OP");
                                    relations.add(newRelation);
                                    relations.add(newOpRelation);

                                    if (source.getAttribute("EntityType").equals(r.getSource().getAttribute("EntityType"))
                                            && target.getAttribute("EntityType").equals(r.getTarget().getAttribute("EntityType"))){
                                        total_entity_type_match ++;
                                        if (source.getAttribute("EntitySubtype").equals(r.getSource().getAttribute("EntitySubtype"))
                                                && target.getAttribute("EntitySubtype").equals(r.getTarget().getAttribute("EntitySubtype"))){
                                            total_entity_subtype_match ++;
                                            if (source.getAttribute("EntityMentionType").equals(r.getSource().getAttribute("EntityMentionType"))
                                                    && target.getAttribute("EntityMentionType").equals(r.getTarget().getAttribute("EntityMentionType"))) {
                                                total_entity_mention_match ++;
                                            }
                                        }
                                    }
                                    total_predicted_relation_count ++;
                                    found_tag = true;
                                    break;
                                }
                            }
                            if (!found_tag){
                                Relation newRelation = new Relation("NOT_RELATED", source, target, 1.0f);
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                //relations.add(newRelation);
                            }
                        }
                    }
                }
            }
            System.out.println("Total gold: " + total_gold_relation_count);
            System.out.println("Total predicted: " + total_predicted_relation_count);
            System.out.println("Total type match: " + total_entity_type_match);
            System.out.println("Total subtype match: " + total_entity_subtype_match);
            System.out.println("Total triple match: " + total_entity_mention_match);
            System.out.println("Total gold mentions: " + total_gold_mention_count);
            System.out.println("Total predicted mentions: " + total_predicted_mention_count);
            System.out.println("Total correct mentions: " + total_correct_mention_count);
            System.out.println("Total correct mentions with types: " + total_correct_mention_with_type);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    */

    public PredictedMentionReader(String path){
        relations = new ArrayList<Relation>();
        try {
            ACEReader aceReader = new ACEReader(path, false);
            entity_type_classifier etc = new entity_type_classifier();
            entity_subtype_classifier esc = new entity_subtype_classifier();
            POSAnnotator pos_annotator = new POSAnnotator();
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://localhost", "8080");
            annotator.setViews(ViewNames.DEPENDENCY, ViewNames.SHALLOW_PARSE);
            BrownClusterViewGenerator bc_annotator = new BrownClusterViewGenerator("c1000", BrownClusterViewGenerator.file1000);
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            for (TextAnnotation ta : aceReader){
                ta.addView(pos_annotator);
                chunker.addView(ta);
                bc_annotator.addView(ta);
                annotator.addView(ta);
                View predictedView = new SpanLabelView("RELATION_EXTRACTION_RELATIONS", RelationAnnotator.class.getCanonicalName(), ta, 1.0f, true);
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                List<Constituent> gold_mentions = entityView.getConstituents();
                List<Constituent> predicted_mentions = AllTest.MentionTest(ta);
                for (Constituent c : predicted_mentions){
                    String entity_type = etc.discreteValue(c);
                    String entity_subtype = esc.discreteValue(c);
                    c.addAttribute("EntityType", entity_type);
                    c.addAttribute("EntitySubtype", entity_subtype);
                    predictedView.addConstituent(c);
                }
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                List<Relation> gold_relations = entityView.getRelations();
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE).getConstituents()){
                    consMap.put(c,null);
                    for (Constituent pc : predictedView.getConstituents()){
                        Constituent ch = getEntityHeadForConstituent(c, ta, "TESTG");
                        Constituent pch = getEntityHeadForConstituent(pc, ta, "TESTP");
                        if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
                            consMap.put(c, pc);
                            break;
                        }
                    }
                }
                /*
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence = ta.getSentence(i);
                    List<Constituent> in_cur_sentence = entityView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < in_cur_sentence.size(); j++){
                        for (int k = j + 1; k < in_cur_sentence.size(); k++){
                            Constituent source = in_cur_sentence.get(j);
                            Constituent target = in_cur_sentence.get(k);
                            Constituent source_predicted = consMap.get(source);
                            Constituent target_predicted = consMap.get(target);
                            if (source_predicted != null && target_predicted != null){
                                boolean found = false;
                                for (Relation r : gold_relations){
                                    if (r.getSource().equals(source) && r.getTarget().equals(target)){
                                        Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), source_predicted, target_predicted, 1.0f);
                                        newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                        newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                        String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                        Relation newOpRelation = new Relation(opTypeName, target_predicted, source_predicted, 1.0f);
                                        newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                        newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                        relations.add(newRelation);
                                        relations.add(newOpRelation);
                                        found = true;
                                        break;
                                    }
                                    else if (r.getSource().equals(target) && r.getTarget().equals(source)){
                                        Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), target_predicted, source_predicted, 1.0f);
                                        newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                        newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                        String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                        Relation newOpRelation = new Relation(opTypeName, source_predicted, target_predicted, 1.0f);
                                        newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                        newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                        relations.add(newRelation);
                                        relations.add(newOpRelation);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found){
                                    Relation newRelation = new Relation("NOT_RELATED", source_predicted, target_predicted, 1.0f);
                                    newRelation.addAttribute("RelationType", "NOT_RELATED");
                                    newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                    relations.add(newRelation);
                                    Relation newRelationOp = new Relation("NOT_RELATED", target_predicted, source_predicted, 1.0f);
                                    newRelationOp.addAttribute("RelationType", "NOT_RELATED");
                                    newRelationOp.addAttribute("RelationSubtype", "NOT_RELATED");
                                    relations.add(newRelationOp);
                                }
                            }
                        }
                    }
                }
                */
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence = ta.getSentence(i);
                    List<Constituent> in_cur_sentence = predictedView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < in_cur_sentence.size(); j++){
                        for (int k = j + 1; k < in_cur_sentence.size(); k++){
                            Constituent source = in_cur_sentence.get(j);
                            Constituent target = in_cur_sentence.get(k);
                            boolean found_tag = false;
                            for (Relation r : gold_relations){
                                Constituent gold_source_head = getEntityHeadForConstituent(r.getSource(), ta, "EntityGoldHeads");
                                Constituent gold_target_head = getEntityHeadForConstituent(r.getTarget(), ta, "EntityGoldHeads");
                                Constituent predicted_source_head = getEntityHeadForConstituent(source, ta, "EntityPredictedHeads");
                                Constituent predicted_target_head = getEntityHeadForConstituent(target, ta, "EntityPredictedHeads");
                                if (gold_source_head.getStartSpan() == predicted_source_head.getStartSpan()
                                        && gold_source_head.getEndSpan() == predicted_source_head.getEndSpan()
                                        && gold_target_head.getStartSpan() == predicted_target_head.getStartSpan()
                                        && gold_target_head.getEndSpan() == predicted_target_head.getEndSpan()){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), source, target, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation newOpRelation = new Relation(opTypeName, target, source, 1.0f);
                                    newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                    relations.add(newRelation);
                                    //relations.add(newOpRelation);
                                    found_tag = true;
                                    break;
                                }
                                else if (gold_target_head.getStartSpan() == predicted_source_head.getStartSpan()
                                        && gold_target_head.getEndSpan() == predicted_source_head.getEndSpan()
                                        && gold_source_head.getStartSpan() == predicted_target_head.getStartSpan()
                                        && gold_source_head.getEndSpan() == predicted_target_head.getEndSpan()){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), target, source, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation newOpRelation = new Relation(opTypeName, source, target, 1.0f);
                                    newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                    relations.add(newRelation);
                                    //relations.add(newOpRelation);
                                    found_tag = true;
                                    break;
                                }
                            }
                            if (!found_tag){
                                Relation newRelation = new Relation("NOT_RELATED", source, target, 1.0f);
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                Relation newRelationOp = new Relation("NOT_RELATED", target, source, 1.0f);
                                newRelationOp.addAttribute("RelationType", "NOT_RELATED");
                                newRelationOp.addAttribute("RelationSubtype", "NOT_RELATED");
                                relations.add(newRelation);
                                relations.add(newRelationOp);
                            }
                        }
                    }
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){

    }
    public Object next(){
        if (currentRelationIndex == relations.size()) {
            return null;
        } else {
            currentRelationIndex++;
            return relations.get(currentRelationIndex - 1);
        }
    }

    public void reset(){
        currentRelationIndex = 0;
    }
}
