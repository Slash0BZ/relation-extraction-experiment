package edu.illinois.cs.cogcomp.lbj.coref.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Formatter;
//import assertions.*;
import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import weka.gui.SysErrLog;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BIODecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ExtendHeadsDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoderOntonote;
// import edu.illinois.cs.cogcomp.lbj.coref.decoders.SieveMentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocTextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MDExtendHeads;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MTypePredictor;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MentionDetectorMyBIOHead;
import edu.illinois.cs.cogcomp.lbj.coref.learned.aceCorefSPLearner;
import edu.illinois.cs.cogcomp.lbj.coref.scorers.OutputJsonScorer;

public class Myadmin {
	private String path = "/shared/experiments/hpeng7/src/Coref-Resources/";
	private String configFile = path + "config/AcePlainTextConfig";
	DocLoader loader;
	public Myadmin() throws Exception{
		init(configFile);
	}
	public void init(String configFile) throws Exception{
		ConfigSystem.parseProps(configFile);
		// MentionDecoder mdDec =new SieveMentionDecoder();
		System.out.println("HERE: INIT mdDec");
		MentionDecoder mdDec= new ExtendHeadsDecoder(new MDExtendHeads(),
						new BIODecoder(new MentionDetectorMyBIOHead()));
		MTypePredictor mTyper = new MTypePredictor();
		loader = new DocFromTextLoader(mdDec, mTyper); // From a string
		Parameters.readParams(configFile);
		System.err.println("INIT Config");
		ConfigSystem.init(configFile);
		System.err.println("INIT Config done");
	}
	
	public String rain(){
		return "Hello World";
	}
	public String rain(String args) {
		boolean removeSigleton= false;
		if(args.startsWith("!!!REMOVESINGLETON!!!")){
			args= args.replace("!!!REMOVESINGLETON!!!", "");
			removeSigleton = true;
			System.out.println("REMOVE SINGLETON");
		}
		Doc doc = null;
	    String fullText = args;//"";
	    /*for (String s :args){
	      fullText += s+"\n";
	    }*/
	    System.out.println("Start Load");
	    doc = loader.loadDoc(fullText);
	    System.out.println("Load Finished");
	    
	    for(Mention m: doc.getPredMentions()){
	    	System.err.println("----:" +m);
	    }
	    
	    List<Doc> docs = new ArrayList<Doc>();
		docs.add(doc);
		System.err.println("Start Solve Coref");
		String annotation = null;
		try {
			ChainSolution<Mention> res  = ConfigSystem.TestCoref(docs, configFile, removeSigleton);
			OutputJsonScorer scorer = new OutputJsonScorer("d3");
			annotation = scorer.getScore(null, res).getScoreString();
			System.out.println(annotation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annotation.trim();
	}

	public static void main(String[] args) {
/*		Myadmin myad = new Myadmin();
		args = new String[3];
		args[0] = new String("Patient recently took a tablet of Aspirin .");
		args[1] = new String("After he took the tablet , he expressed the need for chest x-ray .");
		args[2] = new String("It was found out that the patient had suffered from heart attack in previous life .");
		myad.rain(Arrays.asList(args));
*/
		try {
			System.out.println("Attempting to start XML-RPC Server...");
			WebServer webServer = new WebServer(8082);
			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("sample", Myadmin.class); //new JavaServer().getClass());
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			//serverConfig.setKeepAliveEnabled(true);
			boolean res = serverConfig.isKeepAliveEnabled(); 
			webServer.start();
			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");
		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}

	public void admin_done(){
	}

	public static void startServer(int portNumber) {
		try {
			System.out.println("Attempting to start XML-RPC Server...");
			WebServer webServer = new WebServer(portNumber);
			XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("sample", Myadmin.class); //new JavaServer().getClass());
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);
			//serverConfig.setKeepAliveEnabled(true);
			boolean res = serverConfig.isKeepAliveEnabled();
			webServer.start();
			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");
		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}
}

enum Displaywhat {
	LITTLE, LOTS
};

enum Makewhat {
	ALL, NONE
};

enum Myconfig {
	B, BK, BKP, BKPC, BKC, BPC, BKPCJ
};
