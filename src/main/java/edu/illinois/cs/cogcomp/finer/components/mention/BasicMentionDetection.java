package edu.illinois.cs.cogcomp.finer.components.mention;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finer.components.MentionDetecter;
import edu.illinois.cs.cogcomp.finer.datastructure.AnnotationReason;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 1/15/17.
 */
public class BasicMentionDetection implements MentionDetecter {
    TypeMapper mapper;

    public BasicMentionDetection(TypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<FineTypeConstituent> getMentionCandidates(Sentence sentence) {
        List<FineTypeConstituent> ret = new ArrayList<FineTypeConstituent>();
        View ner = sentence.getView(ViewNames.NER_ONTONOTES);
        for (Constituent c : ner.getConstituents()) {
            FinerType coarseType = mapper.getType(c.getLabel());
            if (coarseType == null) {
                continue;
            }
            String typeName = coarseType.toString();
            Map<String, Double> l2s = new HashMap<>();
            l2s.put(typeName, 1.0);
            FineTypeConstituent mention = new FineTypeConstituent(c.getTextAnnotation(),
                    c.getStartSpan(), c.getEndSpan());
            mention.addCoarseType(coarseType);
            AnnotationReason reason = new AnnotationReason(BasicMentionDetection.class);
            mention.addReason(typeName, reason);
            ret.add(mention);
        }
        return ret;
    }
}
