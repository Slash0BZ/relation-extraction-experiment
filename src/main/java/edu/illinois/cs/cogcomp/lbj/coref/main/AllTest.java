/**
 * 
 */
package edu.illinois.cs.cogcomp.lbj.coref.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;






import java.util.Random;











//import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.SimpleGazetteerAnnotator;
import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BIODecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ExtendHeadsDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTaLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MDExtendHeads;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MTypePredictor;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MentionDetectorMyBIOHead;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.IOManager;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;

public class AllTest {

	public static List<Constituent> MentionTest(TextAnnotation ta) throws Exception {
		POSAnnotator pos_annotator = new POSAnnotator();
		ta.addView(pos_annotator);
		//ServerClientAnnotator annotator = new ServerClientAnnotator();
		//annotator.setUrl("http://austen.cs.illinois.edu", "5800");
		//annotator.setViews(ViewNames.NER_CONLL);
		//SimpleGazetteerAnnotator gvc = new SimpleGazetteerAnnotator();
		try {
			//annotator.addView(ta);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		String modelPrefix = "4_tc_ns_";
		MentionDecoder mdDec =
				new ExtendHeadsDecoder(new MDExtendHeads("models_exp/" + modelPrefix + "md_extent.lc", "models_exp/" + modelPrefix + "md_extent.lex"),
						new BIODecoder(new MentionDetectorMyBIOHead("models_exp/" + modelPrefix + "md_head.lc", "models_exp/" + modelPrefix + "md_head.lex")));
		MTypePredictor mTyper = new MTypePredictor("models/md_tmp_type.lc", "models/md_tmp_type.lex");
		DocLoader loader = new DocFromTaLoader(mdDec, mTyper);
		Doc doc_test = loader.loadDoc(ta);
		List<Constituent> ret = new ArrayList<Constituent>();
		for (Mention m: doc_test.getMentions()) {
			int extentStart = m.getExtentFirstWordNum();
			int extentEnd = m.getExtentLastWordNum() + 1;
			int headCharStart = ta.getTokenCharacterOffset(m.getHeadFirstWordNum()).getFirst();
			int headCharEnd = ta.getTokenCharacterOffset(m.getHeadLastWordNum()).getSecond();
			Constituent newCons = new Constituent("Predicted", "RELATION_EXTRACTION", ta, extentStart, extentEnd);
			newCons.addAttribute("EntityHeadStartCharOffset", Integer.toString(headCharStart));
			newCons.addAttribute("EntityHeadEndCharOffset", Integer.toString(headCharEnd));
			newCons.addAttribute("EntityMentionType", m.getType());
			ret.add(newCons);
			/*
			System.out.println(m.getType()+"\t"+m.getHead().getCleanText()+"\t"+m.getHead().getStart()+"\t"+m.getHead().getEnd()+"\t"+
			                              m.getExtent().getCleanText()+"\t"+m.getExtent().getStart()+"\t"+m.getExtent().getEnd()+"\n");
			                    */
		}
		return ret;
	}
}


