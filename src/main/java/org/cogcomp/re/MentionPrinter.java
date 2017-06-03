package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.stanford.nlp.util.ArrayMap;
import org.cogcomp.re.ACEMentionReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;

import java.io.*;
import java.util.*;

/**
 * Created by xuanyu on 4/27/17.
 */
public class MentionPrinter {

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

    public static void printFullTxtAndMention() {
        try {
            ACEReader aceReader = new ACEReader("data/small", false);
            FileOutputStream out = new FileOutputStream("outputs/gold_mention");
            for (TextAnnotation ta : aceReader) {
                out.write(ta.getText().getBytes());
                out.write("\n".getBytes());
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                for (Constituent ct : entityView.getConstituents()) {
                    out.write(ct.toString().getBytes());
                    out.write((": " + ct.getLabel() + "\n").getBytes());
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printCombinedTxtAndMention() {
        try {
            ACEReader aceReader = new ACEReader("data/fivedoc", false);
            FileOutputStream out = new FileOutputStream("outputs/five_doc_mention_inline_head");
            for (TextAnnotation ta : aceReader) {
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                String output_to_file = "";
                for (int i = 0; i < ta.getTokens().length; i++) {
                    List<Constituent> retList = entityView.getConstituentsCoveringToken(i);
                    for (Constituent c : retList) {
                        Constituent c_head = getEntityHeadForConstituent(c, ta, "HeadView");
                        if (c_head.getStartSpan() == i) {
                            output_to_file += "[ ";
                        }
                    }
                    output_to_file += ta.getToken(i) + " ";
                    for (Constituent c : retList) {
                        Constituent c_head = getEntityHeadForConstituent(c, ta, "HeadView");
                        if (c_head.getEndSpan() - 1 == i) {
                            output_to_file += "]_" + c.getAttribute("EntityType") + " ";
                        }
                    }
                }
                out.write((output_to_file + "\n").getBytes());
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String transform(String s) {
        if (s.equals("u.s")) {
            return "u.s.";
        }
        if (s.equals("u.n")) {
            return "u.n.";
        }
        if (s.equals("u.k")) {
            return "u.k.";
        }
        return s;
    }

    public static boolean contextCheck(String s) {
        if (s.contains("[")) {
            return false;
        }
        if (s.contains("]")) {
            return false;
        }
        return true;
    }


    public static void calculate_head_matches() {
        try {
            ACEReader aceReader = new ACEReader("data/original", false);

            String line = null;
            int total_coref_head_count = 0;
            int total_gold_head_count = 0;
            int total_matches = 0;
            int total_words = 0;
            for (TextAnnotation ta : aceReader) {
                String newName = ta.getId().substring(3, ta.getId().length() - 8);
                FileReader fileReader = new FileReader(new File("outputs/trainedMD/" + newName + ".txt"));
                BufferedReader br = new BufferedReader(fileReader);
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                String coref_doc = br.readLine();
                String[] coref_tokens = coref_doc.split(" ");
                List<String> coref_parsed = Arrays.asList(coref_tokens);
                List<String> coref_heads = new ArrayList<String>();
                Map<Integer, List<String>> coref_heads_map = new HashMap<Integer, List<String>>();
                String newCompose = "";
                List contextList = new ArrayList();
                for (int i = 1; i < coref_parsed.size() - 1; i++) {
                    if (coref_parsed.get(i - 1).equals("[")) {
                        newCompose += coref_parsed.get(i);
                        int contextAdded = 0;
                        for (int j = i - 2; j >= 0; j--) {
                            if (contextCheck(coref_parsed.get(j)) == false) {
                                continue;
                            }
                            contextList.add(coref_parsed.get(j));
                            contextAdded++;
                            if (contextAdded > 1) {
                                break;
                            }
                        }
                    }
                    if (coref_parsed.get(i + 1).startsWith("]")) {
                        if (newCompose.equals(coref_parsed.get(i)) == false) {
                            newCompose += coref_parsed.get(i);
                        }
                        coref_heads.add(newCompose);
                        int contextAdded = 0;
                        for (int j = i + 2; j < coref_parsed.size(); j++) {
                            if (coref_parsed.get(j).contains("[") || coref_parsed.get(j).contains("]")) {
                                continue;
                            }
                            contextList.add(coref_parsed.get(j));
                            contextAdded++;
                            if (contextAdded > 1) {
                                break;
                            }
                        }
                        coref_heads_map.put(coref_heads.size() - 1, contextList);
                        newCompose = "";
                        contextList = new ArrayList();
                    }
                }
                for (int i = 0; i < coref_heads.size(); i++) {
                    System.out.println("HEAD: " + coref_heads.get(i));
                    List head_context = coref_heads_map.get(i);
                    for (int j = 0; j < head_context.size(); j++) {
                        System.out.print(head_context.get(j) + " ");
                    }
                    System.out.println();
                }
                List<String> gold_heads = new ArrayList<String>();
                Map<Integer, List<String>> gold_heads_map = new HashMap<Integer, List<String>>();
                int curIdx = 0;
                for (Constituent c : entityView.getConstituents()) {
                    Constituent c_head = getEntityHeadForConstituent(c, ta, "HeadView");
                    String newComposeHead = "";
                    List<String> goldContextList = new ArrayList<String>();
                    for (int i = c_head.getStartSpan(); i < c_head.getEndSpan(); i++) {
                        newComposeHead += transform(ta.getToken(i));
                    }
                    gold_heads.add(newComposeHead);
                    for (int i = c_head.getStartSpan() - 1; i > c_head.getStartSpan() - 4; i--) {
                        if (i < 0) {
                            break;
                        }
                        goldContextList.add(transform(ta.getToken(i)));
                    }
                    for (int i = c_head.getEndSpan(); i < c_head.getEndSpan() + 3; i++) {
                        if (i >= ta.getTokens().length) {
                            break;
                        }
                        goldContextList.add(transform(ta.getToken(i)));
                    }
                    gold_heads_map.put(curIdx, goldContextList);
                    curIdx++;
                }
                for (int i = 0; i < gold_heads.size(); i++) {
                    System.out.println("GOLD_HEAD: " + gold_heads.get(i));
                    List goldHeadContext = gold_heads_map.get(i);
                    for (int j = 0; j < goldHeadContext.size(); j++) {
                        System.out.print(goldHeadContext.get(j) + " ");
                    }
                    System.out.println();
                }
                int correctPredicted = 0;
                for (int i = 0; i < coref_heads.size(); i++) {
                    int match = -1;
                    for (int j = 0; j < gold_heads.size(); j++) {
                        if (coref_heads.get(i).equals(gold_heads.get(j))) {
                            List<String> head_list = coref_heads_map.get(i);
                            List<String> gold_head_list = gold_heads_map.get(j);
                            int valid = 1;
                            for (String s : head_list) {
                                if (gold_head_list.contains(s) == false) {
                                    valid = 0;
                                    break;
                                }
                            }
                            if (valid == 1) {
                                match = j;
                                correctPredicted++;
                                break;
                            }
                        }
                    }
                    if (match == -1) {
                    }
                }
                System.out.println("Total in Coref: " + coref_heads.size());
                System.out.println("Total in ACE Gold: " + gold_heads.size());
                System.out.println("Intersect: " + correctPredicted);
                total_coref_head_count += coref_heads.size();
                total_gold_head_count += gold_heads.size();
                total_matches += correctPredicted;
                total_words += ta.getTokens().length;
            }
            System.out.println("Total in Coref: " + total_coref_head_count);
            System.out.println("Total in Gold: " + total_gold_head_count);
            System.out.println("Total correct: " + total_matches);
            System.out.println("Total words: " + total_words);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void transformFiles() {
        try {
            ACEReader aceReader = new ACEReader("data/partition/eval/4", false);
            int idx = 0;
            for (TextAnnotation ta : aceReader) {
                String newName = ta.getId().substring(3, ta.getId().length() - 8);
                FileOutputStream out = new FileOutputStream("outputs/transformed_by_set/4/" + newName + ".txt");
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                idx++;
                out.write((ta.getTokenizedText() + "\n").getBytes());
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void concatFiles() {
        try {
            FileOutputStream out = new FileOutputStream("outputs/combined_coref");
            for (int i = 0; i < 215; i++) {
                FileReader fileReader = new FileReader(new File("outputs/coref/test" + i + ".txt"));
                BufferedReader br = new BufferedReader(fileReader);
                String cur_coref = br.readLine();
                out.write((cur_coref + "\n").getBytes());
            }
            out.close();
        } catch (Exception e) {
        }
    }

    public static void renameFiles() {
        try {
            ACEReader aceReader = new ACEReader("data/original", false);
            int idx = 0;
            for (TextAnnotation ta : aceReader) {
                File file = new File("data/annotated_out/temp/test" + idx + ".ann");
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                fis.read(data);
                fis.close();
                idx++;
                String newName = ta.getId().substring(3, ta.getId().length() - 8);
                System.out.println(newName);
                FileOutputStream out = new FileOutputStream("data/annotated_out/span/" + newName + ".ann");
                out.write(data);
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        calculate_head_matches();
    }
}
