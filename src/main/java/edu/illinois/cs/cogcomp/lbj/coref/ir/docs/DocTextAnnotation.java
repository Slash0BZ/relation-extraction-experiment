/**
 * contains methods to load input from XML files.
 */
package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.MentionHeadFinder;
import edu.illinois.cs.cogcomp.lbj.coref.features.EntityTypeFeatures;
import edu.illinois.cs.cogcomp.lbj.coref.features.Gazetteers;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Chunk;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Entity;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

/**
 * The superclass of documents loaded from CoNLL 2011 format
 * 
 * @author Kai-Wei Chang
 */
public abstract class DocTextAnnotation extends DocBase {
	boolean setCorefLayer=true;
	private static final long serialVersionUID = 45L;
	protected Map<Mention,Constituent> m_mentionTAMap = null;
	private static String[] posForDetConjunction = new String[]{"NN","NNS","PRP","NNP","NNPS","PRP$","NML","JJ","HYPH","POS","JJS","JJR","DT","CC","CD","RB","-LRB-","-RRB-",","};
	private static String[] posCanBeAHeadWord = new String[]{"NN","NNS","PRP","NNP","NNPS","PRP$","NML","CC","CD","JJ","JJS","JJR","DT","VBG","VBN"};
		//"PRP","NN")|| pos.equals("NNS")|| pos.equals("NNP")|| pos.equals("NNPS") || pos.equals("PRP$")||  pos.equals("NML")|| pos.equals("JJ")||pos.equals("HYPH"
	

	public DocTextAnnotation(){
		super();
	}


	public void initTextAnnotation() {
		List<String> sentences = new ArrayList<String>();
		StringBuffer curSent = new StringBuffer();
		int sidx = 0;
		for(int i=0; i<this.getWords().size();i++){
			if(i==this.getSentStartNum(sidx)){
				sentences.add(curSent.toString());
				curSent = new StringBuffer();
				curSent.append(this.getWord(i));
				sidx++;
			}
			else{
				curSent.append(" " + this.getWord(i));
			}
		}
		/*for(String sent : sentences){
			System.out.print("====");
			System.out.print(sent);
			System.out.print("====");
		}*/
		sentences.add(curSent.toString());
		
		System.out.println(m_docID);
		TextAnnotation ta = null;
		/*
		try {
			ta = MyCuratorClient.client.createBasicTextAnnotation(m_docID, m_docID, this.getPlainText());
			MyCuratorClient.client.addView(ta, ViewNames.POS);
			//ta.addView(MyCuratorClient.gazetteers);
			ta.addView(MyCuratorClient.gazetteers.getViewName(), MyCuratorClient.gazetteers.getView(ta));
		} catch (AnnotatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		/*
		TokenLabelView posView = new TokenLabelView(ViewNames.POS, "GoldStandard", ta, 1.0);
		for (int j = 0; j < this.getWords().size(); j++)
			posView.addTokenLabel(j, this.getPOS(j), 1.0);
		ta.addView(ViewNames.POS, posView);
		*/
		this.setTextAnnotation(ta);
		
		//Haoruo added
		TextAnnotation[] tripleTA = new TextAnnotation[sentences.size()];
		for (int i=0;i<sentences.size();i++) {
			String str = sentences.get(i);
			if (i==0 || i==1) {
				tripleTA[i] = null;
				continue;
			}
			char c = str.charAt(0);
			if (c>='a' && c<='z') {
				c = (char) (c - 'a' +'A');
				str = c + str.substring(1);
			}
			tripleTA[i] = null;
			/*
			TextAnnotation tmpTA;
			try {
				tmpTA = client.createAnnotatedTextAnnotation(m_docID, new Integer(i).toString(), str);
				client.addView(tmpTA, ViewNames.POS);
				client.addView(tmpTA, ViewNames.DEPENDENCY_STANFORD);
				tripleTA[i] = tmpTA;
			} catch (AnnotatorException e) {
				tripleTA[i] = null;
				e.printStackTrace();
			}
			*/
		}
		this.setTripleTextAnnotation(tripleTA);
		System.out.println("Text Annotation Done!");
	}
	
	public void setWordsFromTA(){
		String[] words = getTextAnnotation().getTokens();
		StringBuffer countingText = new StringBuffer();
		for(int i = 0; i< words.length; i++)
		{
			String word = words[i];
			int w_start = countingText.length() + 1; // the begin of the word
			if(countingText.length()==0){
				w_start = 0;
				countingText.append(word);
			}
			else
				countingText.append(" " + word);
			int w_end = countingText.length() - 1; // the end of the word
			recordWordLocation(i, w_start, w_end);
		}
		//setM_countingText(getTextAnnotation().getTokenizedText());
		if (this.getPlainText() == null) {
			setM_countingText(getTextAnnotation().getTokenizedText());
		}
		setM_countingText(this.getPlainText());
		setWords(Arrays.asList(getTextAnnotation().getTokens()));
		List<Integer> sentNumList = new ArrayList<Integer>();
		// parsing words and tags
		for (int i = 0; i < getTextAnnotation().getNumberOfSentences(); i++) {
			Sentence s = getTextAnnotation().getSentence(i);
			for(int j=0; j < s.getTokens().length; j++)
				sentNumList.add(i);
		}
		TokenLabelView posView = (TokenLabelView) getTextAnnotation().getView(ViewNames.POS);
		setPOSTags(posView.getLabelsCovering(new Constituent("Dummy", "Dummy", getTextAnnotation(),
				0, getTextAnnotation().size())));
		setSentenceNumbers(sentNumList);
		this.calcAndSetQuotes();
	}
	
	public void setGoldMentionsFromTA(boolean setGoldEntity){
		if(m_mentionTAMap==null)
			m_mentionTAMap = new HashMap<Mention, Constituent>();
		List<Constituent> mentionView = getTextAnnotation().getView(Constants.GOLD_MENTION_VIEW).getConstituents();
		Map<String, Entity> entityMap = new HashMap<String, Entity>();
		CoreferenceView corefView = null;
		try {
			if(setGoldEntity) {
				corefView = (CoreferenceView) getTextAnnotation().getView(Constants.GOLD_COREF_VIEW);
				Set<Constituent> entities = corefView.getCanonicalEntities();
				for(Constituent e : entities)
				{
					String entityID = e.getAttribute(Constants.EntityID);
					Entity entity = new Entity(String.valueOf(entityID),
							"NONE",
							"NONE", "Ident");
					//this.addTrueEntity(entity);
					entityMap.put(entityID, entity);
				}
			}
		} catch (IllegalArgumentException e){
			System.out.println("ERROR" + e.toString());
		}
				
		Iterator<Constituent> mentionIter = mentionView.iterator();
		
		while(mentionIter.hasNext()){
			Constituent m = mentionIter.next();
			String mentionID = m.getAttribute(Constants.MentionID);
			int headStart = Integer.parseInt(m.getAttribute(Constants.MentionHeadStart));
			int headEnd = Integer.parseInt(m.getAttribute(Constants.MentionHeadEnd));
			String mentionType = m.getAttribute(Constants.MentionType);
			String entityType = m.getAttribute(Constants.EntityType);
			int spanStart = m.getStartSpan();
			int spanEnd = m.getEndSpan();
			Chunk extent = this.makeChunk(spanStart, spanEnd-1);
			Chunk head = this.makeChunk(headStart, headEnd-1);
			Mention mention = new Mention(this,
					mentionID, "NONE", "NONE", "NONE", "NONE", extent, head,
					"NONE", "NONE", "NONE",
					"Ident", true);
			
			mention.setType(mentionType);
			if(setGoldEntity) {
				//System.out.println("EID" + m.getAttribute(Constants.EntityID));
				String entityID = m.getAttribute(Constants.EntityID);
				mention.setTrueEntityID(entityID);
				entityMap.get(entityID).addMention(mention);
			}
			mention.setEntityType(entityType);
			this.addTrueMention(mention);
			m_mentionTAMap.put(mention,m);
		}
		for(Entity entity: entityMap.values()){
			this.addTrueEntity(entity);
		}
		sortTrueMentions();
	}
	
	public void setPredictMentionsFromTA(){
		if(m_mentionTAMap==null)
			m_mentionTAMap = new HashMap<Mention, Constituent>();
		List<Mention> predictedMentions = new ArrayList<Mention>();
		List<Constituent> mentionView = getTextAnnotation().getView(Constants.PRED_MENTION_VIEW).getConstituents();
		Iterator<Constituent> mentionIter = mentionView.iterator();
		int idx=0;
		while(mentionIter.hasNext()){
			Constituent m = mentionIter.next();
			Mention mention = genMention(idx, m, this, true);
			//if(mention.getExtentFirstWordNum() == 129)
			//System.out.println(mention.getSurfaceText() + " " + mention);
			//System.out.println(mention + " " + eType);
			
			m_mentionTAMap.put(mention,m);
			predictedMentions.add(mention);
		}
		this.setPredictedMentions(predictedMentions);
		sortPredictedMentions();
	}

	public static Mention genMention(int nM, Constituent m, Doc doc, boolean isPred) {
		//if(m.getStartSpan() == 129)
			//System.out.println(m.getSurfaceString());
		String mentionID = m.getAttribute(Constants.MentionID);
		Constituent nerConstituent = null;
		if(mentionID == null){
			if(isPred)
				mentionID = "pred_"+ nM;
			//nM++;
		}
		int spanStart = m.getStartSpan();
		int spanEnd = m.getEndSpan();
		int headStart, headEnd;
		String headStartS = m.getAttribute(Constants.MentionHeadStart);
		if(headStartS == null){
			int headPosition = DocTextAnnotation.getRobustHeadWordPosition(m);
			nerConstituent = doc.getNameEntitySpan(headPosition);
			Chunk head;
			if(nerConstituent == null || nerConstituent.getStartSpan() < m.getStartSpan() 
					|| (nerConstituent.getEndSpan())> m.getEndSpan()){
				headStart = headPosition;
				headEnd = headPosition+1;
				if(headStart >= spanStart+1 && WordHelpers.getWord(m.getTextAnnotation(),headStart-1).toLowerCase().equals("vice"))
					headStart = headPosition-1;
			}
			else {
				headStart = nerConstituent.getStartSpan();
				headEnd = nerConstituent.getEndSpan();
				if(WordHelpers.getWord(m.getTextAnnotation(), headEnd-1).equals("'s") && headEnd-headStart>1)
					headEnd --;
			}
							
		}
		else{
			headStart = Integer.parseInt(headStartS);
			headEnd = Integer.parseInt(m.getAttribute(Constants.MentionHeadEnd));
		}
		Chunk extent = doc.makeChunk(spanStart, spanEnd-1);
		Chunk head = doc.makeChunk(headStart, headEnd-1);
		Mention mention = new Mention(doc,
				mentionID, "NONE", "NONE", "NONE", "NONE", extent, head,
				"NONE", "NONE", "NONE",
				"Ident", true);
		String mentionType = m.getAttribute(Constants.MentionType);
		if(mentionType == null){
			if(Gazetteers.getPronouns().contains(mention.getHead().getText().toLowerCase()))
				mentionType = "PRO";
			else if(nerConstituent != null)
					mentionType =  "NAM";
			else
				mentionType = "NOM";
		}
		if(mention.getExtent().getText().equals("US"))
			mentionType = "NAM";
		mention.setType(mentionType);
		String eType = EntityTypeFeatures.getEType(mention);
		if(nerConstituent != null && mention.getType().equals("NAM")){
			String nerTag = nerConstituent.getLabel();
			if(nerTag.equals("PERSON"))
				eType = "PER";
			else if(!eType.equals(nerTag)){
				//System.out.println("##################"+ m + " " + eType + " "+ nerTag);
				eType = nerTag;
			}
		}
	/*	EntityDataBaseExp.initMapEntityCluster();
		String index = mention.getDoc().getDocID()+"_"+mention.getExtentFirstWordNum() + "_"+ mention.getExtentLastWordNum();
		  if(EntityDataBaseExp.MapFixNER.containsKey(index)){
			  //System.out.println("NER FIX"+EntityDataBaseExp.MapFixNER.get(index));
			  eType = EntityDataBaseExp.MapFixNER.get(index);
		  }*/
		mention.setEntityType(eType);
		return mention;
	}
	
	@Override
	public void setPredEntities(ChainSolution<Mention> sol) {
		super.setPredEntities(sol);
		if(getTextAnnotation() == null || !setCorefLayer)
			return;
		Set<Mention> usedMentionSet = new HashSet<Mention>();
		CoreferenceView corefView = new CoreferenceView("dump", "dump", getTextAnnotation(), 0.0);
		
		/*
			if(usedMentionSet.contains(m))
				 continue;
			if(!sol.contains(m))
				continue;
			Constituent canonicalMention = getConstituent(m);
			List<Constituent> corefMentions= new ArrayList<Constituent>();
			
			 for(Mention a : sol.getChainOf(m)){
				 usedMentionSet.add(a);
				 corefMentions.add(getConstituent(a));
			 }
			double [] scores = new double[corefMentions.size()];
			corefView.addCorefEdges(canonicalMention, corefMentions, scores);
		 }
		 */
		HashMap<Mention, Constituent> menConMap = new HashMap<Mention, Constituent>();
		List<Set<Mention>> mention_Chains = sol.getChains();
		int entityId = 0;
		//System.out.println(mention_Chains.size());
		for(Set<Mention> chain : mention_Chains){
			for(Mention m : chain){
				m.setPredictedEntityID(String.valueOf(entityId));
			}
			entityId++;
		}
		
		for(Mention m: this.getMentions()) {
			if(!sol.contains(m))
				continue;
			Constituent c_tmp = getConstituent(m);
			Constituent c = new Constituent(m.getPredictedEntityID(), Constants.PRED_COREF_VIEW, c_tmp.getTextAnnotation(), c_tmp.getStartSpan(), c_tmp.getEndSpan());
			for (String attr : c_tmp.getAttributeKeys()) {
				c.addAttribute(attr, c_tmp.getAttribute(attr));
			}
			menConMap.put(m, c);
			corefView.addConstituent(c);
		}
		for(Mention m: this.getMentions()) {
			if(!sol.contains(m))
				continue;
			
			Constituent c = menConMap.get(m);
			for(Mention a : sol.getChainOf(m)) {
				 if (a.equals(m)) {
					 continue;
				 }
				 Constituent coref_c = menConMap.get(a);
				 corefView.addRelation(new Relation("coref", c, coref_c, sol.getScore(m, a)));
			}
		}
		
		getTextAnnotation().addView(Constants.PRED_COREF_VIEW, corefView);
	}
	
	public int getRobustHeadWordPosition(int spanStart, int spanEnd) {
		Constituent c = new Constituent("Dummy", "Dummy", getTextAnnotation(),
				spanStart, spanEnd);
		return getRobustHeadWordPosition(c); 
	}
	public static int getRobustHeadWordPosition(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		View view = ta.getView(Constants.ParserName);
		List<Constituent> treeNodesWithSameSpan = (List<Constituent>) view
				.where(Queries.sameSpanAsConstituent(c));
		int sentenceId = ta.getSentenceId(c);
		int sentenceStart = ta.getSentence(sentenceId).getStartSpan();
		int start = c.getStartSpan() - sentenceStart;
		int end = c.getEndSpan() - sentenceStart;
		int headWordPosition = c.getEndSpan()-1;
	    //Check if it is a list
		Tree<String> subtree = ParseHelper.getTreeCovering(ParseHelper.getParseTree(Constants.ParserName, ta, sentenceId), start, end);
		boolean hasCC = false;
		boolean onlyNP = true;
		int numNP = 0;
		for(Tree<String> child : subtree.childrenIterator()){
			if(child.getLabel().equals("CC")){
				hasCC = true;
			}
			else if(!(child.getLabel().equals("NP")||child.getLabel().equals(","))){
				onlyNP = false;
			}
			if(child.getLabel().equals("NP"))
				numNP++;
		}
		if(hasCC&&onlyNP) {// it is a list
			//System.err.println(c.getSurfaceString());
			for(int  ii = c.getStartSpan(); ii < c.getEndSpan(); ii++)
				if(WordHelpers.getPOS(ta, ii).equals("CC"))
					return ii;
		}
		if(!hasCC && onlyNP && numNP > 1){
			int minNPSize = Integer.MAX_VALUE;
			Tree<String> minNP = null;
			for(Tree<String> child : subtree.childrenIterator()){
				if(child.getLabel().equals("NP")){
					int NPSize = 0;
					for(Tree<String> t:child.getYield()){
						if(t.isLeaf()){
							NPSize++;
						}
					}
					if(minNPSize > NPSize){
						minNPSize = NPSize;
						minNP = child;
					}
				}
			}
			StringBuffer sb = new StringBuffer();
			if(minNP!=null){
				int minNPStart = 0;
				for(Tree<String> t: TreeTraversal.depthFirstTraversal(subtree)){
					if(t==minNP)
						break;
					if(t.isLeaf()){
						sb.append(t.getLabel() +" ");
						minNPStart++;
					}
				}
				Constituent newC = new Constituent("", "", c.getTextAnnotation(),
						c.getStartSpan() + minNPStart, c.getStartSpan() + minNPStart + minNPSize);
				
				if(c.getSurfaceForm().startsWith(sb.toString())) // The tree matches the mention 
					return getRobustHeadWordPosition(newC);
			}
		}
		//TreeGrep<String> grepper = new TreeGrep<String>(TreeParserFactory
         //       .getStringTreeParser().parse("(NP NP CC NP )"));
		
		//if (grepper.matches(subtree)) {
		//	System.err.println(c.getSurfaceString());
/*	        for (TreeGrepMatch<String> match : grepper.getMatches()) {
	                //System.out.println(match.toString());
	                //
	                for (Tree<String> t : match.getPatternDFSMatches()) {
	                        System.out.println(t);
	                }
	                System.out.println("===");

	        }
	        System.out.println("====");
	        for(Tree<String> t: subtree.getChildren()){
	        	System.out.println(t.getLabel());
	        	for(Tree<String> tr : t.getYield())
	        		System.out.print(tr.getLabel()+ " ");

	        	System.out.println("====");
	        }*/
		//}

		boolean pureNounPhase = true;

		/*
		for(int  ii = c.getStartSpan(); ii < c.getEndSpan(); ii++){
			String pos = WordHelpers.getPOS(ta, ii);
			if(!(myAux.isStringEquals(pos,posForDetConjunction , false)))
				pureNounPhase = false;
		}
		//System.out.println(c);
		
		if(pureNounPhase)
			for(int  ii = c.getStartSpan(); ii < c.getEndSpan(); ii++)
				if(WordHelpers.getWord(ta, ii).equals("and") ||WordHelpers.getWord(ta, ii).equals("or"))
					return ii;
					
//		Check if the first/last phase is NE if "and"/"or" is in the mention span
		if(WordHelpers.getWord(ta, headWordPosition).equals("and") ||WordHelpers.getWord(ta, headWordPosition).equals("or")){
			List<Constituent> nerCs = SpanLabelsHelper.getConstituentsInBetween((SpanLabelView) ta.getView(ViewNames.NER), c.getStartSpan(), c.getEndSpan());
			for(Constituent nerC : nerCs){
				if(c.getStartSpan() == nerC.getStartSpan() || c.getEndSpan() == nerC.getEndSpan()){
					return nerC.getEndSpan()-1;
			}
			}
		}*/
		

		//Check if the tree is well structured
		if (treeNodesWithSameSpan.size() >= 1) {
			MentionHeadFinder headFinder = new MentionHeadFinder();
			headWordPosition = getHeadWordPosition(c, headFinder,
					Constants.ParserName);
			Tree<String> pt = ParseHelper.getParseTreeCovering(Constants.ParserName, c);
			//if(pt.getLabel().equals("NML")){
				//headWordPosition = c.getEndSpan()-1;
			//}
			//System.out.println("TREE OK" + WordHelpers.getWord(ta, headWordPosition));
			//System.out.println(pt);
		}
		// Else use the last noun before a preposition as head if the tree is not structured
		else { 
			int lastNounPosition = c.getStartSpan();
			int ii;
			for( ii = c.getStartSpan(); ii < c.getEndSpan(); ii++){
				String pos = WordHelpers.getPOS(ta, ii);
				if(pos.equals("PRP") || pos.equals("NN")|| pos.equals("NNS")|| pos.equals("NNP")|| pos.equals("NNPS") || pos.equals("PRP$")||  pos.equals("NML") )
					lastNounPosition = ii;
				if(pos.equals("WDT")||pos.equals("TO") || pos.equals("WP")|| pos.equals("IN")|| pos.equals(","))
					break;
			}
			if(lastNounPosition != headWordPosition && ii !=c.getEndSpan())
				headWordPosition = lastNounPosition;
			//System.out.println("TREE NOT OK" + WordHelpers.getWord(ta, headWordPosition));
				//System.out.println("CHECK HEAD:" + c.getSurfaceString() + WordHelpers.getWord(ta, lastNounPosition));
		}
		
//		String treeString ="(TOP(S(NP (DT A)  (NNP US)  (NN poll)) (VP (VBZ shows) (SBAR(S(NP(NP (NNP President)  (NNP Clinton))  (CC and) (NP(NP (PRP$ his)  (NN wife))  (, ,) (NP(NML (NNP First)  (NNP Lady))  (NNP Hillary)  (NNP Rodham)  (NNP Clinton)))) (VP (VBP are) (NP(NP (DT the)  (NN man)  (CC and)  (NN woman)) (VP(ADVP (RBS most))  (VBN admired) (PP (IN by) (NP (NNPS Americans)))))))))  (. .)))";
		if(!c.doesConstituentCover(headWordPosition)){
			System.err.println("ERR: HEAD NOT IN SPAN " + c + " cb: "+ c.getStartSpan() + " ce: " + c.getEndSpan() + " head:" + headWordPosition + c.getSurfaceForm() + 
					"head:" + WordHelpers.getWord(ta, headWordPosition));
			headWordPosition = c.getEndSpan() - 1;
		}
		// If word is in the possesses form, the head should be the noun.
		// For example Cuba 's : the head should be Cuba.
		String headWordPos =WordHelpers.getPOS(ta, headWordPosition);
		String headWordText  =  WordHelpers.getWord(ta,headWordPosition); 
//		if((WordHelpers.getPOS(ta, headWordPosition).equals("POS") || WordHelpers.getWord(ta,headWordPosition).equals("'s") || WordHelpers.getWord(t) && c.doesConstituentCover(headWordPosition -1)){
			//System.out.println(c.getSurfaceString());
		if(headWordText.equals("'s") || headWordPos.equals("POS")){
			if(c.doesConstituentCover(headWordPosition-1) &&  headWordPosition == c.getEndSpan()-1){
				headWordPosition -=1;
				if(headWordPosition == c.getEndSpan()-2 && c.getEndSpan()-c.getStartSpan()>=3 &&
						WordHelpers.getWord(ta, headWordPosition).toLowerCase().equals("government") &&
						Gazetteers.getCountriesDemAdj().contains(WordHelpers.getWord(ta, headWordPosition-1).toLowerCase())
				){
					headWordPosition-=1;
				}
				return headWordPosition;
			}
			else if (c.doesConstituentCover(headWordPosition+1))
				return getRobustHeadWordPosition(new Constituent(c.getLabel(), c.getViewName(), c.getTextAnnotation(), headWordPosition+1, c.getEndSpan()));
		}
		if(c.doesConstituentCover(headWordPosition-1) &&  !(myAux.isStringEquals(headWordPos, posCanBeAHeadWord , false))){
			headWordPosition -= 1;
		}
		
		//If last word is head, check if the head need to be move to previous word
		
		// Deal with the case "Jinlin, China" 
		if(headWordPosition == c.getEndSpan()-1 && c.getEndSpan()-c.getStartSpan()>=3 && 
				WordHelpers.getWord(ta, c.getEndSpan()-2).equals(",")){
			headWordPosition-=2;
		}
		// if the head is government, check if there is a country name before it
		if(headWordPosition == c.getEndSpan()-1 && c.getEndSpan()-c.getStartSpan()>=2 &&
				WordHelpers.getWord(ta, headWordPosition).toLowerCase().equals("government") &&
				Gazetteers.getCountriesDemAdj().contains(WordHelpers.getWord(ta, headWordPosition-1).toLowerCase())
		){
			headWordPosition-=1;
		}
//		System.out.println(WordHelpers.getWord(ta, headWordPosition)+" ");

		return headWordPosition;
	}

	static int getHeadWordPosition(Constituent c, MentionHeadFinder headFinder,
			String parseViewName) {
		TextAnnotation ta = c.getTextAnnotation();
		TreeView view = (TreeView)ta.getView(parseViewName);
		/*
		int sentenceId = ta.getSentenceId(c);
		Tree<String> tree = ParseHelper.getParseTree(parseViewName, ta,
				sentenceId);
		int sentenceStart = ta.getSentence(sentenceId).getStartSpan();

		//int start = c.getStartSpan() - sentenceStart;
		int end = c.getEndSpan() - sentenceStart;

		Tree<String> treeCoveringConstituent = ParseHelper.getTreeCovering(
				tree, start, end),;

		Tree<String> parse = ParseHelper.getParseTree(parseViewName, ta,
				ta.getSentenceId(c));
		*/
		Constituent parsePhrase = null;
		try {
			parsePhrase = view.getParsePhrase(c);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return headFinder.getHeadWordPosition(parsePhrase);
	}

	public Constituent getNameEntitySpan(int headPosition) {
		List<Constituent> cons;
		System.out.println("Got ner");
		try {
		View nerView = getTextAnnotation().getView(ViewNames.NER);
		cons = nerView.getConstituentsCoveringSpan(headPosition, headPosition+1);
		} catch (IllegalArgumentException e){
			return null;
		}
		if(cons.isEmpty())
			return null;
		else{
			Constituent c = cons.get(0);
			// include the honor
			if(c.getStartSpan() > 1){
				if(Gazetteers.getHonors().contains(getTextAnnotation().getToken(c.getStartSpan()-1).toLowerCase()))
						c = new Constituent(c.getLabel(), c.getViewName(),
								c.getTextAnnotation(), c.getStartSpan() -1, c.getEndSpan());
			}
			return c;
		}
		
	}

	public Map<Mention,Constituent> getMentionTAMap() {
		return this.m_mentionTAMap;
	}
	
	public void setMentionTAMap(Map<Mention,Constituent> m_mentionTAMap) {
		this.m_mentionTAMap = m_mentionTAMap;
	}

	public Constituent getConstituent(Mention m) {
		return m_mentionTAMap.get(m);
	}
	
} // End class Doc
