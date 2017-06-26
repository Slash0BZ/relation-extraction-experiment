package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.List;

/**
 * Created by xuany on 6/26/2017.
 */
public class TokenizerTester {
    public static void printErrors(){
        try {
            ACEReader aceReader = new ACEReader("data/original", false);
            for (TextAnnotation ta : aceReader){
                View aceView = ta.getView(ViewNames.MENTION_ACE);
                List<Relation> relations = aceView.getRelations();
                for (Relation r : relations){
                    if (r.getSource().getSentenceId() != r.getTarget().getSentenceId()){
                        System.out.println("Source: " + r.getSource().toString());
                        System.out.println("SENTENCE_1: " + ta.getSentence(r.getSource().getSentenceId()));
                        System.out.println("Target: " + r.getTarget().toString());
                        System.out.println("SENTENCE_2: " + ta.getSentence(r.getTarget().getSentenceId()));
                    }
                }
                break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void printSentences(){
        try {
            ACEReader aceReader = new ACEReader("data/original", false);
            for (TextAnnotation ta : aceReader){
                System.out.println("[TEXT]: " + ta.getText());
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    System.out.println("[SENTENCE]: " + ta.getSentence(i));
                }
                break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void printEntities(){
        try {
            ACEReader aceReader = new ACEReader("data/original", false);
            for (TextAnnotation ta : aceReader){
                for (Constituent c : ta.getView(ViewNames.TOKENS).getConstituents()){
                    System.out.println("[C]: " + c.toString());
                }
                break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main (String[] args){
        printSentences();
        printEntities();
    }
}
