package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadVerbFrames {
	static Map<String, List<String>> verbs = new HashMap<String, List<String>>(); 
	static String file1 = "/shared/shelley/khashab2/CorporaAndDumps/Verb_lists/verbs.txt"; 
	static String file2 = "/shared/shelley/khashab2/CorporaAndDumps/Verb_lists/verbs-irreg.txt"; 
	
	public static void readAllVerbFrames() { 
		// test the verbs 
		ReadTheVrbs(file1);
		ReadTheVrbs(file2);
	}
	
	public static void ReadTheVrbs(String fileAddress) { 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileAddress));
			String line;
			while ( (line = br.readLine()) != null) {
				List<String> verbs_list = new ArrayList<String> (); 
				String[] verbs_split = line.split("\t"); 
				for( int i = 0; i < verbs_split.length; i++ ) { 
					verbs_list.add( verbs_split[i] ); 
					//System.out.println(verbs_split[i] + "/"); 
				}
				//System.out.println(line); 
				for( int i = 0; i < verbs_split.length; i++ ) { 
					verbs.put(verbs_split[i], verbs_list); 
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<String> getVerbFrames(String verb) { 
		if( verbs.containsKey( verb ) )
			return verbs.get( verb ); 
		else { 
			List<String> verb_tmp = new ArrayList<String> (); 
			verb_tmp.add( verb ); 
			return verb_tmp; 
		}
	}
	
	public static void printVerbFrames(String verb) { 
		List<String> frames1 = getVerbFrames(verb);
		for( String str : frames1 )
			System.out.println("is -> " + str);
		
	}
}
