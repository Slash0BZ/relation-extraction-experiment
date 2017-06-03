package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.util.xml.XMLException;
import edu.illinois.cs.cogcomp.lbj.coref.features.EntityTypeFeatures;
import edu.illinois.cs.cogcomp.lbj.coref.features.GigaWord;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;


/**
 * @author Eric Bengtson
 *
 */
public class DocAPF extends DocXMLBase {
    private static final long serialVersionUID = 46L;


    /* Constructors */

    /** Basic constructor: Not recommended. */
    public DocAPF() {
	super();
	this.domain = "AFP";
    }

    /**
     * Loads filename file and reads in the XML representation.
     * @param filename The name of the file.
     */
    public DocAPF(String filename) throws XMLException {
	super(filename, "apf.xml");
	this.domain = "AFP";
    }

    /**
     * Loads filename file and reads in the XML representation.
     * @param filename The name of the file.
     * @param posSource Where the document should get POS tags from.
     * If {@code PosSource.FILE},
     * attempts to make the system more exactly
     * reproduce the previously published results.
     * This requires a corpus that is preprocessed offline using
     * CogComp preprocessing tools available at
     * {@literal http://L2R.cs.uiuc.edu/~cogcomp}
     * If {@code PosSource.SNOW}, use a local SNoW based preprocessor
     * called tagger, located in {@code PATH_POS} environment variable
     * (which must be exported).
     * This is generally slow.
     * Otherwise, uses the LBJ preprocesor (fastest, but performance may differ
     * from published results).
     * PosSource.FILE Loads offline preprocessing from files ending in 
     * {@literal .sgm.strip_chunker}.
     * PosSource.SNOW Uses an offline
     */
    public DocAPF(String filename, PosSource posSource)
     throws XMLException {
	super(filename, "apf.xml", posSource);
	this.domain = "AFP";
    }

    public DocAPF(String filename, Classifier caser) throws XMLException {
	super(filename, "apf.xml", caser);
	this.domain = "AFP";
    }



    /* Loading */

    /** 
     * Loads an entity from an {@literal XML} representation and returns it.
     * As a side effect, adds true mentions to the document.
     */
    protected Entity loadEntity(Node nEntity) throws XMLException {
	String id, type, subtype, specificity;
	try {
	    /* Get attributes */
	    NamedNodeMap attrs = nEntity.getAttributes();
	    id = getOptAttrib(attrs, "ID", "");
	    type = getOptAttrib(attrs, "TYPE", "");
	    subtype = getOptAttrib(attrs, "SUBTYPE", "");
	    specificity = getOptAttrib(attrs, "CLASS", "");
	} catch (NullPointerException e) {
	    throw new XMLException("Expected Entity Information not found", e);
	}
	Entity e = new Entity(id, type, subtype, specificity);

	/* Get mentions and attributes */
	NodeList nlEntContents = nEntity.getChildNodes();
	for (int i = 0; i < nlEntContents.getLength(); i++) {
	    Node nEntContent = nlEntContents.item(i);
	    if (nEntContent.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    Element eEntContent = (Element) nEntContent;
	    if (nEntContent.getNodeName() == "entity_mention") {
		Mention m = processEntityMention(eEntContent, id, type,
		    subtype, specificity);
		// Be careful not use gold entity type
    	// To be safe don't load the entity typ
		if(!Parameters.useGoldEntityTypes){
			m.setEntityType(EntityTypeFeatures.getEType(m));
			//System.out.println(m + " " + EntityTypeFeatures.getEType(m));
		}
		e.addMention(m);
		
		this.addTrueMention(m); //Side effect.

	    } else if (nEntContent.getNodeName() == "entity_attributes") {
		List<Chunk> entNames = processAttributes(eEntContent, "name");
		e.addNames(entNames);
	    }
	}	
	return e;
    }

    /**
     * Load a chunk.
     *
     * @param element An element containing a charseq Element.
     * @return The desired chunk.
     */
    protected Chunk processChunk(Element element) throws XMLException {
	try {
	    Node charseq = element.getElementsByTagName("charseq").item(0);
	    NamedNodeMap attrs = charseq.getAttributes();
	    String sStart = attrs.getNamedItem("START").getNodeValue();
	    int start = Integer.parseInt(sStart);
	    String sEnd = attrs.getNamedItem("END").getNodeValue();
	    int end = Integer.parseInt(sEnd);
	    String text = charseq.getFirstChild().getNodeValue();
	    return new Chunk(this, start, end, text);
	} catch (NullPointerException e) {
	    throw new XMLException("Chunk malformed.");
	}
    }



    /* Output */

    public void write(boolean usePredictions) {
	//TODO: Generalize file name.
	this.write("predictions/"+m_docID+".pred.apf.xml", usePredictions);
    }

    public void write(String filenameBase, boolean usePredictions) {
	//open file
	PrintStream dout;
	try {
	    dout = new PrintStream(
	    new FileOutputStream(filenameBase + ".apf.xml"));
	} catch (IOException e) {
	    System.err.println("Cannot open file for writing.");
	    e.printStackTrace();
	    return;
	}
	dout.println("<?xml version=\"1.0\"?>");
	dout.println("<!DOCTYPE source_file SYSTEM \"apf.v4.0.1.dtd\">");
	dout.println("<source_file URI=\"" + m_docID + ".sgm\" "
	 + "SOURCE=\"" + m_source + "\" TYPE=\"" + m_docType + "\" "
	 + "VERSION=\"" + m_version + "\" "
	 + "AUTHOR=\"" + m_annotationAuthor + "\" "
	 + "ENCODING=\"" + m_encoding + "\">");

	dout.println("<document DOCID=\"" + m_docID + "\">");

	List<Entity> entities = new ArrayList<Entity>(getEntities());
	Collections.sort(entities);
	for (Entity e : entities) {
	    //TODO: Subtypes???
	    dout.println(toXMLString(e) + "\n");
	}

	//TODO: Use predicted relations when appropriate.
	/* TODO: Include relations in output?
	for (Relation r : m_relations) {
	    dout.println(toXMLString(r));
	}
	*/

	dout.println("</document>");
	dout.println("</source_file>");
	//Close file
	dout.close();
    }

    /**
     * Removes the extension (including the periods) from the filename,
     * if it has an extension.
     * For DocAPF files, the extension is {@literal ".apf.xml"}.
     * @param filename The name of the file.
     * @return The name of the file with the extension removed.
     */
    protected String getBaseFilename(String filename) {
	if (filename.endsWith(".apf.xml"))
	    return filename.substring(0, filename.length() - 8);
	else
	    return filename;
    }

    protected String toXMLString(Chunk c) {
	return "<charseq START=\"" + c.getStart() + "\" "
	 + "END=\"" + c.getEnd() + "\">" + toXMLString(c.getText()) + "</charseq>";
    }

    protected String toXMLString(Entity e) {
	String result = "<entity ID=\"" + e.getID() + "\" "
	 + "TYPE=\"" + e.getType() + "\" ";
	if (e.getSubtype() != null && !e.getSubtype().equals(""))
	    result += "SUBTYPE=\"" + e.getSubtype() + "\" ";
	result += "CLASS=\"" + e.getSpecificity() + "\">\n";

	List<Mention> nams = new ArrayList<Mention>();
	List<Mention> ms = new ArrayList<Mention>(e.getMentions());
	Collections.sort(ms);
	for (Mention m : ms) {
	    if (m.getType().equals("NAM")) {
		nams.add(m);
	    }
	    result += toXMLString(m, "  ") + "\n";
	}
	Collections.sort(nams);
	if (nams.size() > 0) {
	    result += "  <entity_attributes>\n";
	    for (Mention nam : nams) {
		result += "    <name>\n";
		result += "      " + toXMLString(nam.getHead()) + "\n";
		result += "    </name>\n";
	    }
	    result += "  </entity_attributes>\n";
	}
	//FIXME: Add entity attributes?
	result += "</entity>";
	return result;
    }

	public void setDocID(String docID){ m_docID = docID; }

} //End class
