package org.cogcomp.re;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
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

/*
 * This is the Relation/Entity reader class for RE
 * It utilizes ACEReader class included in cogcomp packages
 * @Inputs
 * file: the path to the parsing data
 * type: the type you want this reader to read
 *      type "entity": reads all the gold entities in the data
 *      type "relation": reads all the gold relations in the data
 *      type "relation_full": reads all the relations including negative relations in the data
 *      type "relation_full_bi": reads all the relations including negative relations into bi-directional labels
 *      type "relation_full_trim": reads all the relations, including a trimmed set of negative relations at rate "trim_factor"
 */
public class ACEMentionReader implements Parser
{
    private List<Relation> relations;
    private List<Constituent> entities;
    private List<Relation> relations_full;
    private List<Relation> relations_full_trim;
    private List<Relation> relations_full_bi;
    private List<Relation> relations_bi;
    private List<Relation> relation_full_bi_test;
    private int currentRelationIndex;
    private int currentEntityIndex;
    private int currentRelationFullIndex;
    private int currentRelationFullTrimIndex;
    private int currentRelationFullBiIndex;
    private int currentRelationBiIndex;
    private int currentRelationFullBiTestIndex;
    private String readType = "NULL";
    private double trim_factor = 0.5;

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

    public String getOppoName(String name){
        if (name.equals("Family") || name.equals("Lasting-Personal") || name.equals("Near") || name.equals("Business")){
            return name;
        }
        return name + "_OP";
    }

    public boolean skipTypes(String type){
        if (type.equals("Ownership") || type.equals("Ownership_OP")
            || type.equals("Student-Alum") || type.equals("Student-Alum_OP")
                || type.equals("Artifact") || type.equals("Artifact_OP")){
            return true;
        }
        return false;
    }
    public ACEMentionReader(String file, String type) {
        readType = type;
        relations = new ArrayList<Relation>();
        entities = new ArrayList<Constituent>();
        relations_full = new ArrayList<Relation>();
        relations_full_trim = new ArrayList<Relation>();
        relations_full_bi = new ArrayList<Relation>();
        relations_bi = new ArrayList<Relation>();
        relation_full_bi_test = new ArrayList<Relation>();
        try {
            ACEReader reader = new ACEReader(file, new String[]{"bn", "nw"}, false);
            POSAnnotator pos_annotator = new POSAnnotator();

            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://localhost", "8080");
            //annotator.setViews(ViewNames.DEPENDENCY, ViewNames.SHALLOW_PARSE);
            annotator.setViews(ViewNames.SHALLOW_PARSE);
            BrownClusterViewGenerator bc_annotator = new BrownClusterViewGenerator("c1000", BrownClusterViewGenerator.file1000);
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            Map<Integer, Integer> distMap = new HashMap<Integer, Integer>();
            for (TextAnnotation ta : reader) {
                ta.addView(pos_annotator);
                chunker.addView(ta);
                bc_annotator.addView(ta);
                //annotator.addView(ta);
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                relations.addAll(entityView.getRelations());
                entities.addAll(entityView.getConstituents());
                List<Relation> existRelations = entityView.getRelations();
                Random rand = new Random();
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence= ta.getSentence(i);
                    List<Constituent> cins = entityView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < cins.size(); j++){
                        for (int k = j + 1; k < cins.size(); k++){
                            Constituent firstArg = cins.get(j);
                            Constituent secondArg = cins.get(k);
                            boolean found_as_source = false;
                            boolean found_as_target = false;
                            for (Relation r : existRelations){
                                /*
                                if (r.getAttribute("RelationSubtype").equals("Artifact")){
                                    System.out.println(r.getSource().getAttribute("EntityType"));
                                    System.out.println(r.getTarget().getAttribute("EntityType"));
                                    System.out.println();
                                }
                                */
                                if (r.getSource() == firstArg && r.getTarget() == secondArg){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    relations_full.add(r);
                                    Relation opdirNeg = new Relation("NOT_RELATED", secondArg, firstArg, 1.0f);
                                    opdirNeg.addAttribute("RelationType", "NOT_RELATED");
                                    relations_full.add(opdirNeg);
                                    relations_full_trim.add(r);
                                    found_as_source = true;
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation opdir = new Relation(opTypeName, secondArg, firstArg, 1.0f);
                                    opdir.addAttribute("RelationSubtype", opTypeName);
                                    opdir.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    if (!RelationFeatureExtractor.isFourType(r)) {
                                        relations_full_bi.add(r);
                                    }
                                    if (!RelationFeatureExtractor.isFourType(opdir)) {
                                        relations_full_bi.add(opdir);
                                    }
                                    relation_full_bi_test.add(r);
                                    relation_full_bi_test.add(opdir);
                                    relations_bi.add(r);
                                    relations_bi.add(opdir);
                                    relations_full_trim.add(opdir);
                                    break;
                                }
                                if (r.getTarget() == firstArg && r.getSource() == secondArg){
                                    if (skipTypes(r.getAttribute("RelationSubtype"))) continue;
                                    relations_full.add(r);
                                    Relation opdirNeg = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                    opdirNeg.addAttribute("RelationType", "NOT_RELATED");
                                    relations_full.add(opdirNeg);
                                    relations_full_trim.add(r);
                                    found_as_target = true;
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation opdir = new Relation(opTypeName, firstArg, secondArg, 1.0f);
                                    opdir.addAttribute("RelationSubtype", opTypeName);
                                    opdir.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    if (!RelationFeatureExtractor.isFourType(r)) {
                                        relations_full_bi.add(r);
                                    }
                                    if (!RelationFeatureExtractor.isFourType(opdir)) {
                                        relations_full_bi.add(opdir);
                                    }
                                    relation_full_bi_test.add(r);
                                    relation_full_bi_test.add(opdir);
                                    relations_bi.add(r);
                                    relations_bi.add(opdir);
                                    relations_full_trim.add(opdir);
                                    break;
                                }
                            }
                            if (!found_as_source){
                                Relation newRelation = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                //relations_full.add(newRelation);
                            }
                            if (!found_as_target){
                                Relation newRelation = new Relation("NOT_RELATED", secondArg, firstArg, 1.0f);
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                //relations_full.add(newRelation);
                            }
                            if (!found_as_source && !found_as_target){
                                Relation newRelation_1 = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                newRelation_1.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation_1.addAttribute("RelationType", "NOT_RELATED");
                                Relation newRelation_2 = new Relation("NOT_RELATED", secondArg, firstArg, 1.0f);
                                newRelation_2.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation_2.addAttribute("RelationType", "NOT_RELATED");
                                int dist_1 = getEntityHeadForConstituent(newRelation_1.getSource(), newRelation_1.getSource().getTextAnnotation(), "K").getStartSpan() -
                                        getEntityHeadForConstituent(newRelation_1.getTarget(), newRelation_1.getTarget().getTextAnnotation(), "K").getStartSpan();
                                if (dist_1 < 0) dist_1 = -dist_1;
                                int dist_2 = getEntityHeadForConstituent(newRelation_2.getSource(), newRelation_2.getSource().getTextAnnotation(), "K").getStartSpan() -
                                        getEntityHeadForConstituent(newRelation_2.getTarget(), newRelation_2.getTarget().getTextAnnotation(), "K").getStartSpan();
                                if (dist_2 < 0) dist_2 = -dist_2;
                                if (distMap.containsKey(dist_1)){
                                    distMap.put(dist_1, distMap.get(dist_1) + 1);
                                }
                                else {
                                    distMap.put(dist_1, 1);
                                }
                                if (distMap.containsKey(dist_2)){
                                    distMap.put(dist_2, distMap.get(dist_2) + 1);
                                }
                                else {
                                    distMap.put(dist_2, 1);
                                }
                                relations_full.add(newRelation_1);
                                relations_full.add(newRelation_2);
                                relation_full_bi_test.add(newRelation_1);
                                relation_full_bi_test.add(newRelation_2);
                                if (!RelationFeatureExtractor.isFourType(newRelation_1)) {
                                    relations_full_bi.add(newRelation_1);
                                }
                                if (rand.nextDouble() < trim_factor) {
                                    relations_full_trim.add(newRelation_1);
                                }
                                if (!RelationFeatureExtractor.isFourType(newRelation_2)){
                                    relations_full_bi.add(newRelation_2);
                                }
                                if (rand.nextDouble() < trim_factor) {
                                    relations_full_trim.add(newRelation_2);
                                }
                            }
                        }
                    }
                }
            }
            for (int i : distMap.keySet()){
                //System.out.println("dist " + i + ": " + distMap.get(i));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){}
    public Object next(){
        if (readType == "relation") {
            if (currentRelationIndex == relations.size()) {
                return null;
            } else {
                currentRelationIndex++;
                return relations.get(currentRelationIndex - 1);
            }
        }
        if (readType == "entity"){
            if (currentEntityIndex == entities.size()) {
                return null;
            } else {
                currentEntityIndex++;
                return entities.get(currentEntityIndex - 1);
            }
        }
        if (readType == "relation_full"){
            if (currentRelationFullIndex == relations_full.size()){
                return null;
            }
            else{
                currentRelationFullIndex ++;
                return relations_full.get(currentRelationFullIndex - 1);
            }
        }
        if (readType == "relation_full_trim"){
            if (currentRelationFullTrimIndex == relations_full_trim.size()){
                return null;
            }
            else{
                currentRelationFullTrimIndex++;
                return relations_full_trim.get(currentRelationFullTrimIndex - 1);
            }
        }
        if (readType == "relation_full_bi"){
            if (currentRelationFullBiIndex == relations_full_bi.size()){
                return null;
            }
            else{
                currentRelationFullBiIndex ++;
                return relations_full_bi.get(currentRelationFullBiIndex - 1);
            }
        }
        if (readType == "relation_bi"){
            if (currentRelationBiIndex == relations_bi.size()){
                return null;
            }
            else{
                currentRelationBiIndex ++;
                return relations_bi.get(currentRelationBiIndex - 1);
            }
        }
        if (readType == "relation_full_bi_test"){
            if (currentRelationFullBiTestIndex == relation_full_bi_test.size()){
                return null;
            }
            else{
                currentRelationFullBiTestIndex ++;
                return relation_full_bi_test.get(currentRelationFullBiTestIndex - 1);
            }
        }
        else{
            return null;
        }
    }

    public void reset(){
        currentRelationIndex = 0;
        currentEntityIndex = 0;
        currentRelationFullIndex = 0;
        currentRelationFullTrimIndex = 0;
        currentRelationFullBiIndex = 0;
        currentRelationBiIndex = 0;
        currentRelationFullBiTestIndex = 0;
    }

}
