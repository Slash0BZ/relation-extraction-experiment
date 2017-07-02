package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbj.coref.main.AllTest;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.*;

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;

    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

public class RelationAnnotatorStatistics {

    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        return ACEMentionReader.getEntityHeadForConstituent(extentConstituent, textAnnotation, viewName);
    }

    public static void main (String[] args){
        List<String> output = new ArrayList<String>();
        Map<String, Integer> missed_gold = new HashMap<>();
        Map<String, Integer> missed_gold_hit = new HashMap<>();
        Map<String, Integer> missed_predicted = new HashMap<>();
        try{
            ACEReader aceReader = new ACEReader("data/partition_with_dev/eval/4", false);
            int total_count = 0;
            int total_match = 0;
            int mention_type_match = 0;
            int mention_subtype_match = 0;
            int total_labeled_mention = 0;
            int total_predicted_mention = 0;
            int total_correct_mention = 0;
            int docCount = 0;
            entity_type_classifier etc = new entity_type_classifier("models/entity_type_classifier.lc", "models/entity_type_classifier.lex");
            entity_subtype_classifier esc = new entity_subtype_classifier("models/entity_subtype_classifier.lc", "models/entity_subtype_classifier.lex");
            for (TextAnnotation ta : aceReader){
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    View mentionView = ta.getView(ViewNames.MENTION_ACE);
                    //System.out.println("[SENTENCE]: " + ta.getSentence(i));
                    for (Constituent c : mentionView.getConstituentsCoveringSpan(ta.getSentence(i).getStartSpan(), ta.getSentence(i).getEndSpan())){
                       //System.out.println("[MENTION]: " + c.toString());
                    }
                }
                docCount++;
                //System.out.println("[DOC]: " + ta.getText());
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                List<Constituent> predictedMentions = AllTest.MentionTest(ta);
                List<Constituent> predictedMentionWithTypes = new ArrayList<Constituent>();
                for (Constituent c : predictedMentions){
                    String entity_type = etc.discreteValue(c);
                    String entity_subtype = esc.discreteValue(c);
                    c.addAttribute("EntityType", entity_type);
                    c.addAttribute("EntitySubtype", entity_subtype);
                    predictedMentionWithTypes.add(c);
                    //System.out.println("[PREDICTED]: " + c.toString() + " | [HEAD]: " + getEntityHeadForConstituent(c, ta, "A").toString());
                }
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE).getConstituents()){
                    //System.out.print("[GOLD]: " + c.toString()+ " | [HEAD]: " + getEntityHeadForConstituent(c, ta, "A").toString());
                    consMap.put(c,null);
                    for (Constituent pc : predictedMentionWithTypes){
                        Constituent ch = getEntityHeadForConstituent(c, ta, "TESTG");
                        Constituent pch = getEntityHeadForConstituent(pc, ta, "TESTP");
                        //if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
                        if ((ch.getStartSpan() >= pch.getEndSpan() || pch.getStartSpan() >= ch.getEndSpan()) == false){
                            consMap.put(c, pc);
                            //System.out.print(" [C] ");
                            break;
                        }
                    }
                    //System.out.println();
                }
                List<Constituent> goldMentions = ta.getView(ViewNames.MENTION_ACE).getConstituents();
                total_labeled_mention += goldMentions.size();
                total_predicted_mention += predictedMentionWithTypes.size();
                for (Constituent c : goldMentions){
                    if (consMap.get(c) != null){
                        total_correct_mention ++;
                    }
                    else{
                        String mistake = c.toString();
                        if (missed_gold.containsKey(mistake)){
                            missed_gold.put(mistake, missed_gold.get(mistake) + 1);
                        }
                        else{
                            missed_gold.put(mistake, 1);
                        }
                        if (c.toString().toLowerCase().contains("nasdaq") || c.toString().toLowerCase().contains("times")) {

                            System.out.println(ta.getId());
                            System.out.println("[DOC]: " + ta.getText());
                            System.out.println("[SENTENCE]: " + ta.getSentence(c.getSentenceId()));
                            System.out.println("Sentence start at " + ta.getSentence(c.getSentenceId()).getStartSpan());
                            System.out.println("Mention start at " + c.getStartSpan());
                            System.out.println("[PREDICTED_GOLD]: " + c.toString() + " | " + getEntityHeadForConstituent(c, ta, "A").toString());

                        }
                    }
                }
                for (Constituent c : predictedMentions){
                    if (!consMap.containsValue(c)){
                        String mistake = c.toString();
                        if (missed_predicted.containsKey(mistake)){
                            missed_predicted.put(mistake, missed_predicted.get(mistake) + 1);
                        }
                        else{
                            missed_predicted.put(mistake, 1);
                        }
                        if (c.toString().equals("it")) {
                            //System.out.println("[DOC]: " + ta.getText());
                            //System.out.println("[SENTENCE]: " + ta.getSentence(c.getSentenceId()));
                            //System.out.println("[PREDICTED]: " + c.toString() + " | " + getEntityHeadForConstituent(c, ta, "A").toString());
                        }
                    }
                }
                Set<String> missed_gold_list = missed_gold.keySet();
                for (Constituent c : goldMentions){
                    if (consMap.get(c) != null){
                        String hit = c.toString();
                        if (missed_gold_list.contains(hit)){
                            if (missed_gold_hit.containsKey(hit)){
                                missed_gold_hit.put(hit, missed_gold_hit.get(hit) + 1);
                            }
                            else {
                                missed_gold_hit.put(hit, 1);
                            }
                        }
                    }
                }
                List<Relation> goldRelations = ta.getView(ViewNames.MENTION_ACE).getRelations();
                total_count += goldRelations.size();
                for (Relation r : goldRelations){
                    if (consMap.get(r.getSource()) != null && consMap.get(r.getTarget()) != null){
                        total_match ++;
                        Constituent ps = consMap.get(r.getSource());
                        Constituent pt = consMap.get(r.getTarget());
                        if (ps.getAttribute("EntityType").equals(r.getSource().getAttribute("EntityType"))
                                && pt.getAttribute("EntityType").equals(r.getTarget().getAttribute("EntityType"))){
                            mention_type_match++;
                            if (ps.getAttribute("EntitySubtype").equals(r.getSource().getAttribute("EntitySubtype"))
                                    && pt.getAttribute("EntitySubtype").equals(r.getTarget().getAttribute("EntitySubtype"))){
                                mention_subtype_match ++;
                            }
                        }
                    }
                    else {
                        if (consMap.get(r.getSource()) == null){
                            //System.out.println(ta.getSentence(ta.getSentenceId(r.getSource())));
                            //System.out.println(getEntityHeadForConstituent(r.getSource(), ta, "A").toString());
                        }
                    }
                }
                if (docCount > 5) {
                    break;
                }
            }
            //ValueComparator bvc = new ValueComparator(missed_gold);
            //TreeMap<String, Integer> sorted_missed_gold = new TreeMap<>(bvc);

            for (String s : missed_gold.keySet()){
                //System.out.println(s + "\t" + missed_gold.get(s));
            }
            System.out.println("=======Relation Performance======");
            System.out.println("Total count: " + total_count);
            System.out.println("Total match: " + total_match);
            System.out.println("Type match: " + mention_type_match);
            System.out.println("Subtype match: " + mention_subtype_match);
            System.out.println("\n=======Mention Performance======");
            System.out.println("Total labeled mention: " + total_labeled_mention);
            System.out.println("Total predicted mention: " + total_predicted_mention);
            System.out.println("Total correct mention: " + total_correct_mention);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
