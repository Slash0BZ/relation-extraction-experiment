package org.cogcomp.re;

import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.omg.CORBA.OBJ_ADAPTER;

/**
 * Created by xuany on 6/22/2017.
 */
public class EntityTyperTester {

    public static void test_five_fold() {
        int total_correct_type = 0;
        int total_correct_subtype = 0;
        int total_correct_both = 0;
        int total_count = 0;
        for (int i = 0; i < 5; i++) {
            entity_type type_output = new entity_type();
            entity_subtype subtype_output = new entity_subtype();
            Parser train_parser = new ACEMentionReader("data/partition/train/" + i, "entity");
            entity_type_classifier etc = new entity_type_classifier();
            entity_subtype_classifier esc = new entity_subtype_classifier();
            etc.setLexiconLocation("tmp/etc_fold_" + i + ".lex");
            esc.setLexiconLocation("tmp/esc_fold_" + i + ".lex");
            BatchTrainer trainer_etc = new BatchTrainer(etc, train_parser);
            Lexicon etcLexicon = trainer_etc.preExtract("tmp/etc_fold_" + i + ".ex");
            etc.setLexicon(etcLexicon);
            trainer_etc.train(1);
            train_parser.reset();
            BatchTrainer trainer_esc = new BatchTrainer(esc, train_parser);
            Lexicon escLexicon = trainer_esc.preExtract("tmp/esc_fold_" + i + ".ex");
            esc.setLexicon(escLexicon);
            trainer_esc.train(1);
            train_parser.reset();

            Parser test_parser = new ACEMentionReader("data/partition/eval/" + i, "entity");
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                total_count ++;
                String predicted_type = etc.discreteValue(example);
                String predicted_subtype = esc.discreteValue(example);
                Boolean correctType = predicted_type.equals(type_output.discreteValue(example));
                Boolean correctSubtype = predicted_subtype.equals(subtype_output.discreteValue(example));
                if (correctType){
                    total_correct_type ++;
                }
                if (correctSubtype){
                    total_correct_subtype ++;
                }
                if (correctType && correctSubtype){
                    total_correct_both ++;
                }
            }
            test_parser.reset();
            esc.forget();
            etc.forget();
        }
        System.out.println("Total: " + total_count);
        System.out.println("Type match: " + total_correct_type);
        System.out.println("Subtype match: " + total_correct_subtype);
        System.out.println("Both match: " + total_correct_both);
    }

    public static void main(String[] args){
        test_five_fold();
    }
}
