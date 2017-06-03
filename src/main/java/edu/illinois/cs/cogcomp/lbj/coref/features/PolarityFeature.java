package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Triple;
import edu.illinois.cs.cogcomp.lbj.coref.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;

public class PolarityFeature {
	public static Map<String, polrarityInstance> polarityMap = new HashMap<String, polrarityInstance>(); 

	public static void setup() { 
		String file = "/shared/shelley/khashab2/CorporaAndDumps/SubjectivityLexicon/subjectivity_clues_hltemnlp05/subjectivity_clues_hltemnlp05/subjclueslen1-HLTEMNLP05.tff"; 
		try {
			BufferedReader br= new BufferedReader(new FileReader(file));
			String line;
			while ( (line = br.readLine()) != null) {
				String[] split = line.split(" ");   
				//				System.out.println("line = " + line);
				//				System.out.println("split.length = " + split.length);

				polrarityInstance ins = new polrarityInstance(); 
				int ind1 = split[0].indexOf("=");
				ins.type = split[0].substring(ind1+1); 

				ind1 = split[1].indexOf("="); 
				ins.len = Integer.parseInt( split[1].substring(ind1+1) );

				ind1 = split[2].indexOf("=");
				ins.word1 = split[2].substring(ind1+1);

				ind1 = split[3].indexOf("=");
				ins.pos1 = split[3].substring(ind1+1);

				if( split[4].equals("y") )
					ins.stemmed1 = true;
				else 
					ins.stemmed1 = false;

				if( split[5].equals("priorpolarity=positive") )
					ins.priorpolarity = 1;
				else if( split[5].equals("priorpolarity=neutral") )
					ins.priorpolarity = 0;
				else if( split[5].equals("priorpolarity=negative") )
					ins.priorpolarity = -1;
				else if( split[5].equals("priorpolarity=both") )
					ins.priorpolarity = 2;
				else if( split[5].equals("priorpolarity=weakneg") )
					ins.priorpolarity = -2;
				else {
					System.out.println("Error in reading the file: " + split[5]); 
					break; 
				}	
				//				System.out.println( ins.toString() ); 
				polarityMap.put(ins.word1, ins); 
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*  Usage : 
	 * 
	 *  if( s1 > s2 * th ) {
	 *  	if( label == l1 ) { 
	 *      	// corect 
	 *      }
	 *      else { 
	 *      	// incorrect 
	 *      } 
	 *   } 
	 *   else if( s1 * th < s2 ) {
	 *  	if( label == l1 ) { 
	 *      	// incorect 
	 *      }
	 *      else { 
	 *      	// correct 
	 *      } 
	 *   } 
	 *   else { 
	 *   	// no decision 
	 *   }
	 *   
  */ 
	
	public static double[] getThresholdValues() { 
		return new double[] { 1.0, 1.0, 1.0 }; 
	}

	public static double[] getPolarityFeatureGivenTripleSubset(Triple t1, Triple tp, String connective, String pr_comparativeAdverb ) {
		double[] all = getPolarityFeatureGivenTriple( t1, tp, connective, pr_comparativeAdverb ); 
		
		if (all.length >= 5) {
			double[] subset = new double[3];
			subset[0] = all[1];
			subset[1] = all[3];
			subset[2] = all[4]; 
			return subset; 
		}
		else {
			return null;
		}
	}
	
	public static double[] getPolarityFeatureGivenTriple( Triple t1, Triple tp, String connective, String pr_comparativeAdverb ) { 

		final boolean printDebug = false;
		
		String pr_verb = t1.predicate; 
		String a1_verb = tp.predicate; 
		//String a2_verb = "";
		
		if( printDebug ) { 
			//System.out.println(instance_num); 
			//System.out.println(ins.sentence);
			System.out.println("connective = " + connective);
			System.out.println("Verbs before lemma: pr_verb=" + pr_verb + " a1_verb=" + a1_verb ); 
		} 
		pr_verb = MyCuratorClient.lemmatizer.getLemma(pr_verb, "VB");
		a1_verb = MyCuratorClient.lemmatizer.getLemma(a1_verb, "VB");
		if( printDebug ) { 
			System.out.println("Verbs after lemma: pr_verb=" + pr_verb + " a1_verb=" + a1_verb );
		} 
		// prior polarities 
		int pr_po = 0; 
		int a1_po = 0; 
		if( PolarityFeature.polarityMap.containsKey(pr_verb) ) {
			int p = PolarityFeature.polarityMap.get(pr_verb).priorpolarity; 
			if( p == 1 || p == -1 ) { 
				pr_po = p; 
			}
		}
		if( PolarityFeature.polarityMap.containsKey(a1_verb) ) {
			int p = PolarityFeature.polarityMap.get(a1_verb).priorpolarity; 
			if( p == 1 || p == -1 ) { 
				a1_po = p; 
			}
		}
		if( printDebug ) { 
			System.out.println("Initial Polarity: pr_po=" + pr_po + "  a1_po="+ a1_po);
		} 
		
		if( pr_po == 0 ) { 
			String[] toks = pr_verb.split(" "); 
			for( int k = 0; k < toks.length; k++ ) { 
				if( PolarityFeature.polarityMap.containsKey(toks[k]) ) {
					int p = PolarityFeature.polarityMap.get(toks[k]).priorpolarity; 
					if( p == 1 || p == -1 ) { 
						pr_po = p; 
						break; 
					}
				}
			}
			for( int k = 0; k < toks.length; k++ ) {
				String lem = ""; 
				try {
					lem =  MyCuratorClient.lemmatizer.getLemma(toks[k] , "V");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if( PolarityFeature.polarityMap.containsKey(lem) ) {
					int p = PolarityFeature.polarityMap.get(lem).priorpolarity; 
					if( p == 1 || p == -1 ) { 
						pr_po = p; 
						break; 
					}
				}
			}		
		}

		if( a1_po == 0 ) { 
			String[] toks = a1_verb.split(" "); 
			for( int k = 0; k < toks.length; k++ ) { 
				if( PolarityFeature.polarityMap.containsKey(toks[k]) ) {
					int p = PolarityFeature.polarityMap.get(toks[k]).priorpolarity; 
					if( p == 1 || p == -1 ) { 
						a1_po = p; 
						break; 
					}
				}
			}
			for( int k = 0; k < toks.length; k++ ) {
				String lem = ""; 
				try {
					lem = MyCuratorClient.lemmatizer.getLemma(toks[k] , "V");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if( PolarityFeature.polarityMap.containsKey(lem) ) {
					int p = PolarityFeature.polarityMap.get(lem).priorpolarity; 
					if( p == 1 || p == -1 ) { 
						a1_po = p; 
						break; 
					}
				}
			}		
		}
		
		if( t1.role.equals("o") )
			a1_po *= -1; 
		
		if( tp.role.equals("o") )
			pr_po *= -1; 
		
		if( pr_verb.contains("not") || pr_verb.contains("n't") )
			pr_po *= -1;
		
		if( a1_verb.contains("not") || a1_verb.contains("n't") )
			a1_po *= -1;
		
		if( ConnectivePolarity(connective) == -1 ) { 
			pr_po *= -1; 
			if( printDebug )
				System.out.println("Polarity change with connectives! "); 
		}
		
		pr_po += ComparativeAdverbPolarity(pr_comparativeAdverb);
		//a1_po += ComparativeAdverbPolarity(a1_comparativeAdverb);
		
		if( printDebug ) { 
			System.out.println("Final Polarity: pr_po=" + pr_po + "  a1_po="+ a1_po);
			System.out.println("-----------------------------"); 
		} 

		double[] feature = new double[0]; 
		double[] f1 = new double[6];
//		double[] f2 = new double[3];
//		double[] f3 = new double[3];
		
		if( pr_po != 0 && a1_po != 0  ) { 
			// HP1 
			if( pr_po > 0 && a1_po > 0 || pr_po < 0 && a1_po < 0 )
				f1[0] = 1;
			else 
				f1[0] = 0;
			if( pr_po > 0 && a1_po > 0 )
				f1[1] = 1; 
			else 
				f1[1] = 0;
			if( pr_po < 0 && a1_po < 0 )
				f1[2] = 1;
			else 
				f1[2] = 0;
			
			if( pr_po > 0 && a1_po > 0 || pr_po < 0 && a1_po < 0 )
				f1[3] = a1_po;
			else 
				f1[3] = 0;
			if( pr_po > 0 && a1_po > 0 )
				f1[4] = a1_po; 
			else 
				f1[4] = 0;
			if( pr_po < 0 && a1_po < 0 )
				f1[5] = a1_po;
			else 
				f1[5] = 0;
		}
		
		feature = ArrayUtils.addAll(f1, feature);
		return feature; 
	}
		
	// comparative adverbs:  [lower, quicker, scarier, higher, further, faster, earlier, smarter, more, better, straighter, longer, challenger, less]
	public static int ComparativeAdverbPolarity(String str) { 
		if( str.equals("less") || str.equals("lower") || str.equals("less") )
			return -1; 
		else if( str.equals("quicker") || str.equals("higher") || str.equals("further") || str.equals("smarter") || str.equals("more") || str.equals("better") || str.equals("straighter") || str.equals("longer")  )
			return 1; 
		else 
			return 0; 
	}

	public static int ConnectivePolarity(String str) { 
		if( str.equals("but") || str.equals("though") || str.equals("although") || str.equals("unless") )
			return -1;
		else
			return 0; 
	}
}
