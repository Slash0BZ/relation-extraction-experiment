package edu.illinois.cs.cogcomp.lbj.coref.decoders;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.BIOExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChunkSolution;


/**
 * Translates a BIO classifier's decisions into a {@code ChunkSolution}.
 */
public class BIODecoder extends ChunkDecoder {

    /**
     * Constructs the decoder.
     * @param bioClassifier A classifier that classifies a word
     * (wrapped in a {@code BIOExample}),
     * as beginning ("b"), inside ("i") or outside ("o") a chunk.
     */
    public BIODecoder(Classifier bioClassifier) {
	super(bioClassifier);
    }
    
    /**
     * Classifies each word (wrapped in a {@code BIOExample}) in a document,
     * starting at the first text word,
     * as beginning ('b'), inside ('i') or outside ('o') a chunk,
     * and decodes these decisions into a list of chunks.
     * @param doc The document to process.
     * @return A {@code ChunkSolution} containing a list of the detected
     * chunks.
     */
    public ChunkSolution decode(Doc doc) {
	if (m_classifier == null) {
	    System.err.println("No bioClassifier provided.");
	    return new ChunkSolution();
	}
	List<Chunk> chunks = new ArrayList<Chunk>();
	char prevBIO = 'o';
	int start = -1; // -1 Denoting none.
	int n = doc.getWords().size();
	int firstWN = doc.getTextFirstWordNum();
	BIOExample prevEx = null;
	for (int i = firstWN; i < n; ++i) {
	    BIOExample ex = new BIOExample(doc, i, 'u', prevEx); //'u'=unknown.
	    char bio = m_classifier.discreteValue(ex).charAt(0);
	    if (bio == 'o') {
		if (prevBIO != 'o') { //End previous word:
		    if (start > i-1)
			System.err.println("Start occurs after end: " + start + ", " + (i-1) );
		    Chunk head = doc.makeChunk(start, i-1);
		    chunks.add(head);
		    //TODO: Return them in some other form?
		    start = -1;
		} // else {	//Still no word: Do nothing. }
	    } else if (bio == 'i') {
		if (prevBIO == 'o') { //Labeling error.  Treat bio as 'b'?
		    start = i;
		} else {
		    //Continue the word; do nothing.
		}
	    } else { //bio == 'b'
		if (prevBIO != 'o') { //End Prev word and start new: 
		    if (start > i-1)
			System.err.println("Start occurs after end: " + start + ", " + (i-1) );
		    Chunk head = doc.makeChunk(start, i-1);
		    chunks.add(head);
		} // else { //Just start a word. }
		start = i;
	    }
	    prevBIO = bio;
	    prevEx = ex;
	}
	//Check to see whether we're still in a mention, and close it:
	if (prevBIO != 'o') { //In mention, close it.
	    System.err.println("Closing chunk from " + start + " to " + (n-1));
		if (start > n-1)
		    System.err.println("Start occurs after end: " + start + ", " + (n-1) );
	    Chunk head = doc.makeChunk(start, n-1);
	    chunks.add(head);
	}

	return new ChunkSolution(chunks);
    } //End method decodeToChunks

}
