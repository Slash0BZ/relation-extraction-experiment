package org.cogcomp.re;
import com.sun.org.apache.regexp.internal.RE;
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

    public static boolean isPossessive(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        if ((source_head.getStartSpan() >= target.getStartSpan() && source_head.getEndSpan() <= target.getEndSpan())
            || (target_head.getStartSpan() >= source.getStartSpan() && target_head.getEndSpan() <= source.getEndSpan())){
            if (source_head.getStartSpan() > target_head.getStartSpan()){
                front = target;
                front_head = target_head;
                back = source;
                back_head = source_head;
            }
            else{
                front = source;
                front_head = source_head;
                back = target;
                back_head = target_head;
            }
        }
        if (front == null){
            return false;
        }
        for (int i = front_head.getEndSpan(); i < back_head.getStartSpan(); i++){
            if (ta.getToken(i).equals("'s")){
                return true;
            }
            if (i < back_head.getStartSpan() - 1 && ta.getToken(i).equals("'") && ta.getToken(i+1).equals("s")){
                return true;
            }
        }
        if (posView.getLabelsCoveringToken(front_head.getEndSpan()).get(0).equals("POS")){
            return true;
        }
        if (posView.getLabelsCoveringToken(front_head.getEndSpan() - 1).get(0).equals("PRP$")
                || posView.getLabelsCoveringToken(front_head.getEndSpan() - 1).get(0).equals("WP$")){
            return true;
        }
        return false;
    }

    public static boolean isNoun(String posTag){
        if (posTag.startsWith("NN") || posTag.startsWith("RB") || posTag.startsWith("WP")){
            return true;
        }
        return false;
    }

    public static boolean isPreposition(Relation r){
        if (RelationFeatureExtractor.isPossessive(r)){
            //return false;
        }
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        int SentenceStart = ta.getSentence(ta.getSentenceId(source)).getStartSpan();
        View posView = ta.getView(ViewNames.POS);
        View spView = ta.getView(ViewNames.SHALLOW_PARSE);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        boolean found_in_to = false;
        boolean noNp = true;
        for (int i = front_head.getEndSpan(); i < back.getStartSpan(); i++){
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN") || posView.getLabelsCoveringToken(i).get(0).equals("TO")){
                found_in_to = true;
            }
        }
        if (found_in_to && noNp){
            return true;
        }
        boolean found_in = false;
        noNp = true;
        /*
        for (int i = front_head.getStartSpan() - 1; i >= SentenceStart; i--){
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN")){
                found_in = true;
            }
        }
        if (found_in && noNp) {
            return true;
        }
        */
        found_in_to = false;
        noNp = true;
        boolean non_overlap = false;
        for (int i = front.getEndSpan(); i < back.getStartSpan(); i++){
            non_overlap = true;
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN") || posView.getLabelsCoveringToken(i).get(0).equals("TO")){
                found_in_to = true;
            }
        }
        if (found_in_to && noNp){
            return true;
        }
        found_in = false;
        for (int i = front.getStartSpan() - 1; i >= SentenceStart; i--){
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN")){
                found_in = true;
            }
        }
        if (found_in && noNp && non_overlap) {
            return true;
        }
        return false;
    }

    public static boolean isFormulaic(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        View spView = ta.getView(ViewNames.SHALLOW_PARSE);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        for (int i = front_head.getEndSpan(); i < back_head.getStartSpan() - 1; i++){
            if (!posView.getLabelsCoveringToken(i).get(0).startsWith("NN")
                    && !posView.getLabelsCoveringToken(i).get(0).equals(",")){
                return false;
            }
        }
        if ((front.getAttribute("EntityType").equals("PER") || front.getAttribute("EntityType").equals("ORG") || front.getAttribute("EntityType").equals("GPE")) &&
                (back.getAttribute("EntityType").equals("ORG") || back.getAttribute("EntityType").equals("GPE"))){
            return true;
        }
        return false;
    }

    public static boolean onlyNounBetween(Constituent front, Constituent back){
        TextAnnotation ta = front.getTextAnnotation();
        View posView = ta.getView(ViewNames.POS);
        for (int i = front.getEndSpan(); i < back.getStartSpan(); i++){
            if (!posView.getLabelsCoveringToken(i).get(0).startsWith("NN")){
                    //&& !posView.getLabelsCoveringToken(i).get(0).startsWith("JJ")){
                return false;
            }
        }
        return true;
    }

    public static boolean isPremodifier(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        View spView = ta.getView(ViewNames.SHALLOW_PARSE);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        if (front == null){
            return false;
        }
        if (front.getStartSpan() >= back.getStartSpan()) {
            if (front_head.getEndSpan() == back_head.getStartSpan() ||
                    (front_head.getEndSpan() == back_head.getStartSpan() - 1 && ta.getToken(front_head.getEndSpan()).contains(".")) ||
                    onlyNounBetween(front_head, back_head)) {
                if (front_head.getStartSpan() == back.getStartSpan()) {
                    if (posView.getLabelsCoveringToken(front_head.getStartSpan()).equals("PRP$")) {
                        return false;
                    }
                    return true;
                }
                for (int i = back.getStartSpan(); i < front_head.getStartSpan(); i++) {
                    if (!posView.getLabelsCoveringToken(i).get(0).startsWith("JJ") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("RB") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("VB") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("CD") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("DT") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("PD")) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isFourType (Relation r){
        return (isPremodifier(r) || isPossessive(r) || isFormulaic(r) || isPreposition(r));
    }


    public List<String> getLexicalFeaturePartA(Relation r){

        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        TextAnnotation ta = source.getTextAnnotation();
        for (int i = source.getStartSpan(); i < source.getEndSpan(); i++){
            ret_features.add(source.getTextAnnotation().getToken(i));
            ret_features.add("BC_" + getBrownClusterLabel(ta, i));
        }
        return ret_features;
    }
    public List<String> getLexicalFeaturePartB(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent target = r.getTarget();
        TextAnnotation ta = target.getTextAnnotation();
        for (int i = target.getStartSpan(); i < target.getEndSpan(); i++){
            ret_features.add(target.getTextAnnotation().getToken(i));
            ret_features.add("BC_" + getBrownClusterLabel(ta, i));
        }
        return ret_features;
    }
    public List<String> getLexicalFeaturePartC(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        if (source.getEndSpan() == target.getStartSpan() - 1){
            ret_features.add("singleword_" + source.getTextAnnotation().getToken(source.getEndSpan()));
            //ret_features.add("singlewordbc_" + getBrownClusterLabel(ta, source.getEndSpan()));
        }
        else if (target.getEndSpan() == source.getStartSpan() - 1){
            ret_features.add("singleword_" + target.getTextAnnotation().getToken(target.getEndSpan()));
            //ret_features.add("singlewordbc_" + getBrownClusterLabel(ta, target.getEndSpan()));
        }
        else {
            ret_features.add("No_singleword");
            //ret_features.add("No_singleword_bc");
        }
        return ret_features;
    }
    public List<String> getLexicalFeaturePartCC(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target = r.getTarget();
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        if (source_head.getEndSpan() < target_head.getStartSpan()){
            for (int i = source_head.getEndSpan(); i < target_head.getStartSpan(); i++) {
                ret_features.add("bowbethead_" + source.getTextAnnotation().getToken(i));
                ret_features.add("bowbetheadbc_" + getBrownClusterLabel(ta, i));
            }
        }
        if (target_head.getEndSpan() < source_head.getStartSpan()){
            for (int i = target_head.getEndSpan(); i < source_head.getStartSpan(); i++) {
                ret_features.add("bowbethead_" + source.getTextAnnotation().getToken(i));
                ret_features.add("bowbetheadbc_" + getBrownClusterLabel(ta, i));
            }
        }
        return ret_features;
    }
    public List<String> getLexicalFeaturePartD(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
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
        else if (source.getStartSpan() - target.getEndSpan() > 1){
            ret_features.add("between_first_" + ta.getToken(target.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - target.getEndSpan() > 2){
                for (int i = target.getEndSpan() + 1; i < source.getStartSpan() - 1; i++){
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        else {
            ret_features.add("No_between_features");
        }
        return ret_features;
    }
    public List<String> getLexicalFeaturePartE(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        int sentenceStart = ta.getSentence(source.getSentenceId()).getStartSpan();
        int sentenceEnd = ta.getSentence(source.getSentenceId()).getEndSpan();
        if (source.getStartSpan() - sentenceStart > 0){
            ret_features.add("fwM1_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - sentenceEnd > 1){
                ret_features.add("swM1_" + ta.getToken(source.getStartSpan() - 2));
            }
            else{
                ret_features.add("swM1_NULL");
            }
        }
        else{
            ret_features.add("fwM2_NULL");
            ret_features.add("swM1_NULL");
        }
        if (sentenceEnd - target.getEndSpan() > 0){
            ret_features.add("fwM2_" + ta.getToken(target.getEndSpan()));
            if (sentenceEnd - target.getEndSpan() > 1){
                ret_features.add("swM2_" + ta.getToken(target.getEndSpan() + 1));
            }
            else {
                ret_features.add("swM2_NULL");
            }
        }
        else {
            ret_features.add("fwM2_NULL");
            ret_features.add("swM2_NULL");
        }
        return ret_features;
    }

    public List<String> getLexicalFeaturePartF(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();
        ret_features.add("HM1_" + sourceHeadWord);
        ret_features.add("HM2_" + targetHeadWord);
        ret_features.add("HM12_" + sourceHeadWord + "_" + targetHeadWord);
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
        else{
            ret_features.add("s_m1_m1_null");
        }
        if (sourceHead.getEndSpan() < source.getEndSpan()) {
            ret_features.add("s_p1_p1_" + source.getTextAnnotation().getToken(sourceHead.getEndSpan()));
        }
        else {
            ret_features.add("s_p1_p1_null");
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
        else {
            ret_features.add("t_m1_m1_null");
        }
        if (targetHead.getEndSpan() < target.getEndSpan()) {
            ret_features.add("t_p1_p1_" + target.getTextAnnotation().getToken(targetHead.getEndSpan()));
        }
        else{
            ret_features.add("t_p1_p1_null");
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
        else if (source.getStartSpan() > target.getEndSpan()){
            List<Constituent> middle = mentionView.getConstituentsCoveringSpan(target.getEndSpan(), source.getStartSpan() - 1);
            ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            ret.add("middle_word_size_" + Integer.toString(source.getStartSpan() - target.getEndSpan()));
        }
        else{
            ret.add("middle_mention_size_null");
            ret.add("middle_word_size_null");
        }
        if (source.doesConstituentCover(target)){
            ret.add("m2_in_m1");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m2_in_m1");
            ret.add("cb2_" + sourceHead.toString() + "_" + targetHead.toString() + "_m2_in_m1");
        }
        else if (target.doesConstituentCover(source)){
            ret.add("m1_in_m2");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m1_in_m2");
            ret.add("cb2_" + sourceHead.toString() + "_" + targetHead.toString() + "_m1_in_m2");
        }
        else{
            ret.add("m1_m2_no_coverage");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m1_m2_no_coverage");
            ret.add("cb2_" + sourceHead.toString() + "_" + targetHead.toString() + "_m1_m2_no_coverage");
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
        TreeView parse = (TreeView) r.getSource().getTextAnnotation().getView(ViewNames.DEPENDENCY);
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
        View bcView = ta.getView("BROWN_CLUSTERS_c1000");
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
        if (source.getStartSpan() - target.getEndSpan() == 1){
            List<String> poss = posView.getLabelsCoveringToken(target.getEndSpan());
            if (poss.size() > 0) {
                ret.add("pos_single_word_" + poss.get(0));
            }
            else{
                ret.add("pos_single_word_no_pos");
            }
        }
        else if (target.getStartSpan() - source.getEndSpan() == 1){
            List<String> poss = posView.getLabelsCoveringToken(source.getEndSpan());
            if (poss.size() > 0) {
                ret.add("pos_single_word_" + poss.get(0));
            }
            else {
                ret.add("pos_single_word_no_pos");
            }
        }
        else{
            ret.add("no_single_word");
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
        String sourceHeadPos = "NULL_POS_S";
        String targetHeadPos = "NULL_POS_T";
        if (posView.getConstituentsCoveringToken(sourceHead.getStartSpan()).size() > 0){
            sourceHeadPos = posView.getLabelsCoveringToken(sourceHead.getStartSpan()).get(0);
        }
        if (posView.getConstituentsCoveringToken(targetHead.getStartSpan()).size() > 0){
            targetHeadPos = posView.getLabelsCoveringToken(targetHead.getStartSpan()).get(0);
        }
        /*
        ret.add(sourceHeadPos);
        ret.add(targetHeadPos);
        ret.add(t_pos_p1_p1);
        ret.add(t_pos_m1_m1);
        ret.add(t_pos_m2_m1);
        ret.add(t_pos_m1_p1);
        ret.add(t_pos_p1_p2);
        ret.add(s_pos_p1_p1);
        ret.add(s_pos_m1_m1);
        ret.add(s_pos_m2_m1);
        ret.add(s_pos_m1_p1);
        ret.add(s_pos_p1_p2);
        */
        sourceHeadPos = sourceHead.toString();
        targetHeadPos = targetHead.toString();
        ret.add("pos_1_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_p1_p1 + "_" + t_pos_p1_p1);
        ret.add("pos_2_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m1_m1 + "_" + t_pos_m1_m1);
        ret.add("pos_3_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m2_m1 + "_" + t_pos_m2_m1);
        ret.add("pos_4_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_m1_p1 + "_" + t_pos_m1_p1);
        ret.add("pos_5_" + sourceHeadPos + "_" + targetHeadPos + "_" + s_pos_p1_p2 + "_" + t_pos_p1_p2);
        //assert(ret.size() == 6);

        return ret;
    }

    public List<String> getShallowParseFeature(Relation r) {
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent sourceHead = getEntityHeadForConstituent(source, ta, "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, ta, "EntityHeads");
        View spView = ta.getView(ViewNames.SHALLOW_PARSE);
        if (sourceHead.getStartSpan() >= targetHead.getEndSpan() - 1) {
            List<Constituent> cons = spView.getConstituentsCoveringSpan(targetHead.getEndSpan(), sourceHead.getStartSpan() - 1);
            Set<Constituent> labels_no_overlap = new HashSet<Constituent>(cons);
            String fet = "";
            for (Constituent c : labels_no_overlap) {
                fet += c.getLabel();
                if (c.getLabel().equals("NP") == false) {
                    ret_features.add(c.getLabel());
                }
            }

            //ret_features.add(fet);
        }
        if (targetHead.getStartSpan() >= sourceHead.getEndSpan() - 1) {
            List<Constituent> cons = spView.getConstituentsCoveringSpan(sourceHead.getEndSpan(), targetHead.getStartSpan() - 1);
            Set<Constituent> labels_no_overlap = new HashSet<Constituent>(cons);
            String fet = "";
            for (Constituent c : labels_no_overlap) {
                fet += c.getLabel();
                if (c.getLabel().equals("NP") == false) {
                    ret_features.add(c.getLabel());
                }
            }
            System.out.println(r.getSource() + " | " + r.getTarget() + ": " + fet);
            //ret_features.add(fet);
        }
        return ret_features;
    }

    public List<String> getTemplateFeature(Relation r){
        List<String> ret_features = new ArrayList<String>();
        if (isFormulaic(r)){
            ret_features.add("is_formulaic_structure");
        }
        if (isPreposition(r)){
            ret_features.add("is_preposition_structure");
        }
        if (isPossessive(r)){
            ret_features.add("is_possessive_structure");
        }
        if (isPremodifier(r)){
            ret_features.add("is_premodifier_structure");
        }
        return  ret_features;
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
