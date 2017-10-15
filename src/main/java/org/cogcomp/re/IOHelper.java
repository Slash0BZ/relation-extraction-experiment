package org.cogcomp.re;

import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import org.apache.xmlrpc.webserver.ServletWebServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuany on 10/6/2017.
 */
public class IOHelper {
    public static void serializeDataOut(ACEMentionReader input, String outputFile){
        String fileName= outputFile;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(input);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ACEMentionReader serializeDataIn(String inputFile){
        String fileName= inputFile;
        ACEMentionReader ret = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            ret = (ACEMentionReader) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static void serializeRelationsOut(List<Relation> inputs, String outputFile){
        String fileName= outputFile;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (Relation r : inputs){
                oos.writeObject(r);
            }
            oos.writeObject(null);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<Relation> serializeRelationsIn(String inputFile){
        String fileName= inputFile;
        List<Relation> ret = new ArrayList<>();
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            Relation r = null;
            while ((r = (Relation) ois.readObject()) != null){
                ret.add(r);
            }
            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static void outputRelationsNonBinary(List<Relation> examples, String fileName){
        List<String> toFile = new ArrayList<>();
        for (Relation r : examples){
            TextAnnotation ta = r.getSource().getTextAnnotation();
            Sentence sentence = ta.getSentence(r.getSource().getSentenceId());
            int sentenceStart = sentence.getStartSpan();
            int s_e_start = r.getSource().getStartSpan() - sentenceStart;
            int s_e_end = r.getSource().getEndSpan() - sentenceStart;
            int t_e_start = r.getTarget().getStartSpan() - sentenceStart;
            int t_e_end = r.getTarget().getEndSpan() - sentenceStart;
            Constituent sourceHead = RelationFeatureExtractor.getEntityHeadForConstituent(r.getSource(), ta, "");
            Constituent targetHead = RelationFeatureExtractor.getEntityHeadForConstituent(r.getTarget(), ta, "");
            int s_h_start = sourceHead.getStartSpan() - sentenceStart;
            int s_h_end = sourceHead.getEndSpan() - sentenceStart;
            int t_h_start = targetHead.getStartSpan() - sentenceStart;
            int t_h_end = targetHead.getEndSpan() - sentenceStart;
            toFile.add(sentence.toString());
            toFile.add(s_e_start + "-" + s_e_end + "," + s_h_start + "-" + s_h_end + "," + t_e_start + "-" + t_e_end + "," + t_h_start + "-" + t_h_end);
        }
        try {
            FileWriter fw = new FileWriter(fileName);
            for (String s : toFile) {
                fw.write(s + "\n");
            }
            fw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static List<Relation> inputRelationsNonBinary(String fileName){
        File file = new File(fileName);
        List<Relation> ret = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            fileReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        StatefulTokenizer statefulTokenizer = new StatefulTokenizer(false);
        for (int i = 0; i < lines.size() / 2; i++){
            String sentence = lines.get(i * 2);
            String arguments = lines.get(i * 2 + 1);
            Pair<String[], IntPair[]> tokenizedSentence = statefulTokenizer.tokenizeSentence(sentence);
            List<String[]> tokens = new ArrayList<>();
            tokens.add(tokenizedSentence.getFirst());
            TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
            String[] mentionsRaw = arguments.split(",");
            Pair<Integer, Integer>[] mentionsIdx = new Pair[4];
            for (int j = 0; j < mentionsRaw.length; j++){
                String s = mentionsRaw[j];
                int arg_1 = Integer.parseInt(s.split("-")[0]);
                int arg_2 = Integer.parseInt(s.split("-")[1]);
                mentionsIdx[j] = new Pair<>(arg_1, arg_2);
            }
            Constituent source = new Constituent("label", ViewNames.MENTION_ACE, ta, mentionsIdx[0].getFirst(), mentionsIdx[0].getSecond());
            source.addAttribute("EntityHeadStartSpan", Integer.toString(mentionsIdx[1].getFirst()));
            source.addAttribute("EntityHeadEndSpan", Integer.toString(mentionsIdx[1].getSecond()));
            Constituent target = new Constituent("label", ViewNames.MENTION_ACE, ta, mentionsIdx[2].getFirst(), mentionsIdx[2].getSecond());
            target.addAttribute("EntityHeadStartSpan", Integer.toString(mentionsIdx[3].getFirst()));
            target.addAttribute("EntityHeadEndSpan", Integer.toString(mentionsIdx[3].getSecond()));
            Relation r = new Relation("label", source, target, 1.0f);
            printRelation(r);
            ret.add(r);
        }
        return ret;
    }

    public static void produceFiveFoldReader(){
        for (int i = 0; i < 5; i++){
            ACEMentionReader curTrain = new ACEMentionReader("data/partition_with_dev/train/" + i, "relation_full_bi_test");
            serializeDataOut(curTrain, "preprocess/reader/train_fold_" + i);
            ACEMentionReader curTest = new ACEMentionReader("data/partition_with_dev/eval/" + i, "relation_full_bi_test");
            serializeDataOut(curTest, "preprocess/reader/test_fold_" + i);
        }
    }

    public static ACEMentionReader readFiveFold(int fold, String mode){
        if (mode.equals("TRAIN")) {
            return serializeDataIn("preprocess/reader/train_fold_" + fold);
        }
        else {
            return serializeDataIn("preprocess/reader/test_fold_" + fold);
        }
    }

    public static void printRelation (Relation r){
        TextAnnotation ta = r.getSource().getTextAnnotation();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "");
        Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "");
        System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
        System.out.println(r.getAttribute("RelationType") + ":" + r.getAttribute("RelationSubtype"));
        System.out.println(source.toString() + " || " + target.toString());
        System.out.println(source_head.toString() + " || " + target_head.toString());
    }

    public static void main (String[] args){
        List<Relation> ret = inputRelationsNonBinary("preprocess/relations/PHYS_no_binary.txt");
    }
}
