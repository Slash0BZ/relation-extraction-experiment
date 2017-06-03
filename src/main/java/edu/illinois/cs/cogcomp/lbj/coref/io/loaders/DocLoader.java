package edu.illinois.cs.cogcomp.lbj.coref.io.loaders;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Maps;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.features.EntityTypeFeatures;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.MentionSolution;


/**
  * Loads a corpus of documents.
  * <p>
  * To load a document, construct a subclass of this and then call
  * the {@link #loadDocs} method or the {@link #loadDoc} method
  * called with the correct type of input (see the relevant subclass for details.
  * </p><p>
  * To get the default document loader (which currently loads
  * documents from annotated .apf.xml files, use {@link #getDefaultLoader}
  * </p>
 */
abstract public class DocLoader {
  /*
    * Constructors in this class take the loading parameters (e.g. filenames)
    * rather than the load method to avoid specifying the type of parameters
    * in the class hierarchy.
   */


  /* Members */

  /**
    * Classifier that decides the true case (uppercase, etc) of text.
    * Not currently used.
   */
  protected Classifier m_caser = null;

  /** Name of file containing list of document filenames, one per line. */
  protected String m_fileListFN = null;

  /** Decoder that extracts predicted mentions from a document */
  protected MentionDecoder m_mdDecoder = null;

  /**
    * Classifier that determines the mention types of a mention.
    * Takes {@code Mention} objects as input and returns the type as a string,
    * "NAM", "NOM", "PRE", or "PRO".
   */
  protected Classifier m_mTypeClassifier = null;
  protected String m_preWordCount = null;
  
  protected Map<String,Integer> cCounts = null;

  /* Constructors */

  /** 
    * Construct a loader that loads a list of documents from a file.
    * The file contains a list of filenames, one per line.
    * Mentions will be predicted using the provided decoders and classifiers.
    * @param fileListFN The name of the corpus file,
    * containing a list of document filenames, one per line.
    * @param mentionDecoder The mention decoder extracts mentions from
    * a document.
    * @param mTyper Determines the mention types of each mention.
    * Takes {@code Mention} objects as input and returns the type as a string,
    * "NAM", "NOM", "PRE", or "PRO".
   */
  public DocLoader(String fileListFN, MentionDecoder mentionDecoder,
                   Classifier mTyper) {

    this(fileListFN);
    m_mdDecoder = mentionDecoder; 
    m_mTypeClassifier = mTyper;
  }

  /** 
    * Construct a loader for use when no file is used.
    * In this case, do not call {@code loadDocs()}, but rather call
    * {@code loadDoc(String inputString)} using the text as the input. 
    * Mentions will be predicted using the provided decoders and classifiers.
    * containing a list of filenames corresponding to documents.
    * @param mentionDecoder The mention decoder extracts mentions from
    * a document.
    * @param mTyper Determines the mention types of each mention.
    * Takes {@code Mention} objects as input and returns the type as a string,
    * "NAM", "NOM", "PRE", or "PRO".
   */
  public DocLoader(MentionDecoder mentionDecoder, Classifier mTyper) {
    m_mdDecoder = mentionDecoder;
    m_mTypeClassifier = mTyper;
  }

  /** 
    * Construct a loader that loads a list of documents from a file.
    * The file contains a list of filenames, one per line.
    * The resulting documents will have true mentions
    * but no predicted mentions.
    * @param fileListFN The name of the corpus file,
    * containing a list of document filenames, one per line.
   */
  public DocLoader(String fileListFN) {
    m_fileListFN = fileListFN;
  }

  /** 
    * Default constructor.
    * For use when no file is used.
    * In this case, do not call {@code loadDocs()}, but rather call
    * {@code loadDoc(String inputString)} using the text as the input. 
   */
  public DocLoader() {
  }



  /* Main Load function */


  /**
    * Load all the documents using filename and utilities already set.
    * @return A list of documents, possibly empty if IO problems..
   */
  public List<Doc> loadDocs() {
    //TODO: OPTIMIZE: Cache copy of loaded Docs in memory
    // (Make sure to know they're fresh but not take up too much memory.
    List<Doc> docs = new ArrayList<Doc>();
    String[] filenames = getFilenames();
    if(Parameters.PreWordCount!=null){
    	m_preWordCount = Parameters.PreWordCount;
    }
    for (String filenameBase : filenames) {
    	if(filenameBase.startsWith("#"))
    		continue;
      //System.out.println("Loading " + filenameBase);
      Doc d = loadDoc(filenameBase);
      if (d != null) {
    	  docs.add(d);
      }
    }
    if(m_preWordCount!=null){
    	if(cCounts == null)
    		loadCountWord();
    }
    else {
    	cCounts = new HashMap<String,Integer>();
    	for (Doc d : docs) {
    		Maps.addAllAToB(d.getWholeDocCounts(), cCounts);
    	}
    }
    for (Doc d : docs) {
    	d.setCorpusCounts(cCounts);
    }

    return docs;
  }



  /**
    * Loads a document.  Delegates to the {@code createDoc} method,
    * which may treat {@code inputString} as a filename or as text. 
    * @param inputString The filename or text, depending on the subclass.
    * If a filename, it may end with the appropriate extension.
    * @return a document corresponding to the {@code inputString}, either
    * representing the text of {@code inputString} or saved in the file
    * named by {@code inputString}
   */
  public Doc loadDoc(String inputString) {
    Doc d = createDoc(inputString);
    if(cCounts == null)
		loadCountWord();
    if (d == null)
      System.err.println("Problem reading file: ");
    if (m_mdDecoder != null && d != null) {
      List<Mention> predMents = getPredMents(d);
      
      Map<Mention, Constituent> map = null;
      if (d instanceof DocTextAnnotation) {
    	  map = ((DocTextAnnotation) d).getMentionTAMap();
  	  }
      if (map != null) {
	      for (Mention m : predMents) {
	    	  //System.out.println(map.get(m).getSurfaceForm() + "\t" + m.getType() + "\t" + m.getEntityType());
	    	  map.get(m).addAttribute(Constants.MentionType, String.valueOf(m.getType()));
	    	  map.get(m).addAttribute(Constants.EntityType, String.valueOf(m.getEntityType()));
	      }
      }
      
      d.setPredictedMentions(predMents);
    }
    if(cCounts !=null)
    	d.setCorpusCounts(cCounts);
    return d;
  }

  public Doc loadDoc(TextAnnotation ta){
      Doc d = createDoc(ta);
      if(cCounts == null)
          loadCountWord();
      if (d == null)
          System.err.println("Problem reading file: ");
      if (m_mdDecoder != null && d != null) {
          List<Mention> predMents = getPredMents(d);

          Map<Mention, Constituent> map = null;
          if (d instanceof DocTextAnnotation) {
              map = ((DocTextAnnotation) d).getMentionTAMap();
          }
          if (map != null) {
              for (Mention m : predMents) {
                  //System.out.println(map.get(m).getSurfaceForm() + "\t" + m.getType() + "\t" + m.getEntityType());
                  map.get(m).addAttribute(Constants.MentionType, String.valueOf(m.getType()));
                  map.get(m).addAttribute(Constants.EntityType, String.valueOf(m.getEntityType()));
              }
          }
          d.setPredictedMentions(predMents);
      }
      if(cCounts !=null)
          d.setCorpusCounts(cCounts);
      return d;
  }

  /**
    * Create a document from the given string,
    * treating {@code inputString} as a filename or as text
    * depending on the subclass.
    * @param inputString The filename or text, depending on the subclass.
    * If a filename, it may end with the appropriate extension.
    * @return a document corresponding to the {@code inputString}, either
    * representing the text of {@code inputString} or saved in the file
    * named by {@code inputString}
   */
  abstract protected Doc createDoc(Object inputString);


  /**
    * Opens the given file and reads a list of filenames from it,
    * one per line.
    * @param fileListFN The name of a file, relative to the "fileLists"
    * directory in the classpath, containing a list of filenames.
    * @return An array of strings corresponding to filenames
    * read from the specified file, or an empty array on failure.
   */
  public String[] getFilenames() {
    InputStream fileListStream = null;
    try { 
      fileListStream =
        getClass().getResourceAsStream("/" + m_fileListFN);
    } catch (Exception e) {
      System.err.println(m_fileListFN + " not found in /");
      e.printStackTrace();
      System.exit(1);
    }

    if (fileListStream == null) {
      try {
        fileListStream = getClass().getResourceAsStream("/fileLists/" + m_fileListFN);
      } catch (Exception e) {
        System.out.println(m_fileListFN + " not found in /fileLists");
        e.printStackTrace();
        System.exit(1);
      }
    }

    if (fileListStream == null) {
      System.err.println("Could not get the file " + m_fileListFN);
    }

    //TODO: Switch to readLines().
    String sFilenames = myIO.readAll(fileListStream);
    if (sFilenames == null || sFilenames.length() == 0) {
      System.err.println("Could not load fileList " + m_fileListFN);
      return new String[0];
    } else {
    	List<String> fileList = new ArrayList<String>();
    	for(String s : sFilenames.split("\n")){
    		if(s.startsWith("#"))
    			continue;
    		else
    			fileList.add(s);
    	}
      return (String[]) fileList.toArray(new String[fileList.size()]);
    }
  }



  /**
    * Predict mentions using predicted mention decoder, sets
    * mention types predicted by mention type classifier, and sets
    * entity types using the entity type feature.
    * To be called by the loadDoc or loadDocs methods.
    * @param doc The document whose mentions should be predicted.
    * @return The predicted mentions.
   */
  protected List<Mention> getPredMents(Doc doc) {
    List<Mention> results = new ArrayList<Mention>();

    //Does the decoder need to be reset before reusing?
    //System.err.println("Start Mention Detector");
    MentionSolution predMents = m_mdDecoder.decode(doc);
    //System.err.println("end Mention Detector");
    //System.err.println("Mentions detected:\n" + predMents);
    
    if (m_mTypeClassifier == null) {
      //System.err.println("No mention type classifier supplied.");
    }
    for (Mention m : predMents.getMentions()) {
      if (m_mTypeClassifier != null) {
        String mType = m_mTypeClassifier.discreteValue(m);
        m.setType(mType);
      }
      String eType = EntityTypeFeatures.getEType(m);
      m.setEntityType(eType);

      results.add(m);
    }
    return results;
  }


  /**
    * Gets the default loader.
    * This version is used when a list of files is specified.
    * @return the default DocLoader, which is currently DocAPFLoader.
    * @param fileList The name of the file list @see DocAPFLoader constructor.
   */
  public static DocLoader getDefaultLoader(String fileList) {
    return new DocAPFLoader(fileList);
  }

  /**
    * Gets the default loader.
    * This version is used when the loader does not take parameters.
    * @return the default DocLoader, which is currently DocAPFLoader.
   */
  public static DocLoader getDefaultLoader() {
    return new DocAPFLoader();
  }
  // Helper Functions
  protected void loadCountWord(){
	  if(m_preWordCount!=null){
		  System.out.println("LOADING THE COUNTWORD");
		  try {
			  ObjectInputStream oos = new ObjectInputStream(new FileInputStream(m_preWordCount));
			  cCounts =(Map<String,Integer>)oos.readObject();
			  oos.close();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (ClassNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
  }
  public void setM_preWordCount(String fileName){
	  m_preWordCount = fileName;
	  loadCountWord();
  }
}
