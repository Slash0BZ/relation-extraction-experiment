package org.cogcomp.re;
import edu.illinois.cs.cogcomp.edison.features.factory.DependencyPath;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.*;
import java.lang.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.trees.*;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.annotators.*;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;


/*
 * The feature extractor class for RE
 * Each extraction function returns a List of string features
 * Includes some helper functions
 */

public class RelationFeatureExtractor {

    /*
     * getEntityHeadForConstituent
     * This is the helper function to get entity head constituent for each mention
     * The returned constituent may contain multiple tokens
     */
    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        if (extentConstituent.getAttribute("IsPredicted") != null){
            return extentConstituent;
        }
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

    /*
     * getLexicalFeature
     * This function extracts the lexical feature set defined at
     * http://cogcomp.cs.illinois.edu/papers/ChanRo10.pdf Table 1
     */
    public List<String> getLexicalFeature(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();
        for (int i = source.getStartSpan(); i < source.getEndSpan(); i++){
            ret_features.add(source.getTextAnnotation().getToken(i));
        }
        for (int i = target.getStartSpan(); i < target.getEndSpan(); i++) {
            ret_features.add(target.getTextAnnotation().getToken(i));
        }
        if (source.getEndSpan() == target.getStartSpan() - 1){
            ret_features.add("singleword_" + source.getTextAnnotation().getToken(source.getEndSpan()));
        }
        if (target.getEndSpan() == source.getStartSpan() - 1){
            ret_features.add("singleword_" + target.getTextAnnotation().getToken(target.getEndSpan()));
        }
        TextAnnotation ta = source.getTextAnnotation();
        if (target.getStartSpan() - source.getEndSpan() > 1) {
            ret_features.add("between_first_" + ta.getToken(source.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(target.getStartSpan() - 1));
            if (target.getStartSpan() - source.getEndSpan() > 2) {
                for (int i = source.getEndSpan() + 1; i < target.getStartSpan() - 1; i++) {
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        if (source.getStartSpan() - target.getEndSpan() > 1){
            ret_features.add("between_first_" + ta.getToken(target.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - target.getEndSpan() > 2){
                for (int i = target.getEndSpan() + 1; i < source.getStartSpan() - 1; i++){
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        int sentenceStart = ta.getSentence(source.getSentenceId()).getStartSpan();
        int sentenceEnd = ta.getSentence(source.getSentenceId()).getEndSpan();

        if (source.getStartSpan() - sentenceStart > 0){
            ret_features.add("fwM1_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - sentenceEnd > 1){
                ret_features.add("swM1_" + ta.getToken(source.getStartSpan() - 2));
            }
        }
        if (sentenceEnd - target.getEndSpan() > 0){
            ret_features.add("fwM2_" + ta.getToken(target.getEndSpan()));
            if (sentenceEnd - target.getEndSpan() > 1){
                ret_features.add("swM2_" + ta.getToken(target.getEndSpan() + 1));
            }
        }

        ret_features.add("HM1_" + sourceHeadWord);
        ret_features.add("HM2_" + targetHeadWord);
        ret_features.add("HM12_" + sourceHeadWord + "_" + targetHeadWord);

        return ret_features;
    }


    public List<String> getLexicalFeatureHeadOnly(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();
        for (int i = sourceHead.getStartSpan(); i < sourceHead.getEndSpan(); i++){
            ret_features.add(source.getTextAnnotation().getToken(i));
        }
        for (int i = targetHead.getStartSpan(); i < targetHead.getEndSpan(); i++) {
            ret_features.add(target.getTextAnnotation().getToken(i));
        }
        if (sourceHead.getEndSpan() == targetHead.getStartSpan() - 1){
            ret_features.add("singleword_" + source.getTextAnnotation().getToken(sourceHead.getEndSpan()));
        }
        if (targetHead.getEndSpan() == sourceHead.getStartSpan() - 1){
            ret_features.add("singleword_" + target.getTextAnnotation().getToken(targetHead.getEndSpan()));
        }
        TextAnnotation ta = source.getTextAnnotation();
        if (targetHead.getStartSpan() - sourceHead.getEndSpan() > 1) {
            ret_features.add("between_first_" + ta.getToken(sourceHead.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(targetHead.getStartSpan() - 1));
            if (targetHead.getStartSpan() - sourceHead.getEndSpan() > 2) {
                for (int i = sourceHead.getEndSpan() + 1; i < targetHead.getStartSpan() - 1; i++) {
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        if (sourceHead.getStartSpan() - targetHead.getEndSpan() > 1){
            ret_features.add("between_first_" + ta.getToken(targetHead.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(sourceHead.getStartSpan() - 1));
            if (sourceHead.getStartSpan() - targetHead.getEndSpan() > 2){
                for (int i = targetHead.getEndSpan() + 1; i < sourceHead.getStartSpan() - 1; i++){
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        int sentenceStart = ta.getSentence(source.getSentenceId()).getStartSpan();
        int sentenceEnd = ta.getSentence(source.getSentenceId()).getEndSpan();

        if (sourceHead.getStartSpan() - sentenceStart > 0){
            ret_features.add("fwM1_" + ta.getToken(sourceHead.getStartSpan() - 1));
            if (sourceHead.getStartSpan() - sentenceEnd > 1){
                ret_features.add("swM1_" + ta.getToken(sourceHead.getStartSpan() - 2));
            }
        }
        if (sentenceEnd - targetHead.getEndSpan() > 0){
            ret_features.add("fwM2_" + ta.getToken(targetHead.getEndSpan()));
            if (sentenceEnd - targetHead.getEndSpan() > 1){
                ret_features.add("swM2_" + ta.getToken(targetHead.getEndSpan() + 1));
            }
        }

        ret_features.add("HM1_" + sourceHeadWord);
        ret_features.add("HM2_" + targetHeadWord);
        ret_features.add("HM12_" + sourceHeadWord + "_" + targetHeadWord);

        return ret_features;
    }
    /*
     * getLexicalFeature_BC
     * Replaces all the words in lexical features to BrownCluster representations
     */
    public List<String> getLexicalFeature_BC(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        TextAnnotation ta = source.getTextAnnotation();
        for (int i = source.getStartSpan(); i < source.getEndSpan(); i++){
            ret_features.add("bc_" + getBrownClusterLabel(ta, i));
        }
        for (int i = target.getStartSpan(); i < target.getEndSpan(); i++) {
            ret_features.add("bc_" + getBrownClusterLabel(ta, i));
        }
        if (source.getEndSpan() == target.getStartSpan() - 1){
            ret_features.add("singleword_bc_" + getBrownClusterLabel(ta, source.getEndSpan()));
        }
        if (target.getEndSpan() == source.getStartSpan() - 1){
            ret_features.add("singleword_bc_" + getBrownClusterLabel(ta, target.getEndSpan()));
        }
        if (target.getStartSpan() - source.getEndSpan() > 1) {
            ret_features.add("between_first_bc_" + getBrownClusterLabel(ta, source.getEndSpan()));
            ret_features.add("between_first_bc_" + getBrownClusterLabel(ta, target.getStartSpan() - 1));
            if (target.getStartSpan() - source.getEndSpan() > 2) {
                for (int i = source.getEndSpan() + 1; i < target.getStartSpan() - 1; i++) {
                    ret_features.add("in_between_bc_" + getBrownClusterLabel(ta, i));
                }
            }
        }
        if (source.getStartSpan() - target.getEndSpan() > 1){
            ret_features.add("between_first_bc_" + getBrownClusterLabel(ta, target.getEndSpan()));
            ret_features.add("between_first_bc_" + getBrownClusterLabel(ta, source.getStartSpan() - 1));
            if (source.getStartSpan() - target.getEndSpan() > 2){
                for (int i = target.getEndSpan() + 1; i < source.getStartSpan() - 1; i++){
                    ret_features.add("in_between_bc_" + getBrownClusterLabel(ta, i));
                }
            }
        }
        int sentenceStart = ta.getSentence(source.getSentenceId()).getStartSpan();
        int sentenceEnd = ta.getSentence(source.getSentenceId()).getEndSpan();

        if (source.getStartSpan() - sentenceStart > 0){
            ret_features.add("fwM1_bc_" + getBrownClusterLabel(ta,source.getStartSpan() - 1));
            if (source.getStartSpan() - sentenceEnd > 1){
                ret_features.add("swM1_bc_" + getBrownClusterLabel(ta,source.getStartSpan() - 2));
            }
        }
        if (sentenceEnd - target.getEndSpan() > 0){
            ret_features.add("fwM2_bc_" + getBrownClusterLabel(ta, target.getEndSpan()));
            if (sentenceEnd - target.getEndSpan() > 1){
                ret_features.add("swM2_bc_" + getBrownClusterLabel(ta, target.getEndSpan() + 1));
            }
        }

        ret_features.add("HM1_bc_" + getBrownClusterLabel(ta, sourceHead.getStartSpan()));
        ret_features.add("HM2_bc_" + getBrownClusterLabel(ta, targetHead.getStartSpan()));
        ret_features.add("HM12_bc_" + getBrownClusterLabel(ta, sourceHead.getStartSpan()) + "_" + getBrownClusterLabel(ta, targetHead.getStartSpan()));

        return ret_features;

    }

    /*
     * getCollocationsFeature
     * This function extracts the collocation feature set defined at
     * http://cogcomp.cs.illinois.edu/papers/ChanRo10.pdf Table 1
     */
    public List<String> getCollocationsFeature(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();

        //Source Features
        String source_c_m1_p1 = "s_m1_p1_";
        for (int i = sourceHead.getStartSpan() - 1; i < sourceHead.getEndSpan() + 1; i++) {
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m1_p1 = source_c_m1_p1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m1_p1);

        String source_c_m2_m1 = "s_m2_m1_";
        for (int i = sourceHead.getStartSpan() - 2; i < sourceHead.getStartSpan(); i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m2_m1 = source_c_m2_m1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m2_m1);

        String source_c_p1_p2 = "s_p1_p2_";
        for (int i = sourceHead.getEndSpan(); i < sourceHead.getEndSpan() + 2; i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_p1_p2 = source_c_p1_p2 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_p1_p2);

        if (sourceHead.getStartSpan() > source.getStartSpan()) {
            ret_features.add("s_m1_m1_" + source.getTextAnnotation().getToken(sourceHead.getStartSpan() - 1));
        }
        if (sourceHead.getEndSpan() < source.getEndSpan()) {
            ret_features.add("s_p1_p1_" + source.getTextAnnotation().getToken(sourceHead.getEndSpan()));
        }

        //Target Features
        String target_c_m1_p1 = "t_m1_p1_";
        for (int i = targetHead.getStartSpan() - 1; i < targetHead.getEndSpan() + 1; i++) {
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m1_p1 = target_c_m1_p1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m1_p1);

        String target_c_m2_m1 = "t_m2_m1_";
        for (int i = targetHead.getStartSpan() - 2; i < targetHead.getStartSpan(); i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m2_m1 = target_c_m2_m1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m2_m1);

        String target_c_p1_p2 = "t_p1_p2_";
        for (int i = targetHead.getEndSpan(); i < targetHead.getEndSpan() + 2; i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_p1_p2 = target_c_p1_p2 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_p1_p2);

        if (targetHead.getStartSpan() > target.getStartSpan()) {
            ret_features.add("t_m1_m1_" + target.getTextAnnotation().getToken(targetHead.getStartSpan() - 1));
        }
        if (targetHead.getEndSpan() < target.getEndSpan()) {
            ret_features.add("t_p1_p1_" + target.getTextAnnotation().getToken(targetHead.getEndSpan()));
        }

        return ret_features;
    }

    public List<String> getCollocationsFeatureHeadOnly(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();

        //Source Features
        String source_c_m1_p1 = "s_m1_p1_";
        for (int i = sourceHead.getStartSpan() - 1; i < sourceHead.getEndSpan() + 1; i++) {
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                source_c_m1_p1 = source_c_m1_p1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m1_p1);

        String source_c_m2_m1 = "s_m2_m1_";
        for (int i = sourceHead.getStartSpan() - 2; i < sourceHead.getStartSpan(); i++){
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                source_c_m2_m1 = source_c_m2_m1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m2_m1);

        String source_c_p1_p2 = "s_p1_p2_";
        for (int i = sourceHead.getEndSpan(); i < sourceHead.getEndSpan() + 2; i++){
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                source_c_p1_p2 = source_c_p1_p2 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_p1_p2);

        if (sourceHead.getStartSpan() > 0) {
            ret_features.add("s_m1_m1_" + source.getTextAnnotation().getToken(sourceHead.getStartSpan() - 1));
        }
        if (sourceHead.getEndSpan() < source.getTextAnnotation().getTokens().length) {
            ret_features.add("s_p1_p1_" + source.getTextAnnotation().getToken(sourceHead.getEndSpan()));
        }

        //Target Features
        String target_c_m1_p1 = "t_m1_p1_";
        for (int i = targetHead.getStartSpan() - 1; i < targetHead.getEndSpan() + 1; i++) {
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                target_c_m1_p1 = target_c_m1_p1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m1_p1);

        String target_c_m2_m1 = "t_m2_m1_";
        for (int i = targetHead.getStartSpan() - 2; i < targetHead.getStartSpan(); i++){
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                target_c_m2_m1 = target_c_m2_m1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m2_m1);

        String target_c_p1_p2 = "t_p1_p2_";
        for (int i = targetHead.getEndSpan(); i < targetHead.getEndSpan() + 2; i++){
            if (i >= 0 && i < source.getTextAnnotation().getTokens().length) {
                target_c_p1_p2 = target_c_p1_p2 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_p1_p2);

        if (targetHead.getStartSpan() > 0) {
            ret_features.add("t_m1_m1_" + target.getTextAnnotation().getToken(targetHead.getStartSpan() - 1));
        }
        if (targetHead.getEndSpan() < target.getTextAnnotation().getTokens().length) {
            ret_features.add("t_p1_p1_" + target.getTextAnnotation().getToken(targetHead.getEndSpan()));
        }

        return ret_features;
    }

    /*
     * getCollocationsFeature_BC
     * Replaces all the words in collocation feature set to BrownCluster representations
     */
    public List<String> getCollocationsFeature_BC(Relation r){

        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();
        TextAnnotation ta = source.getTextAnnotation();
        //Source Features
        String source_c_m1_p1 = "s_m1_p1_bc";
        for (int i = sourceHead.getStartSpan() - 1; i < sourceHead.getEndSpan() + 1; i++) {
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m1_p1 = source_c_m1_p1 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(source_c_m1_p1);

        String source_c_m2_m1 = "s_m2_m1_bc";
        for (int i = sourceHead.getStartSpan() - 2; i < sourceHead.getStartSpan(); i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m2_m1 = source_c_m2_m1 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(source_c_m2_m1);

        String source_c_p1_p2 = "s_p1_p2_";
        for (int i = sourceHead.getEndSpan(); i < sourceHead.getEndSpan() + 2; i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_p1_p2 = source_c_p1_p2 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(source_c_p1_p2);

        if (sourceHead.getStartSpan() > source.getStartSpan()) {
            ret_features.add("s_m1_m1_" + getBrownClusterLabel(ta, sourceHead.getStartSpan() - 1));
        }
        if (sourceHead.getEndSpan() < source.getEndSpan()) {
            ret_features.add("s_p1_p1_" + getBrownClusterLabel(ta, sourceHead.getEndSpan()));
        }

        //Target Features
        String target_c_m1_p1 = "t_m1_p1_";
        for (int i = targetHead.getStartSpan() - 1; i < targetHead.getEndSpan() + 1; i++) {
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m1_p1 = target_c_m1_p1 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(target_c_m1_p1);

        String target_c_m2_m1 = "t_m2_m1_";
        for (int i = targetHead.getStartSpan() - 2; i < targetHead.getStartSpan(); i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m2_m1 = target_c_m2_m1 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(target_c_m2_m1);

        String target_c_p1_p2 = "t_p1_p2_";
        for (int i = targetHead.getEndSpan(); i < targetHead.getEndSpan() + 2; i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_p1_p2 = target_c_p1_p2 + getBrownClusterLabel(ta, i);
            }
        }
        ret_features.add(target_c_p1_p2);

        if (targetHead.getStartSpan() > target.getStartSpan()) {
            ret_features.add("t_m1_m1_" + getBrownClusterLabel(ta, targetHead.getStartSpan() - 1));
        }
        if (targetHead.getEndSpan() < target.getEndSpan()) {
            ret_features.add("t_p1_p1_" + getBrownClusterLabel(ta, targetHead.getEndSpan()));
        }

        return ret_features;
    }

    /*
     * getStructualFeature
     * This function extracts the structual feature set defined at
     * http://cogcomp.cs.illinois.edu/papers/ChanRo10.pdf Table 1
     */
    public List<String> getStructualFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        View mentionView = source.getTextAnnotation().getView(ViewNames.MENTION_ACE);
        if (target.getStartSpan() > source.getEndSpan()){
            List<Constituent> middle = mentionView.getConstituentsCoveringSpan(source.getEndSpan(), target.getStartSpan() - 1);
            ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            ret.add("middle_word_size_" + Integer.toString(target.getStartSpan() - source.getEndSpan()));
        }
        if (source.getStartSpan() > target.getEndSpan()){
            List<Constituent> middle = mentionView.getConstituentsCoveringSpan(target.getEndSpan(), source.getStartSpan() - 1);
            ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            ret.add("middle_word_size_" + Integer.toString(source.getStartSpan() - target.getEndSpan()));
        }
        if (source.doesConstituentCover(target)){
            ret.add("m2_in_m1");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m2_in_m1");
            ret.add("cb2_" + sourceHead.toString() + "_" + targetHead.toString() + "_m2_in_m1");
        }
        if (target.doesConstituentCover(source)){
            ret.add("m1_in_m2");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m1_in_m2");
            ret.add("cb2_" + sourceHead.toString() + "_" + targetHead.toString() + "_m1_in_m2");
        }
        return ret;
    }

    public List<String> getStructualFeatureHeadOnly(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        /*
        View mentionView = source.getTextAnnotation().getView(ViewNames.MENTION_ACE);
        if (sourceHead.getAttribute("IsPredicted") != null){
            mentionView = source.getTextAnnotation().getView("PREDICTED_MENTION_EXTRACTOR");
        }
        */
        if (targetHead.getStartSpan() > sourceHead.getEndSpan()){
            //List<Constituent> middle = mentionView.getConstituentsCoveringSpan(sourceHead.getEndSpan(), targetHead.getStartSpan() - 1);
            //ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            //ret.add("middle_word_size_" + Integer.toString(targetHead.getStartSpan() - sourceHead.getEndSpan()));
        }
        if (sourceHead.getStartSpan() > targetHead.getEndSpan()){
            //List<Constituent> middle = mentionView.getConstituentsCoveringSpan(targetHead.getEndSpan(), sourceHead.getStartSpan() - 1);
            //ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            //ret.add("middle_word_size_" + Integer.toString(sourceHead.getStartSpan() - targetHead.getEndSpan()));
        }
        return ret;
    }

    /*
     * getMentionFeature
     * This function extracts the mention feature set defined at
     * http://cogcomp.cs.illinois.edu/papers/ChanRo10.pdf Table 1
     */
    public List<String> getMentionFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        String source_m_lvl = source.getAttribute("EntityMentionType");
        String target_m_lvl = target.getAttribute("EntityMentionType");
        String source_main_type = source.getAttribute("EntityType");
        String target_main_type = target.getAttribute("EntityType");
        //String source_sub_type = source_main_type;
        //String target_sub_type = target_main_type;
        String source_sub_type = source.getAttribute("EntitySubtype");
        String target_sub_type = target.getAttribute("EntitySubtype");

        ret.add("mlvl_" + source_m_lvl + "_" + target_m_lvl);
        ret.add("mt_" + source_main_type + "_" + target_main_type);
        ret.add("st_" + source_sub_type + "_" + target_sub_type);
        ret.add("mlvl_mt_" + source_m_lvl + "_" + source_main_type + "_" + target_m_lvl + "_" + target_main_type);
        ret.add("mlvl_st_" + source_m_lvl + "_" + source_sub_type + "_" + target_m_lvl + "_" + target_sub_type);

        if (target.doesConstituentCover(source)){
            ret.add("mlvl_cont_1_" + source_m_lvl + "_" + target_m_lvl + "_" + "True");
        }
        else{
            ret.add("mlvl_cont_1_" + source_m_lvl + "_" + target_m_lvl + "_" + "False");
        }
        if (source.doesConstituentCover(target)){
            ret.add("mlvl_cont_2_" + source_m_lvl + "_" + target_m_lvl + "_" + "True");
        }
        else{
            ret.add("mlvl_cont_2_" + source_m_lvl + "_" + target_m_lvl + "_" + "False");
        }

        return ret;
    }

    /*
     * getDependenceFeature
     * This function extracts the dependency feature set defined at
     * http://cogcomp.cs.illinois.edu/papers/ChanRo10.pdf Table 1
     * Notice: This function expects View DEPENDENCY_STANFORD
     * The feature "dep labels between m1 and m2" was commented due to performance issues
     */
    public List<String> getDependencyFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        TreeView parse = (TreeView) r.getSource().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent source_head = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent target_head = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");

        List<Constituent> source_parsed_list = parse.getConstituentsCoveringToken(source_head.getStartSpan());
        List<Constituent> target_parsed_list = parse.getConstituentsCoveringToken(target_head.getStartSpan());
        if (source.getSentenceId() == target.getSentenceId()){
            try {
                if (source_parsed_list.size() != 0 && target_parsed_list.size() != 0) {
                    Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
                    Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
                    ret.add(PathFeatureHelper.getDependencyPathString(source_parsed, target_parsed, 100));
                }
            }catch (Exception e){
                ret.add("no_path");
            }
        }
        else{
            ret.add("not_same_sentence");
        }
        try{
            if (source_parsed_list.size() != 0 && target_parsed_list.size() != 0) {
                Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
                Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
                ret.add("hw_" + source_head.toString() + "_" + parse.getParent(source_parsed).toString());
                ret.add("hw_" + target_head.toString() + "_" + parse.getParent(target_parsed).toString());
            }
        }catch (Exception e){
            ret.add("hw_parent_none");
        }
        //This block is commented out due to performance issues.
        //The features extracted by the commented block are listed in the paper
        //But they failed to bring performance consistency
        /*
        try {
            Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
            Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
            List<Constituent> spl = PathFeatureHelper.getPathToRoot(source_parsed, 100);
            List<Constituent> tpl = PathFeatureHelper.getPathToRoot(target_parsed, 100);
            for (Constituent sp : spl){
                ret.add(sp.getLabel());
            }
            for (Constituent tp : tpl){
                ret.add(tp.getLabel());
            }
        }catch (Exception e){
            ret.add("no_between_deplabel");
        }
        */
        return ret;
    }

    /*
     * getBrownClusterLabel
     * This is the helper function that extracts the brown cluster for a single token
     * When getConstituentsCoveringToken returns multiple constituents,
     * the function returns the one that matches the input token
     * Notice: This function expects view BROWN_CLUSTERS_c100
     */
    public String getBrownClusterLabel(TextAnnotation ta, int spanIdex){
        View bcView = ta.getView("BROWN_CLUSTERS_c100");
        for (Constituent c : bcView.getConstituentsCoveringToken(spanIdex)){
            if (c.toString().equals(ta.getToken(spanIdex).toString())){
                return (c.getLabel() + "00000000").substring(0, 4);
            }

        }
        return "NULL_LABEL";
    }

    /*
     * This feature extracts the BOW feature in Brown Cluster representations
     * for the source and target in the given relation
     * THis feature set is for experimenting purposes.
     */
    public List<String> getBrownClusterBOWFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        for (int i = source.getStartSpan(); i < source.getEndSpan(); i++){
            ret.add("BrownCluster_" + getBrownClusterLabel(source.getTextAnnotation(), i));
        }
        for (int i = target.getStartSpan(); i < target.getEndSpan(); i++){
            ret.add("BrownCluster_" + getBrownClusterLabel(target.getTextAnnotation(), i));
        }
        return ret;
    }

    public List<String> getPOSFeature(Relation r) {
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent sourceHead = getEntityHeadForConstituent(source, ta, "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, ta, "EntityHeads");
        View posView = ta.getView(ViewNames.POS);
        View mentionView = ta.getView(ViewNames.MENTION_ACE);
        if (source.getStartSpan() - target.getEndSpan() == 1){
            List<String> poss = posView.getLabelsCoveringToken(target.getEndSpan());
            if (poss.size() > 0) {
                ret.add("pos_single_word_" + poss.get(0));
            }
        }
        if (target.getStartSpan() - source.getEndSpan() == 1){
            List<String> poss = posView.getLabelsCoveringToken(source.getEndSpan());
            if (poss.size() > 0) {
                ret.add("pos_single_word_" + poss.get(0));
            }
        }

        List<String> sourceHeadPosList = posView.getLabelsCoveringToken(sourceHead.getStartSpan());
        List<String> targetHeadPosList = posView.getLabelsCoveringToken(targetHead.getStartSpan());
        String sourceHeadPos = "NULL_POS";
        String targetHeadPos = "NULL_POS";
        if (sourceHeadPosList.size() > 0){
            sourceHeadPos = sourceHeadPosList.get(0);
        }
        if (targetHeadPosList.size() > 0){
            targetHeadPos = targetHeadPosList.get(0);
        }
        String s_pos_m1_m1 = "NULL_s_pos_m1_m1";
        String s_pos_m2_m1 = "NULL_s_pos_m2_m1";
        if (sourceHead.getStartSpan() > 0){
            s_pos_m1_m1 = posView.getLabelsCoveringToken(sourceHead.getStartSpan() - 1).get(0);
            if (sourceHead.getStartSpan() > 1){
                s_pos_m2_m1 = posView.getLabelsCoveringToken(sourceHead.getStartSpan() - 2).get(0)
                              + posView.getLabelsCoveringToken(sourceHead.getStartSpan() - 1).get(0);
            }
        }
        String s_pos_p1_p1 = "NULL_s_pos_p1_p1";
        String s_pos_p1_p2 = "NULL_s_pos_p1_p2";
        if (sourceHead.getEndSpan() < ta.getTokens().length){
            s_pos_p1_p1 = posView.getLabelsCoveringToken(sourceHead.getEndSpan()).get(0);
            if (sourceHead.getEndSpan() < ta.getTokens().length - 1){
                s_pos_p1_p2 = posView.getLabelsCoveringToken(sourceHead.getEndSpan()).get(0)
                              + posView.getLabelsCoveringToken(sourceHead.getEndSpan() + 1).get(0);
            }
        }
        String s_pos_m1_p1 = "NULL_s_pos_m1_p1";
        if (sourceHead.getStartSpan() > 0 && sourceHead.getEndSpan() < ta.getTokens().length){
            s_pos_m1_p1 = posView.getLabelsCoveringToken(sourceHead.getStartSpan() - 1).get(0)
                          + posView.getLabelsCoveringToken(sourceHead.getEndSpan()).get(0);
        }
        String t_pos_m1_m1 = "NULL_t_pos_m1_m1";
        String t_pos_m2_m1 = "NULL_t_pos_m2_m1";
        if (targetHead.getStartSpan() > 0){
            t_pos_m1_m1 = posView.getLabelsCoveringToken(targetHead.getStartSpan() - 1).get(0);
            if (targetHead.getStartSpan() > 1){
                t_pos_m2_m1 = posView.getLabelsCoveringToken(targetHead.getStartSpan() - 2).get(0)
                              + posView.getLabelsCoveringToken(targetHead.getStartSpan() - 1).get(0);
            }
        }
        String t_pos_p1_p1 = "NULL_t_pos_p1_p1";
        String t_pos_p1_p2 = "NULL_t_pos_p1_p2";
        if (targetHead.getEndSpan() < ta.getTokens().length){
            t_pos_p1_p1 = posView.getLabelsCoveringToken(targetHead.getEndSpan()).get(0);
            if (targetHead.getEndSpan() < ta.getTokens().length - 1){
                t_pos_p1_p2 = posView.getLabelsCoveringToken(targetHead.getEndSpan()).get(0)
                              + posView.getLabelsCoveringToken(targetHead.getEndSpan() + 1).get(0);
            }
        }
        String t_pos_m1_p1 = "NULL_t_pos_m1_p1";
        if (targetHead.getStartSpan() > 0 && targetHead.getEndSpan() < ta.getTokens().length){
            t_pos_m1_p1 = posView.getLabelsCoveringToken(targetHead.getStartSpan() - 1).get(0)
                          + posView.getLabelsCoveringToken(targetHead.getEndSpan()).get(0);
        }
        ret.add("pos_1_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_p1_p1 + "_" + t_pos_p1_p1);
        ret.add("pos_2_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m1_m1 + "_" + t_pos_m1_m1);
        ret.add("pos_3_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m2_m1 + "_" + t_pos_m2_m1);
        ret.add("pos_4_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m1_p1 + "_" + t_pos_m1_p1);
        ret.add("pos_5_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_p1_p2 + "_" + t_pos_p1_p2);
        return ret;
    }

    public String getCorefTag(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        if (source.getAttribute("EntityID") != null && target.getAttribute("EntityID") != null) {
            if (source.getAttribute("EntityID").equals(target.getAttribute("EntityID"))) {
                return "TRUE";
            }
        }
        return "FALSE";
    }
}
