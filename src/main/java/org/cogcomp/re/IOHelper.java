package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

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

    public static void main (String[] args){
        ACEMentionReader aceMentionReader = new ACEMentionReader("data/partition_with_dev/dev", "relation_full_bi_test");
        serializeDataOut(aceMentionReader, "preprocess/reader/dev");
    }
}
