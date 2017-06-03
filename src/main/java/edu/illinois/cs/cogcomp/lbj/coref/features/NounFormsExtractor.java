package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NounFormsExtractor {
	static String[] uncountable = { "advice", "fun", "honesty", "information", "ambition", 
							"steel", "wood", "plastic", "stone", "concrete", "wool",  
							"water", "wine", "beer", "soda", "oil", "gasoline" }; 

	static Map<String, Set<String>> nouns_forms = new HashMap<String, Set<String>> (); 
	static String file = "/shared/shelley/khashab2/CorporaAndDumps/NounList/irregular_merged.txt"; 
	public NounFormsExtractor() {
		try {
			readTheFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Set<String> getNounForms(String noun) { 
		
		// check the irregulars 
		if( nouns_forms.containsKey(noun) ) { 
			return nouns_forms.get(noun); 
		}
		int length = noun.length(); 
		Set<String> forms = new HashSet<String>();
		if( length > 1 && noun.substring(length-1, length).equals("s") ) { 
			forms.add(noun);
			forms.add( noun.substring(0, length-1) );  
			if( noun.substring(length-2, length).equals("es") ) { 
				forms.add( noun.substring(0, length-2) );
			}
		}
		else { 
			forms.add(noun);
			forms.addAll(returThePlural(noun));
		}
		return forms; 
	}
	
	public static void readTheFile() throws IOException { 
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ( (line = br.readLine()) != null) {
			String[] split = line.split("\t");
			Set<String> forms = new HashSet<String> (); 
			for( String str : split) 
				forms.add(str); 
			for( String str : split) 
				nouns_forms.put(str, forms); 	 
		}
	}
	
	// given a singular noun, it returns its plural form, assuming that it has a regular form
	// based on this: http://esl.about.com/od/common_mistakes/a/Plural-Noun-Forms.htm 
	public static List<String> returThePlural(String noun) { 
		List<String> output = new ArrayList<String>(); 
		int length = noun.length(); 
		String lastChar; 
		if( length >= 1 ) 
			lastChar = noun.substring(length-1, length); 
		else 
			lastChar = noun; 
		String lastTwoChars = ""; 
		if( length >= 2 ) 
			lastTwoChars = noun.substring(length-2, length); 
		else 
			lastTwoChars = lastChar; 
		
		// check if it belongs to the uncountable list 
		for( int i = 0; i < uncountable.length; i++ ) 
			if( noun.equals( uncountable[i] ) ) { 
				output.add(noun); 
				return output; 
			}
		
		if( lastChar.equals("y") ) { 
			output.add( noun.substring(0, length-1) + "ies" );
		}
		else if( lastTwoChars.equals("sh") ||  lastTwoChars.equals("ch") || lastChar.equals("s") 
				|| lastChar.equals("x") || lastChar.equals("z") ) { 
			output.add( noun + "es" ); 
		}
		else if( lastChar.equals("o")  ) { 
			// one of these 
			output.add( noun + "es" );
			output.add( noun + "s" );
		}
		else 
			output.add( noun + "s" );
		return output; 
	}
}
