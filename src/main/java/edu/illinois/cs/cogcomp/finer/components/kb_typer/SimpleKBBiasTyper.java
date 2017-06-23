package edu.illinois.cs.cogcomp.finer.components.kb_typer;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.finer.components.IFinerTyper;
import edu.illinois.cs.cogcomp.finer.datastructure.AnnotationReason;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 5/15/17.
 */
public class SimpleKBBiasTyper implements IFinerTyper {
    Map<String, Map<FinerType, Double>> surfaceToTypeDB;

    public SimpleKBBiasTyper(Map<String, Map<FinerType, Double>> surfaceToTypeDB) {
        this.surfaceToTypeDB = surfaceToTypeDB;
    }

    public List<Pair<String, Double>> annotateSingleMention(Constituent mention, FinerType coarseType) {
        List<Pair<String, Double>> ret = new ArrayList<>();
        String surface = mention.getSurfaceForm();
        Map<FinerType, Double> candidates = surfaceToTypeDB.getOrDefault(surface, new HashMap<>());

        for (Map.Entry<FinerType, Double> entry : candidates.entrySet()) {
            FinerType t = entry.getKey();
            double v = entry.getValue();
            if (t != null) {
                Pair<String, Double> cur = new Pair<>(t.getType(), v);
                ret.add(cur);
            }
        }
        return ret;

    }

    @Override
    public void annotate(List<FineTypeConstituent> mentions, Sentence sentence) {

    }
}
