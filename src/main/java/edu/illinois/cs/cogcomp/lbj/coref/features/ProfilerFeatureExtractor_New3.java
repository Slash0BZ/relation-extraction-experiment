package edu.illinois.cs.cogcomp.lbj.coref.features;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Triple;

/**
 * Created by khashab2 on 2/25/15.
 */

public class ProfilerFeatureExtractor_New3 {
/*
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


    public static double[] getProfilerFeatureGivenPairOfTriplesSubset(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap, contextInfo_NEW.PROFILER_FEATURE_TYPE corefType) {
        double[] all = getProfilerFeatureGivenPairOfTriples(t1, t3, profilerClient, vf, nf, myMap );
        double[] subset = null;

        switch (corefType) {
            case ALL:
                subset = new double[20];
                subset[0] = all[52];
                subset[1] = all[52];
                subset[2] = all[40];
                subset[3] = all[42];
                subset[4] = all[50];
                subset[5] = all[40];
                subset[6] = all[42];
                subset[7] = all[38];
                subset[8] = all[38];
                subset[9] = all[36];
                subset[10] = all[36];
                subset[11] = all[46];
                subset[12] = all[46];
                subset[13] = all[296];
                subset[14] = all[6] ;
                subset[15] = all[44];
                subset[16] = all[6] ;
                subset[17] = all[44];
                subset[18] = all[44];
                subset[19] = all[6] ;

                break;
            case VERB_MEN_WIKI_BASIC:
                break;
            case VERB_MEN_VERB_BASIC:
                break;
            case VERB_MEN_WIKI_DEP:
                break;
            case VERB_MEN_VERB_DEP:
                break;
            case MEN_VEB_WIKI_BASIC:
                break;
            case MEN_VEB_WIKI_DEP:
                break;
            case MEN_ARG_WIKI_BASIC:
                break;
            case MEN_ARG_WIKI_DEP:
                break;
            case ARG_NEN_WIKI_BASIC:
                break;
            case ARG_MEN_WIKI_DEP:
                break;
        }

        return subset;
    }

    public static double[] getThresholdValues() {
        return new double[] {
                1.05,
                1.1,
                1.05,
                1.05,
                1.05,
                1.1,
                1.1,
                1.1,
                1.05,
                1.05,
                1.1,
                1.05,
                1.1,
                1.1500000000000001,
                1.05,
                1.05,
                1.1,
                1.1,
                1.2500000000000002,
                1.4000000000000004
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
    }

	public static void setup() {
		// TODO Auto-generated method stub
		
	}*/
}