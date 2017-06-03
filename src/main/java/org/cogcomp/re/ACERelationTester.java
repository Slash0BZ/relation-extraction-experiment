package org.cogcomp.re;

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import org.cogcomp.re.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import org.cogcomp.re.ACEMentionReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    }
    /*
     * This function only tests the constrained classifier
     * It performs a similar five-fold cv
     */
    public static void test_constraint(){
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "relation_full_bi");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1, 10);
            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            Parser parser_full = new ACEMentionReader("data/partition/eval/" + i, "relation_full_bi");
            TestDiscrete tester_full = TestDiscrete.testDiscrete(constrainedClassifier, output, parser_full);
            tester_full.printPerformance(System.out);
            classifier.forget();
            parser_full.reset();
            train_parser.reset();
        }
    }

    public static void test_constraint_predicted(){
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "relation_full_bi");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("src/main/java/org/cogcomp/re/classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1, 10);
            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            Parser parser_full = new PredictedMentionReader("data/partition/eval/" + i);
            TestDiscrete tester_full = TestDiscrete.testDiscrete(constrainedClassifier, output, parser_full);
            tester_full.printPerformance(System.out);
            classifier.forget();
            parser_full.reset();
            train_parser.reset();
        }
    }
    public static void main(String[] args){
        test_constraint();
    }
}
