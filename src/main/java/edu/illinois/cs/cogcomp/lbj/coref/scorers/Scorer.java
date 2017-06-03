package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.Solution;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;

/** 
 * Base class for scorers that take a predicted solution
 * and an answer key and return some quality metric.
 * @param <ST> The solution type.
 */
abstract public class Scorer<ST extends Solution> {

    /** Default constructor. */
    protected Scorer() {
    }

    //Subclasses should have methods like these, that take subclasses
    //of solutions:
    /**
     * Gets the score, whose contents will vary depending on the type of scorer.
     * @param key The answer key (gold standard solution).
     * @param pred The predicted solution.
     * @return The score.
     */
    abstract public Score getScore(ST key, ST pred);
}
