package edu.illinois.cs.cogcomp.finer.datastructure;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haowu4 on 5/20/17.
 */
public class AnnotationReason {
    String annotatorName;
    List<Constituent> clues;
    String comment;

    public AnnotationReason(String annotatorName, List<Constituent> clues) {
        this.annotatorName = annotatorName;
        this.clues = clues;
        comment = null;
    }

    public AnnotationReason(Class annotatorClazz, List<Constituent> clues) {
        this(annotatorClazz.getCanonicalName(), new ArrayList<Constituent>());
    }

    public AnnotationReason(Class annotatorClazz) {
        this(annotatorClazz, new ArrayList<Constituent>());
    }

    public AnnotationReason(String annotatorName) {
        this(annotatorName, new ArrayList<Constituent>());
    }

    public void addClue(Constituent c) {
        this.clues.add(c);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
