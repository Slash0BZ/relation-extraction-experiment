package edu.illinois.cs.cogcomp.lbj.coref.scorers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.cooccurancedata.datastructures.WinogradCorefInstance5;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.FScore;
import edu.illinois.cs.cogcomp.lbj.coref.ir.scores.Score;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

public class VincentScorer extends ChainScorer<Mention> {
	public List<WinogradCorefInstance5> allInstances_withXMLEscaped = new ArrayList<WinogradCorefInstance5>(); 
	String cache_file_withSRL_withAnts_withDependencyAnnotation_withCats_withXMLEscaped = "/shared/shelley/khashab2/CorporaAndDumps/Altaf_Ng_2012_Pronoun_Resolution/cached_data_with_SRL_withAntecedantAnnotation_withDependencyAnnotation_withCategories_with_XML_Escaped.bin";
	
	public void deserializeData5() { 
		try
		{
			System.out.println("Starting deserialization! ");
			//FileInputStream fileIn = new FileInputStream(cache_file_withSRL);
			FileInputStream fileIn = new FileInputStream(cache_file_withSRL_withAnts_withDependencyAnnotation_withCats_withXMLEscaped);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			allInstances_withXMLEscaped = (List<WinogradCorefInstance5>) in.readObject();
			System.out.println("Done reading the data from " + cache_file_withSRL_withAnts_withDependencyAnnotation_withCats_withXMLEscaped);
			in.close();
			fileIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Score getScore(List<ChainSolution<Mention>> keys,
			List<ChainSolution<Mention>> preds) {
		deserializeData5();
		
		double p=1.0,r=1.0;
		double count=0;
		double correct=0;
		for (int i=0;i<keys.size();i++) {
			List<Set<Mention>> gold_chains = keys.get(i).getChains();
			List<Set<Mention>> pred_chains = preds.get(i).getChains();
			//output(gold_chains);
			//output(pred_chains);
			int index = Integer.parseInt(gold_chains.get(0).iterator().next().getDoc().getDocID());
			WinogradCorefInstance5 ins = allInstances_withXMLEscaped.get(index);
			for (int j=0;j<gold_chains.size();j++) {
				Iterator<Mention> iter = gold_chains.get(j).iterator();
				while (iter.hasNext()) {
					Mention m=iter.next();
					if (!checkPro(m, ins.pronoun_word_start)) {
						continue;
					}
					int offset = 0;
					if (ins.correct_antecedent.equals(ins.antecedent1)) {
						offset = ins.antecedent1_token_start;
					}
					else {
						offset = ins.antecedent2_token_start;
					}
					Mention ante = getAntecedent(keys.get(i).getChainOf(m), offset);
					if (preds.get(i).getChainOf(m)!=null && preds.get(i).getChainOf(m).contains(ante)) {
						correct++;
						//System.out.println("Correct");
					}	
					else {
						//System.out.println("Wrong");
					}
				}
			}
		}
		count=keys.size();
		p=correct/count;
		return new FScore(p, r);
	}

	private Mention getAntecedent(Set<Mention> mens, int offset) {
		for (Mention m: mens) {
			if (m.getExtentFirstWordNum() == offset+1) {
				return m;
			}
		}
		return null;
	}

	private void output(List<Set<Mention>> chains) {
		for (int i=0;i<chains.size();i++) {
			System.out.print("[");
			Iterator<Mention> iter = chains.get(i).iterator();
			while (iter.hasNext()) {
				Mention m=iter.next();
				System.out.print(m.getCleanText()+" ");
			}
			System.out.print("]");
		}
		System.out.println();
	}

	private boolean checkPro(Mention m, int p) {
		if (m.getExtentFirstWordNum() == p+1) {
			return true;
		}
		return false;
	}
}
