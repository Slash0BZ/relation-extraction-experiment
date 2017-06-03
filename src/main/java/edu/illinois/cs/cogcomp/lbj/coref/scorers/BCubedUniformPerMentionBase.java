package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

/**
 * Base class for scorers that compute the B-Cubed F-Score
 * for a collection of documents, giving equal weight to each mention.
 * See (Amit) Bagga and Baldwin (MUC-7 1998).
 * @author Eric Bengtson
 */
abstract public class BCubedUniformPerMentionBase extends BCubedBase {

    /** Default constructor. */
    protected BCubedUniformPerMentionBase() {
	//Does nothing.
    }

    /**
     * Computes the B-Cubed F-Score for a collection of documents.
     * <P>
     * The precision of a collection of documents is the average
     * of the precisions of all mentions in all documents.
     * The precision of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the predicted cluster containing m.
     * </P><P>
     * The recall of a collection of documents is the average
     * of the recalls of all mentions in all documents.
     * The recall of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the true cluster containing m.
     * </P><P>
     * The B-Cubed F-Score is the harmonic mean of the precision and recall
     * defined above.
     * This B-Cubed F-Score is weighted so that every mention's precision and 
     * recall gets equal weight.
     * @param keys A collection of true (gold standard) solutions
     * (for example, one per document)
     * @param preds A collection of predicted solutions
     * (for example, one per document)
     * @return The B-Cubed F-Score.
     */
    abstract public Score getScore(List<ChainSolution<Mention>> keys,
     List<ChainSolution<Mention>> preds);

    /**
     * Computes the B-Cubed precision for a collection of documents.
     * @param keys A collection of true (gold standard) solutions
     * (for example, one per document)
     * @param preds A collection of predicted solutions
     * (for example, one per document)
     * @return The B-Cubed precision.
     */
    abstract public double getPrecision(List<ChainSolution<Mention>> keys,
     List<ChainSolution<Mention>> preds);

    /**
     * Computes the B-Cubed recall for a collection of documents.
     * @param keys A collection of true (gold standard) solutions
     * (for example, one per document)
     * @param preds A collection of predicted solutions
     * (for example, one per document)
     * @return The B-Cubed recall.
     */
    abstract public double getRecall(List<ChainSolution<Mention>> keys,
			    List<ChainSolution<Mention>> preds);
}
