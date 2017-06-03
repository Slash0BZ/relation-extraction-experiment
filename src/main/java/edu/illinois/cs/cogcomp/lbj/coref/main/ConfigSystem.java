/**
 * 
 */
package edu.illinois.cs.cogcomp.lbj.coref.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.cooccurancedata.GoogleQuery.GoogleQueryFeature;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.constraints.Constraint;
import edu.illinois.cs.cogcomp.lbj.coref.constraints.IdenticalDetNom;
import edu.illinois.cs.cogcomp.lbj.coref.constraints.IdenticalProperName;
import edu.illinois.cs.cogcomp.lbj.coref.constraints.SameEntityExtendSpanConstraints;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BestLinkDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BestLinkDecoderAce;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BestLinkDecoderPronouns;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.CorefKeyDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ILPAllLinkDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ILPDecoder_BestLink_WithTransCon;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ScoredCorefDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.HardCorefDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.features.GigaWord;
import edu.illinois.cs.cogcomp.lbj.coref.features.PolarityFeature;
import edu.illinois.cs.cogcomp.lbj.coref.features.ProfilerFeature;
import edu.illinois.cs.cogcomp.lbj.coref.features.ProfilerFeatureExtractor_New;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.learned.ACL_Coref;
import edu.illinois.cs.cogcomp.lbj.coref.learned.aceCorefSPLearner;
import edu.illinois.cs.cogcomp.lbj.coref.postProcessing.NestedPostProcessing;
import edu.illinois.cs.cogcomp.lbj.coref.postProcessing.PosConstarintPostProcessing;
import edu.illinois.cs.cogcomp.lbj.coref.postProcessing.RemoveSingletonProcessing;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.VincentScorer;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.WinoScorer;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.BCubedScorer;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.CoNLLScorer;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.OutputD3Scorer;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.OutputJsonScorer;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;


//import edu.illinois.cs.cogcomp.lbjava.jni.GLPKHook;
public class ConfigSystem {
	private static double CorefThreshold = 0;
	private static double PronounThreshold = 0;
	private static String OntonotesCorefModel = null;
	private static String OntonotesProModel = null;
	private static String AceCorefModel = null;
	private static String AceProModel = null;
	public static String OutputDirName = null;
	private static String OutputFormat = "None";
	private static String AnnotationFormat = null;
	private static Boolean UseGoldMentions = false;
	private static String CuratorHost = null;
	private static int CuratorPort=-1;
	private static List<Doc> hard_coref_docs = null;
	static Learner corefClassifier = null;
	static Learner pronounClassifier = null;
	static ScoredCorefDecoder decoder= null;
	static List<Constraint> posConPool = null;

	static class LogFormatter extends Formatter {
		@Override
		public String format(LogRecord rec) {
			StringBuilder buf = new StringBuilder(1000);
			buf.append(formatMessage(rec));
			buf.append('\n');
			return buf.toString();
		}
	}	
	

	public static void init(String configFile) throws Exception{
		Parameters.readParams(configFile);
		
		if(AceCorefModel != null) {
			if (getAnnotationFormat().equals("Ace")) {
				corefClassifier = new aceCorefSPLearner(AceCorefModel+".lc", AceCorefModel+".lex");
			}
			if (getAnnotationFormat().equals("Ontonotes")) {
				corefClassifier = new ACL_Coref(AceCorefModel+".lc", AceCorefModel+".lex");
			}
			//System.out.println("Using new model!");
		}
		
		if(corefClassifier == null)
			corefClassifier = new aceCorefSPLearner();
			//new Emnlp8();
		
		if (Parameters.inferenceMethod.equals("Cluster")) {
			decoder= new BestLinkDecoderPronouns(corefClassifier,pronounClassifier);
			//decoder= new BestLinkDecoder(corefClassifier);
		}
		if (Parameters.inferenceMethod.equals("ILP")) {
			decoder = new ILPDecoder_BestLink_WithTransCon(corefClassifier,pronounClassifier);
		}
		if (Parameters.inferenceMethod.equals("ILPAddCon")) {
			decoder = new ILPAllLinkDecoder(corefClassifier);
		}
		
		posConPool = new ArrayList<Constraint>();
		posConPool.add(new SameEntityExtendSpanConstraints());
		posConPool.add(new IdenticalDetNom());
		posConPool.add(new IdenticalProperName());

		if(corefClassifier == null)
			System.out.println("Error: no coreference classifier");
		
		decoder.setThreshold(CorefThreshold);
		if (decoder instanceof BestLinkDecoderPronouns) {
			((BestLinkDecoderPronouns) decoder).setPronounThreshold(PronounThreshold);
		}
	}
	
	public static void Train(Learner classifier, Parser trainingParser, 
			int trainingRounds, String trainSampleFilename) throws Exception {
		System.out.println("Training...");
		classifier.forget();		
		BatchTrainer btTrain = new BatchTrainer(classifier, trainingParser, 100000);
		classifier.setLexicon(btTrain.preExtract(trainSampleFilename));
		btTrain.train(trainingRounds);
		System.out.println("Done training; saving the classifier");
		classifier.save();
		System.out.println("Done saving the classifier");
	}
	public static ChainSolution<Mention> TestPM(List<Doc> docs) throws Exception {
		return TestPM(docs, false);
	}
	
	public static ChainSolution<Mention> TestPM(List<Doc> docs, boolean removeSingletonFlag) throws Exception {		PosConstarintPostProcessing posConstraintPosProcessor = new PosConstarintPostProcessing();
		
		List<ChainSolution<Mention>> preds = new ArrayList<ChainSolution<Mention>>();
		RemoveSingletonProcessing removeSingleton = new RemoveSingletonProcessing();
		NestedPostProcessing removeNested = new NestedPostProcessing();
		
		GigaWord gw = new GigaWord();
		if (AceCorefModel.contains("Know") || Parameters.inferenceMethod.contains("Con")) {
			gw.setup();
			PolarityFeature.setup();
			ProfilerFeature.setup();
			GoogleQueryFeature.setup();
			//ProfilerFeatureExtractor_New.setup();
		}
		
		if (hard_coref_docs == null) {
			hard_coref_docs = new ArrayList<Doc>();
			hard_coref_docs.addAll(myIO.readSerializedDocs("/shared/experiments/hpeng7/src/Coref-Resources/data/winograd/WinoTrain.bin"));
			hard_coref_docs.addAll(myIO.readSerializedDocs("/shared/experiments/hpeng7/src/Coref-Resources/data/winograd/WinoTest.bin"));
		}
		
		ChainSolution<Mention> sol = new ChainSolution<Mention>();
		
		FileOutputStream fq = null;
		if(OutputFormat.contains("Query")){
			String str = docs.get(0).getDocID();
			str = str.split("#")[0];
			str = str.substring(0, str.length()-4);
			fq = new FileOutputStream(new File(str + ".txt"));
		}
		
		for (int i=0;i<docs.size();i++) {
			Doc d = docs.get(i);
			//System.out.println(i);
			
			boolean flag = false;
			if (hard_coref_docs != null) {
				String txt = d.getPlainText().trim();
				for (Doc doc: hard_coref_docs) {
					if (doc.getPlainText().substring(5).trim().equals(txt)) {
						System.out.println("Hard Coref");
						HardCorefDecoder hcdecoder = new HardCorefDecoder();
						hcdecoder.setDocRef(doc);
						sol = hcdecoder.decode(d);
						preds.add(sol);
						flag = true;
					}
				}
			}
			
			if (!flag) {
				d.setGigaWord(gw);
				d.setUsePredictedMentions(!UseGoldMentions);
				ChainSolution<Mention> presol = decoder.decode(d);
				sol = presol;
				
				for(Constraint con : posConPool){
					posConstraintPosProcessor.setConstraint(con);
					sol = posConstraintPosProcessor.decode(d, sol);
				}
				if(removeSingletonFlag||getAnnotationFormat().equals("Ontonotes"))
					sol = removeSingleton.decode(d, removeNested.decode(d, sol));
				// Set All singleton Pronouns to its nearest left neighbor
				//sol = postProcessing(sol);
				preds.add(sol);
				if (getAnnotationFormat().equals("Ace")) {					
					d.setPredEntities(sol);
//					for (Constituent c : d.getTextAnnotation().getView(Constants.PRED_COREF_VIEW).getConstituents()) {
//						if (c.getIncomingRelations() != null && c.getIncomingRelations().size() > 0) {
//							System.out.println(c.getIncomingRelations().get(0).getScore());
//							System.out.println(c.getSurfaceForm() + "\t" + c.getIncomingRelations().get(0).getSource().getSurfaceForm());
//						}
//						System.out.println(c.getAttributeKeys());
						//System.out.println(c.getLabel());
//					}
				}
//                              sol.addChain(((DocTextAnnotation) d).getAddMents()); 
			}
			
			if(OutputFormat.contains("Ontonotes")){
				CoNLLScorer scorer;
				if(OutputDirName==null)
					scorer = new CoNLLScorer(d.getDocID()+".column.coref", null);
				else
					scorer = new CoNLLScorer(OutputDirName +"/"+d.getDocID()+".column.coref", null);
				scorer.getScore(sol,d);
			}
			if(OutputFormat.contains("Ace")){
				FileOutputStream fop = null;
				String fileName = d.getDocID()+"ace.coref";
				if(OutputDirName!=null)
					fileName = OutputDirName + "/" + d.getDocID()+".ace.coref";
				
				try {
					File directory = new File(new File(fileName).getParent());
					if(!directory.exists())
						if(directory.mkdir()==false){
							System.err.println("Error in creating directory " + directory.getName());
						}
						else{
							System.err.println("Creating directory " + directory.getName());
						}
					
					fop = new FileOutputStream(new File(fileName));
					fop.write(d.toAnnotatedString(false, false, false, true).getBytes());
					fop.flush();
					fop.close();	 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fop != null) {
							fop.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if(OutputFormat.contains("D3")){
				OutputD3Scorer scorer;
				scorer = new OutputD3Scorer(OutputDirName);
				scorer.getScore(null, sol);
			}
			if(OutputFormat.equals("Json")){
				OutputJsonScorer scorer = new OutputJsonScorer("d3");
				//return scorer.getScore(null, preds.get(0)).getScoreString();
				//scorer.printPlainText(d);
			}
			if(OutputFormat.contains("Query")){
				fq.write(d.toAnnotatedString(false, false, false, true).getBytes());
				fq.write('\n');
				fq.flush();	 
			}
			System.out.println(i);
		}
		if(OutputFormat.contains("Query")){
			fq.close();
		}

		// Scoring:
//		if(OutputFormat.equals("BCub")){
//			List<ChainSolution<Mention>>
//		      keys = new ArrayList<ChainSolution<Mention>>();
//		    for (Doc d : docs)
//		      keys.add(new CorefKeyDecoder().decode(d));
//		    BCubedScorer scorer = new BCubedScorer();
//		    System.out.println(scorer.getScore(keys, preds));			
//		}
		
		// Scoring for Winograd-extended
		if(OutputFormat.contains("Wino")){
			List<ChainSolution<Mention>>
		      keys = new ArrayList<ChainSolution<Mention>>();
		    for (Doc d : docs) {
		      keys.add(new CorefKeyDecoder().decode(d));
		    }
		    WinoScorer scorer = new WinoScorer();
		    System.out.println(scorer.getScore(keys, preds));	
		}
		
		// Scoring for Winograd-original
		if(OutputFormat.contains("Vincent")){
			List<ChainSolution<Mention>>
		      keys = new ArrayList<ChainSolution<Mention>>();
		    for (Doc d : docs) {
		      keys.add(new CorefKeyDecoder().decode(d));
		    }
		    VincentScorer scorer = new VincentScorer();
		    System.out.println(scorer.getScore(keys, preds));	
		}
		
//		gw.stat();
		
		return sol;
	}
	
	private static ChainSolution<Mention> postProcessing(ChainSolution<Mention> sol) {
		Set<Mention> ments = sol.getAllMembers();
		for (Mention m: ments) {
			if (m.getType().equals("PRO") && sol.getChainOf(m).size() == 1) {
				Mention ante = findLeftNearest(ments, m.getExtent().getStart());
				if (ante != null) {
					sol.recordEquivalence(ante,m);
				}
			}
		}
		return sol;
	}

	private static Mention findLeftNearest(Set<Mention> ments, int start) {
		Mention ante = null;
		for (Mention m: ments) {
			if (m.getExtent().getEnd() <= start && !m.getType().equals("PRO") && ((ante == null) || (ante.getExtent().getStart() < m.getExtent().getStart()))) {
				ante = m;
			}
		}
		return ante;
	}

	private static List<BestLinkDecoderPronouns> decoders=null;

	public static void parseProps(String configFileName){
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configFileName));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
		if(props.containsKey("Perceptron.LearningRate")){
			Parameters.Perceptron_learningRate = Double.parseDouble(props.getProperty("PERCEPTRON.LearningRate"));
			System.out.println("PERCEPTRON LEARNING RATE " + Parameters.Perceptron_learningRate);
		}
		if(props.containsKey("Perceptron.Thickness")){
			Parameters.Perceptron_thickness = Double.parseDouble(props.getProperty("PERCEPTRON.Thickness"));
			System.out.println("PERCEPTRON Thickness " + Parameters.Perceptron_thickness);
		}
		if(props.containsKey("Perceptron.Round")){
			Parameters.Perceptron_round = Integer.parseInt(props.getProperty("PERCEPTRON.Round"));
			System.out.println("PERCEPTRON ROUND " + Parameters.Perceptron_round);
		}
		
		
		if(props.containsKey("CorefThreshold")){
			CorefThreshold = Double.parseDouble(props.getProperty("CorefThreshold"));
			//System.out.println("COREF Threshold: " + CorefThreshold);
		}
		if(props.containsKey("PronounThreshold")){
			PronounThreshold = Double.parseDouble(props.getProperty("PronounThreshold"));
			//System.out.println("PRONOUN Threshold: " + PronounThreshold);
		}
		
		if(props.containsKey("OutputDirName")){
			OutputDirName = props.getProperty("OutputDirName");
			//System.out.println("result files: " + OutputDirName);
		}
		if(props.containsKey("OntonotesCorefModel")){
			OntonotesCorefModel = (props.getProperty("OntonotesCorefModel"));
			//System.out.println("OntonotesCorefModel: " + OntonotesCorefModel);
		}
		if(props.containsKey("OntonotesProModel")){
			OntonotesProModel = (props.getProperty("OntonotesProModel"));
			//System.out.println("OntonotesProModel: " + OntonotesProModel);
		}
		if(props.containsKey("AceCorefModel")){
			AceCorefModel = (props.getProperty("AceCorefModel"));
			Parameters.aceCorefModel = AceCorefModel;
			//System.out.println("AceCorefModel: " + AceCorefModel);
		}
		if(props.containsKey("AceProModel")){
			AceProModel = (props.getProperty("AceProModel"));
			//System.out.println("AceProModel: " + AceProModel);
		}
		if(props.containsKey("OutputFormat")){
			OutputFormat = (props.getProperty("OutputFormat"));
			//System.out.println("OutputFormat: " + OutputFormat);
		}
		if(props.containsKey("AnnotationFormat")){
			setAnnotationFormat((props.getProperty("AnnotationFormat")));
			//System.out.println("AnnotationFormat: " + getAnnotationFormat());
		}
		if(props.containsKey("UseGoldMentions")){
			UseGoldMentions = Boolean.valueOf(props.getProperty("UseGoldMentions"));
			//System.out.println("UseGoldMentions: " + UseGoldMentions);
		}
		if(props.containsKey("CuratorHost")){
			setCuratorHost(props.getProperty("CuratorHost"));
			//System.out.println("CuratorHost: " + getCuratorHost());
		}
		if(props.containsKey("CuratorPort")){
			setCuratorPort(Integer.valueOf(props.getProperty("CuratorPort")));
			//System.out.println("CuratorPort: " + getCuratorPort());
		}
		if(getAnnotationFormat() == null)
			System.out.println("Please specify annotation format (Ontonotes or Ace)");
	}

	/**
	 * This function demonstrate how to generate coreference annotation to the documents 
	 * @param docs: test data
	 * @param configFile: configuration
	 * @throws Exception
	 */
	public static ChainSolution<Mention> TestCoref(List<Doc> docs, String configFile) throws Exception{
		return TestCoref(docs, configFile, false);
	}
	
	public static ChainSolution<Mention> TestCoref(List<Doc> docs, String configFile, boolean removeSingletonFlag) throws Exception{
		
		init(configFile);
		
		return TestPM(docs, removeSingletonFlag);
	}

	public static void setAnnotationFormat(String annotationFormat) {
		AnnotationFormat = annotationFormat;
	}

	public static String getAnnotationFormat() {
		return AnnotationFormat;
	}

	public static void setCuratorHost(String curatorHost) {
		CuratorHost = curatorHost;
	}

	public static String getCuratorHost() {
		return CuratorHost;
	}

	public static void setCuratorPort(int curatorPort) {
		CuratorPort = curatorPort;
	}

	public static int getCuratorPort() {
		return CuratorPort;
	}
}
