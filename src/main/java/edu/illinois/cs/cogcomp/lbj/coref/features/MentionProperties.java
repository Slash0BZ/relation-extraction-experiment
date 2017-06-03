package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.util.aux.myAux;

public class MentionProperties {
	private static String[] maleHonor = new String[]{"mr.", "mister", "king", "president", "prince", "brother"};
	private static String[] femaleHonor = new String[]{"mrs.", "miss", "ms.", "queen","princess", "sister"};
	private static Map<String, Map<String,String>> cached = null; 
    public static Map<String, String> resolvedMentionProp(Mention m){
    	if(cached == null)
    		cached = new HashMap<String, Map<String,String>>();
    	if(cached.containsKey(m.getSurfaceText()))
    		return cached.get(m.getSurfaceText());
    	Map<String, String> prop = new HashMap<String, String>();
    	String gender = null;
    	if(!m.getType().equals("NAM") ||!m.getEntityType().equals("PER"))
    		return prop;
    	String head = m.getHead().getText().toLowerCase();
    	String[] words = head.split("\\s");
    	String lastWord = words[words.length-1];
    	int nameStartPosition = 0;
    	//Get first name:
        String firstName = null, honor = null, lastName = null, profTitle = null;
        // resolve honor
        if(m.getHeadFirstWordNum() - m.getExtentFirstWordNum() >0){
        	String wordBefore = m.getDoc().getWord(m.getHeadFirstWordNum()-1).toLowerCase();
        	if ( Gazetteers.getHonors().contains(wordBefore) ) {
        		honor = GenderFeatures.removePunctuationFromEnds(wordBefore);
        		if(myAux.isStringEquals(honor, maleHonor, false))
        			gender = "male";
        		if(myAux.isStringEquals(honor, femaleHonor, false))
        			gender = "female";
        	}
        }
        String word0 = words[0];
        if ( Gazetteers.getHonors().contains(word0)) {
    	    honor = GenderFeatures.removePunctuationFromEnds(word0);
    	    nameStartPosition++;
        }
        else if ( Gazetteers.getProfTitle().contains(word0)) {
    	    profTitle = GenderFeatures.removePunctuationFromEnds(word0);
    	    nameStartPosition++;
        }
        
        if(honor !=null || profTitle != null){
        	if(words.length == 2){
        		lastName =  words[nameStartPosition];
        		nameStartPosition ++;
        	}
        	else if(words.length == 3){
        		firstName = lastName =  words[nameStartPosition];
        		lastName =  words[nameStartPosition+1];
        		nameStartPosition +=2;
        	}
        }
        
        boolean chinese = false;
        if(nameStartPosition<words.length){
        	
        	String firstNameWord  = words[nameStartPosition];
    		if(Gazetteers.femaleFirstNames.contains(firstNameWord)|| Gazetteers.maleFirstNames.contains(firstNameWord)){
    			firstName = firstNameWord;
    			nameStartPosition++;
    		}
    		else if (Gazetteers.lastNames.contains(firstNameWord)){
    			lastName = firstNameWord;
    			nameStartPosition++;
    		}
    		else if (Gazetteers.chineseLastName.contains(firstNameWord)){
    			lastName = firstNameWord;
    			nameStartPosition++;
    			chinese = true;
    		}
        }
        if(chinese){
        	if(nameStartPosition==words.length-1 && firstName == null){
        		firstName = words[nameStartPosition];
        	}
        }
        else {
        	if(words.length == 1 && firstName == null && lastName == null)
        		firstName = words[0];
        	else if(nameStartPosition==words.length-1 && lastName == null){
        		lastName = words[nameStartPosition];
        	}
        }
        
        
        
        if(firstName != null){
        	boolean ismale = false;
        	boolean isfemale = false;
        	if(Gazetteers.femaleFirstNames.contains(firstName))
				isfemale= true;
			if(Gazetteers.maleFirstNames.contains(firstName)){
				ismale = true;
			if(isfemale && !ismale)
				gender = "female";
			if(ismale && !isfemale)
				gender = "male";
			}
        }
    	if(gender !=null)
    		prop.put("gender",gender);
    	if(honor!=null)
    		prop.put("honor", honor);
    	if(firstName!=null)
    		prop.put("firstName", firstName);
    	if(lastName!=null)
    		prop.put("lastName", lastName);
    	if(profTitle !=null)
    		prop.put("profName", profTitle);
    	cached.put(m.getSurfaceText(), prop);
    	return prop;
    }
}
