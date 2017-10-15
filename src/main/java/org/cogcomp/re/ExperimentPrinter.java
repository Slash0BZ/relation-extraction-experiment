package org.cogcomp.re;

import com.sun.org.apache.regexp.internal.RE;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
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
    public static void printDependency(){
        try{
            ACEReader aceReader = new ACEReader("data/all", false);
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            for (TextAnnotation ta : aceReader){
                System.out.println(ta.getId());
                if (ta.getId().equals("bn\\CNN_ENG_20030424_070008.15.apf.xml")){
                    continue;
                }
                stanfordDepHandler.addView(ta);
                for (Relation r : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                    System.out.println(ta.getSentenceFromToken(r.getSource().getStartSpan()));
                    System.out.println();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void printPattern(){
        try{
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            ACEReader aceReader = new ACEReader("data/partition_with_dev/dev", false);
            int pos_pattern = 0;
            int pos_nonpattern = 0;
            int neg_pattern = 0;
            int neg_nonpattern = 0;
            for (TextAnnotation ta : aceReader){
                List<Relation> null_relations = new ArrayList<>();
                if (ta.getId().equals("bn\\CNN_ENG_20030424_070008.15.apf.xml")){
                    continue;
                }
                stanfordDepHandler.addView(ta);
                View entityView = ta.getView(ViewNames.MENTION_ACE);
                for (int i = 0; i < ta.getNumberOfSentences(); i++) {
                    Sentence curSentence = ta.getSentence(i);
                    List<Constituent> cins = entityView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < cins.size(); j++) {
                        for (int k = j + 1; k < cins.size(); k++) {
                            Constituent firstArg = cins.get(j);
                            Constituent secondArg = cins.get(k);
                            Constituent firstArgHead = RelationFeatureExtractor.getEntityHeadForConstituent(firstArg, firstArg.getTextAnnotation(), "A");
                            Constituent secondArgHead = RelationFeatureExtractor.getEntityHeadForConstituent(secondArg, secondArg.getTextAnnotation(), "A");

                            boolean found = false;
                            for (Relation r : entityView.getRelations()) {
                                if (r.getSource().getStartSpan() == firstArg.getStartSpan() && r.getSource().getEndSpan() == firstArg.getEndSpan()
                                        && r.getTarget().getStartSpan() == secondArg.getStartSpan() && r.getTarget().getEndSpan() == secondArg.getEndSpan()) {
                                    found = true;
                                    break;
                                }
                                if (r.getTarget().getStartSpan() == firstArg.getStartSpan() && r.getTarget().getEndSpan() == firstArg.getEndSpan()
                                        && r.getSource().getStartSpan() == secondArg.getStartSpan() && r.getSource().getEndSpan() == secondArg.getEndSpan()) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Relation newRelation = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                null_relations.add(newRelation);
                            }
                        }
                    }
                }

                for (Relation r : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                    List<String> ret = RelationFeatureExtractor.patternRecognition(r.getSource(), r.getTarget());
                    RelationFeatureExtractor.patternRecognition(r.getTarget(), r.getSource());
                    Constituent source = r.getSource();
                    Constituent target = r.getTarget();
                    Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "TEST");
                    Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "TEST");

                    if (ret.contains("prep_pobj_dep_structure")){

                        pos_pattern ++;
                    }
                    else {
                        pos_nonpattern ++;
                    }
                }
                for (Relation r : null_relations){
                    List<String> ret = RelationFeatureExtractor.patternRecognition(r.getSource(), r.getTarget());
                    RelationFeatureExtractor.patternRecognition(r.getTarget(), r.getSource());
                    Constituent source = r.getSource();
                    Constituent target = r.getTarget();
                    Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "TEST");
                    Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "TEST");

                    if (ret.contains("prep_pobj_dep_structure")){
                        System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
                        System.out.println(source.toString() + " || " + target.toString());
                        System.out.println(source_head.toString() + " || " + target_head.toString());
                        System.out.println(source.getAttribute("EntityType") + " || " + target.getAttribute("EntityType"));
                        System.out.println();
                        neg_pattern ++;
                    }
                    else {
                        neg_nonpattern ++;
                    }
                }
            }
            System.out.println("POSITIVE: " + (double)pos_pattern / (double)(pos_nonpattern + pos_pattern));
            System.out.println("NEGATIVE: " + (double)neg_pattern / (double)(neg_nonpattern + neg_pattern));
            System.out.println("Pattern fit and gold / pattern fit: " + (double)pos_pattern / (double)(pos_pattern + neg_pattern));
            System.out.println("Pattern nonfit and gold / pattern nonfit: " + (double)pos_nonpattern / (double)(pos_nonpattern + neg_nonpattern));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void produceSamples(){
        try {
            Random rand = new Random();
            ACEReader aceReader = new ACEReader("data/dev", false);
            List<Relation> examples = new ArrayList<>();
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            for (TextAnnotation ta : aceReader){
                if (ta.getId().equals("bn\\CNN_ENG_20030424_070008.15.apf.xml")){
                    continue;
                }
                stanfordDepHandler.addView(ta);
                for (Relation r : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                    if (r.getAttribute("RelationType").equals("PHYS")) {
                        if (rand.nextDouble() < 0.05){
                            examples.add(r);
                        }
                    }
                }
            }
            IOHelper.serializeRelationsOut(examples, "preprocess/relations/PHYS");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void printSimilarities(){
        try {
            ACEReader aceReader = new ACEReader("data/partition_with_dev/dev", false);
            List<Relation> examples = IOHelper.inputRelationsNonBinary("preprocess/relations/PHYS_non_binary.txt");
            for (Relation r : examples){
                TextAnnotation ta = r.getSource().getTextAnnotation();
                Constituent source = r.getSource();
                Constituent target = r.getTarget();
                Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "");
                Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "");
                System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
                System.out.println(r.getAttribute("RelationType") + ":" + r.getAttribute("RelationSubtype"));
                System.out.println(source.toString() + " || " + target.toString());
                System.out.println(source_head.toString() + " || " + target_head.toString());
                List<String> fets = Comparator.compareRelationsHelper(r);
                for (String s : fets){
                    System.out.println(s);
                }
            }
            //System.exit(0);
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            Comparator comparator = new Comparator();
            double[] avg_is_phys = new double[14];
            for (int i = 0; i < avg_is_phys.length; i++){
                avg_is_phys[i] = 0.0;
            }
            int avg_is_phys_counter = 0;
            double[] avg_is_phys_op = new double[14];
            for (int i = 0; i < avg_is_phys_op.length; i++){
                avg_is_phys_op[i] = 0.0;
            }
            int avg_is_phys_op_counter = 0;
            double[] avg_not_phys = new double[14];
            for (int i = 0; i < avg_not_phys.length; i++){
                avg_not_phys[i] = 0.0;
            }
            int avg_not_phys_counter = 0;
            double[] avg_not_rel = new double[14];
            int avg_not_rel_counter = 0;
            ACEMentionReader aceMentionReader = IOHelper.serializeDataIn("preprocess/reader/dev");
            for (Relation r : aceMentionReader.readList()){

                if (r.getAttribute("RelationType").contains("PHYS")){
                    TextAnnotation ta = r.getSource().getTextAnnotation();
                    Constituent source = r.getSource();
                    Constituent target = r.getTarget();
                    Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "");
                    Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "");
                    System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
                    System.out.println(r.getAttribute("RelationType") + ":" + r.getAttribute("RelationSubtype"));
                    System.out.println(source.toString() + " || " + target.toString());
                    System.out.println(source_head.toString() + " || " + target_head.toString());
                    for (String s : Comparator.compareRelationsHelper(r)){
                        System.out.println(s);
                    }
                    System.out.println();
                }


                if (r.getAttribute("RelationType").contains("_OP")){
                    //continue;
                }
                if (r.getAttribute("RelationType").equals("PHYS")) {
                    double[] scores = comparator.score(r, "");
                    for (int i = 0; i < scores.length; i++){
                        avg_is_phys[i] += scores[i];
                    }
                    avg_is_phys_counter ++;
                }
                else if (r.getAttribute("RelationType").equals("PHYS_OP")){
                    double[] scores = comparator.score(r, "");
                    for (int i = 0; i < scores.length; i++){
                        avg_is_phys_op[i] += scores[i];
                    }
                    avg_is_phys_op_counter ++;
                }
                else if (!r.getAttribute("RelationType").equals("NOT_RELATED")){
                    double[] scores = comparator.score(r, "");
                    for (int i = 0; i < scores.length; i++){
                        avg_not_phys[i] += scores[i];
                    }
                    avg_not_phys_counter ++;
                }
                else {
                    double[] scores = comparator.score(r, "");
                    for (int i = 0; i < scores.length; i++){
                        avg_not_rel[i] += scores[i];
                    }
                    avg_not_rel_counter ++;
                }


            }
            for (int i = 0; i < avg_is_phys.length; i++){
                avg_is_phys[i] = avg_is_phys[i] / (double)avg_is_phys_counter;
                System.out.print(avg_is_phys[i] + "\t");
            }
            System.out.println("\n");
            for (int i = 0; i < avg_is_phys_op.length; i++){
                avg_is_phys_op[i] = avg_is_phys_op[i] / (double)avg_is_phys_op_counter;
                System.out.print(avg_is_phys_op[i] + "\t");
            }
            System.out.println("\n");
            for (int i = 0; i < avg_not_phys.length; i++){
                avg_not_phys[i] = avg_not_phys[i] / (double)avg_not_phys_counter;
                System.out.print(avg_not_phys[i] + "\t");
            }
            System.out.println("\n");
            for (int i = 0; i < avg_not_rel.length; i++){
                avg_not_rel[i] = avg_not_rel[i] / (double)avg_not_rel_counter;
                System.out.print(avg_not_rel[i] + "\t");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
       printSimilarities();
    }
}
