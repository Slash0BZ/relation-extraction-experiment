package edu.illinois.cs.cogcomp.lbj.coref.decoders;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.MentionSolution;

/**
 * A decoder that translates a document into a {@code MentionSolution}.
 * Decoding is stateless, meaning that the same decoder can be used
 * multiple times.
 * @author Eric Bengtson
 */
abstract public class MentionDecoder
 extends DecoderWithOptions<MentionSolution> {
    
    /**
     * Override to translate a document into a {@code MentionSolution}.
     * @param doc A document.
     * @return A MentionSolution.
     */
    abstract public MentionSolution decode(Doc doc);
    
}
