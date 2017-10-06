package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.*;

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

    public static void produceFiveFoldReader(){
        for (int i = 0; i < 5; i++){
            ACEMentionReader cur = new ACEMentionReader("data/partition_with_dev/train/" + i, "relation_full_bi_test");
            serializeDataOut(cur, "preprocess/reader/fold_" + i);
        }
    }

    public static ACEMentionReader readFiveFold(int fold){
        return serializeDataIn("preprocess/reader/fold_" + fold);
    }

    public static void main (String[] args){
        produceFiveFoldReader();
    }
}
