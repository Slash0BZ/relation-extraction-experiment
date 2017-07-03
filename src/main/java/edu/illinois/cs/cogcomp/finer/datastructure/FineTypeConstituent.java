package edu.illinois.cs.cogcomp.finer.datastructure;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.util.*;

/**
 * Created by haowu4 on 5/17/17.
 */
public class FineTypeConstituent extends Constituent {

    private FinerType coarseType;
    private Set<FinerType> fineTypes;
    private Map<String, List<AnnotationReason>> reasons;

    private static Map<String, Double> getLablToScore() {
        Map<String, Double> ret = new HashMap<String, Double>();
        ret.put("FinerType", 1.0);
        return ret;
    }

    public FineTypeConstituent(TextAnnotation text, int start, int end) {
        super(getLablToScore(), FinerAnnotator.VIEW_NAME, text, start, end);
        reasons = new HashMap<>();
        this.fineTypes = new HashSet<>();
    }


    public void addReason(String type, AnnotationReason reason) {
        List<AnnotationReason> x = reasons.getOrDefault(type, new ArrayList<>());
        reasons.put(type, x);
    }

    public void addFineType(FinerType t) {
        this.fineTypes.add(t);
    }

    public void addCoarseType(FinerType t) {
        this.coarseType = t;
    }


    public FinerType getCoarseType() {
        return coarseType;
    }

    public void finish() {
        this.labelsToScores.clear();
        for (FinerType t : this.fineTypes) {
            if (t.isVisible()) {
                this.labelsToScores.put(t.getType(), 1.0);
            }
        }

        if (this.coarseType.isVisible()) {
            this.labelsToScores.put(this.coarseType.getType(), 1.0);
        }
    }

    public List<AnnotationReason> getReasons(String type) {
        return reasons.getOrDefault(type, new ArrayList<>());
    }

    public Optional<Constituent> toConstituent() {
        this.finish();
        if (labelsToScores.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new Constituent(labelsToScores, this.viewName, this.textAnnotation, getStartSpan(), getEndSpan()));
        }

    }

}
