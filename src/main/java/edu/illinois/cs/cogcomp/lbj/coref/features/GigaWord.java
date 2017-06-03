package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;
import edu.illinois.cs.cogcomp.lbj.coref.util.io.IOManager;

public class GigaWord {
	String path = "/shared/experiments/hpeng7/data/TripleRel/";
	static HashMap<String, TriplePairScore> scores_3 = new HashMap<String, TriplePairScore>();
	static HashMap<String, TriplePairScore> scores_1 = new HashMap<String, TriplePairScore>();
	static double sum1 = 0;
	static double sum3 = 0;
	static double cover1 = 0;
	static double cover3 = 0;
	static BufferedWriter bw = null;
	
	public void setup() throws Exception {
		System.out.print("Loading GigaWord Data...");
		scores_3 = loadMap(3);
		scores_1 = loadMap(1);
		System.out.println("\t Loading Finished");
		bw = IOManager.openWriter(Parameters.gigaWordQuery);
	}
	
	public double getScore(String str, int option) {
		if (bw == null) {
			bw = IOManager.openWriter(Parameters.gigaWordQuery);
		}
		if (option==3) {
			if (scores_3 == null) return 0;
			sum3++;
			TriplePairScore score = scores_3.get(str);
			if (score==null) {
				try {
					bw.write("Miss3:"+str+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
			}
			else {
				try {
					bw.write("Cover3:"+str+"\t"+score.count+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				cover3++;
				if (score.count > Parameters.gigaWordThreshold) {
					return Math.log10(score.count);
				}
				else {
					return 0;
				}
			}
		}
		if (option==1) {
			if (scores_1 == null) return 0;
			sum1++;
			TriplePairScore score = scores_1.get(str);
			if (score==null) {
				try {
					bw.write("Miss1:"+str+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
			}
			else {
				try {
					bw.write("Cover1:"+str+"\t"+score.count+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				cover1++;
				if (score.count > Parameters.gigaWordThreshold) {
					return Math.log10(score.count);
				}
				else {
					return 0;
				}
			}
		}
		return 0;
	}

	private HashMap<String, TriplePairScore> loadMap(int option) {
		HashMap<String, TriplePairScore> scores = new HashMap<String, TriplePairScore>();
		ArrayList<String> lines = IOManager.readLines(path+"TriplePairs/mat_cat"+option+".txt");
		for (int i=0;i<lines.size();i++) {
			String line = lines.get(i);
			String[] strs = line.split("\t");
			TriplePairScore score = TriplePairScore.decode(strs[1]);
			scores.put(strs[0], score);
		}
		return scores;
	}

	public void stat() throws IOException {
		System.out.println(cover1+"/"+sum1+"="+(cover1/sum1));
		System.out.println(cover3+"/"+sum3+"="+(cover3/sum3));
		bw.close();
	}
}
