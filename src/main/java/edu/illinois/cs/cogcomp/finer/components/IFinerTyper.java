package edu.illinois.cs.cogcomp.finer.components;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.util.List;

/**
 * Created by haowu4 on 5/15/17.
 */
public interface IFinerTyper {
    void annotate(List<FineTypeConstituent> mentions, Sentence sentence);
}
