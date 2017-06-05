package org.cogcomp.re;
import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;
import static edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import edu.illinois.cs.cogcomp.pos.*;
import edu.illinois.cs.cogcomp.edison.annotators.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.lang.*;
public class EREComparator {
    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        if (extentConstituent.getAttribute("EntityHeadStartCharOffset") == null ||
                extentConstituent.getAttribute("EntityHeadEndCharOffset") == null){
            return extentConstituent;
        }
        int startCharOffset =
                Integer.parseInt(extentConstituent
                        .getAttribute("EntityHeadStartCharOffset"));
        int endCharOffset =
                Integer.parseInt(extentConstituent.getAttribute("EntityHeadEndCharOffset")) - 1;
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
        //System.out.println("null: " + startToken + " " + endToken);
        return null;
    }
    public static void main(String[] args) {
        EREMentionRelationReader emr = null;
        ACEReader aceReader = null;
        try {
            emr = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            aceReader = new ACEReader("data/original", false);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        //RelationAnnotator ra = new RelationAnnotator();
        int total_match = 0;
        int total_type_match = 0;
        int total_predicted = 0;
        int total_labeled = 0;
        //for (XmlTextAnnotation xta : emr){

        for (TextAnnotation ta : aceReader){
            //TextAnnotation ta = xta.getTextAnnotation();
            //System.out.println(ta.getId());
            try {
                //ra.addView(ta);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            //List<Relation> predictedRelations = ta.getView("RELATION_EXTRACTION_RELATIONS").getRelations();
            List<Relation> goldRelations = ta.getView(ViewNames.MENTION_ACE).getRelations();
            //System.out.println("Predicted Relations");
            //for (Relation r : predictedRelations){
                //System.out.println("[Source:] " + r.getSource().toString() + " [Target:] " + r.getTarget().toString() + " [Tag:] " + r.getRelationName());
            //}
            //System.out.println("Gold Relations");
            int far_relations_count = 0;
            for (Relation r : goldRelations) {
                if (r.getAttribute("RelationSubtype").equals("Subsidiary")) {
                    //System.out.println("[Source:] " + r.getSource().toString() + " [Target:] " + r.getTarget().toString() + " [Tag]: " + r.getAttribute("RelationSubtype"));
                }
                if (r.getSource().getStartSpan() > r.getTarget().getEndSpan() || r.getTarget().getStartSpan() > r.getSource().getEndSpan()){
                    far_relations_count ++;
                }
            }
            if (ta.getId().equals("nw/AFP_ENG_20030425.0408.apf.xml")){
                System.out.println(ta.getId());
                for (Relation r : goldRelations){
                    System.out.println("[Source:] " + r.getSource().toString() + " [Target:] " + r.getTarget().toString() + " [Tag]: " + r.getAttribute("RelationSubtype"));
                }
            }
            total_labeled += goldRelations.size();
            //total_predicted += predictedRelations.size();
            int match = 0;
            int type_match = 0;/*
            for (Relation r : predictedRelations){
                Constituent psh = getEntityHeadForConstituent(r.getSource(), ta, "PRTEST");
                Constituent peh = getEntityHeadForConstituent(r.getTarget(), ta, "PRTEST");
                if (psh == null || peh == null) continue;
                for (Relation gr : goldRelations){
                    Constituent gsh = getEntityHeadForConstituent(gr.getSource(), ta, "GRTEST");
                    Constituent geh = getEntityHeadForConstituent(gr.getTarget(), ta, "GRTEST");
                    if (psh.getStartSpan() == gsh.getStartSpan() &&
                            psh.getEndSpan() == gsh.getEndSpan() &&
                            peh.getStartSpan() == geh.getStartSpan() &&
                            peh.getEndSpan() == geh.getEndSpan()){
                        match ++;
                        break;
                    }
                    if (psh.getStartSpan() == geh.getStartSpan() &&
                            psh.getEndSpan() == geh.getEndSpan() &&
                            peh.getStartSpan() == gsh.getStartSpan() &&
                            peh.getEndSpan() == gsh.getEndSpan()){
                        match ++;
                        break;
                    }
                }
            }
            total_match += match;
            total_type_match += type_match;
            */
            /*
            System.out.println("Total labeled: " + total_labeled);
            System.out.println("Total predicted: " + total_predicted);
            System.out.println("Total match: " + total_match);
            //break;
            */
        }
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total match: " + total_match);
    }
}
