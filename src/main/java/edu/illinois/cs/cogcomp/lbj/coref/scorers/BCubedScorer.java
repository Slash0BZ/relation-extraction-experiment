package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.FScore;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;


/**
  * Computes the within-document B-Cubed F-Score for a collection of documents.
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
  * </P><P>
  * This is the algorithm that Culotta says he used
  * in Culotta, Wick, and McCallum (HLT 2007),
  * modified to accept prediction solutions
  * that contain different mentions than the key solutions,
  * by counting overlap as 0 for mentions not contained in both.
  * </P>
  * See (Amit) Bagga and Baldwin (MUC-7 1998).
  * @author Eric Bengtson
 */
public class BCubedScorer extends BCubedUniformPerMentionBase {

  /** Default Constructor. */
  public BCubedScorer() {
    //Does nothing.
  }

  /**
    * Computes the within-document B-Cubed F-Score
    * for a collection of documents.
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
    * </P><P>
    * This is the algorithm that Culotta says he used
    * in Culotta, Wick, and McCallum (HLT 2007),
    * modified to accept prediction solutions
    * that contain different mentions than the key solutions,
    * by counting overlap as 0 for mentions not contained in both.
    * </P>
    * @param keys A collection of true (gold standard) solutions
    * (for example, one per document)
    * @param preds A collection of predicted solutions
    * (for example, one per document)
    * @return The within-document B-Cubed F-Score.
   */
  public Score getScore(List<ChainSolution<Mention>> keys,
                        List<ChainSolution<Mention>> preds) {
    double[] pr = this.calcPR(keys, preds); //switch roles.
    return new FScore(pr[0], pr[1]);
  }

  /**
    * Computes the within-document B-Cubed precision
    * for a collection of documents.
    * <P>
    * The precision of a collection of documents is the average
    * of the precisions of all mentions in all documents.
    * The precision of a mention m is calculated as the number of mentions
    * correctly predicted to be in the same cluster as m (including m)
    * divided by the number of mentions in the predicted cluster containing m.
    * and then averages those scores of all mentions in across all documents.
    * </P>
    * @param keys A collection of true (gold standard) solutions
    * (for example, one per document)
    * @param preds A collection of predicted solutions
    * (for example, one per document)
    * @return The within-document B-Cubed precision.
   */
  public double getPrecision(List<ChainSolution<Mention>> keys,
                             List<ChainSolution<Mention>> preds) {
    return this.calcPR(keys, preds)[0]; //switch roles.
  }

  /**
    * Computes the within-document B-Cubed recall
    * for a collection of documents.
    * <P>
    * The recall of a collection of documents is the average
    * of the recalls of all mentions in all documents.
    * The recall of a mention m is calculated as the number of mentions
    * correctly predicted to be in the same cluster as m (including m)
    * divided by the number of mentions in the true cluster containing m.
    * </P>
    * @param keys A collection of true (gold standard) solutions
    * (for example, one per document)
    * @param preds A collection of predicted solutions
    * (for example, one per document)
    * @return The within-document B-Cubed recall.
   */
  public double getRecall(List<ChainSolution<Mention>> keys,
                          List<ChainSolution<Mention>> preds) {
    return calcPR(keys, preds)[1];
  }

  /**
    * Computes the within-document B-Cubed precision and recall
    * for a collection of documents.
    * <P>
    * The precision of a collection of documents is the average
    * of the precisions of all mentions in all documents.
    * The precision of a mention m is calculated as the number of mentions
    * correctly predicted to be in the same cluster as m (including m)
    * divided by the number of mentions in the predicted cluster containing m.
    * and then averages those scores of all mentions in across all documents.
    * </P><P>
    * The recall of a collection of documents is the average
    * of the recalls of all mentions in all documents.
    * The recall of a mention m is calculated as the number of mentions
    * correctly predicted to be in the same cluster as m (including m)
    * divided by the number of mentions in the true cluster containing m.
    * </P>
    * @param keys A collection of true (gold standard) solutions
    * (for example, one per document)
    * @param predictions A collection of predicted solutions
    * (for example, one per document)
    * @return An array containing the precision and the recall, in that order.
   */
  private Map<String, double[]> calcPR(
      List<ChainSolution<Mention>> keys,
      List<ChainSolution<Mention>> predictions, MentionTypeTranslator f) {
    Map<String, double[]> result = new TreeMap<String, double[]>();
    int numDocs = Math.min( keys.size(), predictions.size() );

    for (int iD = 0; iD < numDocs; ++iD) {
      ChainSolution<Mention> keySol = keys.get(iD);
      ChainSolution<Mention> predSol = predictions.get(iD);

      List<Mention> ments
        = new ArrayList<Mention>(keySol.getAllMembers());
      Collections.sort(ments);

      for (Mention mi : ments) {
        Set<Mention> backChain = keySol.getContainerFor(mi);
        if (backChain == null) {
          System.err.println("Null backChain"); //TODO: Better msg
          continue;
        }
        int intersize = 0;
        //WAS: if (predSol.getAllMembers().contains(mi)) {
        Set<Mention> foreChain = predSol.getContainerFor(mi);
        if (foreChain == null) {
          System.err.println("Null foreChain"); //TODO: Better msg
          continue;
        }
        Set<Mention> inter = new HashSet<Mention>(backChain);
        inter.retainAll(foreChain);
        intersize = inter.size();
        //}

        String type = f.translate(mi.getType());
        double[] pr = result.get(type);
        if (pr == null) {
          pr = new double[3];
          result.put(type, pr);
        }

        pr[0] += (intersize / (double) foreChain.size());
        pr[1] += (intersize / (double) backChain.size());
        pr[2]++;
      }
    }

    for (double[] pr : result.values()) {
      pr[0] /= pr[2];
      pr[1] /= pr[2];
    }

    return result;
  }


  private double[] calcPR(List<ChainSolution<Mention>> keys,
                          List<ChainSolution<Mention>> predictions) {
    return
      calcPR(keys, predictions,
             new MentionTypeTranslator() {
               public String translate(String type) { return ""; }
             }).get("");
  }


  public Map<String, double[]> calcPRByType(
      List<ChainSolution<Mention>> keys,
      List<ChainSolution<Mention>> predictions) {
    return
      calcPR(keys, predictions,
             new MentionTypeTranslator() {
               public String translate(String type) { return type; }
             });
  }


  interface MentionTypeTranslator { String translate(String type); }
}

