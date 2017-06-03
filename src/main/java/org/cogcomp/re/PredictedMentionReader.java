package org.cogcomp.re;
import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
//import edu.illinois.cs.cogcomp.pos.*;
import edu.illinois.cs.cogcomp.edison.annotators.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.lang.*;

public class PredictedMentionReader implements Parser{
    private List<Relation> relations;
    private int currentRelationIndex;
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
