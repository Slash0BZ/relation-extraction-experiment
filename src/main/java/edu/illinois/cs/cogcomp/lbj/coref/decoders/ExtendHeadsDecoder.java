package edu.illinois.cs.cogcomp.lbj.coref.decoders;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.ExtendHeadExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.MentionSolution;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;


/**
 * A decoder for determining the extents of the mentions, given their
 * heads as chunks.
 */
public class ExtendHeadsDecoder extends MentionDecoder {
    
    /** Decodes the heads */
    protected ChunkDecoder m_headDecoder;
    
    /** Given a head, determines whether a word is part of its extent. */
    public Classifier m_extendClassifier;

    /**
     * Constructs a decoder using an {@code extender} classifier that
     * takes an {@code ExtendHeadExample} representing a word and a chunk
     * and returns "true" if the word represented by the example
     * should be part of the extent of chunk.
     * The chunks to be extended will be derived using the {@code headDecoder}.
     * @param extender The classifier to decide whether a given word should
     * be part of a given chunk.
     * @param headDecoder The decoder used to derive the head chunks.
     */
    public ExtendHeadsDecoder(Classifier extender, ChunkDecoder headDecoder) {
	m_extendClassifier = extender;
	m_headDecoder = headDecoder;
    }
    
    /**
     * Applies a classifier to chunks to determine their extents
     * and decode these decisions into a {@code MentionSolution}.
     * The chunks will be discovered by the provided {@code headDecoder}.
     * The resulting mentions do not have their metadata set. 
     * @param doc The document whose mentions should be determined.
     * @return The mentions detected in the document, encoded as
     * a {@code MentionSolution}.
     */
    public MentionSolution decode(Doc doc) {
	if (m_extendClassifier == null) {
	    System.err.println("No classifier provided.");
	    return new MentionSolution();
	}
	List<Mention> mentions = new ArrayList<Mention>();
	SpanLabelView typedView = new SpanLabelView(
			Constants.PRED_MENTION_VIEW, "predict", doc.getTextAnnotation(), 1.0, true);
	
	int mN = 0;
	for (Chunk h : m_headDecoder.decode(doc).getItems()) {
		int senNum = doc.getSentNum(h.getStartWN());
		//System.out.println(senNum + h.getCleanText());
		
	    int wN = h.getStartWN() - 1;
	    while (wN >= 0) {
	    	ExtendHeadExample ex = new ExtendHeadExample(doc, h, wN);
	    	boolean in = m_extendClassifier.discreteValue(ex).equals("true");
	    	if (!in) break;
	    	if (doc.getSentNum(wN) != senNum) break;
	    	wN--;
	    }
	    int firstWN = wN + 1;
	    
	    wN = h.getEndWN() + 1;
	    while (wN < doc.getWords().size()) {
	    	ExtendHeadExample ex = new ExtendHeadExample(doc, h, wN);
	    	boolean in = m_extendClassifier.discreteValue(ex).equals("true");
	    	if (!in) break;
	    	if (doc.getSentNum(wN) != senNum) break;
	    	wN++;
	    }
	    int lastWN = wN - 1;
	    Chunk e = doc.makeChunk(firstWN, lastWN);
    	String id = "pred_" + mN;
	    if(doc instanceof DocTextAnnotation){
	    	Constituent c = new Constituent("PredMent", 0.0, typedView.getViewName(),
	    			doc.getTextAnnotation(), e.getStartWN(), e.getEndWN()+1);
	    	c.addAttribute(Constants.MentionHeadStart, String.valueOf(h.getStartWN()));
	    	c.addAttribute(Constants.MentionHeadEnd, String.valueOf(h.getEndWN()+1));
	    	typedView.addConstituent(c);
	    }
	    else{
	    	Mention m = new Mention(doc,id,"NONE","NONE","NONE",
	    			"NONE", e, h, "NONE", "NONE", "", "SPC", false);
	    	mentions.add(m);
	    }
	    ++mN;
	}
	if(doc instanceof DocTextAnnotation){
		doc.getTextAnnotation().addView(Constants.PRED_MENTION_VIEW, typedView);
		((DocTextAnnotation)doc).setPredictMentionsFromTA();
	}
	return new MentionSolution(doc.getPredMentions());
    }

}
