package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.finer.components.FinerTyperFactory;
import edu.illinois.cs.cogcomp.finer.components.IFinerTyper;
import edu.illinois.cs.cogcomp.finer.components.hyp_typer.SimpleHypernymTyper;
import edu.illinois.cs.cogcomp.finer.config.FinerConfiguration;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;

import java.io.*;
import java.util.*;

/**
 * Created by xuany on 6/20/2017.
 */
public class SupportFeatureExtractor {

    public List<String> getHypernymFeature(Constituent c){
        List<String> ret_features = new ArrayList<>();
        FinerConfiguration finerConfiguration = new FinerConfiguration();
        FinerTyperFactory finerTyperFactory = new FinerTyperFactory(finerConfiguration, false);
        try {
            SimpleHypernymTyper hymTyper = (SimpleHypernymTyper)finerTyperFactory.getHypTyper(new FileInputStream(finerConfiguration.getWordSenseDBPath()));
            hymTyper.annotateOneMention(c, null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret_features;
    }
}
