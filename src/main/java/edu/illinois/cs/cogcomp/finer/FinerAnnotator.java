package edu.illinois.cs.cogcomp.finer;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.finer.components.IFinerTyper;
import edu.illinois.cs.cogcomp.finer.components.MentionDetecter;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;

import java.util.*;

/**
 * Created by haowu4 on 1/15/17.
 */
public class FinerAnnotator extends Annotator {
    public static final String VIEW_NAME = "FINE_GRAIN_ENTITY_TYPE";
    private MentionDetecter mentionDetecter;
    private List<IFinerTyper> typers;


    public FinerAnnotator(MentionDetecter mentionDetecter, List<IFinerTyper> typers) {
        super(VIEW_NAME, new String[]{ViewNames.POS, ViewNames.NER_ONTONOTES});
        this.mentionDetecter = mentionDetecter;
        this.typers = typers;
    }

    public void setMentionDetecter(MentionDetecter mentionDetecter) {
        this.mentionDetecter = mentionDetecter;
    }

    public void addTyper(IFinerTyper typer) {
        this.typers.add(typer);
    }

    @Override
    public void initialize(ResourceManager rm) {

    }

    public void addView(TextAnnotation ta) {
        List<FineTypeConstituent> fineTypes = this.getAllFineTypeConstituents(ta);
        View finalAnnotation = new SpanLabelView(VIEW_NAME, ta);
        for (FineTypeConstituent c : fineTypes) {
            Optional<Constituent> ret = c.toConstituent();
            ret.ifPresent(finalAnnotation::addConstituent);
        }
        ta.addView(VIEW_NAME, finalAnnotation);
    }

    public List<FineTypeConstituent> getAllFineTypeConstituents(TextAnnotation ta) {
        List<FineTypeConstituent> allCandidates = new ArrayList<>();
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sent = ta.getSentence(i);
            List<FineTypeConstituent> sentence_candidates = mentionDetecter.getMentionCandidates(sent);
            for (IFinerTyper typer : this.typers) {
                typer.annotate(sentence_candidates, sent);
            }

            for (FineTypeConstituent c : sentence_candidates) {
                c.finish();
                allCandidates.add(c);
            }
        }
        return allCandidates;
    }

}
