package edu.illinois.cs.cogcomp.finer.components.pattern_typer;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finer.components.IFinerTyper;
import edu.illinois.cs.cogcomp.finer.datastructure.AnnotationReason;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 5/15/17.
 */
public class SimplePatternBasedTyper implements IFinerTyper {

    private Map<SimplePattern, List<FinerType>> allPatterns;
    private int windowSize = 5;

    public SimplePatternBasedTyper(Map<SimplePattern, List<FinerType>> allPatterns) {
        this(allPatterns, 5);
    }

    public SimplePatternBasedTyper(Map<SimplePattern, List<FinerType>> allPatterns, int windowSize) {
        this.allPatterns = allPatterns;
        this.windowSize = windowSize;
    }

    public List<SimplePattern> extractAllPattern(String[] surface) {
        List<SimplePattern> ret = new ArrayList<SimplePattern>();
        for (int i = 0; i < surface.length; i++) {
            String w = surface[i];
            int word_before = i;
            for (int j = 0; j < this.windowSize; j++) {
                int word_after = surface.length - i - j;
                if (word_after < 0) {
                    break;
                }

                SimplePattern sp = new SimplePattern(word_before, word_after, Arrays.copyOfRange(surface, i, i + j));
                ret.add(sp);
            }
        }
        return ret;
    }

    public void annotateOneMention(FineTypeConstituent c) {
        FinerType coarseType = c.getCoarseType();
        int start = c.getStartSpan();
        int end = c.getEndSpan();
        String[] surface = new String[end - start];
        TextAnnotation ta = c.getTextAnnotation();
        for (int i = 0; i < surface.length; i++) {
            surface[i] = ta.getToken(start + i);
        }

        List<SimplePattern> existing_patterns = extractAllPattern(surface);

        for (SimplePattern sp : existing_patterns) {
            for (FinerType t : allPatterns.getOrDefault(sp, new ArrayList<>())) {
                /*
                if (t.isChildOf(coarseType)) {
                    c.addFineType(t);
                    AnnotationReason ar = new AnnotationReason(SimplePatternBasedTyper.class);
                    ar.setComment(sp.toString());
                    c.addReason(t.getType(), ar);
                }
                */
                System.out.println(t.getType());
            }
        }
    }

    @Override
    public void annotate(List<FineTypeConstituent> mentions, Sentence sentence) {
        for (FineTypeConstituent mention : mentions) {
            annotateOneMention(mention);
        }
    }
}
