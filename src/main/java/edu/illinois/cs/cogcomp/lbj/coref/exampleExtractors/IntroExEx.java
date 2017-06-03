/** @author Eric Bengtson */
package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.IntroExample;


/**
 * Extracts introduction examples ({@code IntroExample}s) from a document.
 * Extracts {@code IntroExample}s representing true and false examples
 * of EntityMentions that are the first mention (i.e. introduction)
 * of an entity in a document.
 */
public class IntroExEx extends DocFilteredBatchExEx<IntroExample>
 implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Generates introduction examples ({@code IntroExample}s)
     * from all mentions of a document.
     * Uses predicted or true mentions according to the output of
     * the document's {@link Doc#getMentions()} method.
     * @return A list of introduction examples.
     */
    protected List<IntroExample> generateAllExamples() {
	List<IntroExample> xes = new ArrayList<IntroExample>();
	Doc d = getDoc();
	Set<String> known = new HashSet<String>();
	for (Mention m : d.getMentions()) {
	    boolean isFirst = !known.contains(m.getEntityID());
	    known.add(m.getEntityID());
	    xes.add(new IntroExample(m, isFirst));
	}
	return xes;
    }

}

