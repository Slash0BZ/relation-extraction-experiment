package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class PredictedMentionAnnotator extends Annotator {

    private static final String NAME = PredictedMentionAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(PredictedMentionAnnotator.class);

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
    public PredictedMentionAnnotator() {
        this(true);
    }


    public PredictedMentionAnnotator(boolean lazilyInitialize) {
        super("PREDICTED_MENTION_EXTRACTOR", new String[]{ViewNames.MENTION_ACE}, lazilyInitialize);
    }

    @Override
    public void initialize(ResourceManager rm) {

    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        if (!ta.hasView(ViewNames.MENTION_ACE)){
            throw new AnnotatorException("Input TextAnnotation must have MENTION_ACE View");
        }
        View newView = new SpanLabelView("PREDICTED_MENTION_EXTRACTOR", PredictedMentionAnnotator.class.getCanonicalName(), ta, 1.0f, true);
        View entityView = ta.getView(ViewNames.MENTION_ACE);
        for (Constituent c : entityView.getConstituents()){
            //System.out.println("GOLD: " + c.toString() + " " + c.getAttribute("EntityHeadStartCharOffset") + "-" + c.getAttribute("EntityHeadEndCharOffset"));
        }
        String newName = ta.getId().substring(3, ta.getId().length() - 8);
        String annotatedPath = "data/annotated_out/renamed/" + newName + ".txt";
        String mlvlPath = "data/annotated_out/mlvl/" + newName + ".txt";
        String spanPath = "data/annotated_out/span/" + newName + ".ann";
        List<String> annotated_token = new ArrayList<String>();
        List<String> annotated_tag = new ArrayList<String>();
        List<String> mlvl_token = new ArrayList<String>();
        List<String> mlvl_tag = new ArrayList<String>();
        List<Integer> charoffset_keys = new ArrayList<Integer>();
        Map<Integer, String> charoffset_head_maps = new HashMap<Integer, String>();
        Map<Integer, IntPair> charoffset_offset_maps = new HashMap<Integer, IntPair>();
        try {
            FileReader fileReader = new FileReader(new File(annotatedPath));
            BufferedReader br = new BufferedReader(fileReader);
            String line = "";
            while ((line = br.readLine()) != null){
                String[] group = line.split("\t");
                if (group.length != 2){
                    continue;
                }
                annotated_token.add(group[0]);
                annotated_tag.add(group[1]);
            }
            br.close();
            fileReader.close();
            fileReader = new FileReader(new File(mlvlPath));
            br = new BufferedReader(fileReader);
            line = "";
            while ((line = br.readLine()) != null){
                String[] group = line.split("\t");
                if (group.length != 2){
                    continue;
                }
                mlvl_token.add(group[0]);
                mlvl_tag.add(group[1]);
            }
            br.close();
            fileReader.close();
            fileReader = new FileReader(new File(spanPath));
            br = new BufferedReader(fileReader);
            line = "";
            while ((line = br.readLine()) != null){
                String[] group = line.split("\t");
                int extentCharOffsetLeft = Integer.parseInt(group[2]) - Integer.parseInt(group[5]);
                int extentCharOffsetRight = Integer.parseInt(group[6]) - Integer.parseInt(group[3]);
                int headStartCharOffset = Integer.parseInt(group[2]);
                String head = group[1];
                charoffset_keys.add(headStartCharOffset);
                charoffset_head_maps.put(headStartCharOffset, head);
                charoffset_offset_maps.put(headStartCharOffset, new IntPair(extentCharOffsetLeft, extentCharOffsetRight));
            }
            Collections.sort(charoffset_keys);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        assert(mlvl_token.size() == annotated_token.size());
        int mentionCount = 0;
        for (int i = 0; i < ta.getTokens().length; i++){
            String taToken = ta.getToken(i).toLowerCase();
            String anToken = "";
            if (i < annotated_token.size()) {
                anToken = annotated_token.get(i).toLowerCase();
            }
            String curTag = annotated_tag.get(i);
            String mlvlTag = mlvl_tag.get(i);
            if (curTag.charAt(0) == 'B'){
                int start = i;
                int end = i + 1;
                for (int j = i + 1; j < ta.getTokens().length; j++){
                    if (curTag.charAt(0) != 'I'){
                        i = j - 1;
                        end = j;
                        break;
                    }
                }
                Constituent headConstituent = new Constituent(curTag, "PREDICTED_MENTION_EXTRACTOR", ta, start, end);
                headConstituent.addAttribute("EntityMentionType", mlvlTag);
                headConstituent.addAttribute("EntityType", curTag.substring(2, curTag.length()));
                headConstituent.addAttribute("EntitySubtype", curTag.substring(2, curTag.length()));
                headConstituent.addAttribute("IsPredicted", "TRUE");

                //entity_level_classifier elc = new entity_level_classifier();
                //String classified_mention_level = (String)elc.discreteValue(headConstituent);
                entity_type_classifier etc = new entity_type_classifier();
                String classified_mention_type = (String)etc.discreteValue(headConstituent);
                entity_subtype_classifier esc = new entity_subtype_classifier();
                String clssified_mention_subtype = (String)esc.discreteValue(headConstituent);

                //headConstituent.addAttribute("EntityMentionType", classified_mention_level);
                headConstituent.addAttribute("EntityType", classified_mention_type);
                headConstituent.addAttribute("EntitySubtype", clssified_mention_subtype);

                int charoffset_key = charoffset_keys.get(mentionCount);
                String charoffset_head = charoffset_head_maps.get(charoffset_key);
                IntPair charoffset_offsets = charoffset_offset_maps.get(charoffset_key);

                int rightOffset = 0;
                int leftOffset = 0;
                if (headConstituent.toString().equals(charoffset_head) == false) {
                    String[] head_words = charoffset_head.split(" ");
                    for (int hi = 0; hi < head_words.length; hi++){
                        if (head_words[hi].equals(head_words[hi])){
                            leftOffset = hi;
                            rightOffset = head_words.length - hi - 1;
                            break;
                        }
                    }
                }
                int newHeadStart = start - leftOffset;
                int newHeadEnd = end + rightOffset;

                int newHeadCharStart = ta.getTokenCharacterOffset(newHeadStart).getFirst();
                int newHeadCharEnd = ta.getTokenCharacterOffset(newHeadEnd - 1).getSecond();

                int extentCharStart = newHeadCharStart - charoffset_offsets.getFirst();
                int extentCharEnd =  newHeadCharEnd + charoffset_offsets.getSecond();

                //System.out.println("Extent: " + extentCharStart + "-" + extentCharEnd);
                int extentTokenStart = ta.getTokenIdFromCharacterOffset(extentCharStart);
                while (extentTokenStart == -1){
                    extentCharStart ++;
                    extentTokenStart = ta.getTokenIdFromCharacterOffset(extentCharStart);
                }
                int extentTokenEnd = ta.getTokenIdFromCharacterOffset(extentCharEnd);
                while (extentTokenEnd == -1){
                    extentCharEnd ++;
                    extentTokenEnd = ta.getTokenIdFromCharacterOffset(extentCharEnd);
                }

                Constituent extentConstituent = new Constituent(headConstituent.getLabel(), "PREDICTED_MENTION_EXTRACTOR", ta, extentTokenStart, extentTokenEnd);
                extentConstituent.addAttribute("EntityMentionType", headConstituent.getAttribute("EntityMentionType"));
                extentConstituent.addAttribute("EntityType", headConstituent.getAttribute("EntityType"));
                extentConstituent.addAttribute("EntitySubtype", headConstituent.getAttribute("EntitySubtype"));
                extentConstituent.addAttribute("EntityHeadStartCharOffset", Integer.toString(newHeadCharStart));
                extentConstituent.addAttribute("EntityHeadEndCharOffset", Integer.toString(newHeadCharEnd));

                newView.addConstituent(extentConstituent);
                mentionCount++;
            }
        }

        ta.addView("PREDICTED_MENTION_EXTRACTOR", newView);
    }
}