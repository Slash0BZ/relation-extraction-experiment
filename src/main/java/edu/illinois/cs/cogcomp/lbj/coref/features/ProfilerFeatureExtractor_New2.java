package edu.illinois.cs.cogcomp.lbj.coref.features;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Triple;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Created by khashab2 on 2/25/15.
 */

public class ProfilerFeatureExtractor_New2 {
/*
    public static String[] nounContext = {SchemaKeys.NPB, SchemaKeys.NPIB, SchemaKeys.NPIA, SchemaKeys.NPA,
            SchemaKeys.NPB, SchemaKeys.NNA, SchemaKeys.MOD, SchemaKeys.NNPB, SchemaKeys.NNPB, SchemaKeys.NPC,
            SchemaKeys.NPIA, SchemaKeys.NNPA, SchemaKeys.AKA,  SchemaKeys.DepN, SchemaKeys.DepNP};

    public static String[] verbContext = {SchemaKeys.NVPB, SchemaKeys.NVPA, SchemaKeys.NVB, SchemaKeys.NVA, SchemaKeys.VPB,
            SchemaKeys.VPA,  SchemaKeys.VPIA, SchemaKeys.VPIB,  SchemaKeys.DepV, SchemaKeys.DepVP};

    public static String[] nearestPairContext = {SchemaKeys.NEAREST_NER_PAIR};

    public static String[] tripleContext_noAggregation_after = {
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION
    };

    public static String[] tripleContext_noAggregation_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION,
    };
    public static String[] tripleContext_one_element_removed_after = {
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT
    };
    public static String[] tripleContext_one_element_removed_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT
    };
    public static String[] tripleContext_both_removed_after = {
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH
    };
    public static String[] tripleContext_both_removed_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH
    };
    public static String[] tripleContext_noAggregation_withConnective_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION,
    };
    public static String[] tripleContext_noAggregation_withConnective_after = {
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION
    };
    public static String[] tripleContext_one_element_removed_withConnective_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
    };
    public static String[] tripleContext_one_element_removed_withConnective_after = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT,
    };
    public static String[] tripleContext_both_removed_withConnective_after = {
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH
    };
    public static String[] tripleContext_both_removed_withConnective_before = {
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH,
            SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH
    };


    // All triples
    public static String[] tripleContext_all = { SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION,
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

        // note: these are tuned for an older version
        double[] all = getProfilerFeatureGivenPairOfTriples_Basic_Number2(t1, t3, profilerClient, vf, nf, myMap);
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



        // Above numbers cleaned and sorted


//        acc_new[52] = 0.6  coverage = cov_new[52] = 0.003663003663003663 correct[k] =3.0 th[52] = 1.05
//        acc_new[52] = 0.6  coverage = cov_new[52] = 0.003663003663003663 correct[k] =3.0 th[52] = 1.1
//        acc_new[40] = 0.5833333333333334  coverage = cov_new[40] = 0.008791208791208791 correct[k] =7.0 th[40] = 1.05
//        acc_new[42] = 0.5833333333333334  coverage = cov_new[42] = 0.008791208791208791 correct[k] =7.0 th[42] = 1.05
//        acc_new[50] = 0.5714285714285714  coverage = cov_new[50] = 0.005128205128205128 correct[k] =4.0 th[50] = 1.05
//        acc_new[40] = 0.5833333333333334  coverage = cov_new[40] = 0.008791208791208791 correct[k] =7.0 th[40] = 1.1
//        acc_new[42] = 0.5833333333333334  coverage = cov_new[42] = 0.008791208791208791 correct[k] =7.0 th[42] = 1.1
//        acc_new[38] = 0.5714285714285714  coverage = cov_new[38] = 0.010256410256410256 correct[k] =8.0 th[38] = 1.1
//        acc_new[38] = 0.5714285714285714  coverage = cov_new[38] = 0.010256410256410256 correct[k] =8.0 th[38] = 1.05
//        acc_new[36] = 0.5789473684210527  coverage = cov_new[36] = 0.01391941391941392 correct[k] =11.0 th[36] = 1.05
//        acc_new[36] = 0.5789473684210527  coverage = cov_new[36] = 0.01391941391941392 correct[k] =11.0 th[36] = 1.1
//        acc_new[46] = 0.6  coverage = cov_new[46] = 0.007326007326007326 correct[k] =6.0 th[46] = 1.05
//        acc_new[46] = 0.6  coverage = cov_new[46] = 0.007326007326007326 correct[k] =6.0 th[46] = 1.1
//        acc_new[296] = 0.6  coverage = cov_new[296] = 0.01098901098901099 correct[k] =9.0 th[296] = 1.1500000000000001
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.05
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.05
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.1
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.1
//        acc_new[44] = 0.6363636363636364  coverage = cov_new[44] = 0.00805860805860806 correct[k] =7.0 th[44] = 1.2500000000000002
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 1.4000000000000004



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



    public static double[] getProfilerFeatureGivenPairOfTriplesSubset_Basic_Number2_Additive(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap, contextInfo_NEW.PROFILER_FEATURE_TYPE corefType) {
        double[] all = getProfilerFeatureGivenPairOfTriples_Basic_Number2(t1, t3, profilerClient, vf, nf, myMap );
        double[] subset = null;


//        acc_new[4] = 0.5652173913043478  coverage = cov_new[4] = 0.01684981684981685 correct[k] =13.0 th[4] = 0.8999999999999999
//        acc_new[51] = 0.5789473684210527  coverage = cov_new[51] = 0.01391941391941392 correct[k] =11.0 th[51] = 0.8999999999999999
//        acc_new[53] = 0.5714285714285714  coverage = cov_new[53] = 0.010256410256410256 correct[k] =8.0 th[53] = 0.8999999999999999
//        acc_new[188] = 0.5724137931034483  coverage = cov_new[188] = 0.10622710622710622 correct[k] =83.0 th[188] = 1.3
//        acc_new[226] = 0.56  coverage = cov_new[226] = 0.03663003663003663 correct[k] =28.0 th[226] = 10.799999999999978
//        acc_new[228] = 0.5777777777777777  coverage = cov_new[228] = 0.03296703296703297 correct[k] =26.0 th[228] = 10.799999999999978
//        acc_new[210] = 0.5590551181102362  coverage = cov_new[210] = 0.09304029304029304 correct[k] =71.0 th[210] = 21.20000000000003
//        acc_new[212] = 0.5714285714285714  coverage = cov_new[212] = 0.06153846153846154 correct[k] =48.0 th[212] = 21.20000000000003
//        acc_new[214] = 0.5909090909090909  coverage = cov_new[214] = 0.04835164835164835 correct[k] =39.0 th[214] = 21.20000000000003
//        acc_new[216] = 0.5964912280701754  coverage = cov_new[216] = 0.041758241758241756 correct[k] =34.0 th[216] = 21.20000000000003
//        acc_new[218] = 0.5714285714285714  coverage = cov_new[218] = 0.03076923076923077 correct[k] =24.0 th[218] = 21.20000000000003
//
//        acc_new[210] = 0.5658914728682171  coverage = cov_new[210] = 0.0945054945054945 correct[k] =73.0 th[210] = 20.900000000000027
//        acc_new[212] = 0.5747126436781609  coverage = cov_new[212] = 0.06373626373626373 correct[k] =50.0 th[212] = 20.900000000000027
//        acc_new[214] = 0.5970149253731343  coverage = cov_new[214] = 0.04908424908424908 correct[k] =40.0 th[214] = 20.900000000000027
//        acc_new[216] = 0.5862068965517241  coverage = cov_new[216] = 0.04249084249084249 correct[k] =34.0 th[216] = 20.900000000000027
//        acc_new[218] = 0.5652173913043478  coverage = cov_new[218] = 0.0336996336996337 correct[k] =26.0 th[218] = 20.900000000000027
//
//        acc_new[49] = 0.5833333333333334  coverage = cov_new[49] = 0.008791208791208791 correct[k] =7.0 th[49] = 1.3
//        acc_new[55] = 0.5833333333333334  coverage = cov_new[55] = 0.008791208791208791 correct[k] =7.0 th[55] = 0.8999999999999999
//        acc_new[57] = 0.5833333333333334  coverage = cov_new[57] = 0.008791208791208791 correct[k] =7.0 th[57] = 0.8999999999999999
//        acc_new[354] = 0.5882352941176471  coverage = cov_new[354] = 0.012454212454212455 correct[k] =10.0 th[354] = 0.8999999999999999
//        acc_new[216] = 0.5813953488372093  coverage = cov_new[216] = 0.063003663003663 correct[k] =50.0 th[216] = 10.799999999999978
//
//        acc_new[216] = 0.5897435897435898  coverage = cov_new[216] = 0.05714285714285714 correct[k] =46.0 th[216] = 11.899999999999974
//        acc_new[218] = 0.5942028985507246  coverage = cov_new[218] = 0.05054945054945055 correct[k] =41.0 th[218] = 11.899999999999974
//        acc_new[220] = 0.5873015873015873  coverage = cov_new[220] = 0.046153846153846156 correct[k] =37.0 th[220] = 11.899999999999974
//        acc_new[222] = 0.576271186440678  coverage = cov_new[222] = 0.04322344322344322 correct[k] =34.0 th[222] = 11.899999999999974
//        acc_new[224] = 0.5769230769230769  coverage = cov_new[224] = 0.0380952380952381 correct[k] =30.0 th[224] = 11.899999999999974
//        acc_new[226] = 0.5681818181818182  coverage = cov_new[226] = 0.03223443223443224 correct[k] =25.0 th[226] = 11.899999999999974
//        acc_new[228] = 0.5897435897435898  coverage = cov_new[228] = 0.02857142857142857 correct[k] =23.0 th[228] = 11.899999999999974
//
//        acc_new[220] = 0.5909090909090909  coverage = cov_new[220] = 0.04835164835164835 correct[k] =39.0 th[220] = 10.799999999999978
//        acc_new[222] = 0.5833333333333334  coverage = cov_new[222] = 0.04395604395604396 correct[k] =35.0 th[222] = 10.799999999999978
//        acc_new[224] = 0.5964912280701754  coverage = cov_new[224] = 0.041758241758241756 correct[k] =34.0 th[224] = 10.799999999999978
//
//        acc_new[1] = 0.6  coverage = cov_new[1] = 0.003663003663003663 correct[k] =3.0 th[1] = 0.1
//        acc_new[356] = 0.6  coverage = cov_new[356] = 0.01098901098901099 correct[k] =9.0 th[356] = 0.5
//        acc_new[61] = 0.6  coverage = cov_new[61] = 0.007326007326007326 correct[k] =6.0 th[61] = 0.8999999999999999
//        acc_new[55] = 0.6  coverage = cov_new[55] = 0.003663003663003663 correct[k] =3.0 th[55] = 1.3
//        acc_new[220] = 0.6052631578947368  coverage = cov_new[220] = 0.02783882783882784 correct[k] =23.0 th[220] = 20.900000000000027
//
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 0.7999999999999999
//        acc_new[6] = 0.6363636363636364  coverage = cov_new[6] = 0.00805860805860806 correct[k] =7.0 th[6] = 0.8999999999999999
//
//        acc_new[218] = 0.6056338028169014  coverage = cov_new[218] = 0.05201465201465202 correct[k] =43.0 th[218] = 10.799999999999978
//        acc_new[220] = 0.6216216216216216  coverage = cov_new[220] = 0.027106227106227107 correct[k] =23.0 th[220] = 21.20000000000003
//
//        acc_new[59] = 0.6363636363636364  coverage = cov_new[59] = 0.00805860805860806 correct[k] =7.0 th[59] = 0.8999999999999999
//        acc_new[51] = 0.6363636363636364  coverage = cov_new[51] = 0.00805860805860806 correct[k] =7.0 th[51] = 1.3
//        acc_new[45] = 0.6363636363636364  coverage = cov_new[45] = 0.00805860805860806 correct[k] =7.0 th[45] = 7.599999999999989
//
//        acc_new[222] = 0.6571428571428571  coverage = cov_new[222] = 0.02564102564102564 correct[k] =23.0 th[222] = 20.900000000000027
//        acc_new[224] = 0.6666666666666666  coverage = cov_new[224] = 0.02197802197802198 correct[k] =20.0 th[224] = 20.900000000000027
//        acc_new[222] = 0.6774193548387096  coverage = cov_new[222] = 0.02271062271062271 correct[k] =21.0 th[222] = 21.20000000000003
//        acc_new[228] = 0.68  coverage = cov_new[228] = 0.018315018315018316 correct[k] =17.0 th[228] = 21.20000000000003
//        acc_new[226] = 0.6923076923076923  coverage = cov_new[226] = 0.01904761904761905 correct[k] =18.0 th[226] = 20.900000000000027
//        acc_new[228] = 0.6923076923076923  coverage = cov_new[228] = 0.01904761904761905 correct[k] =18.0 th[228] = 20.900000000000027
//        acc_new[226] = 0.6923076923076923  coverage = cov_new[226] = 0.01904761904761905 correct[k] =18.0 th[226] = 21.20000000000003
//        acc_new[224] = 0.7037037037037037  coverage = cov_new[224] = 0.01978021978021978 correct[k] =19.0 th[224] = 21.20000000000003

        switch (corefType) {
            case ALL:
                subset = new double[15];
                subset[0] = all[6];
                subset[1] = all[6];
                subset[2] = all[218];
                subset[3] = all[220];
                subset[4] = all[59];
                subset[5] = all[51];
                subset[6] = all[45];
                subset[7] = all[222];
                subset[8] = all[224];
                subset[9] = all[222];
                subset[10] = all[228];
                subset[11] = all[226];
                subset[12] = all[228];
                subset[13] = all[226];
                subset[14] = all[224];
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

    public static double[] getThresholdValues_Basic_Number2_Additive() {
        return new double[] {
            0.7999999999999999,
            0.8999999999999999,
            10.799999999999978,
            21.20000000000003,
            0.8999999999999999,
            1.3,
            7.599999999999989,
            20.900000000000027,
            20.900000000000027,
            21.20000000000003,
            21.20000000000003,
            20.900000000000027,
            20.900000000000027,
            21.20000000000003,
            21.20000000000003
        };
    }


    public static double[] getProfilerFeatureGivenPairOfTriplesSubset_Basic_Number2_Additive_Reversed(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap, contextInfo_NEW.PROFILER_FEATURE_TYPE corefType) {
        double[] all = getProfilerFeatureGivenPairOfTriples_Basic_Number2(t1, t3, profilerClient, vf, nf, myMap );
        double[] subset = null;


//        acc_new[287] = 0.5833333333333334  coverage = cov_new[287] = 0.017582417582417582 correct[k] =14.0 th[287] = 27.000000000000114
//        acc_new[289] = 0.5454545454545454  coverage = cov_new[289] = 0.01611721611721612 correct[k] =12.0 th[289] = 27.000000000000114
//        acc_new[289] = 0.5454545454545454  coverage = cov_new[289] = 0.01611721611721612 correct[k] =12.0 th[289] = 25.800000000000097
//        acc_new[295] = 0.6153846153846154  coverage = cov_new[295] = 0.009523809523809525 correct[k] =8.0 th[295] = 36.40000000000025
//        acc_new[295] = 0.6153846153846154  coverage = cov_new[295] = 0.009523809523809525 correct[k] =8.0 th[295] = 37.600000000000264
//        acc_new[299] = 0.6153846153846154  coverage = cov_new[299] = 0.009523809523809525 correct[k] =8.0 th[299] = 27.000000000000114
//        acc_new[301] = 0.6153846153846154  coverage = cov_new[301] = 0.009523809523809525 correct[k] =8.0 th[301] = 27.000000000000114
//        acc_new[303] = 0.6363636363636364  coverage = cov_new[303] = 0.00805860805860806 correct[k] =7.0 th[303] = 27.000000000000114
//        acc_new[305] = 0.6363636363636364  coverage = cov_new[305] = 0.00805860805860806 correct[k] =7.0 th[305] = 27.000000000000114
//        acc_new[307] = 0.6363636363636364  coverage = cov_new[307] = 0.00805860805860806 correct[k] =7.0 th[307] = 27.000000000000114
//        acc_new[309] = 0.6363636363636364  coverage = cov_new[309] = 0.00805860805860806 correct[k] =7.0 th[309] = 27.000000000000114
//        acc_new[311] = 0.6363636363636364  coverage = cov_new[311] = 0.00805860805860806 correct[k] =7.0 th[311] = 27.000000000000114
//        acc_new[313] = 0.6363636363636364  coverage = cov_new[313] = 0.00805860805860806 correct[k] =7.0 th[313] = 27.000000000000114
//        acc_new[297] = 0.6363636363636364  coverage = cov_new[297] = 0.00805860805860806 correct[k] =7.0 th[297] = 37.600000000000264
//        acc_new[299] = 0.6363636363636364  coverage = cov_new[299] = 0.00805860805860806 correct[k] =7.0 th[299] = 37.600000000000264
//        acc_new[301] = 0.6363636363636364  coverage = cov_new[301] = 0.00805860805860806 correct[k] =7.0 th[301] = 37.600000000000264
//        acc_new[303] = 0.6363636363636364  coverage = cov_new[303] = 0.00805860805860806 correct[k] =7.0 th[303] = 37.600000000000264
//        acc_new[305] = 0.6363636363636364  coverage = cov_new[305] = 0.00805860805860806 correct[k] =7.0 th[305] = 37.600000000000264
//        acc_new[307] = 0.6363636363636364  coverage = cov_new[307] = 0.00805860805860806 correct[k] =7.0 th[307] = 37.600000000000264
//        acc_new[309] = 0.6363636363636364  coverage = cov_new[309] = 0.00805860805860806 correct[k] =7.0 th[309] = 37.600000000000264
//        acc_new[311] = 0.6363636363636364  coverage = cov_new[311] = 0.00805860805860806 correct[k] =7.0 th[311] = 37.600000000000264
//        acc_new[313] = 0.6  coverage = cov_new[313] = 0.007326007326007326 correct[k] =6.0 th[313] = 37.600000000000264
//        acc_new[285] = 0.5740740740740741  coverage = cov_new[285] = 0.03956043956043956 correct[k] =31.0 th[285] = 25.800000000000097
//        acc_new[299] = 0.6153846153846154  coverage = cov_new[299] = 0.009523809523809525 correct[k] =8.0 th[299] = 25.800000000000097
//        acc_new[301] = 0.6153846153846154  coverage = cov_new[301] = 0.009523809523809525 correct[k] =8.0 th[301] = 25.800000000000097
//        acc_new[303] = 0.6153846153846154  coverage = cov_new[303] = 0.009523809523809525 correct[k] =8.0 th[303] = 25.800000000000097
//        acc_new[305] = 0.6363636363636364  coverage = cov_new[305] = 0.00805860805860806 correct[k] =7.0 th[305] = 25.800000000000097
//        acc_new[307] = 0.6363636363636364  coverage = cov_new[307] = 0.00805860805860806 correct[k] =7.0 th[307] = 25.800000000000097
//        acc_new[309] = 0.6363636363636364  coverage = cov_new[309] = 0.00805860805860806 correct[k] =7.0 th[309] = 25.800000000000097
//        acc_new[311] = 0.6363636363636364  coverage = cov_new[311] = 0.00805860805860806 correct[k] =7.0 th[311] = 25.800000000000097
//        acc_new[313] = 0.6363636363636364  coverage = cov_new[313] = 0.00805860805860806 correct[k] =7.0 th[313] = 25.800000000000097
//        acc_new[285] = 0.6  coverage = cov_new[285] = 0.03663003663003663 correct[k] =30.0 th[285] = 27.000000000000114
//        acc_new[287] = 0.625  coverage = cov_new[287] = 0.023443223443223443 correct[k] =20.0 th[287] = 25.800000000000097
//        acc_new[194] = 0.75  coverage = cov_new[194] = 0.0029304029304029304 correct[k] =3.0 th[194] = 20.700000000000024
//        acc_new[196] = 0.75  coverage = cov_new[196] = 0.0029304029304029304 correct[k] =3.0 th[196] = 20.700000000000024
//        acc_new[198] = 0.75  coverage = cov_new[198] = 0.0029304029304029304 correct[k] =3.0 th[198] = 20.700000000000024
//        acc_new[198] = 0.75  coverage = cov_new[198] = 0.0029304029304029304 correct[k] =3.0 th[198] = 16.09999999999996
//        acc_new[260] = 0.75  coverage = cov_new[260] = 0.0029304029304029304 correct[k] =3.0 th[260] = 12.099999999999973
//        acc_new[262] = 0.75  coverage = cov_new[262] = 0.0029304029304029304 correct[k] =3.0 th[262] = 12.099999999999973
//        acc_new[264] = 0.75  coverage = cov_new[264] = 0.0029304029304029304 correct[k] =3.0 th[264] = 12.099999999999973
//        acc_new[266] = 0.75  coverage = cov_new[266] = 0.0029304029304029304 correct[k] =3.0 th[266] = 12.099999999999973
//        acc_new[268] = 0.75  coverage = cov_new[268] = 0.0029304029304029304 correct[k] =3.0 th[268] = 12.099999999999973
//        acc_new[311] = 0.6153846153846154  coverage = cov_new[311] = 0.009523809523809525 correct[k] =8.0 th[311] = 14.099999999999966
//        acc_new[313] = 0.6153846153846154  coverage = cov_new[313] = 0.009523809523809525 correct[k] =8.0 th[313] = 14.099999999999966
//        acc_new[297] = 0.6153846153846154  coverage = cov_new[297] = 0.009523809523809525 correct[k] =8.0 th[297] = 36.40000000000025
//        acc_new[252] = 0.6666666666666666  coverage = cov_new[252] = 0.004395604395604396 correct[k] =4.0 th[252] = 17.199999999999974
//        acc_new[256] = 0.6666666666666666  coverage = cov_new[256] = 0.004395604395604396 correct[k] =4.0 th[256] = 13.299999999999969
//        acc_new[192] = 0.6666666666666666  coverage = cov_new[192] = 0.004395604395604396 correct[k] =4.0 th[192] = 20.700000000000024
//        acc_new[299] = 0.6363636363636364  coverage = cov_new[299] = 0.00805860805860806 correct[k] =7.0 th[299] = 36.40000000000025
//        acc_new[188] = 0.6363636363636364  coverage = cov_new[188] = 0.00805860805860806 correct[k] =7.0 th[188] = 20.700000000000024
//        acc_new[301] = 0.6363636363636364  coverage = cov_new[301] = 0.00805860805860806 correct[k] =7.0 th[301] = 36.40000000000025
//        acc_new[303] = 0.6363636363636364  coverage = cov_new[303] = 0.00805860805860806 correct[k] =7.0 th[303] = 36.40000000000025
//        acc_new[305] = 0.6363636363636364  coverage = cov_new[305] = 0.00805860805860806 correct[k] =7.0 th[305] = 36.40000000000025
//        acc_new[307] = 0.6363636363636364  coverage = cov_new[307] = 0.00805860805860806 correct[k] =7.0 th[307] = 36.40000000000025
//        acc_new[309] = 0.6363636363636364  coverage = cov_new[309] = 0.00805860805860806 correct[k] =7.0 th[309] = 36.40000000000025
//        acc_new[311] = 0.6363636363636364  coverage = cov_new[311] = 0.00805860805860806 correct[k] =7.0 th[311] = 36.40000000000025
//        acc_new[313] = 0.6363636363636364  coverage = cov_new[313] = 0.00805860805860806 correct[k] =7.0 th[313] = 36.40000000000025
//        acc_new[188] = 0.6428571428571429  coverage = cov_new[188] = 0.010256410256410256 correct[k] =9.0 th[188] = 16.09999999999996
//        acc_new[190] = 0.6666666666666666  coverage = cov_new[190] = 0.008791208791208791 correct[k] =8.0 th[190] = 16.09999999999996
//        acc_new[192] = 0.6666666666666666  coverage = cov_new[192] = 0.006593406593406593 correct[k] =6.0 th[192] = 16.09999999999996
//        acc_new[190] = 0.6666666666666666  coverage = cov_new[190] = 0.006593406593406593 correct[k] =6.0 th[190] = 20.700000000000024
//        acc_new[196] = 0.8  coverage = cov_new[196] = 0.003663003663003663 correct[k] =4.0 th[196] = 16.09999999999996
//        acc_new[186] = 0.6428571428571429  coverage = cov_new[186] = 0.010256410256410256 correct[k] =9.0 th[186] = 20.700000000000024
//        acc_new[194] = 0.75  coverage = cov_new[194] = 0.005860805860805861 correct[k] =6.0 th[194] = 16.09999999999996

        switch (corefType) {
            case ALL:
                subset = new double[23];
                subset[0] = all[311];
                subset[1] = all[313];
                subset[2] = all[297];
                subset[3] = all[252];
                subset[4] = all[256];
                subset[5] = all[192];
                subset[6] = all[299];
                subset[7] = all[188];
                subset[8] = all[301];
                subset[9] = all[303];
                subset[10] = all[305];
                subset[11] = all[307];
                subset[12] = all[309];
                subset[13] = all[311];
                subset[15] = all[313];
                subset[16] = all[188];
                subset[17] = all[190];
                subset[18] = all[192];
                subset[19] = all[190];
                subset[20] = all[196];
                subset[21] = all[186];
                subset[22] = all[194];
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

    public static double[] getThresholdValues_Basic_Number2_Additive_Reversed() {
        return new double[] {
                14.099999999999966,
                14.099999999999966,
                36.40000000000025,
                17.199999999999974,
                13.299999999999969,
                20.700000000000024,
                36.40000000000025,
                20.700000000000024,
                36.40000000000025,
                36.40000000000025,
                36.40000000000025,
                36.40000000000025,
                36.40000000000025,
                36.40000000000025,
                36.40000000000025,
                16.09999999999996,
                16.09999999999996,
                16.09999999999996,
                20.700000000000024,
                16.09999999999996,
                20.700000000000024,
                16.09999999999996
        };
    }



    public static double[] getProfilerFeatureGivenPairOfTriples_Basic_Number2(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap ) {
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
            String[] split = verb.split(" ");
            for( String verbSplit : split ) {
                java.util.List<Profile> profiles = profilerClient.queryProfiles(verbSplit, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.BASIC);
                for (Profile profile : profiles) {
                    // mention1
                    for (String men1 : men1_list)
                        ctx1_verb.addAll(CheckTheProfileForString(profile, men1, nounContext));
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
                }
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
            String[] split = verb.split(" ");
            for( String verbSplit : split ) {
                java.util.List<Profile> profiles = profilerClient.queryProfiles(verbSplit, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.DEPENDENCY);
                for (Profile profile : profiles) {
                    // mention1
                    for (String men1 : men1_list)
                        ctx1_verb_dep.addAll(CheckTheProfileForString(profile, men1, nounContext));
//				System.out.println("Profile = " + profile);
//				System.out.println("Relevant Context to men1_list = " + ctx1);
                }
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
            all = ArrayUtils.addAll(all, convertToDouble(merged1, myMap, true));
        System.out.println(all.length);
        if( merged1_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_dep, myMap, true));
        System.out.println(all.length);
        if( merged1_verb != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_verb, myMap, true));
        System.out.println(all.length);
        if( merged1_verb_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1_verb_dep, myMap, true));
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
            all = ArrayUtils.addAll(all, convertToDouble(merged4, myMap, true));
        System.out.println(all.length);
        if( merged4_dep != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged4_dep, myMap, true));
        System.out.println(all.length);
        return all;
    }

    public static double[] getProfilerFeatureGivenPairOfTriples_withTripleSchemas(Triple t1, Triple t3, ProfilerClient profilerClient, ReadVerbFrames vf, NounFormsExtractor nf, Map<String,Long> myMap, String connective) {
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

        String m1 = "", p2= t3.predicate, a2= "", a1 = "";
        if( t1.role.equals("s") ) {
            System.out.println("Choosing subject");
            men1_list.add( t1.subject );
            a1 = t1.object;
        }
        else {
            System.out.println("Choosing object");
            men1_list.add( t1.object );
            a1 = t1.subject;
        }
        men1_pred_list.add( t1.predicate );
        men1_arg_list.add( a1 );

        if( t3.role.equals("s") ) {
            a2 = t3.object;
            pr_list.add( t3.subject );
        }
        else {
            a2 = t3.subject;
            pr_list.add( t3.subject );
        }

        String[] schemaContext_after_no_aggregation = null, schemaContext_after_correfed= null, schemaContext_after_aggregate_both= null;
        String[] schemaContext_after_no_aggregation_connective= null, schemaContext_after_correfed_connective= null, schemaContext_after_both_connective= null;
        String[] schemaContext_before_no_aggregation = null, schemaContext_before_correfed= null, schemaContext_before_aggregate_both= null;
        String[] schemaContext_before_no_aggregation_connective= null, schemaContext_before_correfed_connective= null, schemaContext_before_both_connective= null;

        CorefTypes corefType = CorefTypes.SUBJ_SUBJ;
        CorefTypes corefType_reverse = CorefTypes.SUBJ_SUBJ;
        if( t1.role.equals("s") && t3.role.equals("s") ) {
            corefType = CorefTypes.SUBJ_SUBJ;
            corefType_reverse = CorefTypes.SUBJ_SUBJ;
            schemaContext_after_no_aggregation = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION };
            schemaContext_after_correfed = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_aggregate_both = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH };
            schemaContext_after_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION };
            schemaContext_after_correfed_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_both_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION };
            schemaContext_before_correfed = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_aggregate_both = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_NO_AGGREGATION };
            schemaContext_before_correfed_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_both_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_SUBJ_REMOVE_BOTH };

        }
        else if( t1.role.equals("s") && t3.role.equals("o") ) {
            corefType = CorefTypes.SUBJ_OBJ;
            corefType_reverse = CorefTypes.OBJ_SUBJ;
            schemaContext_after_no_aggregation = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION };
            schemaContext_after_correfed = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_aggregate_both = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH };
            schemaContext_after_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION };
            schemaContext_after_correfed_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_both_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION };
            schemaContext_before_correfed = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_aggregate_both = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_NO_AGGREGATION };
            schemaContext_before_correfed_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_both_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVEE_OBJ_SUBJ_REMOVE_BOTH };
        }
        else if( t1.role.equals("o") && t3.role.equals("s") ) {
            corefType = CorefTypes.OBJ_SUBJ;
            corefType_reverse = CorefTypes.SUBJ_OBJ;
            schemaContext_after_no_aggregation = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION };
            schemaContext_after_correfed = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_aggregate_both = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH };
            schemaContext_after_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_NO_AGGREGATION };
            schemaContext_after_correfed_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_both_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_SUBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION };
            schemaContext_before_correfed = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_aggregate_both = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_NO_AGGREGATION };
            schemaContext_before_correfed_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_both_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_SUBJ_OBJ_REMOVE_BOTH };
        }
        else if( t1.role.equals("o") && t3.role.equals("o") ) {
            corefType = CorefTypes.OBJ_OBJ;
            corefType_reverse = CorefTypes.OBJ_OBJ;
            schemaContext_after_no_aggregation = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION };
            schemaContext_after_correfed = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_aggregate_both = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH };
            schemaContext_after_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION };
            schemaContext_after_correfed_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_after_both_connective = new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION };
            schemaContext_before_correfed = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_aggregate_both = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH };
            schemaContext_before_no_aggregation_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_NO_AGGREGATION };
            schemaContext_before_correfed_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_COREFED_ELEMENT };
            schemaContext_before_both_connective = new String[]{ SchemaKeys.TRIPLE_BEFORE_WITH_COREF_LINK_WITH_CONNECTIVE_OBJ_OBJ_REMOVE_BOTH };
        }
        System.out.println("---------------------------------------> " + corefType + "  <-----------------------------------------");

        //men1_verb_list.add( t1.predicate );
        pr_verb_list.add( p2 );
        men1_list.add( m1 );
        pr_arg_list.add( a2 );

        pr_verb_list.addAll( vf.getVerbFrames( p2 ) );
        pr_arg_list.addAll( nf.getNounForms( a2 ) );
        men1_list.addAll( nf.getNounForms( m1 ) );

        men1_pred_list.addAll( vf.getVerbFrames( t1.predicate ) );
        men1_arg_list.addAll( nf.getNounForms( a1 ) );

        // filtering
        if( men1_list.contains("") )
            men1_list.remove("");
        if( men1_list.contains("s") )
            men1_list.remove("s");
        //if( men1_list.contains("***") )
        //    men1_list.remove("***");
        if( men1_list.contains("***s") )
            men1_list.remove("***s");

        if( pr_arg_list.contains("") )
            pr_arg_list.remove("");
        if( pr_arg_list.contains("s") )
            pr_arg_list.remove("s");
        //if( pr_arg_list.contains("***") )
        //    pr_arg_list.remove("***");
        if( pr_arg_list.contains("***s") )
            pr_arg_list.remove("***s");

        if( men1_arg_list.contains("") )
            men1_arg_list.remove("");
        if( men1_arg_list.contains("s") )
            men1_arg_list.remove("s");
        //if( men1_arg_list.contains("***") )
        //    men1_arg_list.remove("***");
        if( men1_arg_list.contains("***s") )
            men1_arg_list.remove("***s");

        if( men1_pred_list.contains("") )
            men1_pred_list.remove("");
        if( men1_pred_list.contains("s") )
            men1_pred_list.remove("s");
        //if( men1_pred_list.contains("***") )
        //    men1_pred_list.remove("***");
        if( men1_pred_list.contains("***s") )
            men1_pred_list.remove("***s");

        if( pr_verb_list.contains("") )
            pr_verb_list.remove("");
        if( pr_verb_list.contains("s") )
            pr_verb_list.remove("s");
        if( pr_verb_list.contains("***") )
            pr_verb_list.remove("***");
        if( pr_verb_list.contains("***s") )
            pr_verb_list.remove("***s");

//        System.out.println(pr_verb_list);
//        System.out.println(pr_arg_list);
//        System.out.println(men1_list);

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

        List<String> pr_split_augmented = new ArrayList<String>();
        for( String verb : pr_verb_list ) {
            String[] pr_split = verb.split(" ");
            pr_split_augmented.addAll(Arrays.asList(pr_split));
            pr_split_augmented.add(verb);
        }
        List<String> men1_split_augmented = new ArrayList<String>();
        for( String verb : men1_pred_list ) {
            String[] men1_split = verb.split(" ");
            men1_split_augmented.addAll(Arrays.asList(men1_split));
            men1_split_augmented.add(verb);
        }


        Set<contextInfo_NEW> ctx1 = new HashSet<contextInfo_NEW>();
        for( String verb : pr_split_augmented ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(verb, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.BASIC);
            //for (Profile profile : profiles) {
                // mention1
            if( profiles.size() == 0 )
                continue;
            for (int i = 1; i < profiles.size(); i++) {
                profiles.get(0).merge(profiles.get(i));
            }
            for( String pro_arg : pr_arg_list ) {
                for( String men1 : men1_list )
                    ctx1.addAll( CheckTheProfileForPairString( profiles.get(0), NlpUtils.getNormalizedTripleArgument(men1),
                            NlpUtils.getNormalizedTripleArgument(pro_arg), nearestPairContext ) );
//				System.out.println("Profile = " + profile);
                System.out.println("Relevant Context to men1_list = " + ctx1);
            }
            //}
        }

        Set<contextInfo_NEW> ctx_triples_noAgrregation_after = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_after = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_after = new HashSet<contextInfo_NEW>();

        Set<contextInfo_NEW> ctx_triples_noAgrregation_withConnective_after = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_withConnective_after = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_withConnective_after = new HashSet<contextInfo_NEW>();

        Set<contextInfo_NEW> ctx_triples_noAgrregation_before = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_before = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_before = new HashSet<contextInfo_NEW>();

        Set<contextInfo_NEW> ctx_triples_noAgrregation_withConnective_before = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_withConnective_before = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_withConnective_before = new HashSet<contextInfo_NEW>();

        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_subj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_subj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_subj_subj = new HashSet<contextInfo_NEW>();
//
        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_obj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_obj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_subj_obj = new HashSet<contextInfo_NEW>();

        Set<contextInfo_NEW> ctx_triples_noAgrregation_obj_subj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_obj_subj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_obj_subj = new HashSet<contextInfo_NEW>();
//
        Set<contextInfo_NEW> ctx_triples_noAgrregation_obj_obj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_coreffed_removed_obj_obj = new HashSet<contextInfo_NEW>();
        Set<contextInfo_NEW> ctx_triples_both_removed_obj_obj = new HashSet<contextInfo_NEW>();

//        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_both_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//
//        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_both_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//
//        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_both_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//
//        Set<contextInfo_NEW> ctx_triples_noAgrregation_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_coreffed_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();
//        Set<contextInfo_NEW> ctx_triples_both_removed_subj_subj_withConnective = new HashSet<contextInfo_NEW>();



        for( String men1_pred: men1_split_augmented ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(men1_pred, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.TRIPLE);
            System.out.println("Profile of = " + men1_pred);
            System.out.println("Relavant profiles = " + profiles.size() );
            if( profiles.size() == 0 )
                continue;
            for (int i = 1; i < profiles.size(); i++) {
                profiles.get(0).merge(profiles.get(i));
            }
            for( String pr_verbSplit : pr_split_augmented ) {

                for( String men1 : men1_list )
                    for( String pr_arg: pr_arg_list )
                        for(String pr:  pr_list )
                            for( String men1_arg: men1_arg_list ) {
                                System.out.println( "Query = " +  NlpUtils.getNormalizedTripleArgument(men1) + ", " +
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred) + ", " +
                                        NlpUtils.getNormalizedTripleArgument(men1_arg) + ", " +
                                        NlpUtils.getNormalizedTripleArgument(pr)+ ", " +
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit)+ ", " +
                                        NlpUtils.getNormalizedTripleArgument(pr_arg) );

                                ctx_triples_noAgrregation_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_no_aggregation,
                                        //tripleContext_noAggregation_after,
                                        CorefAggregationTypes.NO_AGGREGATION, corefType, ""));

                                ctx_triples_coreffed_removed_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_correfed,
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, corefType, ""));

                                ctx_triples_both_removed_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_aggregate_both,
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, corefType, ""));

                                ctx_triples_noAgrregation_withConnective_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_no_aggregation_connective,
                                        CorefAggregationTypes.NO_AGGREGATION, corefType, connective));

                                ctx_triples_coreffed_removed_withConnective_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_correfed_connective,
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, corefType, connective));

                                ctx_triples_both_removed_withConnective_after.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        schemaContext_after_both_connective,
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, corefType, connective));

                                ctx_triples_noAgrregation_subj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_NO_AGGREGATION },
                                        CorefAggregationTypes.NO_AGGREGATION, CorefTypes.SUBJ_SUBJ, connective));

                                ctx_triples_coreffed_removed_subj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_COREFED_ELEMENT },
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, CorefTypes.SUBJ_SUBJ, connective));

                                ctx_triples_both_removed_subj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_SUBJ_REMOVE_BOTH },
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, CorefTypes.SUBJ_SUBJ, connective));

                                ctx_triples_noAgrregation_subj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_NO_AGGREGATION },
                                        CorefAggregationTypes.NO_AGGREGATION, CorefTypes.SUBJ_OBJ, connective));

                                ctx_triples_coreffed_removed_subj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_COREFED_ELEMENT },
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, CorefTypes.SUBJ_OBJ, connective));

                                ctx_triples_both_removed_subj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_SUBJ_OBJ_REMOVE_BOTH },
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, CorefTypes.SUBJ_OBJ, connective));

                                ctx_triples_noAgrregation_obj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_NO_AGGREGATION },
                                        CorefAggregationTypes.NO_AGGREGATION, CorefTypes.OBJ_SUBJ, connective));

                                ctx_triples_coreffed_removed_obj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_COREFED_ELEMENT },
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, CorefTypes.OBJ_SUBJ, connective));

                                ctx_triples_both_removed_obj_subj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_SUBJ_REMOVE_BOTH },
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, CorefTypes.OBJ_SUBJ, connective));

                                ctx_triples_noAgrregation_obj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_NO_AGGREGATION },
                                        CorefAggregationTypes.NO_AGGREGATION, CorefTypes.OBJ_OBJ, connective));

                                ctx_triples_coreffed_removed_obj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_COREFED_ELEMENT },
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, CorefTypes.OBJ_OBJ, connective));

                                ctx_triples_both_removed_obj_obj.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        new String[]{ SchemaKeys.TRIPLE_AFTER_WITH_COREF_LINK_OBJ_OBJ_REMOVE_BOTH },
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, CorefTypes.OBJ_OBJ, connective));
                            }
            }
        }
        System.out.println("Relevant Context to men1_list = ");
        System.out.println( ctx_triples_noAgrregation_after );
        System.out.println( ctx_triples_coreffed_removed_after );
        System.out.println( ctx_triples_both_removed_after );


        // reverse
        for( String pr_verbSplit : pr_split_augmented ) {
            java.util.List<Profile> profiles = profilerClient.queryProfiles(pr_verbSplit, null, EntityTypes.VERBSENSE_ENTITY, SchemaCategories.TRIPLE);
            System.out.println("Profile of = " + pr_verbSplit);
            System.out.println("Relavant profiles = " + profiles.size() );
            if( profiles.size() == 0 )
                continue;
            for (int i = 1; i < profiles.size(); i++) {
                profiles.get(0).merge(profiles.get(i));
            }
            for( String men1_pred: men1_split_augmented ) {
                for( String men1 : men1_list )
                    for( String pr_arg: pr_arg_list )
                        for(String pr:  pr_list )
                            for( String men1_arg: men1_arg_list ) {
                                System.out.println( "Query = " + NlpUtils.getNormalizedTripleArgument(pr)+ ", " +
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit)+ ", " +
                                        NlpUtils.getNormalizedTripleArgument(pr_arg) + ", " +
                                        NlpUtils.getNormalizedTripleArgument(men1) + ", " +
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred) + ", " +
                                        NlpUtils.getNormalizedTripleArgument(men1_arg));

                                ctx_triples_noAgrregation_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_no_aggregation,
                                        //tripleContext_noAggregation_after,
                                        CorefAggregationTypes.NO_AGGREGATION, corefType_reverse, ""));

                                ctx_triples_coreffed_removed_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_correfed,
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, corefType_reverse, ""));

                                ctx_triples_both_removed_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_aggregate_both,
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, corefType_reverse, ""));

                                ctx_triples_noAgrregation_withConnective_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_no_aggregation_connective,
                                        CorefAggregationTypes.NO_AGGREGATION, corefType_reverse, connective));

                                ctx_triples_coreffed_removed_withConnective_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_correfed_connective,
                                        CorefAggregationTypes.REMOVE_CORREFED_ELEMENT, corefType_reverse, connective));

                                ctx_triples_both_removed_withConnective_before.addAll(CheckTheProfileForTriplePair(profiles.get(0),
                                        NlpUtils.getNormalizedTripleArgument(pr),
                                        NlpUtils.getNormalizedTriplePredicate(pr_verbSplit),
                                        NlpUtils.getNormalizedTripleArgument(pr_arg),
                                        NlpUtils.getNormalizedTripleArgument(men1),
                                        NlpUtils.getNormalizedTriplePredicate(men1_pred),
                                        NlpUtils.getNormalizedTripleArgument(men1_arg),
                                        schemaContext_before_both_connective,
                                        CorefAggregationTypes.REMOVE_BOTH_ELEMENTS, corefType_reverse, connective));
                            }


            }
        }

        contextInfo_NEW[] merged1 = mergeContextInfo(ctx1, 0, myMap);
        contextInfo_NEW[] merged2 = mergeContextInfo(ctx_triples_noAgrregation_after, 1, myMap);
        contextInfo_NEW[] merged3 = mergeContextInfo(ctx_triples_coreffed_removed_after, 1, myMap);
        contextInfo_NEW[] merged4 = mergeContextInfo(ctx_triples_both_removed_after, 1, myMap);
        contextInfo_NEW[] merged5 = mergeContextInfo(ctx_triples_noAgrregation_withConnective_after, 1, myMap);
        contextInfo_NEW[] merged6 = mergeContextInfo(ctx_triples_coreffed_removed_withConnective_after, 1, myMap);
        contextInfo_NEW[] merged7 = mergeContextInfo(ctx_triples_both_removed_withConnective_after, 1, myMap);
        contextInfo_NEW[] merged8 = mergeContextInfo(ctx_triples_noAgrregation_before, 1, myMap);
        contextInfo_NEW[] merged9 = mergeContextInfo(ctx_triples_coreffed_removed_before, 1, myMap);
        contextInfo_NEW[] merged10 = mergeContextInfo(ctx_triples_both_removed_before, 1, myMap);
        contextInfo_NEW[] merged11 = mergeContextInfo(ctx_triples_noAgrregation_withConnective_before, 1, myMap);
        contextInfo_NEW[] merged12 = mergeContextInfo(ctx_triples_coreffed_removed_withConnective_before, 1, myMap);
        contextInfo_NEW[] merged13 = mergeContextInfo(ctx_triples_both_removed_withConnective_before, 1, myMap);
        contextInfo_NEW[] merged14 = mergeContextInfo(ctx_triples_noAgrregation_subj_subj, 1, myMap);
        contextInfo_NEW[] merged15 = mergeContextInfo(ctx_triples_coreffed_removed_subj_subj, 1, myMap);
        contextInfo_NEW[] merged16 = mergeContextInfo(ctx_triples_both_removed_subj_subj, 1, myMap);
        contextInfo_NEW[] merged17 = mergeContextInfo(ctx_triples_noAgrregation_subj_obj, 1, myMap);
        contextInfo_NEW[] merged18 = mergeContextInfo(ctx_triples_coreffed_removed_subj_obj, 1, myMap);
        contextInfo_NEW[] merged19 = mergeContextInfo(ctx_triples_both_removed_subj_obj, 1, myMap);
        contextInfo_NEW[] merged20 = mergeContextInfo(ctx_triples_noAgrregation_obj_subj, 1, myMap);
        contextInfo_NEW[] merged21 = mergeContextInfo(ctx_triples_coreffed_removed_obj_subj, 1, myMap);
        contextInfo_NEW[] merged22 = mergeContextInfo(ctx_triples_both_removed_obj_subj, 1, myMap);
        contextInfo_NEW[] merged23 = mergeContextInfo(ctx_triples_noAgrregation_obj_obj, 1, myMap);
        contextInfo_NEW[] merged24 = mergeContextInfo(ctx_triples_coreffed_removed_obj_obj, 1, myMap);
        contextInfo_NEW[] merged25 = mergeContextInfo(ctx_triples_both_removed_obj_obj, 1, myMap);

        double[] all = new double[0];

        if( merged1 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged1, myMap, true));
        if( merged2 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged2, myMap, true));
        if( merged3 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged3, myMap, true));
        if( merged4 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged4, myMap, true));
        if( merged5 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged5, myMap, true));
        if( merged6 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged6, myMap, true));
        if( merged7 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged7, myMap, true));
        if( merged8 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged8, myMap, true));
        if( merged9 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged9, myMap, true));
        if( merged10 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged10, myMap, true));
        if( merged11 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged11, myMap, true));
        if( merged12 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged12, myMap, true));
        if( merged13 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged13, myMap, true));
        if( merged14 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged14, myMap, true));
        if( merged15 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged15, myMap, true));
        if( merged16 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged16, myMap, true));
        if( merged17 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged17, myMap, true));
        if( merged18 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged18, myMap, true));
        if( merged19 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged19, myMap, true));
        if( merged20 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged20, myMap, true));
        if( merged21 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged21, myMap, true));
        if( merged22 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged22, myMap, true));
        if( merged23 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged23, myMap, true));
        if( merged24 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged24, myMap, true));
        if( merged25 != null )
            all = ArrayUtils.addAll(all, convertToDouble(merged25, myMap, true));

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

    public static PairCountKey[] getAggregatedLabelPairCountKey( String str1, String str2 ) {
        PairCountKey[] output = new PairCountKey[ TargetLabels.NER_LABELS.length * TargetLabels.NER_LABELS.length ];
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        int iter = 0;
        for(String label1 : TargetLabels.NER_LABELS) {
            SingleCountKey sck1 = new SingleCountKey(str1, label1);
            for(String label2 : TargetLabels.NER_LABELS) {
                SingleCountKey sck2 = new SingleCountKey(str2, label2);
                output[iter] = new PairCountKey(sck1, sck2);
                iter++;
            }
        }
        return output;
    }

    public static Set<contextInfo_NEW> CheckTheProfileForPairString( Profile pr, String str_toFind, String str_toFind2, String[] context ) {
        PairCountKey[] inputPair = getAggregatedLabelPairCountKey(str_toFind, str_toFind2);

        Set<contextInfo_NEW> allContextsInfo = new HashSet<contextInfo_NEW>();
        for (String ct : context) {
            if (!pr.getAllSchema().keySet().contains(ct))
                continue;
            //System.out.println("----------- ---------------------------------->>>> Contains the schema ! ");

            for (PairCountKey pair : inputPair) {
//                System.out.println( "Key = " + pair.toString() );
                if (pr.getSchema(ct).containsKey(pair.toString())) {
//                    System.out.println("*********************Contains the key!! ******************************");
                    contextInfo_NEW ci = new contextInfo_NEW();
                    ci.stringFound = pair.toString();
                    ci.probability = pr.getProbabilityWithinContext(ct, pair.toString());  // pr.getProbabilityWithinContext(ct, obj);
                    ci.count = pr.getCount(ct, pair.toString());
                    ci.ct = ct;
                    allContextsInfo.add(ci);
                }
            }
            //System.out.println( "schema = " + pr.getAllSchema().get(ct) );
        }
        return allContextsInfo;
    }

    public static Set<contextInfo_NEW> CheckTheProfileForTriplePair( Profile pr, String s1, String p1, String o1, String s2,
                                                                     String p2, String o2, String[] context,
                                                                     CorefAggregationTypes aggType, CorefTypes corefType, String connective) {
        Set<contextInfo_NEW> allContextsInfo = new HashSet<contextInfo_NEW>();

//        SingleCountKey s1_ck = new SingleCountKey("", s1);
//        SingleCountKey p1_ck = new SingleCountKey("", p1);
//        SingleCountKey o1_ck = new SingleCountKey("", o1);
//
//        SingleCountKey s2_ck = new SingleCountKey("", s2);
//        SingleCountKey p2_ck = new SingleCountKey("", p2);
//        SingleCountKey o2_ck = new SingleCountKey("", o2);

//        TripleCountKey tp1 = new TripleCountKey( s1_ck, p1_ck, o1_ck );
//        TripleCountKey tp2 = new TripleCountKey( s2_ck, p2_ck, o2_ck );

        if(  p1 == null || p2 == null  )
            return  allContextsInfo;
        String key = null;
        switch ( aggType ) {
            case NO_AGGREGATION:
                if( s1 == null || p1 == null || o1 == null || s2 == null || p2 == null  || o2 == null )
                    return  allContextsInfo;
                key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2 );
                break;
            case REMOVE_CORREFED_ELEMENT:
                switch (corefType) {
                    case SUBJ_SUBJ:
                        if( o1 == null || o2 == null )
                            return  allContextsInfo;
                        key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2, true, false, true, false);
                        break;
                    case SUBJ_OBJ:
                        if( o1 == null || s2 == null )
                            return  allContextsInfo;
                        key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2, true, false, false, true);
                        break;
                    case OBJ_OBJ:
                        if( s1 == null || s2 == null )
                            return  allContextsInfo;
                        key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2, false, true, false, true);
                        break;
                    case OBJ_SUBJ:
                        if( s1 == null || o2 == null )
                            return  allContextsInfo;
                        key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2, false, true, true, false);
                        break;
                }
                break;
            case REMOVE_BOTH_ELEMENTS:
                key = TripleCoOccurrenceCountKey.generateString( s1, p1, o1, s2, p2, o2, true, true, true, true);
                break;
        }
        if( !connective.equals("") )
            key = TripleCoOccurrenceCountKeyWithConnective.generateString(key, connective);

        for (String ct : context) {
            if (!pr.getAllSchema().keySet().contains(ct))
                continue;
//            System.out.println("---------------> The schema is contained!! ");
//            System.out.println(" Key = " + key);
//            System.out.println("Schema = " + pr.getAllSchema().get(ct));
//            System.out.println("Connective = " + connective);
            if (pr.getSchema(ct).containsKey(key)) {
                System.out.println("****************************************** THE SCHEMA CONTAINS THE KEY!!! ***********************************************");
                System.out.println(aggType);
                contextInfo_NEW ci = new contextInfo_NEW();
                ci.stringFound = key;
                ci.probability = pr.getProbabilityWithinContext(ct, key);  // pr.getProbabilityWithinContext(ct, obj);
                ci.count = pr.getCount(ct, key);
                ci.ct = ct;
                allContextsInfo.add(ci);
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