package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.List;

import edu.illinois.cs.cogcomp.profiler.profilerclient.models.Profile;

public class ProfilerResult {

	String mention; 
	String url; 
	List<Profile> profiles; 
	
	ProfilerResult( String mention ) { 
		this.mention = mention; 
	}
	
	ProfilerResult( String mention, String url) { 
		this.mention = mention; 
		this.url = url; 
	}
}
