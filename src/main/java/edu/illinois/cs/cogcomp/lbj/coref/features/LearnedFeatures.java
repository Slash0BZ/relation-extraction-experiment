package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.*;

import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbj.coref.filters.MFilter;
import edu.illinois.cs.cogcomp.lbj.coref.filters.TypeMFilter;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.IntroExample;


/**
 * A collection of features based on classifiers learned from data.
 */
public class LearnedFeatures {
    public static Learner m_preNamesClassifier = null;
    public static Learner m_introCF = null;
    
    //TODO: Allow alternate classifiers.
    //TODO: Ensure that these classifiers (or their sources) are released.
    private static String preNamesClassifierName =
      "edu.illinois.cs.cogcomp.lbj.coref.learned.EmnlpBasicCoref";
    private static String introClassifierName =
      "edu.illinois.cs.cogcomp.lbj.coref.learned.Introduction";
    
    
    /**
     * Determines whether all the proper name modifiers
     * appearing before the heads of the two mentions
     * are coreferential, as predicted by a learned classifier.
     * Loads and uses the {@code edu.illinois.cs.cogcomp.lbjava Classifier}
     * {@code edu.illinois.cs.cogcomp.lbj.coref.learned.EmnlpBasicCoref}
     * dynamically from the classpath.
     * @param ex The example containing the mentions whose modifiers
     * are being checked for coreference.
     * @return Whether all pairs of modifiers are coreferential.
     * @throws RuntimeException if the learned classifier cannot be loaded.
     */
    public static boolean prenominalModifierNamesCompatible(CExample ex) {
        Doc d = ex.getDoc();
        List<String> namPre = new ArrayList<String>();
        namPre.add("NAM"); namPre.add("PRE");
        MFilter f = new TypeMFilter(namPre);
        if (LearnedFeatures.m_preNamesClassifier == null) {
//		   	System.err.println("LearnedFeatures.m_preNamesClassifier == null ; a.k.a: the baseline classifier was not loaded");
            try {
              LearnedFeatures.m_preNamesClassifier = (Learner)
               (Class.forName(preNamesClassifierName).newInstance());
            } catch (Exception e) {
        	System.err.println("Cannot load " + preNamesClassifierName);
        	throw new RuntimeException(e);
            }
        }
        List<Mention> pres1 = getPreMentionsOf(ex.getM1(), f);
        List<Mention> pres2 = getPreMentionsOf(ex.getM2(), f);
        for (Mention p1 : pres1) {
            for (Mention p2 : pres2) {
        	if (LearnedFeatures.m_preNamesClassifier.discreteValue(
        	 d.getCExampleFor(p1, p2)).equals("false"))
        	{
        	    return false;
        	}
            }
        }
        return true;
    }

    /**
     * Determines whether the mention {@code m} is the first mention
     * of its entity in the document, using a learned classifier.
     * Loads and uses the {@code edu.illinois.cs.cogcomp.lbjava Classifier edu.illinois.cs.cogcomp.lbj.coref.learned.Introduction}
     * dynamically from the classpath.
     * @param m The mention
     * @return Whether the mention is an introduction to its entity.
     * @throws RuntimeException if the classifier cannot be loaded.
     */
    public static String introduction(Mention m) {
        if (m_introCF == null) {
//       	System.err.println("LearnedFeatures.m_introCF == null ; a.k.a: the baseline classifier was not loaded");
            try {
              m_introCF = (Learner)
               (Class.forName(introClassifierName).newInstance());
            } catch (Exception e) {
        	System.err.println("Cannot load " + introClassifierName);
        	throw new RuntimeException(e);
            }
        }
        
        return m_introCF.discreteValue(new IntroExample(m));
    }
    

    /* Utilities */

    /**
     * Returns a list of mentions appearing before the head of {@code m}.
     * @param m The mention whose pre-mentions should be retrieved.
     * @param filter A filter to be applied to the resulting mentions.
     * @return Mentions appearing before the head of {@code m} and accepted by 
     * {@code filter}.
     */
    protected static List<Mention> getPreMentionsOf(Mention m, MFilter filter) {
	Set<Mention> results = new HashSet<Mention>();

	Doc d = m.getDoc();
	for (Mention mC : d.getMentionsContainedIn(m)) {
	    if (mC.getExtentLastWordNum() < m.getHeadFirstWordNum())
		results.add(mC);
	}
	return filter.getFiltered(results);
    }

}
