package org.cogcomp.re;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import edu.illinois.cs.cogcomp.edison.annotators.*;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.md.MentionAnnotator;

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
            POSAnnotator pos_annotator = new POSAnnotator();
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            BrownClusterViewGenerator bc_annotator = new BrownClusterViewGenerator("c1000", BrownClusterViewGenerator.file1000);
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            NERAnnotator co = new NERAnnotator(ViewNames.NER_CONLL);
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            for (TextAnnotation ta : aceReader){
                ta.addView(pos_annotator);
                //chunker.addView(ta);
                bc_annotator.addView(ta);
                //co.addView(ta);
                mentionAnnotator.addView(ta);

                View goldView = ta.getView(ViewNames.MENTION_ACE);
                View predictedView = ta.getView(ViewNames.MENTION);
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                for (Constituent c : goldView.getConstituents()){
                    consMap.put(c,null);
                    Constituent ch = getEntityHeadForConstituent(c, ta, "TESTG");
                    for (Constituent pc : predictedView.getConstituents()){
                        Constituent pch = MentionAnnotator.getHeadConstituent(pc, "TESTP");
                        if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
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
                            for (Relation r : goldView.getRelations()){

                                if (consMap.get(r.getSource()) == null || consMap.get(r.getTarget()) == null){
                                    continue;
                                }

                                Constituent gsh = ACEReader.getEntityHeadForConstituent(r.getSource(), ta, "A");
                                Constituent gth = ACEReader.getEntityHeadForConstituent(r.getTarget(), ta, "A");
                                Constituent psh = MentionAnnotator.getHeadConstituent(source, "B");
                                Constituent pth = MentionAnnotator.getHeadConstituent(target, "B");

                                if (gsh.getStartSpan() == psh.getStartSpan() && gsh.getEndSpan() == psh.getEndSpan()
                                        && gth.getStartSpan() == pth.getStartSpan() && gth.getEndSpan() == pth.getEndSpan()){
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
                                if (gsh.getStartSpan() == pth.getStartSpan() && gsh.getEndSpan() == pth.getEndSpan()
                                        && gth.getStartSpan() == psh.getStartSpan() && gth.getEndSpan() == psh.getEndSpan()){
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
