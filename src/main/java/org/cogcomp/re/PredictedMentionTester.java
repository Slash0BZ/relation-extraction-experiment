package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PredictedMentionTester{
    public static void main(String[] args){
        try {
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://austen.cs.illinois.edu", "5800");
            annotator.setViews(ViewNames.NER_CONLL, ViewNames.NER_ONTONOTES);
            ACEReader reader = new ACEReader("data/original", false);
            Set<String> NER_CONLL_SET = new HashSet<String>();
            Set<String> NER_ONTONOTES_SET = new HashSet<String>();
            for (TextAnnotation ta : reader) {
                annotator.addView(ta);
                View nerView = ta.getView(ViewNames.NER_CONLL);
                View ooNerView = ta.getView(ViewNames.NER_ONTONOTES);
                /*
                String hao_file_name = ta.getId().substring(3, ta.getId().length() - 8) + ".txt";
                FileReader fileReader = new FileReader(new File("outputs/hao/" + hao_file_name));
                BufferedReader br = new BufferedReader(fileReader);
                String outputFile = "";
                String outputFile_NER = "";
                String outputFile_NER_OO = "";
                List<String> hao_output_lines = new ArrayList<String>();
                String curLine = "";
                while ((curLine = br.readLine()) != null){
                    if (curLine.equals("")) continue;
                    hao_output_lines.add(curLine);
                }
                for (int i = 0; i < ta.getTokens().length; i++) {
                    String matchLine = hao_output_lines.get(i);
                    String[] parsedCurLine = matchLine.split("\t");
                    String sign = parsedCurLine[1];
                    if (sign.charAt(0) == 'O'){
                        outputFile += ta.getToken(i) + " ";
                    }
                    if (sign.charAt(0) == 'B') {
                        int cursor = i + 1;
                        String[] parsedSign = (sign.substring(2, sign.length())).split(",");
                        String realSign = parsedSign[parsedSign.length - 1];
                        boolean contFlag = true;
                        while (contFlag){
                            if (cursor == hao_output_lines.size()){
                                break;
                            }
                            String nextLine = hao_output_lines.get(cursor);
                            String[] parsedNextLine = nextLine.split("\t");
                            if (parsedNextLine[1].charAt(0) == 'B' || parsedNextLine[1].charAt(0) == 'O'){
                                contFlag = false;
                            }
                            cursor++;
                        }
                        outputFile += "[ ";
                        for (int j = i; j < cursor - 1; j++) {
                            if (j == cursor - 2){
                                outputFile += ta.getToken(j) + " ]_" + realSign + " ";
                            }
                            else {
                                outputFile += ta.getToken(j) + " ";
                            }
                        }
                        i = cursor - 2;
                    }
                }
                */
                for (int i = 0; i < ta.getTokens().length; i++){
                    List<Constituent> retCons = nerView.getConstituentsCoveringToken(i);
                    boolean hit = false;
                    for (Constituent c : retCons){
                        NER_CONLL_SET.add(c.getLabel());
                        if (c.getStartSpan() == i){
                            //outputFile_NER += "[ " + c.getSurfaceForm() + " ]_" + c.getLabel() + " ";
                            hit = true;
                            i = c.getEndSpan() - 1;
                            break;
                        }
                    }
                    if (hit == false){
                        //outputFile_NER += ta.getToken(i) + " ";
                    }
                }
                for (int i = 0; i < ta.getTokens().length; i++){
                    List<Constituent> retCons = ooNerView.getConstituentsCoveringToken(i);
                    boolean hit = false;
                    for (Constituent c : retCons){
                        NER_ONTONOTES_SET.add(c.getLabel());
                        if (c.getStartSpan() == i){
                            //outputFile_NER_OO += "[ " + c.getSurfaceForm() + " ]_" + c.getLabel() + " ";
                            hit = true;
                            i = c.getEndSpan() - 1;
                            break;
                        }
                    }
                    if (hit == false){
                        //outputFile_NER_OO += ta.getToken(i) + " ";
                    }
                }
            }
            for (String tag : NER_CONLL_SET){
                System.out.println(tag);
            }
            for (String tag : NER_ONTONOTES_SET){
                System.out.println(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
