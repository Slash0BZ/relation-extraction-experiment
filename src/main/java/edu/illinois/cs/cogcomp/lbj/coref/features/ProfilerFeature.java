package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.File;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Triple;
import edu.illinois.cs.cogcomp.profiler.profilerclient.ProfilerClient;
import edu.illinois.cs.cogcomp.profiler.profilerclient.models.Entity;
import edu.illinois.cs.cogcomp.profiler.profilerclient.models.Profile;
import edu.illinois.cs.cogcomp.profiler.profilerclient.models.Profile.ContextType;

/* Context available (as in Profile.ContextType):
- NER: Ner Tags
- NPB: Noun Phrases Before it
- NPIB: Noun Phrases Immediately Before it
- NPC: Noun Phrases that Contains the it
- NPIA: Noun Phrases Immediately After it
- NPA: Noun Phrases After it
- NNB: Nearest Noun Before it
- NNA: Nearest Noun After it
- NVPB: Nearest Verb Phrases Before it
- NVPA: Nearest Verb Phrases After it
- NVB: Nearest Verb Before it
- NVA: Nearest Verb After it
- MOD: Modifiers
- EB: Entities Before it
- EA: Entities After it
- AKA: Also Known As
- PAR: Parenthesis
- APP: Appositions
- HON: Honorifics*/

class contextInfo {
	public String profile; 
	public String stringFound; 
	public double probability; 
	public int count; 
	public ContextType ct; 
	
	public contextInfo() {
		profile = "";
		stringFound = ""; 
		probability = 0; 
		count = 0; 
	}
	
	public contextInfo(String str1, String str2) {
		profile = str1;
		stringFound = str2; 
		probability = 0; 
		count = 0; 
	}
	@Override
	public String toString() { 
		return "|| " + stringFound + " , " + Double.toString(probability) + ", " + Integer.toString(count) + ", " + ct; 
	}

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof contextInfo))return false;
		contextInfo otherCls = (contextInfo)other;
		if( otherCls.count == this.count && Math.abs(otherCls.probability - this.probability ) < 0.01 && otherCls.stringFound.equals(this.stringFound) && otherCls.ct==this.ct )
			return true;
		else 
			return false; 
	}
}

public class ProfilerFeature {

	public static ContextType[] nounContext = {Profile.ContextType.NPB, Profile.ContextType.NPIB, Profile.ContextType.NPIA, Profile.ContextType.NPA, 
		Profile.ContextType.NPB, Profile.ContextType.NNA, Profile.ContextType.MOD, Profile.ContextType.EB, 
		Profile.ContextType.EA, Profile.ContextType.AKA};

	public static ContextType[] verbContext = {Profile.ContextType.NVPB, Profile.ContextType.NVPA, Profile.ContextType.NVB, Profile.ContextType.NVA};

	public static String host = "smeagol.cs.illinois.edu";
	public static int port = 21999;
	public static String user = "profiler";
	public static String pass = "profiler";
	public static ProfilerClient profilerClient = null; 
	public static DB db = null;
	public static Map<String,Long> myMap = null;
	public static ReadVerbFrames vf = null;
	public static NounFormsExtractor nf = null;

	//static Map<Integer, int[]> featureMap = new HashMap<Integer, int[]>(); 
	final static int l1 = 5; 
	final static int l2 = 7;
	//final static int l3 = 5;
	final static int featureVectorSize = l1*nounContext.length + l2*verbContext.length; 
	final static double confidence = 1; 
	final static double confidence_prob = 0.0001;
	
	public static void setup() {
		System.out.println("Old Profiler Setup!");
		String mapdbF = "/shared/shelley/khashab2/CorporaAndDumps/ngrams/1gram_mapdb"; 
		db = DBMaker.newFileDB(new File(mapdbF))
				.readOnly()
				.make();
		
		// Create a Map:
		//long max = 1024908267229;
		long max = (long) 10E6;
		myMap = db.getHashMap("themap");

		try {
			profilerClient = new ProfilerClient(host, port, user, pass);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		vf = new ReadVerbFrames(); 
		vf.readAllVerbFrames(); 
		nf = new NounFormsExtractor();
	}

	public static double[] getProfilerFeatureGivenPairOfTriplesSubset(Triple t1, Triple t3) {
		double[] all = getProfilerFeatureGivenPairOfTriples(t1, t3); 
		double[] subset = new double[18]; 
		
		if (all.length >= 19) {
			subset[0] = all[6];
			subset[1] = all[6];
			subset[2] = all[8];
			subset[3] = all[8];
			subset[4] = all[10];
			subset[5] = all[10];
			subset[6] = all[10];
			subset[7] = all[12];
			subset[8] = all[12];
			subset[9] = all[12];
			subset[10] = all[14];
			subset[11] = all[14];
			subset[12] = all[14];
			subset[13] = all[14];
			subset[14] = all[16];
			subset[15] = all[16];
			subset[16] = all[18];
			subset[17] = all[18];
			return subset; 
		}
		else {
			return null;
		}
	}
	
	public static double[] getThresholdValues() { 
		return new double[] {2.05,3.04, // 6  
							1.1, 2.25, // 8 
							1.1, 2.1, 8.8, // 10 
							1.1, 3.1, 7.14, // 12 
							1, 5, 5.19, 7.19, // 14 
							1, 4.14, // 16 
							1, 4.14}; // 18 
	}
	
	public static double[] getProfilerFeatureGivenPairOfTriples(Triple t1, Triple t3) { 
		Set<String> men1_list = new HashSet<String>(); 
		//Set<String> men1_verb_list = new HashSet<String>(); 

		Set<String> pr_verb_list = new HashSet<String>(); 
		Set<String> pr_arg_list = new HashSet<String>(); 
		
		if( t1 == null || t3 == null || t3.predicate == null || t3.object == null || t1.subject == null )
			return new double[0]; 
		if( t1.role.equals("") || t1.role.equals("") || t1.role.equals("")  ) { 
			return new double[0];
		}
		if (profilerClient == null) {
			return new double[0];
		}
		
		String m1 = "", p2= t3.predicate, a2= ""; 
		if( t1.role.equals("s") ) { 
			//System.out.println("Choosing subject"); 
			men1_list.add( t1.subject );			
		}
		else { 
			//System.out.println("Choosing object");
			men1_list.add( t1.object );
		}		
		if( t3.role.equals("s") ) { 
			a2 = t3.object; 
			//pr_arg_list.add( );		
		}
		else { 
			a2 = t3.subject; 
		}
		//men1_verb_list.add( t1.predicate );
		pr_verb_list.add( p2 );
		men1_list.add( m1 ); 
		pr_arg_list.add( a2 );
		
		pr_verb_list.addAll( vf.getVerbFrames( p2 ) );
		pr_arg_list.addAll( nf.getNounForms( a2 ) );
		men1_list.addAll( nf.getNounForms( m1 ) );
		
		// filtering 
		if( men1_list.contains("") )
			men1_list.remove(""); 
		if( men1_list.contains("s") )
			men1_list.remove("s"); 
		if( men1_list.contains("***") )
			men1_list.remove("***"); 
		if( men1_list.contains("***s") )
			men1_list.remove("***s"); 

		if( pr_arg_list.contains("") )
			pr_arg_list.remove(""); 
		if( pr_arg_list.contains("s") )
			pr_arg_list.remove("s"); 
		if( pr_arg_list.contains("***") )
			pr_arg_list.remove("***"); 
		if( pr_arg_list.contains("***s") )
			pr_arg_list.remove("***s"); 

//		System.out.println(pr_verb_list); 
//		System.out.println(pr_arg_list); 
//		System.out.println(men1_list); 
		
//		men1_verb_list.addAll( nf.getNounForms( t1.predicate ) );	
		
//		System.out.println("======================================="); 
//		if( t1 != null )
//			System.out.println("t1 = " + t1.subject + "; " + t1.predicate + "; " + t1.object );
//		else 
//			System.out.println("Than is null"); 
//		if( t3 != null )
//			System.out.println("t3 = " + t3.subject + "; " + t3.predicate + "; " + t3.object );
//		else 
//			System.out.println("triple3 is null");
//		System.out.println("=======================================");

		Set<contextInfo> ctx1 = new HashSet<contextInfo>(); 
		for( String verb : pr_verb_list ) { 
			java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null);
			for (Profile profile : profiles) {
				// mention1 
				for( String men1 : men1_list )
					ctx1.addAll( CheckTheProfileForString( profile, men1, nounContext) );
//				System.out.println("Profile = " + profile); 
//				System.out.println("Relevant Context to men1_list = " + ctx1); 
			}
		}

		Set<contextInfo> ctx2 = new HashSet<contextInfo>(); 
		for( String men : men1_list ) { 
			java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null);
			for (Profile profile : profiles) {
				for( String verb : pr_verb_list )
					ctx2.addAll( CheckTheProfileForString( profile, verb, verbContext ) );
//				System.out.println("Profile = " + profile); 
//				System.out.println("Relevant Context to men1_list = " + ctx1); 
			}
		}
		
		Set<contextInfo> ctx3 = new HashSet<contextInfo>(); 
		for( String men : men1_list ) { 
			java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null);
			for (Profile profile : profiles) {
				for( String arg : pr_arg_list )
					ctx3.addAll( CheckTheProfileForString( profile, arg, nounContext ) );
//				System.out.println("Profile = " + profile); 
//				System.out.println("Relevant Context to men1_list = " + ctx1); 
			}
		}
		
		Set<contextInfo> ctx4 = new HashSet<contextInfo>(); 
		for( String arg : pr_arg_list ) { 
			java.util.List<Profile> profiles = profilerClient.queryProfiles(arg, null);
			for (Profile profile : profiles) {
				for( String men : men1_list  )
					ctx4.addAll( CheckTheProfileForString( profile, men, nounContext ) );
//				System.out.println("Profile = " + profile); 
//				System.out.println("Relevant Context to men1_list = " + ctx1); 
			}
		}
		
		contextInfo[] merged1 = mergeContextInfo(ctx1, 0, myMap);
//		System.out.println( merged1.length ); 
//		for( int i = 0; i < merged1.length; i++ )  { 
//			if( merged1[i] != null )
//				System.out.println( "Merged[" + i + "]=" + merged1[i].toString() );
//		}
		contextInfo[] merged2 = mergeContextInfo(ctx2, 1, myMap); 
		contextInfo[] merged3 = mergeContextInfo(ctx3, 0, myMap); 
		contextInfo[] merged4 = mergeContextInfo(ctx4, 0, myMap); 

		double[] all = new double[0]; 
		
		if( merged1 != null )
			all = ArrayUtils.addAll(all, convertToDouble(merged1, myMap, false));  
		if( merged2 != null )
			all = ArrayUtils.addAll(all, convertToDouble(merged2, myMap, true));
		if( merged3 != null )
			all = ArrayUtils.addAll(all, convertToDouble(merged3, myMap, true));
		if( merged4 != null )
			all = ArrayUtils.addAll(all, convertToDouble(merged4, myMap, false));

		return all; 
	}
	
	public static double[] convertToDouble( contextInfo[] ctx, Map<String,Long> map, boolean withScoresFliped) { 
		double[] scores = null; 
		if( withScoresFliped ) 
			scores = new double[ 3 * ctx.length ];
		else 
			scores = new double[ 2 * ctx.length ];
		for( int i = 0; i < ctx.length; i++ ) { 
			if( ctx[i] == null ) { 
				if( withScoresFliped ) { 
					scores[3 * i] = 0; 
					scores[3 * i + 1] = 0;
					scores[3 * i + 2] = 0;
				}
				else { 
					scores[2 * i] = 0; 
					scores[2 * i + 1] = 0;
				}
				//System.out.println("null!"); 
			}
			else {
				if( withScoresFliped ) { 
					scores[2 * i] = ctx[i].count; 
					scores[2 * i + 1] = ctx[i].probability;
					int c1 = 1; 
					int c2 = 1; 
					if( map.containsKey(ctx[i].profile) )
						c1 += map.get(ctx[i].profile); 
					if( map.containsKey(ctx[i].stringFound) )
						c2 += map.get(ctx[i].stringFound); 
					scores[3 * i + 2] = 1.0 * ctx[i].probability * c1 / c2;  
				}
				else { 
					scores[2 * i] = ctx[i].count; 
					scores[2 * i + 1] = ctx[i].probability;
				}
			}				
		}
		return scores; 
 	}

	// contextType: 0: noun    1: verb 
	public static contextInfo[] mergeContextInfo(Set<contextInfo> ctx, int contextType, Map<String,Long> myMap) { 
		contextInfo[] all = null; 
		double smoothingRate = 0.00001; 
		//long max = (long) 10e7; 
		if( contextType == 0 ) { 
			all = new contextInfo[nounContext.length]; 
			if( ctx.size() == 0)
				return all; 
			int size = 0; 
			double denomSize = 0; 
			for ( int k = 0; k < nounContext.length; k++ ) { 
				contextInfo merged = new contextInfo( ctx.iterator().next().profile, ctx.iterator().next().stringFound ); 
				for( contextInfo c : ctx ) { 
					merged.count += c.count; 
					//merged.probability += c.probability;
					int count = 1;
					if( myMap.containsKey(c.profile) )
						count += myMap.get(c.profile);
					merged.probability +=  count * c.probability;
					size ++;
					denomSize += count; 
				}
				if( size != 0 ) { 
					merged.count = merged.count / size;
					//merged.probability = merged.probability / size;
					merged.probability = merged.probability / denomSize;  
				}
				all[k] = merged; 
			}
		}
		else if( contextType == 1 ) { 
			all = new contextInfo[verbContext.length]; 
			if( ctx.size() == 0)
				return all; 
			int size = 0;
			for ( int k = 0; k < verbContext.length; k++ ) { 
				contextInfo merged = new contextInfo( ctx.iterator().next().profile, ctx.iterator().next().stringFound ); 
				for( contextInfo c : ctx ) { 
					merged.count += c.count; 
					merged.probability += c.probability; 
					size ++; 
				}
				if( size != 0 ) { 
					merged.count = merged.count / size;
					merged.probability = merged.probability / size; 
				}
				all[k] = merged; 
			}
		}
		
		return all; 
	}
	
	public static Set<contextInfo> CheckTheProfileForString( Profile pr, String str_toFind, ContextType[] context ) { 

		Set<contextInfo> allContextsInfo = new HashSet<contextInfo>(); 
		for( ContextType ct : context ) { 
			List<Object> list = pr.getContextOrderedByCount(ct);
			for (Object obj : list) {
				//System.out.println("Context = " + ct); 
				//edu.illinois.cs.cogcomp.profiler.profilerclient.models.Entity obj_etn = (Entity) obj;
				//String str =  obj_etn.getMention();
				String str = ""; 
				if( ct == Profile.ContextType.EA || ct == Profile.ContextType.EB )
					str =  ((Entity) obj).getMention(); 
				else 
					str =  (String) obj;

				if( str.contains(str_toFind)){ 
					contextInfo ci = new contextInfo(); 
					ci.stringFound = str_toFind;
					ci.probability = pr.getProbabilityWithinContext(ct, obj); 
					ci.count = pr.getCount(ct, obj);
					ci.ct = ct;
					allContextsInfo.add( ci );
					//					System.out.println(str);
					//					System.out.println(pr.getProbabilityWithinContext(ct, obj));
					//					System.out.println(pr.getCount(ct, obj) );
				}	
			}			
		}
		return allContextsInfo; 
	}
}
