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

public class WinoScorer extends ChainScorer<Mention> {
	int no_antecedent = 0;
	int same_sentence = 0;
	public Score getScore(List<ChainSolution<Mention>> keys,
			List<ChainSolution<Mention>> preds) {
		
		double p=1.0,r=1.0;
		double count=0;
		double correct=0;
		for (int i=0;i<keys.size();i++) {
			List<Set<Mention>> gold_chains = keys.get(i).getChains();
			List<Set<Mention>> pred_chains = preds.get(i).getChains();
//			String text = gold_chains.get(0).iterator().next().getDoc().getPlainText();
//			text = text.substring(5);
//			System.out.println(text.trim());
			//output(gold_chains);
			//output(pred_chains);
			for (int j=0;j<gold_chains.size();j++) {
				Iterator<Mention> iter = gold_chains.get(j).iterator();
				while (iter.hasNext()) {
					Mention m=iter.next();
					if (!checkPro(m)) {
						continue;
					}
					ArrayList<Mention> antes = getAntecedents(gold_chains, m);
					for (int k=0;k<antes.size();k++) {
						Mention ante = antes.get(k);
						boolean a = false;
						if (gold_chains.get(j).contains(ante)) {
							a = true;
						}
						boolean b = false;
						if (preds.get(i).getChainOf(m)!=null && preds.get(i).getChainOf(m).contains(ante)) {
							b = true;
						}
						if (a==b) {
							correct=correct+1;
							//System.out.println("Correct "+ante.getCleanText()+"\t"+m.getCleanText());
						}	
						else {
							//System.out.println("Wrong "+ante.getCleanText()+"\t"+m.getCleanText());
						}
						count=count+1;
					}
				}
			}
		}
		System.out.println("Total Count: "+count);
		p=correct/count;
		return new FScore(p, r);
	}

	private ArrayList<Mention> getAntecedents(List<Set<Mention>> gold_chains, Mention men) {
		ArrayList<Mention> list = new ArrayList<Mention>();
		for (int i=0;i<gold_chains.size();i++) {
			for (Mention m: gold_chains.get(i)) {
				if (m.getSentNum() <= men.getSentNum() && m.getSentNum()+2 >= men.getSentNum()
						&& m.getExtentLastWordNum() < men.getExtentFirstWordNum()) {
					list.add(m);
				}
			}
		}
		return list;
	}

	private Mention getNearAntecedent(Set<Mention> set, Mention men) {
		ArrayList<Mention> list = new ArrayList<Mention>();
		for (Mention m: set) {
			if (!m.getType().equals("PRO") && m.getSentNum() <= men.getSentNum() 
					&& m.getExtentLastWordNum() < men.getExtentFirstWordNum()) {
				list.add(m);
			}
		}
		if (list.size()==0) {
			return null;
		}
		Mention ante = list.get(0);
		int dist = men.getSentNum() - ante.getSentNum();
		for (int i=1;i<list.size();i++) {
			int tmp = men.getSentNum() - list.get(i).getSentNum();
			if (tmp < dist) {
				ante = list.get(i);
				dist = tmp;
			}
			if (tmp==dist && list.get(i).getExtentFirstWordNum()>=ante.getExtentFirstWordNum() 
					&& list.get(i).getExtentLastWordNum()<=ante.getExtentLastWordNum()) {
				ante = list.get(i);
			}
		}
		return ante;
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

	private boolean checkPro(Mention m) {
		if (m.getType().equals("PRO")) {
			return true;
		}
		return false;
	}
	
	private boolean checkDoubleAnte(Set<Mention> set, Mention men,  Mention pro) {
		for (Mention m: set) {
			if (m!=men && !m.getType().equals("PRO") && m.getSentNum() == men.getSentNum() 
					&& !(m.getExtentFirstWordNum()<=men.getExtentLastWordNum() && m.getExtentLastWordNum()>=men.getExtentFirstWordNum())
					&& m.getExtentLastWordNum()<pro.getExtentFirstWordNum() ) {
				//System.out.println(m.getDoc().getPlainText());
				//System.out.println(m.getCleanText()+"\t"+men.getCleanText()+"\t"+pro.getCleanText());
				return true;
			}
		}
		return false;
	}
	
	public void getStat(List<ChainSolution<Mention>> keys) {
		System.out.println("Documents: "+keys.size());
		int[] count = new int[15];
		int sum = 0;
		for (int i=0;i<keys.size();i++) {
			List<Set<Mention>> gold_chains = keys.get(i).getChains();
			for (int j=0;j<gold_chains.size();j++) {
				Iterator<Mention> iter = gold_chains.get(j).iterator();
				while (iter.hasNext()) {
					Mention m=iter.next();
					if (!checkPro(m)) {
						continue;
					}
					Mention ante = getNearAntecedent(gold_chains.get(j), m);
					if (ante==null) {
						System.out.println(m.getDoc().getPlainText());
						System.out.println(m.getCleanText()+"\t"+m.getCleanText());
						no_antecedent++;
						continue;
					}
					if (checkDoubleAnte(gold_chains.get(j), ante, m)) {
						same_sentence++;
					}
					int dist = m.getSentNum() - ante.getSentNum();
					if (dist < 15) {
						count[dist]++;
					}
					sum++;
				}
			}
		}
		System.out.print("Distribution: ");
		for (int i=0;i<15;i++)
			System.out.print(count[i]+" ");
		System.out.println(sum);
		System.out.println("Singleton: "+no_antecedent);
		System.out.println("Double: "+same_sentence);
	}
}