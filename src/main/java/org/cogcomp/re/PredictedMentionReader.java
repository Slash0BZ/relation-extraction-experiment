package org.cogcomp.re;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.lbj.coref.main.AllTest;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
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
        return ACEMentionReader.getEntityHeadForConstituent(extentConstituent, textAnnotation, viewName);
    }

    public boolean skipTypes(String type){
        if (type.equals("Ownership") || type.equals("Ownership_OP")
                || type.equals("Student-Alum") || type.equals("Student-Alum_OP")
                || type.equals("Artifact") || type.equals("Artifact_OP")){
            return true;
        }
        return false;
    }

    public PredictedMentionReader(String path){
        relations = new ArrayList<Relation>();
        try {
            ACEReader aceReader = new ACEReader(path, false);
            entity_type_classifier etc = new entity_type_classifier("models/entity_type_classifier.lc", "models/entity_type_classifier.lex");
            entity_subtype_classifier esc = new entity_subtype_classifier("models/entity_subtype_classifier.lc", "models/entity_subtype_classifier.lex");
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
                //annotator.addView(ta);
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
                    //System.out.println(c.getAttribute("EntityType") + " " + c.getAttribute("EntitySubtype") + " " + c.getAttribute("EntityMentionType"));
                }
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                List<Relation> gold_relations = entityView.getRelations();
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE).getConstituents()){
                    consMap.put(c,null);
                    for (Constituent pc : predictedView.getConstituents()){
                        Constituent ch = getEntityHeadForConstituent(c, ta, "TESTG");
                        Constituent pch = getEntityHeadForConstituent(pc, ta, "TESTP");
                        //if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
                        if ((ch.getStartSpan() >= pch.getEndSpan() || pch.getStartSpan() >= ch.getEndSpan()) == false){
                            pc.addAttribute("EntityType", c.getAttribute("EntityType"));
                            pc.addAttribute("EntitySubtype", c.getAttribute("EntitySubtype"));
                            consMap.put(c, pc);
                            break;
                        }
                    }
                }

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
                                /*
                                if (gold_source_head.getStartSpan() == predicted_source_head.getStartSpan()
                                        && gold_source_head.getEndSpan() == predicted_source_head.getEndSpan()
                                        && gold_target_head.getStartSpan() == predicted_target_head.getStartSpan()
                                        && gold_target_head.getEndSpan() == predicted_target_head.getEndSpan()){
                                        */
                                if (consMap.get(r.getSource()) == null || consMap.get(r.getTarget()) == null){
                                    continue;
                                }
                                if (consMap.get(r.getSource()).equals(source) && consMap.get(r.getTarget()).equals(target)){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), source, target, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    newRelation.addAttribute("IsGoldRelation", "True");
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation newOpRelation = new Relation(opTypeName, target, source, 1.0f);
                                    newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                    newOpRelation.addAttribute("IsGoldRelation", "True");
                                    relations.add(newRelation);
                                    relations.add(newOpRelation);
                                    found_tag = true;
                                    break;
                                }
                                /*
                                else if (gold_target_head.getStartSpan() == predicted_source_head.getStartSpan()
                                        && gold_target_head.getEndSpan() == predicted_source_head.getEndSpan()
                                        && gold_source_head.getStartSpan() == predicted_target_head.getStartSpan()
                                        && gold_source_head.getEndSpan() == predicted_target_head.getEndSpan()){
                                */
                                else if (consMap.get(r.getTarget()).equals(source) && consMap.get(r.getSource()).equals(target)){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), target, source, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    newRelation.addAttribute("IsGoldRelation", "True");
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation newOpRelation = new Relation(opTypeName, source, target, 1.0f);
                                    newOpRelation.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    newOpRelation.addAttribute("RelationSubtype", opTypeName);
                                    newOpRelation.addAttribute("IsGoldRelation", "True");
                                    relations.add(newRelation);
                                    relations.add(newOpRelation);
                                    found_tag = true;
                                    break;
                                }
                            }
                            if (!found_tag){
                                Relation newRelation = new Relation("NOT_RELATED", source, target, 1.0f);
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation.addAttribute("IsGoldRelation", "False");
                                Relation newRelationOp = new Relation("NOT_RELATED", target, source, 1.0f);
                                newRelationOp.addAttribute("RelationType", "NOT_RELATED");
                                newRelationOp.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelationOp.addAttribute("IsGoldRelation", "False");
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
