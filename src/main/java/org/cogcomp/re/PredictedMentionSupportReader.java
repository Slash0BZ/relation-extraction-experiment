package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.ArrayList;
import java.util.List;

public class PredictedMentionSupportReader implements Parser{

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

    List<Constituent> constituents;
    private int currentConstituentsIndex;
    public PredictedMentionSupportReader(String path){
        constituents = new ArrayList<Constituent>();
        try {
            ACEReader aceReader = new ACEReader(path, false);
            PredictedMentionAnnotator predictedMentionAnnotator = new PredictedMentionAnnotator();
            for (TextAnnotation ta : aceReader){
                predictedMentionAnnotator.addView(ta);
                View predictedView = ta.getView("PREDICTED_MENTION_EXTRACTOR");
                View goldView = ta.getView(ViewNames.MENTION_ACE);
                List<Constituent> predictedConstituents = predictedView.getConstituents();
                List<Constituent> goldConstituents = goldView.getConstituents();
                for (Constituent pc : predictedConstituents){
                    for (Constituent gc : goldConstituents){
                        Constituent gcHead = getEntityHeadForConstituent(gc, ta, "GOLD_MENTION_HEAD");
                        if (gcHead.getStartSpan() == pc.getStartSpan() && gcHead.getEndSpan() == pc.getEndSpan()){
                            pc.addAttribute("GOLD_EntityMentionType", gc.getAttribute("EntityMentionType"));
                            pc.addAttribute("GOLD_EntityType", gc.getAttribute("EntityType"));
                            pc.addAttribute("GOLD_EntitySubtype", gc.getAttribute("EntitySubtype"));
                            constituents.add(pc);
                        }
                    }
                }
            }
        }
        catch (Exception e){

        }
    }
    public void close(){

    }
    public void reset(){
        currentConstituentsIndex = 0;
    }
    public Object next(){
        if (currentConstituentsIndex == constituents.size()) {
            return null;
        } else {
            currentConstituentsIndex++;
            return constituents.get(currentConstituentsIndex - 1);
        }
    }
}
