package edu.illinois.cs.cogcomp.wsd.embedding;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.wsd.math.DenseVector;

import java.util.Map;

/**
 * Created by haowu4 on 1/14/17.
 */
public interface SentenceEmbeddingGenerator {
    DenseVector getEmbeddingOrNull(Sentence sentence, EmbeddingSpace
            wordEmbeddings);
}
