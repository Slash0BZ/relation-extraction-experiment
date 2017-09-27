package org.cogcomp.re;

import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.cogcomp.md.MentionAnnotator;

import java.util.*;

/**
 * Created by xuany on 9/22/2017.
 */
public class ExperimentPrinter {
    public static void printACESentenceWithRelation(){
        Parser train_parser = new ACEMentionReader("data/all", "relation_full_bi_test");
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("models/predicted_relation_classifier_all.lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("models/predicted_relation_classifier_all.ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);


        Random rn = new Random();
        Map<String, List<Relation>> print = new HashMap<>();
        try{
            Parser test_parser = new ACEMentionReader("data/partition_with_dev/dev", "relation_full_bi_test");
            for (Object object = test_parser.next(); object != null; object = test_parser.next()){
                Relation r = (Relation)object;
                if (r.getAttribute("RelationType").equals("NOT_RELATED") || r.getAttribute("RelationType").contains("_OP")){
                    continue;
                }
                if (print.containsKey(r.getAttribute("RelationType"))){
                    List<Relation> pre = print.get(r.getAttribute("RelationType"));
                    pre.add(r);
                    print.put(r.getAttribute("RelationType"), pre);
                }
                else {
                    List<Relation> pre = new ArrayList<>();
                    pre.add(r);
                    print.put(r.getAttribute("RelationType"), pre);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (String type : print.keySet()){
            List<Relation> relations = print.get(type);
            if (type.equals("PHYS")){
                for (Relation r : relations) {
                    String gotLocMD = "NO";
                    String gotLocNER = "NO";
                    TextAnnotation ta = r.getSource().getTextAnnotation();
                    Sentence sentence = ta.getSentence(r.getSource().getSentenceId());
                    String predictedFineType = constrainedClassifier.discreteValue(r);
                    Constituent targetHead = RelationFeatureExtractor.getEntityHeadForConstituent(r.getTarget(), ta, "A");
                    if (ta.getView(ViewNames.MENTION).getConstituentsCoveringSpan(targetHead.getStartSpan(), targetHead.getEndSpan()).size() > 0){
                        Constituent predictedTargetHead = ta.getView(ViewNames.MENTION).getConstituentsCoveringSpan(targetHead.getStartSpan(), targetHead.getEndSpan()).get(0);
                        if (predictedTargetHead.getAttribute("EntityType").equals("LOC")){
                            gotLocMD = "YES";
                        }
                    }
                    if (ta.getView(ViewNames.NER_CONLL).getConstituentsCoveringSpan(targetHead.getStartSpan(), targetHead.getEndSpan()).size() > 0){
                        Constituent predictedTargetHead = ta.getView(ViewNames.NER_CONLL).getConstituentsCoveringSpan(targetHead.getStartSpan(), targetHead.getEndSpan()).get(0);
                        if (predictedTargetHead.getLabel().equals("LOC")){
                            gotLocNER = "YES";
                        }
                    }
                    String predictedCoarseType = ACERelationTester.getCoarseType(predictedFineType);
                    System.out.println(sentence.toString() + "\t" + r.getSource() + ":[" + r.getAttribute("RelationType") + "]:" + r.getTarget()
                            + "\t" + predictedCoarseType + "\t" + gotLocMD + "\t" + gotLocNER);
                }
            }
            /*
            else {
                for (Relation r : relations) {
                    TextAnnotation ta = r.getSource().getTextAnnotation();
                    Sentence sentence = ta.getSentence(r.getSource().getSentenceId());
                    String predictedFineType = constrainedClassifier.discreteValue(r);
                    String predictedCoarseType = ACERelationTester.getCoarseType(predictedFineType);
                    System.out.println(sentence.toString() + "\t" + r.getSource() + ":[" + r.getAttribute("RelationType") + "]:" + r.getTarget()
                            + "\t" + predictedCoarseType);
                }
            }
            */
        }
    }
    public static void main(String[] args){
        printACESentenceWithRelation();
    }
}
