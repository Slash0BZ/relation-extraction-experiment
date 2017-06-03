package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;


/**
 * Base class for scorers that process {@code ChainSolution}s.
 * @param <T> The type of element in a {@code ChainSolution}.
 * @author Eric Bengtson
 */
abstract public class ChainScorer<T> extends Scorer<ChainSolution<T>> {

    /** 
     * Default constructor.
     */
    protected ChainScorer() {
	//Does nothing.
    }

    /**
     * Gets the score for a single solution.
     * If the single solution version of this method is not a special case
     * of the list version, with a singleton list, then override this method.
     * @param key The answer key (gold standard) chain solution.
     * @param pred The predicted chain solution.
     */
    public Score getScore(ChainSolution<T> key, ChainSolution<T> pred) {
	List<ChainSolution<T>> keys = new ArrayList<ChainSolution<T>>();
	keys.add(key);
	List<ChainSolution<T>> preds = new ArrayList<ChainSolution<T>>();
	preds.add(pred);
	return this.getScore(keys, preds);
    }

    /**
     * Gets the score for a list of documents.
     * @param keys The answer keys (gold standard) chain solutions,
     * one per document.
     * @param preds The predicted chain solutions, one per document.
     */
    abstract public Score getScore(List<ChainSolution<T>> keys,
				   List<ChainSolution<T>> preds);

}
