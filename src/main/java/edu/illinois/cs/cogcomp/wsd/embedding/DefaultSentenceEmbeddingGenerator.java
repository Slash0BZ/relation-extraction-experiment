package edu.illinois.cs.cogcomp.wsd.embedding;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.wsd.math.DenseVector;

import java.util.List;

/**
 * Created by haowu4 on 1/14/17.
 */
public class DefaultSentenceEmbeddingGenerator implements
        SentenceEmbeddingGenerator {

    @Override
    public DenseVector getEmbeddingOrNull(Sentence sentence, EmbeddingSpace
            embeddingSpace) {
        //View tokens = sentence.getView(ViewNames.TOKENS);
        View tokens = sentence.getSentenceConstituent().getTextAnnotation().getView(ViewNames.TOKENS);
        List<Constituent> constituents = tokens.getConstituents();
        DenseVector sum = null;
        for (Constituent c : constituents) {
//            if (c.getSpan().equals(ct.getSpan())) {
//                continue;
//            }
            String token = c.getSurfaceForm();

            DenseVector dv = embeddingSpace.getEmbeddingOrNull(token);
            if (dv == null) continue;
            if (sum == null) {
                sum = new DenseVector(dv);
            } else {
                sum.iadd(dv);
            }
        }
        return sum;
    }
}
