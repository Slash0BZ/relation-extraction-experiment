package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.util.*;

import edu.illinois.cs.cogcomp.lbj.coref.util.collections.MySets;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;


/**
 * Base class for scorers that implement some version of Bagga and Baldwin's
 * B-Cubed scoring algorithm.
 * See (Amit) Bagga and Baldwin (MUC-7 1998).
 * @author Eric Bengtson
 */
abstract public class BCubedBase extends ChainScorer<Mention> {

    /** Default constructor. */
    protected BCubedBase() {
	//Do nothing.
    }

    /**
     * Computes the B-Cubed F-Score for a chain solution.
     * <P>
     * The precision of a chain solution is the average
     * of the precisions of all mentions in the solution.
     * The precision of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the predicted cluster containing m.
     * </P><P>
     * The recall of a chain solution is the average
     * of the recalls of all mentions in the solution.
     * The recall of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the true cluster containing m.
     * </P><P>
     * The B-Cubed F-Score is the harmonic mean of the precision and recall
     * defined above.
     * </P>
     * @param key The true (gold standard) solution.
     * @param pred The predicted solution.
     * @return The B-Cubed F-Score.
     */
    public Score getScore(ChainSolution<Mention> key,
			  ChainSolution<Mention> pred) {
	List<ChainSolution<Mention>> keys = Collections.singletonList(key);
	List<ChainSolution<Mention>> preds = Collections.singletonList(pred);
	return this.getScore(keys, preds);
    }

    /**
     * Computes the B-Cubed precision for a chain solution.
     * <P>
     * The precision of a chain solution is the average
     * of the precisions of all mentions in the solution.
     * The precision of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the predicted cluster containing m.
     * </P>
     * @param key The true (gold standard) solution.
     * @param pred The predicted solution.
     * @return The B-Cubed precision.
     */
    public double getPrecision(ChainSolution<Mention> key,
			      ChainSolution<Mention> pred) {
	List<ChainSolution<Mention>> keys
	 = Collections.singletonList(key);
	List<ChainSolution<Mention>> preds
	 = Collections.singletonList(pred);
	return this.getPrecision(keys, preds);
    }

    /**
     * Computes the B-Cubed recall for a chain solution.
     * <P>
     * The recall of a chain solution is the average
     * of the recalls of all mentions in the solution.
     * The recall of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the true cluster containing m.
     * </P>
     * @param key The true (gold standard) solution.
     * @param pred The predicted solution.
     * @return The B-Cubed recall.
     */
    public double getRecall(ChainSolution<Mention> key,
			      ChainSolution<Mention> pred) {
	List<ChainSolution<Mention>> keys
	 = Collections.singletonList(key);
	List<ChainSolution<Mention>> preds
	 = Collections.singletonList(pred);
	return this.getRecall(keys, preds);
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
     * </P>
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
     * <P>
     * The precision of a collection of documents is the average
     * of the precisions of all mentions in all documents.
     * The precision of a mention m is calculated as the number of mentions
     * correctly predicted to be in the same cluster as m (including m)
     * divided by the number of mentions in the predicted cluster containing m.
     * </P>
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
     * </P>
     * @param keys A collection of true (gold standard) solutions
     * (for example, one per document)
     * @param preds A collection of predicted solutions
     * (for example, one per document)
     * @return The B-Cubed F-Score.
     */
    abstract public double getRecall(List<ChainSolution<Mention>> keys,
			    List<ChainSolution<Mention>> preds);

    /**
     * Partitions the key chain into a list of sets such that
     * each set in the result contains elements that are together
     * in a chain in the predicted solution.
     * @param keyChain A chain (cluster) from the key solution.
     * @param predSol The predicted solution.
     * @return A partition of the elements from the key chain.
     */
    protected List<Set<Mention>> getPartition(
     Set<Mention> keyChain, ChainSolution<Mention> predSol) {
	List<Set<Mention>> result = new ArrayList<Set<Mention>>();
	for (Set<Mention> predChain : predSol.getSubsets()) {
	    Set<Mention> inter = MySets.getIntersection(predChain, keyChain);
	    if (inter.size() > 0)
		result.add(inter);
	}
	return result;
     }

    /**
     * Determines whether the specified solutions have exactly the same
     * set of mentions.
     * @param sol1 One solution.
     * @param sol2 Another solution.
     */
    protected boolean haveSameMembers(ChainSolution<Mention> sol1,
				      ChainSolution<Mention> sol2) {
	Set<Mention> objs1 = sol1.getAllMembers();
	Set<Mention> objs2 = sol2.getAllMembers();
	return objs1.equals(objs2);
    }
}
