package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/*
 * Class RelationAnnotator
 * Generates View "RELATION_EXTRACTION"
 * This is a annotator that currently relies on gold mentions from MENTION_ACE view
 * It reads all the pairs of mentions in all the sentences in the TextAnnotation
 * For each pair, the annotator predicts its relation label
 * If the label is not NULL, the annotator puts the relation to the generating new view
 * For the new relations, attribute "RelationSubtype" is set
 */

public class CorefEntityAnnotator extends Annotator {

    private static final String NAME = CorefEntityAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(CorefEntityAnnotator.class);


    public CorefEntityAnnotator() {
        this(true);
    }


    public CorefEntityAnnotator(boolean lazilyInitialize) {
        super("COREF_ENTITY", new String[]{}, lazilyInitialize);
    }

    @Override
    public void initialize(ResourceManager rm) {

    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        String coref_file_name = ta.getId().substring(3, ta.getId().length() - 8);
    }
}