package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.Constants;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.myIO;

/**
 * Collection of features and tools related to pronouns resolution.
 */
public class PronounResolutionFeatures {
	protected static Map<String, String> PronounAnnotation = null;
	protected static boolean annotationInitialized = false;

	private static void initAnnotation() {
		String b = "gazetteers";
		PronounAnnotation = loadLinesAsMap(b + "/pronouns_annotate.txt", false);
		annotationInitialized = true;
		System.out.println("READFILE");
		return;
	}

	/** Should not need to construct this static feature library. */
	protected PronounResolutionFeatures() {
	}

	/**
	 * Determines whether one mention is a relative pronoun referring to the
	 * other mention.
	 * 
	 * @param ex
	 *            The example containing the mentions.
	 * @return Whether one mention is a relative pronoun referring to the other.
	 */
	public static boolean relativePronounFor(CExample ex) {
		if (!annotationInitialized)
			initAnnotation();
		Doc d = ex.getDoc();
		String m1Head = ex.getM1().getHead().getText().toLowerCase();
		String m2Head = ex.getM2().getHead().getText().toLowerCase();
		int sh1 = ex.getM1().getHeadFirstWordNum();
		int sh2 = ex.getM2().getHeadFirstWordNum();
		// int se1 = ex.getM1().getExtentFirstWordNum();
		// int se2 = ex.getM2().getExtentFirstWordNum();
		int eh1 = ex.getM1().getHeadLastWordNum();
		int eh2 = ex.getM2().getHeadLastWordNum();
		int ee1 = ex.getM1().getExtentLastWordNum();
		int ee2 = ex.getM2().getExtentLastWordNum();

		if (matchesRelative(m1Head)) {
			if (sh1 == ee2 + 1 || sh1 == eh2 + 1)
				return true;
			if (sh1 == ee2 + 2 && d.getWord(ee2 + 1).equals(",")
					|| sh1 == eh2 + 2 && d.getWord(eh2 + 1).equals(","))
				return true;
		}
		if (matchesRelative(m2Head)) {
			if (sh2 == ee1 + 1 || sh2 == eh1 + 1)
				return true;
			if (sh2 == ee1 + 2 && d.getWord(ee1 + 1).equals(",")
					|| sh2 == eh1 + 2 && d.getWord(eh1 + 1).equals(","))
				return true;
		}
		return false;
	}

	// Is this pronounce a subject(R) object (O) possessive(P) or unknown(U)
	public static char proSyntaticRule(CExample ex) {
		return proSyntaticRule(ex.getM2());
	}
	public static char proSyntaticRule(Mention m) {
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(m.getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("s"))
			return 'S';
		else if (feature.contains("o"))
			return 'O';
		else if (feature.contains("p"))
			return 'P';
		else
			return 'U';
	}

	// Is this pronounce Reflective(R) Wh- (W) or Indefinite (I) or Others (U)
	public static char proSyntaticType(CExample ex) {
		return proSyntaticType(ex.getM2());
	}
	public static char proSyntaticType(Mention m){
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(m.getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("r"))
			return 'R';
		else if (feature.contains("w"))
			return 'W';
		else if (feature.contains("I"))
			return 'I';
		else
			return 'U';
	}

	// First Person (1), Second Person (2), Third Person(3) or Unknown (U)
	public static char proPersonRule(CExample ex) {
		return proPersonRule(ex.getM2());
	}

	public static char proPersonRule(Mention m) {
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(m.getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("1"))
			return '1';
		else if (feature.contains("2"))
			return '2';
		else if (feature.contains("3"))
			return '3';
		else
			return 'U';
	}

	// is Human (H), Not Human(N), or Unknown (U)
	public static char proIsHuman(CExample ex) {
		return proIsHuman(ex.getM2());
	}

	public static char proIsHuman(Mention m) {
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(m.getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("H"))
			return 'H';
		else if (feature.contains("N"))
			return 'N';
		else
			return 'U';
	}

	// gender male (M), female(F), or Unknown (U)
	public static char proGender(CExample ex) {
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(ex.getM2().getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("M"))
			return 'M';
		else if (feature.contains("F"))
			return 'F';
		else
			return 'U';
	}

	// number: single (S), plural (P), or Unknown (U)

	public static char proNumber(CExample ex) {
		return proNumber(ex.getM2());
	}

	public static char proNumber(Mention m) {
		if (!annotationInitialized)
			initAnnotation();
		String feature = getPronounFeature(m.getText());
		if (feature.equals("Unknown"))
			return 'U';
		else if (feature.contains("S"))
			return 'S';
		else if (feature.contains("P"))
			return 'P';
		else
			return 'U';
	}

	/**
	 * Determines whether the given string is a relative pronoun.
	 * 
	 * @param w
	 *            The string, lowercased.
	 * @return Whether the given string is a lowercase relative pronoun.
	 */
	public static boolean matchesRelative(String w) {
		if (w.equals("who") || w.equals("whom") || w.equals("which")
				|| w.equals("whose") || w.equals("whoever")
				|| w.equals("whomever") || w.equals("whatever")
				|| w.equals("whichever") || w.equals("that")|| w.equals("where"))
			return true;
		else
			return false;
	}

	public static boolean isPersonPro(Mention m){
		String w = m.getExtent().getText();
		if (w.equals("US"))
			return false;
		w = w.toLowerCase();
		// Personal:
		if (w.equals("he") || w.equals("she") || w.equals("it")
				|| w.equals("him") || w.equals("her") || w.equals("his")
				|| w.equals("hers") || w.equals("its") || w.equals("they")
				|| w.equals("them") || w.equals("their") || w.equals("theirs")
				|| w.equals("i") || w.equals("me") || w.equals("mine")
				|| w.equals("my") || w.equals("we") || w.equals("us")
				|| w.equals("our") || w.equals("ours")
				|| w.equals("you")
				|| w.equals("your")
				|| w.equals("yours")
				|| w.equals("'s"))
			return true;
		return false;
	}

	public static boolean isPersonAndReflectivePronoun(Mention m) {
		return isPersonAndReflectivePronoun(m.getExtent().getText());
	}
	
	public static boolean isPersonAndReflectivePronoun(String w) {
		if (w.equals("US"))
			return false;
		w = w.toLowerCase();
		// Personal:
		if (w.equals("he") || w.equals("she") || w.equals("it")
				|| w.equals("him") || w.equals("her") || w.equals("his")
				|| w.equals("hers") || w.equals("its") || w.equals("they")
				|| w.equals("them") || w.equals("their") || w.equals("theirs")
				|| w.equals("i") || w.equals("me") || w.equals("mine")
				|| w.equals("my") || w.equals("we") || w.equals("us")
				|| w.equals("our") || w.equals("ours")
				|| w.equals("you")
				|| w.equals("your")
				|| w.equals("yours")
				|| w.equals("'s")
				// Reflexive
				|| w.equals("himself") || w.equals("herself")
				|| w.equals("itself") || w.equals("themselves")
				|| w.equals("themself") || w.equals("myself")
				|| w.equals("ourselves") || w.equals("ourself")
				|| w.equals("oneself") || w.equals("yourself")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isFirstorSecondPerson(Mention m) {
		return isFirstorSecondPerson(m.getExtent().getText());
	}

	public static boolean isFirstorSecondPerson(String w) {
		if (w.equals("US"))
			return false;
		w = w.toLowerCase();
		// Personal:
		if (w.equals("i") || w.equals("me") || w.equals("mine")
				|| w.equals("we") || w.equals("us") || w.equals("our")
				|| w.equals("ours") || w.equals("you") || w.equals("your")
				|| w.equals("yours")
				// Reflexive
				|| w.equals("myself") || w.equals("ourselves")
				|| w.equals("ourself")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isThirdPerson(Mention m) {
		return proPersonRule(m) == '3';
	}
	/**
	 * Determines whether the given string is a pronoun.
	 * 
	 * @param w
	 *            The string, lowercased.
	 * @return Whether the given string is a lowercase pronoun.
	 */
	public static boolean isPronoun(String w) {
		// Personal:
		if(w.equals("US"))
			return false;
		else
			w = w.toLowerCase();
		if (w.equals("he")
				|| w.equals("she")
				|| w.equals("it")
				|| w.equals("him")
				|| w.equals("her")
				|| w.equals("his")
				|| w.equals("hers")
				|| w.equals("its")
				|| w.equals("they")
				|| w.equals("them")
				|| w.equals("their")
				|| w.equals("theirs")
				|| w.equals("i")
				|| w.equals("me")
				|| w.equals("mine")
				|| w.equals("we")
				|| w.equals("us")
				|| w.equals("our")
				|| w.equals("ours")
				|| w.equals("you")
				|| w.equals("your")
				|| w.equals("yours")
				// Indefinite
				|| w.equals("one") || w.equals("one's") || w.equals("ones")
				|| w.equals("anyone") || w.equals("anybody")
				|| w.equals("anything") || w.equals("someone")
				|| w.equals("somebody") || w.equals("something")
				|| w.equals("everyone") || w.equals("everybody")
				|| w.equals("everything") || w.equals("nothing")
				|| w.equals("nobody") || w.equals("any")
				|| w.equals("each")
				|| w.equals("either")
				|| w.equals("neither")
				|| w.equals("all")
				|| w.equals("most")
				|| w.equals("some")
				|| w.equals("several")
				|| w.equals("none")
				|| w.equals("both")
				|| w.equals("few")
				|| w.equals("many")
				// Reflexive
				|| w.equals("himself") || w.equals("herself")
				|| w.equals("itself")
				|| w.equals("themselves")
				|| w.equals("themself")
				|| w.equals("myself")
				|| w.equals("ourselves")
				|| w.equals("ourself")
				|| w.equals("oneself")
				// Demonstrative
				|| w.equals("this")
				|| w.equals("that")
				|| w.equals("these")
				|| w.equals("those")
				// Relative
				|| w.equals("who") || w.equals("whom") || w.equals("which")
				|| w.equals("whose") || w.equals("whoever")
				|| w.equals("whomever") || w.equals("whatever")
				|| w.equals("whichever")
				// Interrogative (but non-relative)
				|| w.equals("what")) {
			return true;
		} else {
			return false;
		}
	}

	// Get the POS tags of the word on the left of the pronoun
	public static String leftPOS(Mention m) {
		if (m.getExtentFirstWordNum() == 0)
			return "None";
		else
			return m.getDoc().getPOS().get(m.getExtentFirstWordNum() - 1);
	}

	public static String rightPOS(Mention m) {
		if (m.getExtentLastWordNum() == m.getDoc().getWords().size() - 1)
			return "None";
		else
			return m.getDoc().getPOS().get(m.getExtentLastWordNum() + 1);
	}
	
	public static String aroundPOS(Mention m) {
		if (m.getExtentLastWordNum() == m.getDoc().getWords().size() - 1 || m.getExtentFirstWordNum() == 0)
			return "None";
		else
			return m.getDoc().getPOS().get(m.getExtentFirstWordNum() - 1) + "_" + 	m.getDoc().getPOS().get(m.getExtentLastWordNum()+1);
	}
	// Is the pronoun lower-cased
	public static boolean isLowerCase(Mention m){
		return Character.isLowerCase(m.getExtent().getText().charAt(0));
	}
	
	// definite NP
	static String[] definiteList = {"the","my","his","her","your","their"};
	static String[] demonstrativeList= {"this", "these","that","those"};
	static String[] indefiniteList = {"a","some","many","an", "one"};
	private static boolean inList(String word, String[] list ){
		for(String w: list){
			if(w.equals(word))
				return true;
		}
		return false;
	}
	public static boolean isDemonstrativeNP(Mention m){
		if(inList(m.getDoc().getWord(m.getExtentFirstWordNum()), demonstrativeList))
			return true;
		if(m.getExtentFirstWordNum()>0 && inList(m.getDoc().getWord(m.getExtentFirstWordNum()-1), demonstrativeList))
			return true;
		return false;
	}
	public static boolean isDefiniteNP(Mention m){
		if(inList(m.getDoc().getWord(m.getExtentFirstWordNum()), definiteList))
			return true;
		if(m.getExtentFirstWordNum()>0 && inList(m.getDoc().getWord(m.getExtentFirstWordNum()-1), definiteList))
			return true;
		return false;
	}
	public static boolean isIndefiniteNP(Mention m){
		if(inList(m.getDoc().getWord(m.getExtentFirstWordNum()), indefiniteList))
			return true;
		if(m.getExtentFirstWordNum()>0 && inList(m.getDoc().getWord(m.getExtentFirstWordNum()-1), indefiniteList))
			return true;
		return false;
	}
	
	// The pronoun resolution features for the antecedentm
	public static String numHeadWord(Mention m) {
		int num = m.getHeadLastWordNum() - m.getHeadFirstWordNum()+1;
		if (num > 3)
			return ">3";
		else
			return String.valueOf(num);
	}

	public static String numExtentWord(Mention m) {
		int num = m.getExtentLastWordNum() - m.getExtentFirstWordNum()+1;
		if (num > 3)
			return ">3";
		else
			return String.valueOf(num);
	}
	// return the number of appaeranace of the mention in the plain text
	public static String numExtentWordAppear(Mention m){
		int size = m.getDoc().getPlainText().replace(m.getExtent().getText(),"").length();
		int times =  (m.getDoc().getPlainText().length() - size) / m.getExtent().getText().length();
		if(times > 3)
			return ">3";
		else
			return String.valueOf(times);
	}
	
	public static String numHeadWordAppear(Mention m){
		int size = m.getDoc().getPlainText().replace(m.getHead().getText(),"").length();
		int times =  (m.getDoc().getPlainText().length() - size) / m.getHead().getText().length();
		if(times > 3)
			return ">3";
		else
			return String.valueOf(times);
	}
	
	
	// Check if the antecedent is embedded in another noun phase
	public static boolean anteContainMention(Mention m){
		if(m.getDoc().getMentionsContainedIn(m).size()>1)
			return true;
		else
			return false;
	}
	
	public static boolean anteContainedInMention(Mention m){
		if(m.getDoc().getMentionsContaining(m).size()>1)
			return true;
		else
			return false;
	}
	public static boolean theFirstCompatiableMention(CExample ex){
		Mention ante = ex.getM1();
		Mention pronoun = ex.getM2();
		boolean proisquote = ContextFeatures.isQuoted(pronoun);
		boolean anteisquote = ContextFeatures.isQuoted(ante);
		if(!proisquote && anteisquote)
			return false;
		Doc d  = ex.getDoc();
		if(GenderFeatures.doGendersMatch(ex, true).equals("f"))
			return false;
		if(NumberFeatures.doNumbersMatchStrong(ex, true).equals("f"))
			return false;
		for(int id = d.getMentionPosition(pronoun)-1; id > d.getMentionPosition(ante); id--){
			Mention m =  d.getMention(id);
			if(!proisquote && ContextFeatures.isQuoted(m))
				continue;
			if(!GenderFeatures.doGendersMatch(d.getCExampleFor(m, pronoun), true).equals("f") &&
					!NumberFeatures.doNumbersMatchStrong(d.getCExampleFor(m, pronoun), true).equals("f"))
				return false;
		}
		return true;
		
	}
	
	public static boolean theFirstAgreeMention(CExample ex){
		Mention ante = ex.getM1();
		Mention pronoun = ex.getM2();
		Doc d  = ex.getDoc();
		boolean proisquote = ContextFeatures.isQuoted(pronoun);
		boolean anteisquote = ContextFeatures.isQuoted(ante);
		if(!proisquote && anteisquote)
			return false;
		if(!GenderFeatures.doGendersMatch(ex, true).equals("t"))
			return false;
		if(!NumberFeatures.doNumbersMatchStrong(ex, true).equals("t"))
			return false;
		for(int id = d.getMentionPosition(pronoun)-1; id > d.getMentionPosition(ante); id--){
			Mention m =  d.getMention(id);
			if(!proisquote && ContextFeatures.isQuoted(m))
				continue;
			if(GenderFeatures.doGendersMatch(d.getCExampleFor(m, pronoun), true).equals("t") &&
					NumberFeatures.doNumbersMatchStrong(d.getCExampleFor(m, pronoun), true).equals("t"))
				return false;
		}
		return true;
		
	}
	public static String getPronounFeature(String p) {
		if (!annotationInitialized)
			initAnnotation();
		if (!PronounAnnotation.containsKey(p.toLowerCase()))
			return "Unknown";
		else
			return PronounAnnotation.get(p.toLowerCase());
	}

	protected static Map<String, String> loadLinesAsMap(String filename,
			boolean lower) {
		Map<String, String> result = new HashMap<String, String>();
		List<String> lines = (new myIO()).readLines(filename);
		for (String line : lines) {
			if (line.length() <= 0 || line.startsWith("#"))
				continue;
			if (lower)
				line = line.toLowerCase();
			result.put(line.split(" ")[0], line.split(" ")[1]);
		}
		return result;
	}

	public static String pronounCompatibility(CExample ex) {
		Mention pro = ex.getM2();
		Mention ante = ex.getM1();
		String proEntityType = ante.getEntityType();// EntityTypeFeatures.getEType(ante);
		// if(ante.getEntityType().equals("PER") &&
		// NumberFeatures.doNumbersMatchStrong(ex, true).equals("f"))
		if (ante.getEntityType().equals("PER")
				&& NumberFeatures.doNumbersMatchStrong(ex, true).equals("f"))
			return "f"; // 20, 3
		if (!(proEntityType.equals(Constants.Unknown) || proEntityType
				.equals("PER"))
				&& PronounResolutionFeatures.isFirstorSecondPerson(pro))
			return "f"; // 0,0
		if (proEntityType.equals("PER")
				&& PronounResolutionFeatures.proIsHuman(ex) == 'N')
			return "f"; // 0, 0
		if (proEntityType.equals("PER")
				&& GenderFeatures.doGendersMatchForCompatible(ex, true).equals(
						"f"))
			return "f"; // 159, 64;
		return "t";
	}
	
	public static boolean isHe(String str) {
		if (str.equals("he") || str.equals("him") || str.equals("himself"))
			return true;
		return false;
	}
	
	public static boolean isShe(String str) {
		if (str.equals("she") || str.equals("her") || str.equals("herself"))
			return true;
		return false;
	}
	
	public static boolean isIt(String str) {
		if (str.equals("it") || str.equals("itself"))
			return true;
		return false;
	}
	
	public static boolean isThey(String str) {
		if (str.equals("they") || str.equals("them") || str.equals("themself") || str.equals("themselves"))
			return true;
		return false;
	}
	
	public static boolean isI(String str) {
		if (str.equals("i") || str.equals("me") || str.equals("myself"))
			return true;
		return false;
	}
	
	public static boolean isWe(String str) {
		if (str.equals("we") || str.equals("us") || str.equals("ourself") || str.equals("ourselves"))
			return true;
		return false;
	}
	
	public static boolean isYou(String str) {
		if (str.equals("you") || str.equals("yourself") || str.equals("yourselves"))
			return true;
		return false;
	}
	
	public static boolean isWho(String str) {
		if (str.equals("who") || str.equals("whom") || str.equals("whomself") || str.equals("whomselves") )
			return true;
		return false;
	}
}
