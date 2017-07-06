package edu.illinois.cs.cogcomp.wsd.embedding;

import edu.illinois.cs.cogcomp.wsd.math.DenseVector;

import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 1/14/17.
 */
public class EmbeddingSpace {
    Map<String, Integer> entryToId;
    DenseVector[] embeddings;

    public EmbeddingSpace(Map<String, Integer> entryToId, DenseVector[]
            embeddings) {
        this.entryToId = entryToId;
        this.embeddings = embeddings;
    }

    public EmbeddingSpace(Map<String, Integer> toId, List<DenseVector>
            vectors) {
        this.entryToId = toId;
        this.embeddings = new DenseVector[vectors.size()];
        for (int i = 0; i < vectors.size(); i++) {
            this.embeddings[i] = vectors.get(i);
        }
    }

    public DenseVector getEmbeddingOrNull(String e) {
        if (entryToId.containsKey(e)) {
            return embeddings[entryToId.get(e)];
        }
        return null;
    }

    public DenseVector getEmbeddingFastOrDie(String e) {
        return embeddings[entryToId.get(e)];
    }
}
