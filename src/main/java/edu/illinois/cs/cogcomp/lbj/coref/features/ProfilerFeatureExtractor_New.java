package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.io.File;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Triple;
import edu.illinois.cs.cogcomp.profiler.profilerclient.ProfilerClient;
import edu.illinois.cs.cogcomp.profiler.profilerclient.models.Profile;

/**
 * Created by khashab2 on 2/25/15.
 */

class contextInfo_NEW {
    public String profile;
    public String stringFound;
    public double probability;
    public int count;
    public String ct; // context type

    public enum PROFILER_FEATURE_TYPE {
        ALL,
        VERB_MEN_WIKI_BASIC, VERB_MEN_VERB_BASIC, VERB_MEN_WIKI_DEP, VERB_MEN_VERB_DEP, // ctx1
        MEN_VEB_WIKI_BASIC, MEN_VEB_WIKI_DEP, // ctx2
        MEN_ARG_WIKI_BASIC, MEN_ARG_WIKI_DEP, // ctx3
        ARG_NEN_WIKI_BASIC, ARG_MEN_WIKI_DEP // ctx4
    }

    public contextInfo_NEW() {
        profile = "";
        stringFound = "";
        probability = 0;
        count = 0;
    }

    public contextInfo_NEW(String str1, String str2) {
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
        if (!(other instanceof contextInfo_NEW))return false;
        contextInfo_NEW otherCls = (contextInfo_NEW)other;
        if( otherCls.count == this.count && Math.abs(otherCls.probability - this.probability ) < 0.01 && otherCls.stringFound.equals(this.stringFound) && otherCls.ct==this.ct )
            return true;
        else
            return false;
    }
}

public class ProfilerFeatureExtractor_New {
	/*public static String host = "ec2-54-159-37-153.compute-1.amazonaws.com";
	public static int port = 27017;
	public static ProfilerClient profilerClient = null; 
	public static DB db = null;
	public static Map<String,Long> myMap = null;
	public static ReadVerbFrames vf = null;
	public static NounFormsExtractor nf = null;
	
	public static void setup() {
		System.out.println("New Profiler Setup!");
		String mapdbF = "/shared/shelley/khashab2/CorporaAndDumps/ngrams/1gram_mapdb"; 
		db = DBMaker.newFileDB(new File(mapdbF))
				.readOnly()
				.make();
		
		long max = (long) 10E6;
		myMap = db.getHashMap("themap");

		try {
			profilerClient = new ProfilerClient(host, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		vf = new ReadVerbFrames(); 
		vf.readAllVerbFrames(); 
		nf = new NounFormsExtractor();
	}

    public static String[] nounContext = {SchemaKeys.NPB, SchemaKeys.NPIB, SchemaKeys.NPIA, SchemaKeys.NPA,
            SchemaKeys.NPB, SchemaKeys.NNA, SchemaKeys.MOD, SchemaKeys.NNPB, SchemaKeys.NNPB, SchemaKeys.NPC,
            SchemaKeys.NPIA, SchemaKeys.NNPA, SchemaKeys.AKA,  SchemaKeys.DepN, SchemaKeys.DepNP};

    public static String[] verbContext = {SchemaKeys.NVPB, SchemaKeys.NVPA, SchemaKeys.NVB, SchemaKeys.NVA, SchemaKeys.VPB,
            SchemaKeys.VPA,  SchemaKeys.VPIA, SchemaKeys.VPIB,  SchemaKeys.DepV, SchemaKeys.DepVP};

    public static String[] nearestPairContext = {SchemaKeys.NEAREST_NER_PAIR, SchemaKeys.NEAREST_POS_PAIR};

    public static String[] tripleContext = { SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH };


    public static double[] getProfilerFeatureGivenPairOfTriplesSubset(Triple t1, Triple t3) {
        double[] all = getProfilerFeatureGivenPairOfTriples(t1, t3, profilerClient, vf, nf, myMap );
        double[] subset = null;


        // multiplicative
//        acc_new[0] = 0.5231788079470199  coverage = cov_new[0] = 0.11062271062271062 correct[k] =79.0 th[0] = 1.05
//        acc_new[1] = 0.5510204081632653  coverage = cov_new[1] = 0.14358974358974358 correct[k] =108.0 th[1] = 1.05
//        acc_new[2] = 0.4857142857142857  coverage = cov_new[2] = 0.02564102564102564 correct[k] =17.0 th[2] = 1.05
//        acc_new[3] = 0.5510204081632653  coverage = cov_new[3] = 0.14358974358974358 correct[k] =108.0 th[3] = 1.05
//        acc_new[4] = 0.5652173913043478  coverage = cov_new[4] = 0.01684981684981685 correct[k] =13.0 th[4] = 1.05
//        acc_new[5] = 0.5510204081632653  coverage = cov_new[5] = 0.14358974358974358 correct[k] =108.0 th[5] = 1.05
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.05
//        acc_new[7] = 0.5510204081632653  coverage = cov_new[7] = 0.14358974358974358 correct[k] =108.0 th[7] = 1.05
//        acc_new[8] = 0.5555555555555556  coverage = cov_new[8] = 0.006593406593406593 correct[k] =5.0 th[8] = 1.05
//        acc_new[9] = 0.5510204081632653  coverage = cov_new[9] = 0.14358974358974358 correct[k] =108.0 th[9] = 1.05
//        acc_new[11] = 0.5510204081632653  coverage = cov_new[11] = 0.14358974358974358 correct[k] =108.0 th[11] = 1.05
//        acc_new[36] = 0.5789473684210527  coverage = cov_new[36] = 0.01391941391941392 correct[k] =11.0 th[36] = 1.05
//        acc_new[38] = 0.5714285714285714  coverage = cov_new[38] = 0.010256410256410256 correct[k] =8.0 th[38] = 1.05
//        acc_new[40] = 0.5833333333333334  coverage = cov_new[40] = 0.008791208791208791 correct[k] =7.0 th[40] = 1.05
//        acc_new[42] = 0.5833333333333334  coverage = cov_new[42] = 0.008791208791208791 correct[k] =7.0 th[42] = 1.05
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.05
//        acc_new[46] = 0.6  coverage = cov_new[46] = 0.007326007326007326 correct[k] =6.0 th[46] = 1.05
//        acc_new[50] = 0.5714285714285714  coverage = cov_new[50] = 0.005128205128205128 correct[k] =4.0 th[50] = 1.05
//        acc_new[52] = 0.6  coverage = cov_new[52] = 0.003663003663003663 correct[k] =3.0 th[52] = 1.05
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.1
//        acc_new[21] = 0.5538461538461539  coverage = cov_new[21] = 0.14285714285714285 correct[k] =108.0 th[21] = 1.1
//        acc_new[27] = 0.5538461538461539  coverage = cov_new[27] = 0.14285714285714285 correct[k] =108.0 th[27] = 1.1
//        acc_new[31] = 0.54  coverage = cov_new[31] = 0.07326007326007326 correct[k] =54.0 th[31] = 1.1
//        acc_new[33] = 0.54  coverage = cov_new[33] = 0.07326007326007326 correct[k] =54.0 th[33] = 1.1
//        acc_new[36] = 0.5789473684210527  coverage = cov_new[36] = 0.01391941391941392 correct[k] =11.0 th[36] = 1.1
//        acc_new[38] = 0.5714285714285714  coverage = cov_new[38] = 0.010256410256410256 correct[k] =8.0 th[38] = 1.1
//        acc_new[40] = 0.5833333333333334  coverage = cov_new[40] = 0.008791208791208791 correct[k] =7.0 th[40] = 1.1
//        acc_new[42] = 0.5833333333333334  coverage = cov_new[42] = 0.008791208791208791 correct[k] =7.0 th[42] = 1.1
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.1
//        acc_new[46] = 0.6  coverage = cov_new[46] = 0.007326007326007326 correct[k] =6.0 th[46] = 1.1
//        acc_new[52] = 0.6  coverage = cov_new[52] = 0.003663003663003663 correct[k] =3.0 th[52] = 1.1
//        acc_new[38] = 0.5714285714285714  coverage = cov_new[38] = 0.010256410256410256 correct[k] =8.0 th[38] = 1.1500000000000001
//        acc_new[39] = 0.54  coverage = cov_new[39] = 0.07326007326007326 correct[k] =54.0 th[39] = 1.1500000000000001
//        acc_new[40] = 0.5833333333333334  coverage = cov_new[40] = 0.008791208791208791 correct[k] =7.0 th[40] = 1.1500000000000001
//        acc_new[42] = 0.5833333333333334  coverage = cov_new[42] = 0.008791208791208791 correct[k] =7.0 th[42] = 1.1500000000000001
//        acc_new[296] = 0.6  coverage = cov_new[296] = 0.01098901098901099 correct[k] =9.0 th[296] = 1.1500000000000001
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.2000000000000002
//        acc_new[46] = 0.6  coverage = cov_new[46] = 0.007326007326007326 correct[k] =6.0 th[46] = 1.2000000000000002
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.2500000000000002
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.3500000000000003

//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.4000000000000004



        // multiplicative: the other way around: type = 3
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.0

        subset = new double[21];
        if (all.length<297) {
        	return subset;
        }
        subset[0] = all[6]; // 1.4
        subset[1] = all[44]; // 1.25
        subset[2] = all[6]; // 1.2
        subset[3] = all[296]; // 1.15
        subset[4] = all[46]; // 1.1
        subset[5] = all[52]; // 1.1
        subset[6] = all[44]; // 1.1
        subset[7] = all[42]; // 1.1
        subset[8] = all[40]; // 1.1
        subset[9] = all[38]; // 1.1
        subset[10] = all[36]; // 1.1
        subset[11] = all[6]; // 1.1
        subset[12] = all[52]; // 1.05
        subset[13] = all[50]; // 1.05
        subset[14] = all[46]; // 1.05
        subset[15] = all[44]; // 1.05
        subset[16] = all[42]; // 1.05
        subset[17] = all[40]; // 1.05
        subset[18] = all[38]; // 1.05
        subset[19] = all[36]; // 1.05
        subset[20] = all[6];  // 1.05

        return subset;
    }

    public static double[] getThresholdValues() {
        return new double[] {
         1.4,
         1.25,
         1.2,
         1.15,
         1.1,
         1.1,
         1.1,
         1.1,
         1.1,
         1.1,
         1.1,
         1.1,
         1.05,
         1.05,
         1.05,
         1.05,
         1.05,
         1.05,
         1.05,
         1.05,
         1.05
        };
    }



    public static double[] getProfilerFeatureGivenPairOfTriples(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap ) {
        Set<String> men1_list = new HashSet<String>();
        Set<String> men1_arg_list = new HashSet<String>();
        Set<String> men1_pred_list = new HashSet<String>();

        Set<String> pr_list = new HashSet<String>();
        Set<String> pr_verb_list = new HashSet<String>();
        Set<String> pr_arg_list = new HashSet<String>();


        if( t1 == null || t3 == null || t3.predicate == null || t3.object == null || t1.subject == null )
            return new double[0];
        if( t1.role.equals("") || t1.role.equals("") || t1.role.equals("")  ) {
            return new double[0];
        }

        String m1 = "", p2= t3.predicate, a2= "";
        if( t1.role.equals("s") ) {
            System.out.println("Choosing subject");
            men1_list.add( t1.subject );
            men1_arg_list.add( t1.object );
        }
        else {
            System.out.println("Choosing object");
            men1_list.add( t1.object );
            men1_arg_list.add( t1.subject );
        }
        men1_pred_list.add( t1.predicate );

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

        System.out.println(pr_verb_list);
        System.out.println(pr_arg_list);
        System.out.println(men1_list);

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

        Set<contextInfo_NEW> ctx1 = new HashSet<contextInfo_NEW>();
        for( String verb : pr_verb_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.BASIC);
            for (Profile profile : profiles) {
                // mention1
                for( String men1 : men1_list )
                    ctx1.addAll( CheckTheProfileForString( profile, men1, nounContext) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx1_verb = new HashSet<contextInfo_NEW>();
        for( String verb : pr_verb_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.BASIC);
            for (Profile profile : profiles) {
                // mention1
                for( String men1 : men1_list )
                    ctx1_verb.addAll( CheckTheProfileForString( profile, men1, nounContext) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx1_dep = new HashSet<contextInfo_NEW>();
        for( String verb : pr_verb_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.DEPENDENCY);
            for (Profile profile : profiles) {
                // mention1
                for( String men1 : men1_list )
                    ctx1_dep.addAll( CheckTheProfileForString( profile, men1, nounContext) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx1_verb_dep = new HashSet<contextInfo_NEW>();
        for( String verb : pr_verb_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.DEPENDENCY);
            for (Profile profile : profiles) {
                // mention1
                for( String men1 : men1_list )
                    ctx1_verb_dep.addAll( CheckTheProfileForString( profile, men1, nounContext) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx2 = new HashSet<contextInfo_NEW>();
        for( String men : men1_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.BASIC);
            for (Profile profile : profiles) {
                for( String verb : pr_verb_list )
                    ctx2.addAll( CheckTheProfileForString( profile, verb, verbContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx2_dep = new HashSet<contextInfo_NEW>();
        for( String men : men1_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.DEPENDENCY);
            for (Profile profile : profiles) {
                for( String verb : pr_verb_list )
                    ctx2_dep.addAll( CheckTheProfileForString( profile, verb, verbContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx3 = new HashSet<contextInfo_NEW>();
        for( String men : men1_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.BASIC);
            for (Profile profile : profiles) {
                for( String arg : pr_arg_list )
                    ctx3.addAll( CheckTheProfileForString( profile, arg, nounContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }


        Set<contextInfo_NEW> ctx3_dep = new HashSet<contextInfo_NEW>();
        for( String men : men1_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(men, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.DEPENDENCY);
            for (Profile profile : profiles) {
                for( String arg : pr_arg_list )
                    ctx3_dep.addAll( CheckTheProfileForString( profile, arg, nounContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }


        Set<contextInfo_NEW> ctx4 = new HashSet<contextInfo_NEW>();
        for( String arg : pr_arg_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(arg, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.BASIC);
            for (Profile profile : profiles) {
                for( String men : men1_list  )
                    ctx4.addAll( CheckTheProfileForString( profile, men, nounContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        Set<contextInfo_NEW> ctx4_dep = new HashSet<contextInfo_NEW>();
        for( String arg : pr_arg_list ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(arg, null, EntityTypes.WIKIFIER_ENTITY, SchemaCategories.DEPENDENCY);
            for (Profile profile : profiles) {
                for( String men : men1_list  )
                    ctx4.addAll( CheckTheProfileForString( profile, men, nounContext ) );
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
            }
        }

        contextInfo_NEW[] merged1 = mergeContextInfo(ctx1, 0, myMap);
        contextInfo_NEW[] merged1_dep = mergeContextInfo(ctx1_dep, 0, myMap);
        contextInfo_NEW[] merged1_verb = mergeContextInfo(ctx1_verb, 0, myMap);
        contextInfo_NEW[] merged1_verb_dep = mergeContextInfo(ctx1_verb_dep, 0, myMap);
//		System.out.println( merged1.length );
//		for( int i = 0; i < merged1.length; i++ )  {
//			if( merged1[i] != null )
//				System.out.println( "Merged[" + i + "]=" + merged1[i].toString() );
//		}
        contextInfo_NEW[] merged2 = mergeContextInfo(ctx2, 1, myMap);
        contextInfo_NEW[] merged2_dep = mergeContextInfo(ctx2_dep, 1, myMap);
        contextInfo_NEW[] merged3 = mergeContextInfo(ctx3, 0, myMap);
        contextInfo_NEW[] merged3_dep = mergeContextInfo(ctx3_dep, 0, myMap);
        contextInfo_NEW[] merged4 = mergeContextInfo(ctx4, 0, myMap);
        contextInfo_NEW[] merged4_dep = mergeContextInfo(ctx4_dep, 0, myMap);

        double[] all = new double[0];

        if( merged1 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1, myMap, false));
        System.out.println(all.length);
        if( merged1_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_dep, myMap, false));
        System.out.println(all.length);
        if( merged1_verb != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_verb, myMap, false));
        System.out.println(all.length);
        if( merged1_verb_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_verb_dep, myMap, false));
        System.out.println(all.length);
        if( merged2 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged2, myMap, true));
        System.out.println(all.length);
        if( merged2_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged2_dep, myMap, true));
        System.out.println(all.length);
        if( merged3 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged3, myMap, true));
        System.out.println(all.length);
        if( merged3_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged3_dep, myMap, true));
        System.out.println(all.length);
        if( merged4 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged4, myMap, false));
        System.out.println(all.length);
        if( merged4_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged4_dep, myMap, false));
        System.out.println(all.length);
        return all;
    }

    public static void printFeatureBoundaries() {
        int start = 0;
        int end = nounContext.length*2;

        contextInfo_NEW.PROFILER_FEATURE_TYPE[] ctx1 = {contextInfo_NEW.PROFILER_FEATURE_TYPE.VERB_MEN_WIKI_BASIC, contextInfo_NEW.PROFILER_FEATURE_TYPE.VERB_MEN_VERB_BASIC,
                contextInfo_NEW.PROFILER_FEATURE_TYPE.VERB_MEN_WIKI_DEP, contextInfo_NEW.PROFILER_FEATURE_TYPE.VERB_MEN_VERB_DEP};
        contextInfo_NEW.PROFILER_FEATURE_TYPE[] ctx2 = {contextInfo_NEW.PROFILER_FEATURE_TYPE.MEN_VEB_WIKI_BASIC, contextInfo_NEW.PROFILER_FEATURE_TYPE.MEN_VEB_WIKI_DEP};  // ctx2
        contextInfo_NEW.PROFILER_FEATURE_TYPE[] ctx3 = {contextInfo_NEW.PROFILER_FEATURE_TYPE.MEN_ARG_WIKI_BASIC, contextInfo_NEW.PROFILER_FEATURE_TYPE.MEN_ARG_WIKI_DEP};
        contextInfo_NEW.PROFILER_FEATURE_TYPE[] ctx4 = {contextInfo_NEW.PROFILER_FEATURE_TYPE.ARG_NEN_WIKI_BASIC, contextInfo_NEW.PROFILER_FEATURE_TYPE.ARG_MEN_WIKI_DEP};

        for( contextInfo_NEW.PROFILER_FEATURE_TYPE ctxType : ctx1 ) {
            System.out.println( ctxType + "   ---> Start = " + start + "    End = " + end );
            start += nounContext.length * 2;
            end += nounContext.length * 2;
        }
        for( contextInfo_NEW.PROFILER_FEATURE_TYPE ctxType : ctx2 ) {
            System.out.println( ctxType + "   ---> Start = " + start + "    End = " + end );
            start += verbContext.length * 3;
            end += verbContext.length * 3;
        }
        for( contextInfo_NEW.PROFILER_FEATURE_TYPE ctxType : ctx3 ) {
            System.out.println( ctxType + "   ---> Start = " + start + "    End = " + end );
            start += nounContext.length * 3;
            end += nounContext.length * 3;
        }
        for( contextInfo_NEW.PROFILER_FEATURE_TYPE ctxType : ctx4 ) {
            System.out.println( ctxType + "   ---> Start = " + start + "    End = " + end );
            start += nounContext.length * 2;
            end += nounContext.length * 2;
        }
    }

    // contextType: 0: noun    1: verb
    public static contextInfo_NEW[] mergeContextInfo(Set<contextInfo_NEW> ctx, int contextType, Map<String,Long> myMap) {
        contextInfo_NEW[] all = null;
        double smoothingRate = 0.00001;
        //long max = (long) 10e7;
        if( contextType == 0 ) { // noun
            all = new contextInfo_NEW[nounContext.length];
            if( ctx.size() == 0)
                return all;
            int size = 0;
            double denomSize = 0;
            for ( int k = 0; k < nounContext.length; k++ ) {
                contextInfo_NEW merged = new contextInfo_NEW( ctx.iterator().next().profile, ctx.iterator().next().stringFound );
                for( contextInfo_NEW c : ctx ) {
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
            all = new contextInfo_NEW[verbContext.length];
            if( ctx.size() == 0)
                return all;
            int size = 0;
            for ( int k = 0; k < verbContext.length; k++ ) {
                contextInfo_NEW merged = new contextInfo_NEW( ctx.iterator().next().profile, ctx.iterator().next().stringFound );
                for( contextInfo_NEW c : ctx ) {
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

    public static Set<contextInfo_NEW> CheckTheProfileForString( Profile pr, String str_toFind, String[] context ) {

        Set<contextInfo_NEW> allContextsInfo = new HashSet<contextInfo_NEW>();
        for( String ct : context ) {
            if( !pr.getAllSchema().keySet().contains(ct) )
                continue;
            Set<String> sch = pr.getSchema(ct).keySet();
//            List<Object> list = pr.getKeysOrderedByCount(ct);
            for (String str : sch) {
                //System.out.println("Context = " + ct);
                //edu.illinois.cs.cogcomp.profiler.profilerclient.models.Entity obj_etn = (Entity) obj;
                //String str =  obj_etn.getMention();
//                String str = "";
                
                if( ct.equals(SchemaKeys.EA)  || ct.equals(SchemaKeys.EB) )
                    str =  ((Entity) obj).getMention();
                else
                    str =  (String) obj;
                
                if( str.contains(str_toFind)){
                    contextInfo_NEW ci = new contextInfo_NEW();
                    ci.stringFound = str_toFind;
                    ci.probability = pr.getProbabilityWithinContext(ct, str);  // pr.getProbabilityWithinContext(ct, obj);
                    ci.count = pr.getCount(ct, str);
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

    public static double[] convertToDouble( contextInfo_NEW[] ctx, Map<String,Long> map, boolean withScoresFliped) {
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
    }*/
}