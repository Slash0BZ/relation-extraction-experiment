package edu.illinois.cs.cogcomp.lbj.coref.features;

public class TriplePairScore {
	public int count = 0;
	public int[] subcount = new int[22];
	
	public TriplePairScore() {
		count = 1;
		for (int i=0;i<subcount.length;i++) {
			subcount[i] = 0;
		}
	}
	
	public String encode() {
		String str = ""+count;
		for (int i=0;i<subcount.length;i++) {
			str = str + "|" + subcount[i];
		}
		return str;
	}

	public static TriplePairScore decode(String str) {
		TriplePairScore score = new TriplePairScore();
		String[] strs = str.split("\\|");
		score.count = Integer.parseInt(strs[0]);
		for (int i=0;i<score.subcount.length;i++) {
			score.subcount[i] = Integer.parseInt(strs[i+1]);
		}
		return score;
	}

	public void merge(TriplePairScore score) {
		count += score.count;
		for (int i=0;i<subcount.length;i++) {
			subcount[i] += score.subcount[i];
		}
	}
}
