package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by xuany on 9/22/2017.
 */
public class SemEvalMentionReader {
    public static TextAnnotation readTrainFile(String fileName){
        List<String> sentences = new ArrayList<>();
        List<String> types = new ArrayList<>();
        int counter = 0;
        Random rn = new Random();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (counter % 4 == 0){
                    sentences.add(line.split("\t")[1]);
                }
                if (counter % 4 == 1){
                    types.add(line);
                }
                counter ++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        StatefulTokenizer statefulTokenizer = new StatefulTokenizer();
        List<String[]> tokens= new ArrayList<>();
        List<IntPair> mentions = new ArrayList<>();
        int prevStart = 0;
        for (int i = 0; i < sentences.size(); i++){
            String sentence = sentences.get(i);
            String type = types.get(i);
            Pair<String[], IntPair[]> tokenizedSentence = statefulTokenizer.tokenizeSentence(sentence);
            List<String> curTokens = new LinkedList<String>(Arrays.asList(tokenizedSentence.getFirst()));
            int firstArgStart = 0;
            int firstArgEnd = 0;
            int secondArgStart = 0;
            int secondArgEnd = 0;
            for (int j = 0; j < curTokens.size(); j++){
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e1") && curTokens.get(j + 2).equals(">")){
                    firstArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e1") && curTokens.get(j + 3).equals(">")){
                    firstArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e2") && curTokens.get(j + 2).equals(">")){
                    secondArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e2") && curTokens.get(j + 3).equals(">")){
                    secondArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
            }
            prevStart += curTokens.size();
            mentions.add(new IntPair(prevStart + firstArgStart, prevStart + firstArgEnd));
            mentions.add(new IntPair(prevStart + secondArgStart, prevStart + secondArgEnd));
            tokens.add(curTokens.toArray(new String[curTokens.size()]));
        }
        System.out.println("HERE");
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
        System.out.println("HERE");
        SpanLabelView mentionView = new SpanLabelView("MENTIONS", "MENTIONS", ta, 1.0f);
        for (IntPair cur : mentions){
            Constituent constituent = new Constituent("MENTION", 1.0f, "MENTIONS", ta, cur.getFirst(), cur.getSecond());
            mentionView.addConstituent(constituent);
        }
        System.out.println("HERE");
        ta.addView("MENTIONS", mentionView);
        for (int i = 0; i < ta.getNumberOfSentences(); i++){
            Sentence sentence = ta.getSentence(i);
            for (Constituent c : ta.getView("MENTIONS").getConstituentsCoveringSpan(sentence.getStartSpan(), sentence.getEndSpan())){
                System.out.println(c.toString());
            }
            System.out.println();
        }
        return ta;
    }
    public static void main(String[] args){
        readTrainFile("data/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT");
    }
}
