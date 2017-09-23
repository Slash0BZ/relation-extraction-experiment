package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by xuany on 9/22/2017.
 */
public class ExperimentPrinter {
    public static void printACESentenceWithRelation(){
        Random rn = new Random();
        Map<String, List<String>> print = new HashMap<>();
        try{
            ACEReader aceReader = new ACEReader("data/all", false);
            for (TextAnnotation ta : aceReader){
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence = ta.getSentence(i);
                    if (rn.nextDouble() < 0.95){
                        continue;
                    }
                    System.out.println(curSentence);
                    for (Relation r : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                        Constituent source = r.getSource();
                        Constituent target = r.getTarget();
                        if (source.getSentenceId() == i && target.getSentenceId() == i) {
                            System.out.println("\t" + source + ":[" + r.getAttribute("RelationType") + "]:" + target);
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        printACESentenceWithRelation();
    }
}
