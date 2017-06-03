/*
 * Created on May 31, 2006
 *
 */
// vim:fileformat=unix
package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.alignment.ExtentToHeadAligner;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ChunkDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.features.WordNetTools;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.ExtendHeadExample;


public class ExtendHeadsParser implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
    private ChunkDecoder m_headDecoder;
    private List<Doc> m_docs;
    private List<ExtendHeadExample> m_examples;

    private int m_iD = 0;
    private int m_iX = 0;

    private int m_numExamplesProcessed = 0;


    public ExtendHeadsParser(DocLoader loader, ChunkDecoder headDecoder) {
	m_headDecoder = headDecoder;
	    m_docs = loader.loadDocs();
	reset();
    }
    
    public ExtendHeadsParser(ChunkDecoder headDecoder) {
	m_headDecoder = headDecoder;
    }

    public List<ExtendHeadExample> getExamples(Doc d) {
	List<ExtendHeadExample> xes = new ArrayList<ExtendHeadExample>();

	Set<Chunk> heads = m_headDecoder.decode(d).getItems();
	Set<Chunk> trueExtents = getTrueExtents(d);

	ExtentToHeadAligner aligner = new ExtentToHeadAligner();
	Map<Chunk,Chunk> eToH = aligner.getAlignment(trueExtents, heads);
	Map<Chunk,Chunk> headToTrueExtent = new HashMap<Chunk,Chunk>();
	for (Chunk e : eToH.keySet()) {
	    Chunk h = eToH.get(e);
	    headToTrueExtent.put(h, e);
	}

	for (Chunk head : heads) {
	    Chunk extent = headToTrueExtent.get(head);
	    if (extent == null) continue;
	    for (int i = head.getStartWN() - 1; i >= extent.getStartWN(); --i)
	    {
		ExtendHeadExample ex
		 = new ExtendHeadExample(d, head, i, true);
		xes.add(ex);
	    }
	    int prev = extent.getStartWN() - 1;
	    if (prev >= 0) {
		ExtendHeadExample ex
		 = new ExtendHeadExample(d, head, prev, false);
		xes.add(ex);
	    }

	    for (int i = head.getEndWN() + 1; i <= extent.getEndWN(); ++i)
	    {
		ExtendHeadExample ex
		 = new ExtendHeadExample(d, head, i, true);
		xes.add(ex);
	    }
	    int next = extent.getEndWN() + 1;
	    if (next < d.getWords().size()) {
		ExtendHeadExample ex
		 = new ExtendHeadExample(d, head, next, false);
		xes.add(ex);
	    }
	}
	return xes;
    }

    protected Set<Chunk> getTrueExtents(Doc d) {
	Set<Chunk> trueExts = new HashSet<Chunk>();
	List<Mention> ments = d.getTrueMentions();
	for (Mention m : ments) {
	    trueExts.add(m.getExtent());
	}
	return trueExts;
    }

    public ExtendHeadExample next() {
	while (m_iD < m_docs.size()  && m_examples.size() == 0) {
	    System.err.println("Zero Examples in doc " + m_iD);
	    m_iD++;
	    resetDoc();
	}

	if (m_iD >= m_docs.size()) {
	    System.out.println("Parsed " + m_iD + " Docs");
	    WordNetTools.saveWN();
	    return null; // No more elements remain.
	}

	ExtendHeadExample ex = m_examples.get(m_iX);

	/* Prepare next indices */
	m_iX++;
	if (m_iX >= m_examples.size()) {
	    m_iD++;
	    resetDoc();
	}
	m_numExamplesProcessed++;
	return ex;
    }

    public void enqueue(Object q) {
    }
    
    public void reset() {
	    m_iD = 0;
	    m_numExamplesProcessed = 0;
	    resetDoc();
    }

    public void close() { reset(); }
    
    public void resetDoc() {
	m_iX = 0;
	if (m_iD < m_docs.size()) {
	    m_examples = getExamples(m_docs.get(m_iD));
	} else {
	    m_examples = new ArrayList<ExtendHeadExample>();
	}
    }
}
