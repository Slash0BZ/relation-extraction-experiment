package edu.illinois.cs.cogcomp.lbj.coref.features;

public class polrarityInstance {
	public String type; 
	public int len; 
	public String word1; 
	public String pos1; 
	public boolean stemmed1;  // y: true   n: false 
	public int priorpolarity; // 1: positive 0: neutral  -1: negative 2: both  -2: weakneg
	
	@Override 
	public boolean equals(Object obj) { 
		// TODO 
		return true; 
	}
	
	@Override 
	public String toString() { 
		return "type=" + type + " len=" + len + " word1=" + word1 + " pos1=" + pos1 + " stemmed1=" + stemmed1 + " priorpolarity=" + priorpolarity; 
	}
}

