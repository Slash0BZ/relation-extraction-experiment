package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.annotators.BrownClusterViewGenerator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.md.MentionAnnotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by xuany on 9/22/2017.
 */
public class SemEvalMentionReader implements Parser {

    private List<Relation> relations;
    private int counter = 0;
    private POSAnnotator _posAnnotator;
    private FlatGazetteers _gazetteers;
    private BrownClusters _brownClusters;
    private WordNetManager _wordnet;
    private MentionAnnotator _mentionAnnotator;
    private NERAnnotator _nerAnnotator;

    public void initExternalTools(){
        try {
            _posAnnotator = new POSAnnotator();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            Vector<String> bcs = new Vector<>();
            bcs.add("brown-clusters" + File.separator + "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
            bcs.add("brown-clusters" + File.separator + "brownBllipClusters");
            bcs.add("brown-clusters" + File.separator + "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
            Vector<Integer> bcst = new Vector<>();
            bcst.add(5);
            bcst.add(5);
            bcst.add(5);
            Vector<Boolean> bcsl = new Vector<>();
            bcsl.add(false);
            bcsl.add(false);
            bcsl.add(false);
            BrownClusters.init(bcs, bcst, bcsl);
            WordNetManager.loadConfigAsClasspathResource(true);
            _wordnet = WordNetManager.getInstance();
            _gazetteers = (FlatGazetteers)GazetteersFactory.get();
            _brownClusters = BrownClusters.get();
            _mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            _nerAnnotator = new NERAnnotator(ViewNames.NER_CONLL);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<TextAnnotation> readTrainFile(String fileName, String mode){
        List<String> sentences = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<TextAnnotation> ret = new ArrayList<>();
        int counter = 0;
        Random rn = new Random();
        if (mode.equals("TRAIN")) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (counter % 4 == 0) {
                        String curSentence = line.split("\t")[1];
                        if (curSentence.charAt(0) == '"'){
                            curSentence = curSentence.substring(1);
                        }
                        if (curSentence.charAt(curSentence.length() - 1) == '"'){
                            curSentence = curSentence.substring(0, curSentence.length() - 1);
                        }
                        sentences.add(curSentence);
                    }
                    if (counter % 4 == 1) {
                        types.add(line);
                    }
                    counter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mode.equals("TEST")){
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String curSentence = line.split("\t")[1];
                    if (curSentence.charAt(0) == '"'){
                        curSentence = curSentence.substring(1);
                    }
                    if (curSentence.charAt(curSentence.length() - 1) == '"'){
                        curSentence = curSentence.substring(0, curSentence.length() - 1);
                    }
                    sentences.add(curSentence);
                    types.add("UNKNOWN");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StatefulTokenizer statefulTokenizer = new StatefulTokenizer();

        for (int i = 0; i < sentences.size(); i++){
            List<String[]> tokens = new ArrayList<>();
            String sentence = sentences.get(i);
            String type = types.get(i);
            Pair<String[], IntPair[]> tokenizedSentence = statefulTokenizer.tokenizeSentence(sentence);
            List<String> curTokens = new LinkedList<String>(Arrays.asList(tokenizedSentence.getFirst()));
            int firstArgStart = 0;
            int firstArgEnd = 0;
            int secondArgStart = 0;
            int secondArgEnd = 0;
            for (int j = 0; j < curTokens.size(); j++){
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e1") && curTokens.get(j + 2).equals(">")){
                    firstArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e1") && curTokens.get(j + 3).equals(">")){
                    firstArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e2") && curTokens.get(j + 2).equals(">")){
                    secondArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e2") && curTokens.get(j + 3).equals(">")){
                    secondArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
            }
            tokens.add(curTokens.toArray(new String[curTokens.size()]));
            TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
            try {
                ta.addView(_posAnnotator);
                View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", ta);
                for (Constituent co : ta.getView(ViewNames.TOKENS).getConstituents()){
                    Constituent c = co.cloneForNewView("RE_ANNOTATED");
                    for (String s : co.getAttributeKeys()){
                        c.addAttribute(s, co.getAttribute(s));
                    }
                    c.addAttribute("BC", _brownClusters.getPrefixesCombined(c.toString()));
                    c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(_wordnet, c));
                    c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(_wordnet, c));
                    annotatedTokenView.addConstituent(c);
                }
                ta.addView("RE_ANNOTATED", annotatedTokenView);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            SpanLabelView mentionView = new SpanLabelView("MENTIONS", "MENTIONS", ta, 1.0f);
            Constituent firstArg = new Constituent("MENTION", 1.0f, "MENTIONS", ta, firstArgStart, firstArgEnd);
            Constituent secondArg = new Constituent("MENTION", 1.0f, "MENTIONS", ta, secondArgStart, secondArgEnd);
            firstArg.addAttribute("GAZ", _gazetteers.annotatePhrase(firstArg));
            secondArg.addAttribute("GAZ", _gazetteers.annotatePhrase(secondArg));

            mentionView.addConstituent(firstArg);
            mentionView.addConstituent(secondArg);
            if (type.contains("e1,e2")){
                Relation relation = new Relation(type.split("[(]")[0], firstArg, secondArg, 1.0f);
                relation.addAttribute("RelationSubtype", relation.getRelationName());
                mentionView.addRelation(relation);
            }
            else if (type.contains("e2,e1")){
                Relation relation = new Relation(type.split("[(]")[0], secondArg, firstArg, 1.0f);
                relation.addAttribute("RelationSubtype", relation.getRelationName());
                mentionView.addRelation(relation);
            }
            else{
                Relation relationLeft = new Relation(type, secondArg, firstArg, 1.0f);
                Relation relationRight = new Relation(type, firstArg, secondArg, 1.0f);
                relationLeft.addAttribute("RelationSubtype", relationLeft.getRelationName());
                relationRight.addAttribute("RelationSubtype", relationRight.getRelationName());
                mentionView.addRelation(relationLeft);
                mentionView.addRelation(relationRight);
            }

            ta.addView("MENTIONS", mentionView);
            ret.add(ta);
        }
        return ret;
    }

    public SemEvalMentionReader(String file_path, String mode){
        initExternalTools();
        relations = new ArrayList<>();
        List<TextAnnotation> tas = readTrainFile(file_path, mode);
        for (TextAnnotation ta : tas){
            for (Relation r : ta.getView("MENTIONS").getRelations()) {
                relations.add(r);
            }
        }
    }
    public void close(){}
    public Object next(){
        if (counter == relations.size()) {
            return null;
        } else {
            counter ++;
            return relations.get(counter - 1);
        }
    }

    public void reset(){
        counter = 0;
    }
}
