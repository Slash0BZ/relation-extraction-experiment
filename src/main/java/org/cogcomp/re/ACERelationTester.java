package org.cogcomp.re;

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import org.cogcomp.re.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import org.cogcomp.re.ACEMentionReader;

import java.io.File;
import java.util.*;

import java.lang.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import org.cogcomp.re.ACERelationConstrainedClassifier;

public class ACERelationTester {
    public static void processList(List<Object> in, double rate){
        Random rand = new Random();
        List<Object> to_remove = new ArrayList<Object>();
        for (int i = 0; i < in.size(); i++){
            Relation r = (Relation)(in.get(i));
            if (rand.nextDouble() < rate && r.getAttribute("RelationSubtype") == "NOT_RELATED"){
                in.remove(i);
            }
        }
    }

    public static void delete_files(){
        for (int i = 0; i < 5; i++) {
            File f = new File("src/main/java/org/cogcomp/re/classifier_fold_" + i);
            f.delete();
        }
    }

    public static String getCoarseType(String fine_type){
        if (fine_type.equals("Located") || fine_type.equals("Located_OP")
                || fine_type.equals("Near")){
            return "Physical";
        }
        if (fine_type.equals("Geographical") || fine_type.equals("Geographical_OP")
                || fine_type.equals("Subsidiary") || fine_type.equals("Subsidiary_OP")
                || fine_type.equals("Artifact") || fine_type.equals("Artifact_OP")){
            return "Part-whole";
        }
        if (fine_type.equals("Business")
                || fine_type.equals("Lasting-Personal")
                || fine_type.equals("Family")){
            return "Personal-Social";
        }
        if (fine_type.equals("Employment") || fine_type.equals("Employment_OP")
                || fine_type.equals("Ownership") || fine_type.equals("Ownership_OP")
                || fine_type.equals("Founder") || fine_type.equals("Founder_OP")
                || fine_type.equals("Student-Alum") || fine_type.equals("Student-Alum_OP")
                || fine_type.equals("Sports-Affiliation") || fine_type.equals("Sports-Affiliation_OP")
                || fine_type.equals("Investor-Shareholder") || fine_type.equals("Investor-Shareholder_OP")
                || fine_type.equals("Membership") || fine_type.equals("Membership_OP")){
            return "ORG-Affiliation";
        }
        if (fine_type.equals("User-Owner-Inventor-Manufacturer") || fine_type.equals("User-Owner-Inventor-Manufacturer_OP")){
            return "Agent-Artifact";
        }
        if (fine_type.equals("Citizen-Resident-Religion-Ethnicity") || fine_type.equals("Citizen-Resident-Religion-Ethnicity_OP")
                || fine_type.equals("Org-Location") || fine_type.equals("Org-Location_OP")){
            return "Gen-Affiliation";
        }
        if (fine_type.equals("NOT_RELATED")) {
            return "NOT_RELATED";
        }
        else{
            System.out.print("Err: " + fine_type);
            return "err";
        }
    }

    /*
     * This function trains and tests the five fold cv
     * It uses pre-extract and has the same result as lbjava:compile
     */
    public static void test_normal_cross_validation(){
        for (int i = 0; i < 5; i++) {
            is_null_label output = new is_null_label();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "relation_full_trim");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1,10);
            Parser parser_full = new ACEMentionReader("data/partition/eval/" + i, "relation_full_bi");
            TestDiscrete tester_full = TestDiscrete.testDiscrete(classifier, output, parser_full);
            tester_full.printPerformance(System.out);
            classifier.forget();
            parser_full.reset();
            train_parser.reset();
        }
        //delete_files();
    }

    /*
     * This function only tests the constrained classifier
     * It performs a similar five-fold cv
     */
    public static void test_constraint(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;
        List<String> outputs = new ArrayList<String>();
        Map<String, Integer> pMap = new HashMap<String, Integer>();
        Map<String, Integer> lMap = new HashMap<String, Integer>();
        Map<String, Integer> cMap = new HashMap<String, Integer>();
        int total_real_relation = 0;
        int real_relation_pm = 0;
        int real_relation_ps = 0;
        int real_relation_pp = 0;
        int real_relation_f = 0;
        int real_relation_all = 0;
        int total_null_relation = 0;
        int null_relation_pm = 0;
        int null_relation_ps = 0;
        int null_relation_pp = 0;
        int null_relation_f = 0;
        int null_relation_all = 0;
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "relation_full_bi");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1, 1);
            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            Parser parser_full = new ACEMentionReader("data/partition/eval/" + i, "relation_full_bi_test");
            for (Object example = parser_full.next(); example != null; example = parser_full.next()){
                String predicted_label = constrainedClassifier.discreteValue(example);
                if (predicted_label.equals("NOT_RELATED") == false){
                    if (pMap.containsKey(predicted_label)){
                        pMap.put(predicted_label, pMap.get(predicted_label) + 1);
                    }
                    else{
                        pMap.put(predicted_label, 1);
                    }
                    total_predicted ++;
                }
                String gold_label = output.discreteValue(example);
                if (gold_label.equals("NOT_RELATED") == false){
                    if (lMap.containsKey(gold_label)){
                        lMap.put(gold_label, lMap.get(gold_label) + 1);
                    }
                    else{
                        lMap.put(gold_label, 1);
                    }
                    total_labeled ++;
                }
                //if (getCoarseType(predicted_label).equals(getCoarseType(gold_label))){
                if (predicted_label.equals(gold_label)){
                    if (predicted_label.equals("NOT_RELATED") == false){
                        if (cMap.containsKey(gold_label)){
                            cMap.put(gold_label, cMap.get(gold_label) + 1);
                        }
                        else{
                            cMap.put(gold_label, 1);
                        }
                        total_correct ++;
                    }
                }
                else{
                    if (gold_label.equals("NOT_RELATED") == false){
                        //Relation r = (Relation)example;
                        //System.out.println(r.getSource().toString() + " " + r.getTarget().toString() + " " + gold_label + " " + predicted_label);
                    }
                }
                Relation r = (Relation)example;
                if (r.getAttribute("RelationSubtype").equals("NOT_RELATED")){
                    total_null_relation++;
                    if (RelationFeatureExtractor.isPremodifier(r)){
                        null_relation_pm++;
                    }
                    if (RelationFeatureExtractor.isPossessive(r)){
                        null_relation_ps++;
                    }
                    if (RelationFeatureExtractor.isPreposition(r)){
                        null_relation_pp++;
                    }
                    if (RelationFeatureExtractor.isFormulaic(r)){
                        null_relation_f++;
                    }
                    if (RelationFeatureExtractor.isFourType(r)){
                        null_relation_all++;
                    }
                }
                else {
                    total_real_relation++;
                    if (RelationFeatureExtractor.isPremodifier(r)){
                        real_relation_pm++;
                    }
                    if (RelationFeatureExtractor.isPossessive(r)){
                        real_relation_ps++;
                    }
                    if (RelationFeatureExtractor.isPreposition(r)){
                        real_relation_pp++;
                    }
                    if (RelationFeatureExtractor.isFormulaic(r)){
                        real_relation_f++;
                    }
                    if (RelationFeatureExtractor.isFourType(r)){
                        real_relation_all++;
                        outputs.add(r.getSource().toString() + " | " + r.getTarget().toString() + " " + gold_label);
                    }
                }
            }
            classifier.forget();
            parser_full.reset();
            train_parser.reset();
        }
        for (String o : outputs){
            System.out.println(o);
        }
        for (String s : lMap.keySet()){
            System.out.println(s + ": [labeled] " + lMap.get(s) + ", [predicted] " + pMap.get(s) + ", [correct] " + cMap.get(s));
        }
        System.out.println("Real: " + total_real_relation + "; premodifer: " + real_relation_pm + "; possessive: " + real_relation_ps
        + "; preposition: " + real_relation_pp + "; formulaic: " + real_relation_f + "; all: " + real_relation_all);
        System.out.println("Null: " + total_null_relation + "; premodifer: " + null_relation_pm + "; possessive: " + null_relation_ps
                + "; preposition: " + null_relation_pp + "; formulaic: " + null_relation_f + "; all: " + null_relation_all);
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total correct: " + total_correct);
        double p = (double)total_correct * 100.0/ (double)total_predicted;
        double r = (double)total_correct * 100.0/ (double)total_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        //delete_files();
    }

    public static void test_constraint_predicted(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "relation_full_bi");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1, 1);
            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            Parser parser_full = new PredictedMentionReader("data/partition/eval/" + i);
            for (Object example = parser_full.next(); example != null; example = parser_full.next()){
                String predicted_label = constrainedClassifier.discreteValue(example);
                if (predicted_label.equals("NOT_RELATED") == false){
                    total_predicted ++;
                }
                String gold_label = output.discreteValue(example);
                if (gold_label.equals("NOT_RELATED") == false){
                    total_labeled ++;
                }
                if (getCoarseType(predicted_label).equals(getCoarseType(gold_label))){
                //if (predicted_label.equals(gold_label)){
                    if (predicted_label.equals("NOT_RELATED") == false){
                        total_correct ++;
                    }
                }
            }
            /*
            TestDiscrete tester_full = TestDiscrete.testDiscrete(constrainedClassifier, output, parser_full);
            tester_full.printPerformance(System.out);
            */
            classifier.forget();
            parser_full.reset();
            train_parser.reset();
        }
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total correct: " + total_correct);
        double p = (double)total_correct * 100.0/ (double)total_predicted;
        double r = (double)total_correct * 100.0/ (double)total_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        //delete_files();
    }
    public static void main(String[] args){
        test_constraint();
    }
}
