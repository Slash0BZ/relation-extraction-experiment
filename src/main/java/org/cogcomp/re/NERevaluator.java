package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
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
import java.util.List;

/**
 * Created by xuany on 5/9/2017.
 */
public class NERevaluator {

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

    public static void calculate_ner_hit(){
        try{
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://austen.cs.illinois.edu", "5800");
            annotator.setViews(ViewNames.NER_CONLL, ViewNames.NER_ONTONOTES);
            ACEReader reader = new ACEReader("data/original", false);
            int nerConllCorrect = 0;
            int nerConllTotal = 0;
            int nerOntonotesCorrect = 0;
            int nerOntonotesTotal = 0;
            int mentionTotal = 0;
            for (TextAnnotation ta : reader){
                annotator.addView(ta);
                View aceView = ta.getView(ViewNames.MENTION_ACE);
                View nerConllView = ta.getView(ViewNames.NER_CONLL);
                View nerOntonotesView = ta.getView(ViewNames.NER_ONTONOTES);
                for (Constituent c_full_span : aceView.getConstituents()){
                    mentionTotal ++;
                    Constituent c = getEntityHeadForConstituent(c_full_span, ta, "MentionHeads");
                    String targetLabel = c.getAttribute("EntityType");
                    List<Constituent> ncList = nerConllView.getConstituentsCovering(c);
                    List<Constituent> noList = nerOntonotesView.getConstituentsCovering(c);
                    for (Constituent ncCandidate : ncList){
                        if (ncCandidate.getStartSpan() == c.getStartSpan() && ncCandidate.getEndSpan() == c.getEndSpan()){
                            String label = ncCandidate.getLabel();
                            nerConllTotal ++;
                            if (label.equals("LOC")){
                                if (targetLabel.equals("LOC") || targetLabel.equals("GPE") || targetLabel.equals("FAC")){
                                    nerConllCorrect ++;
                                }
                            }
                            if (label.equals("ORG")){
                                if (targetLabel.equals("ORG")){
                                    nerConllCorrect ++;
                                }
                            }
                            if (label.equals("PER")){
                                if (targetLabel.equals("PER")){
                                    nerConllCorrect ++;
                                }
                            }
                            if (label.equals("MISC")){
                                if (targetLabel.equals("LOC") == false &&
                                        targetLabel.equals("PER") == false &&
                                        targetLabel.equals("ORG") == false){
                                    nerConllCorrect ++;
                                }
                            }
                            break;
                        }
                    }
                    for (Constituent noCandidate : noList){
                        if (noCandidate.getStartSpan() == c.getStartSpan() && noCandidate.getEndSpan() == c.getEndSpan()){
                            String label = noCandidate.getLabel();
                            nerOntonotesTotal ++;
                            if (label.equals("PERSON")){
                                if (targetLabel.equals("PER")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else if (label.equals("LOC")){
                                if (targetLabel.equals("LOC")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else if (label.equals("GPE")){
                                if (targetLabel.equals("GPE")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else if (label.equals("ORG")){
                                if (targetLabel.equals("ORG")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else if (label.equals("FAC")){
                                if (targetLabel.equals("FAC")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else if (label.equals("NORP")){
                                if (targetLabel.equals("GPE") || targetLabel.equals("LOC")){
                                    nerOntonotesCorrect ++;
                                }
                            }
                            else{
                                //System.out.println("Original: " + c.getSurfaceForm() + " Label: " + targetLabel);
                                //System.out.println("Got: " + noCandidate.getSurfaceForm() + " Label: " + label);
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println("Total Mention: " + mentionTotal);
            System.out.println("Total Conll Predicted: " + nerConllTotal);
            System.out.println("Conll Correct: " + nerConllCorrect);
            System.out.println("Total Ontonotes Predicted: " + nerOntonotesTotal);
            System.out.println("Ontonotes Correct: " + nerOntonotesCorrect);
        }
        catch (Exception e){

        }
    }

    public static void outputColumnFormatFiles(){
        try {
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://sauron.cs.illinois.edu", "8080");
            annotator.setViews("STANFORD_TRUE_CASE");
            ACEReader reader = new ACEReader("data/original", false);
            FileReader fileReader = new FileReader(new File("outputs/combined_coref"));
            BufferedReader br = new BufferedReader(fileReader);
            int docIdx = 0;
            for (TextAnnotation ta : reader){
                List<Integer> sentence_ends = new ArrayList<Integer>();
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    sentence_ends.add(ta.getSentence(i).getEndSpan());
                }
                String outputFile = "";
                String newName = ta.getId().substring(3, ta.getId().length() - 8);
                FileOutputStream out = new FileOutputStream("outputs/column_format_mlvl/" + newName + ".txt");
                docIdx ++;
                //annotator.addView(ta);
                String coref_doc = br.readLine();
                String[] coref_tokens = coref_doc.split(" ");
                List<IntPair> coref_mentions_detected = new ArrayList<IntPair>();
                List<String> coref_tokens_detected = new ArrayList<String>();
                List<String> coref_mentions_mlvl = new ArrayList<String>();
                int anchorCount = 0;
                for (int i = 0; i < coref_tokens.length; i++){
                    if (coref_tokens[i].equals("[")){
                        for (int j = i + 1; j < coref_tokens.length; j++){
                            if (coref_tokens[j].contains("]_")){
                                IntPair curMentionPos = new IntPair(i - anchorCount, j - 1 - anchorCount);
                                coref_mentions_detected.add(curMentionPos);
                                coref_mentions_mlvl.add(coref_tokens[j].substring(2, coref_tokens[j].length()));
                                anchorCount += 2;
                                break;
                            }
                            if (coref_tokens[j].equals("]")){
                                coref_tokens_detected.add("[");
                                break;
                            }
                            if (coref_tokens[j].equals("[")){
                                coref_tokens_detected.add("[");
                                break;
                            }
                        }
                    }
                    if (coref_tokens[i].equals("[") == false &&
                            coref_tokens[i].contains("]_") == false){
                        coref_tokens_detected.add(coref_tokens[i]);
                    }
                }
                /*
                for (int i = 0; i < coref_mentions_detected.size(); i++){
                    System.out.println(coref_mentions_detected.get(i));
                    System.out.println(coref_tokens_detected.get(coref_mentions_detected.get(i).getFirst()));
                }
                */
                int jj = 0;
                //View trueCaseView = ta.getView("STANFORD_TRUE_CASE");
                for (int i = 0; i < ta.getTokens().length; i++){
                    //List<Constituent> matchingList = trueCaseView.getConstituentsCoveringToken(i);
                    List<Constituent> matchingList = new ArrayList<Constituent>();
                    String trueCaseToken = "";
                    if (matchingList.size() == 0){
                        trueCaseToken = ta.getToken(i);
                    }
                    else{
                        trueCaseToken = matchingList.get(0).getLabel();
                    }
                    int mentionFlag = 0;
                    int mentionCount = 0;
                    for (IntPair ip : coref_mentions_detected){
                        if (jj == ip.getFirst()){
                            outputFile += trueCaseToken;
                            outputFile += "\t";
                            outputFile += coref_mentions_mlvl.get(mentionCount);
                            outputFile += "\n";
                            mentionFlag = 1;
                            break;
                        }
                        if (jj > ip.getFirst() && jj < ip.getSecond()){
                            outputFile += trueCaseToken;
                            outputFile += "\t";
                            outputFile += "I";
                            outputFile += "\n";
                            mentionFlag = 1;
                            break;
                        }
                        mentionCount++;
                    }
                    if (mentionFlag == 0){
                        outputFile += trueCaseToken;
                        outputFile += "\t";
                        outputFile += "O";
                        outputFile += "\n";
                    }
                    if (sentence_ends.contains(i + 1)){
                        outputFile += "\n";
                    }
                    if (ta.getToken(i).equals(coref_tokens_detected.get(jj)) == false){
                        jj++;
                    }
                    jj++;
                }
                out.write(outputFile.getBytes());
                out.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
       outputColumnFormatFiles();
    }
}
