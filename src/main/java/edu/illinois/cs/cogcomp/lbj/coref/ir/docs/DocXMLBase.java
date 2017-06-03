/**
 * contains methods to load input from XML files.
 */
package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.Debug;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;
import edu.illinois.cs.cogcomp.lbj.coref.util.xml.SimpleXMLParser;
import edu.illinois.cs.cogcomp.lbj.coref.util.xml.XMLException;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.features.Case;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.Relation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.RelationEntityArgument;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.RelationMention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.relations.RelationMentionArgument;


/**
 * The superclass of documents loaded from XML.
 * @author Eric Bengtson
 */
public abstract class DocXMLBase extends DocTextAnnotation {
    private static final long serialVersionUID = 45L;

    /** Basic constructor: Not recommended. */
    public DocXMLBase() {
	super();
    }

    /**
     * Given the name of a file and the extension, 
     * load the file and reads in the {@literal XML} representation.
     * @param filename The filename, which may or may not end with {@code ext}.
     * @param ext The extension of the filename, without a leading period.
     */
    public DocXMLBase(String filename, String ext) throws XMLException {
	this(filename, ext, PosSource.LBJ);
    }

    /**
     * Given the name of a file and the extension, 
     * load the file and reads in the {@literal XML} representation.
     * @param filename The filename, which may or may not end with {@code ext}.
     * @param ext The extension of the filename, without a leading period.
     * @param posSource If {@code PosSource.FILE},
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
     */
    public DocXMLBase(String filename, String ext, PosSource posSource)
     throws XMLException {
	initMembersDefault(); //TODO: super instead?

	m_baseFN = this.getBaseFilename(filename).trim();


	//List<String> words = null;
	//String wordFN = m_baseFN + ".sgm.strip_word";
	//if ((new File(wordFN)).exists()) {
	//    words = DocLoad.loadWords(wordFN);
	//} else {
	//    words = Arrays.asList(this.getPlainText().split("\\s"));
	//}


	loadSGMText(m_baseFN + ".sgm");

	if (posSource == PosSource.SNOW) {
	    System.out.println("Loading POS tags using SNoW.");
	    //FIXME: Don't rely on file here.
	    System.out.println("Loading Word/sentence splits from file.");
	    this.loadChunkedText(m_baseFN + ".sgm.strip_chunker");
	    //loadFromText(getPlainText(), /*split*/ true, /*POS*/ false);
	    String posTaggedText = loadPOSTaggerOutput();
	    if (posTaggedText != null) {
		loadPOSTags(posTaggedText);
	    } else {
		System.err.println(
		 "Cannot use SNoW-based POS tagger in separate process."
		 + " Check PATH_POS and be sure it is exported.");
		//Backoff:
		loadFromText(getPlainText(), /*split*/ true, /*POS*/ true);
	    }
	} else if (posSource == PosSource.FILE) {
	    //Supposedly doesn't work from Applets:
	    //Also, requires .strip_chunker files from UIUC preprocessing tools.
	    System.out.println("Loading POS tags from file.");
	    this.loadChunkedText(m_baseFN + ".sgm.strip_chunker");
	} else { //PosSource.LBJ or default:
	    //Use this instead.
	    //System.err.println("NOTE: Modified to use offline chunk before LBJPOS");
	    //this.loadChunkedText(m_baseFN + ".sgm.strip_chunker");
	    boolean split = true;
	    boolean pos = true;
	    //loadFromText(getPlainText(), split, pos);
	}

	//TODO: This should only be needed for the backwardsCompatible form.
	this.calcAndSetQuotes();

	initTextAnnotation();
	setWordsFromTA();
	
	loadXML(m_baseFN + "." + ext);


	m_bNeedsCasing = !(isCaseSensitive());
	// validatePhrases();
	//printAlignedText();
    }

    public DocXMLBase(String baseFilename, String ext, Classifier caser)
     throws XMLException {
	this(baseFilename, ext);
	m_caser = caser;
	/* Case words if needed */
	if (m_bNeedsCasing == true && m_caser != null) {
	    List<String> newWords = new ArrayList<String>();
	    for (int w = 0; w < this.getWords().size(); ++w) {
		String word = Case.getCasedWord(this, w, m_caser);
		newWords.add(word);
	    }
	    //FIXME: Don't case this way, as it isn't compatible.
	    //this.setWords(newWords);
	    System.out.println("Cased.");
	}
    }


    /**
      *
      * @param filename
      *                file to load containing xml representation.
      */
    public void loadXML(String filename) throws XMLException {
	//System.out.println("Beginning to load xml.");
	/* Get the <Document> and its children */
	if (!filename.startsWith("/")) filename = "/" + filename;
	/*
	InputStream in = this.getClass().getResourceAsStream(filename);
	if (in == null) {
	    throw new XMLException("Cannot find file " + filename);
	}
	System.out.println("Opened stream for xml.");
	*/
	String fqfn = filename;
	try {
	    fqfn = (new myIO()).findFile(filename);
	    //System.out.println("Found file " + fqfn);
	} catch (Exception e) {
	    System.err.println("Cannot find file: " + filename);
	    e.printStackTrace();
	}

	Document doc = SimpleXMLParser.getDocument(fqfn);
	Element eRoot = doc.getDocumentElement();

	//Note: Added to be able to write file back:
	NodeList sfs = doc.getElementsByTagName("source_file");
	Element eSourceFile = null;
	if (sfs.getLength() > 0)
	    eSourceFile = (Element) sfs.item(0);

	if (eSourceFile == null)
	    System.err.println("No <source_file> tag found.");
	else {
	    NamedNodeMap sfAttrs = eSourceFile.getAttributes();
	    m_source = getOptAttrib(sfAttrs, "SOURCE", "").trim();
	    m_docType = getOptAttrib(sfAttrs, "TYPE", "").trim();
	    m_version = getOptAttrib(sfAttrs, "VERSION", "").trim();
	    m_annotationAuthor = getOptAttrib(sfAttrs, "AUTHOR", "").trim();
	    m_encoding = getOptAttrib(sfAttrs, "ENCODING", "").trim();
	}


	Element eDoc = SimpleXMLParser.getElement(eRoot, "document");
	if (eDoc == null)
	    throw new XMLException("No <document> tag found.");
	NamedNodeMap attrs = eDoc.getAttributes();
	String docID = getOptAttrib(attrs, "DOCID", "").trim();
	if (!m_docID.equals(docID))
	    throw new XMLException("Document IDs don't match.");
	NodeList elements = eDoc.getChildNodes();

	/* Process all entities and relations: */
	for (int i = 0; i < elements.getLength(); i++) {
	    Node nEntityOrRelation = elements.item(i);
	    if (nEntityOrRelation.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    Element eEntityOrRelation = (Element) nEntityOrRelation;
	    if (nEntityOrRelation.getNodeName() == "entity") {
		Entity e = loadEntity(eEntityOrRelation); //Side effect: Adds mentions.
		this.addTrueEntity(e);
	    } else if (nEntityOrRelation.getNodeName() == "relation") {
		Relation r =loadRelation(eEntityOrRelation);
		this.addRelation(r);
	    } else {
		Debug.p("Warning: unexpected tag when seeking E or R");
	    }
	}
	if(m_mentionTAMap==null)
		m_mentionTAMap = new HashMap<Mention, Constituent>();

	SpanLabelView typedView = new SpanLabelView(
			Constants.GOLD_MENTION_VIEW, "predict", getTextAnnotation(), 1.0, true);
	System.out.println(getTextAnnotation().getAvailableViews() + "\t" + getTextAnnotation().getTokens().length);
	for(Mention m: getMentions()){
		//System.out.println(m.getExtent().getStart() + "\t" + m.getExtent().getEnd());
		//System.out.println(m.getExtentFirstWordNum() + "\t" + (m.getExtentLastWordNum()+1));
		//System.out.print(m.getCleanText() + "\t");
		//for (int k = m.getExtentFirstWordNum(); k < m.getExtentLastWordNum()+1; k++) {
		//	System.out.print(getTextAnnotation().getToken(k) + " ");
		//}
		//System.out.println();
		Constituent c = new Constituent("PredMent", 0.0, typedView.getViewName(),
		getTextAnnotation(), m.getExtentFirstWordNum(), m.getExtentLastWordNum()+1);
		c.addAttribute(Constants.MentionHeadStart, String.valueOf(m.getHeadFirstWordNum()));
		c.addAttribute(Constants.MentionHeadEnd, String.valueOf(m.getHeadLastWordNum()+1));
		typedView.addConstituent(c);
		m_mentionTAMap.put(m, c);
	}
	this.sortTrueMentions();
	getTextAnnotation().addView(Constants.GOLD_MENTION_VIEW, typedView);
	System.out.println("One Document Done!");
    }


    abstract protected Entity loadEntity(Node nEntity) throws XMLException;

    /** Loads a Relation from an xml representation and returns it. */
    protected Relation loadRelation(Element node) throws XMLException {
	/* Get attributes */
	NamedNodeMap attrs = node.getAttributes();
	String id = getOptAttrib(attrs, "ID", "");
	String type = getOptAttrib(attrs, "TYPE", "");
	String subType = getOptAttrib(attrs, "SUBTYPE", "");

	/* Process Entity args */
	NodeList relEnts = node.getElementsByTagName("rel_entity_arg");
	if (relEnts.getLength() < 1){
		relEnts = node.getElementsByTagName("relation_argument");
		if (relEnts.getLength() < 1){
			throw new XMLException("No relation arguments");
		}
	}
	RelationEntityArgument entArg1 = processRelationEntityArgument((Element) relEnts
	    .item(0));
	RelationEntityArgument entArg2 = null;
	if (relEnts.getLength() >= 2)
	    entArg2 = processRelationEntityArgument((Element) relEnts.item(1));

	/* Process mentions */
	NodeList relMents = node.getElementsByTagName("relation_mention");
	RelationMention m = null;
	if (relMents.getLength() > 0)
	    m = processRelationMention((Element) relMents.item(0));

	return new Relation(id, type, subType, entArg1, entArg2, m);
    }

    /**
         * Gets all Chunks found inside parent with nodeName
         * {@code attrName}.
         *
         * @param parent
         *                of children that have name {@code attrName}.
         * @param attrName
         *                Name of children to extract.
         */
    protected List<Chunk> processAttributes(Element parent, String attrName)
	throws XMLException {
	NodeList nl = parent.getChildNodes();
	List<Chunk> list = new LinkedList<Chunk>();
	for (int i = 0; i < nl.getLength(); i++) {
	    Node node = nl.item(i);
	    if (node.getNodeType() != Node.ELEMENT_NODE
		|| node.getNodeValue() != attrName)
		continue;
	    Chunk c = processChunk((Element) node);
	    list.add(c);
	}
	return list;
    }




    /** ** UTILITY METHODS *** */

    /**
     * Process an {@code mentionType}_mention tag. Must not be
     * called until counting texts and word split texts have been processed.
     *
     * @param node A {@code mentionType}_mention node
     * @param entityID The ID of the entity that this mentions.
     * @param specificity The specificity ("SPC" or "GEN") of the mention.
     * @param entityType The entity-type.
     * @param subtype The entity-type subtype.
     * @return The processed mention.
     * @throws XMLException If the XML cannot be processed.
     */
    protected Mention processEntityMention(Element node, String entityID,
	String entityType, String subtype, String specificity)
	throws XMLException {
	/*
         * TODO: Verify that ID, TYPE, and LDCTYPE are always available. throws
         * XMLException exception otherwise.
         */
	NamedNodeMap attrs = node.getAttributes();
	String id = getOptAttrib(attrs, "ID", "");
	String type = getOptAttrib(attrs, "TYPE", "");
	type = typeRename(type);
	String ldcType = getOptAttrib(attrs, "LDCTYPE", "");
	String ldcAtr = getOptAttrib(attrs, "LDCATR", "");
	String role = getOptAttrib(attrs, "ROLE", "");
	/* NOTE: Redundancy could slow code slightly here: */
	Chunk extent = findAndProcessChunk(node, "extent");
	Chunk head = findAndProcessChunk(node, "head");
	//if(type == "PRE") type = new String("NOM");
	return new Mention(this,
	    id, type, ldcType, ldcAtr, role, extent, head,
	    entityID, entityType, subtype, specificity, true);
    }

    private String typeRename(String type) {
		if(type.equals("NOMINAL"))
			return "NOM";
		if(type.equals("PRONOUN"))
			return "PRO";
		if(type.equals("NAME"))
			return "NAM";
		else
			return type;
	}

	private RelationMention processRelationMention(Element element)
	throws XMLException {
	/* Attributes */
	NamedNodeMap attrs = element.getAttributes();
	String id = getOptAttrib(attrs, "ID", "");
	String ldcLexCond = getOptAttrib(attrs, "LDCLEXICALCONDITION", "");

	/* extent */
	Chunk ldcExtent = findAndProcessChunk(element, "ldc_extent");

	/* Mention Arguments */
	NodeList relMentionArgs = element
	    .getElementsByTagName("rel_mention_arg");
	if (relMentionArgs.getLength() < 1){
		relMentionArgs = element.getElementsByTagName("relation_mention_argument");
		if (relMentionArgs.getLength() < 1){
			throw new XMLException("No relation arguments");
		}
	}
	RelationMentionArgument mArg1 = processRelationMentionArgument((Element) relMentionArgs
	    .item(0));
	RelationMentionArgument mArg2 = null;
	if (relMentionArgs.getLength() >= 2) {
	    Element arg2 = (Element) relMentionArgs.item(1);
	    mArg2 = processRelationMentionArgument(arg2);
	}

	return new RelationMention(id, ldcLexCond, ldcExtent, mArg1, mArg2);
    }

    private RelationMentionArgument processRelationMentionArgument(Element node)
	throws XMLException {
	NamedNodeMap attrs = node.getAttributes();
	String id = getOptAttrib(attrs, "ENTITYMENTIONID", "");
	int argNum = Integer.parseInt(getOptAttrib(attrs, "ARGNUM", "-1"));
	Chunk extent = findAndProcessChunk(node, "extent");
	return new RelationMentionArgument(id, argNum, extent);
    }

    private RelationEntityArgument processRelationEntityArgument(Element node) {
	NamedNodeMap attrs = node.getAttributes();
	String id = getOptAttrib(attrs, "ENTITYID", "");
	int argNum = Integer.parseInt(getOptAttrib(attrs, "ARGNUM", "-1"));
	return new RelationEntityArgument(id, argNum);
    }

    protected String getOptAttrib(NamedNodeMap attribs, String attribName,
	String defaultResult) {
	Node nAttr = attribs.getNamedItem(attribName);
	if (nAttr == null)
	    return defaultResult;
	else
	    return nAttr.getNodeValue();
    }

    /**
         * Find and load a chunk.
         *
         * @param parent
         *                Parent of Node with name {@code tagName}.
         * @param tagName
         *                tagName of desired chunk.
         * @return The desired Chunk.
         */
    private Chunk findAndProcessChunk(Element parent, String tagName)
	throws XMLException {
	NodeList contents = parent.getChildNodes();
	for (int i = 0; i < contents.getLength(); i++) {
	    Node el = contents.item(i);
	    if (el.getNodeName() == tagName) {
		return processChunk((Element) el);
	    }
	}
	return null;
	//throw new XMLException("Chunk '" + tagName + "' not found.");
    }

    /**
     * Load a chunk.
     *
     * @param element An element containing a charseq Element.
     * @return The desired chunk.
     */
    abstract protected Chunk processChunk(Element element) throws XMLException;

    public String getShortEID(String longID) {
	int b = longID.lastIndexOf("-E");
	if (b == -1)
	    b = 0;
	else
	    b += 2;
	return longID.substring(b);
    }

    abstract public void write(boolean usePredictions);
    abstract public void write(String filenameBase, boolean usePredictions);

    /** Trim possible extension from file. */
    abstract protected String getBaseFilename(String filename);





    //Aux functions to save to XML format.
    //Line prefixes are included for multi-line output and are absolute.
    //In general, final newline not included.


    protected String toXMLString(Mention m, String linePrefix) {
	//FIXME: subtypes, etc?
	String result
	 = linePrefix + "<entity_mention ID=\"" + m.getID() + "\" TYPE=\""
	 + m.getType() + "\" LDCTYPE=\"" + m.getLdcType() + "\"";
	 if (m.getLdcAtr().length() > 0)
	     result += " LDCATR=\"" + m.getLdcAtr() + "\"";
         result += ">\n"
	 + linePrefix + "  <extent>\n"
	 + linePrefix + "    " + toXMLString(m.getExtent()) + "\n"
	 + linePrefix + "  </extent>\n"
	 + linePrefix + "  <head>\n"
	 + linePrefix + "    " + toXMLString(m.getHead()) + "\n"
	 + linePrefix + "  </head>\n"
	 + linePrefix + "</entity_mention>";
	return result;
    }

    abstract protected String toXMLString(Chunk c);

    /** Converts plain text to XML safe format by escaping ampersands. */
    protected String toXMLString(String plainText) {
	return plainText.replaceAll("&", "&AMP;");
    }

    protected String toXMLString(Relation r) {
	String result = "<relation ID=\"" + r.getID() + "\" TYPE=\""
	 + r.getType() + "\" SUBTYPE=\"" + r.getSubtype() + "\">\n";
	//TODO: Pred or true entities??
	boolean foundEnts = true;
	if (r.getA1() != null) {
	     result += "  " + toXMLString(r.getA1(), 1) + "\n";
	    if (!foundPredEnt(r.getA1().getID()))
		foundEnts = false;
	}

	if (r.getA2() != null) {
	    result +=  "  " + toXMLString(r.getA2(), 2) + "\n";
	    if (!foundPredEnt(r.getA2().getID()))
		foundEnts = false;
	}

	if (r.getMention() != null)
	    result += toXMLString(r.getMention(), "  ") + "\n";

	result += "</relation>";
	if (foundEnts)
	    return result;
	else
	    return "";
    }

    private boolean foundPredEnt(String eID) {
	for (Entity e : getPredEntities()) {
	    if (e.getID().equals(eID))
		return true;
	}
	return false;
    }

    private String toXMLString(RelationEntityArgument a, int argNum) {
	//FIXME: Better handling of ids in predicted case (and distinguish).
	String eID = m_docID + "-E1";

	String mID = "";
	for (Entity e : getEntities()) {
	    if (e.getID().equals(a.getID())) {
		if (e.getMentions().size() > 0) {
		    mID = e.getMention(0).getID();
		    break;
		}
	    }
	}
	//TODO: What if true entities already?
	List<Entity> entities = this.getPredEntities();
	if (entities != null) {
	    for (Entity e : entities) {
		for (Mention m : e.getMentions()) {
		    if (m.getID().equals(mID)) {
			eID = e.getID();
			break;
		    }
		}
	    }
	}

	String result = "<rel_entity_arg ENTITYID=\"" + eID
	 + "\" ARGNUM=\"" + argNum + "\" />";
	return result;
    }

    private String toXMLString(RelationMention m, String linePrefix) {
	String result = linePrefix + "<relation_mention ID=\"" + m.getID()
	 + "\" LDCLEXICALCONDITION=\"" + m.getLDCLexicalCondition() + "\">\n"
	 + linePrefix + "  <ldc_extent>\n"
	 + linePrefix + "    " + toXMLString(m.getLDCExtent()) + "\n"
	 + linePrefix + "  </ldc_extent>\n"
	 + toXMLString(m.getArg1(), linePrefix + "  ") + "\n";
	if (m.getArg2() != null)
	     result += toXMLString(m.getArg2(), linePrefix + "  ") + "\n";
	result += linePrefix + "</relation_mention>";
	return result;
    }

    private String toXMLString(RelationMentionArgument a, String linePrefix) {
	String result = linePrefix + "<rel_mention_arg ENTITYMENTIONID=\""
	 + a.getID() + "\" ARGNUM=\"" + a.getArgNum() + "\">\n"
	 + linePrefix + "  <extent>\n"
	 + linePrefix + "    " + toXMLString(a.getExtent()) + "\n"
	 + linePrefix + "  </extent>\n"
	 + linePrefix + "</rel_mention_arg>";
	return result;
    }

} //End class Doc
