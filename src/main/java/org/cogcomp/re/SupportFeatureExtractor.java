package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;

import java.io.*;
import java.util.*;

/**
 * Created by xuany on 6/20/2017.
 */
public class SupportFeatureExtractor {
    /*
    IFinerTyper typer = null;
    public SupportFeatureExtractor(){
        try {
            FinerConfiguration finerConfiguration = new FinerConfiguration();
            FinerTyperFactory finerTyperFactory = new FinerTyperFactory(finerConfiguration, false);
            typer = finerTyperFactory.getKBBiasTyper(new FileInputStream(finerConfiguration.getKBMentionDBPath()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Pair<String, Double>> getHypernymFeature(Constituent c){
        try {
            FineTypeConstituent fc = new FineTypeConstituent(c.getTextAnnotation(), c.getStartSpan(), c.getEndSpan());
            return ((SimpleKBBiasTyper)typer).annotateSingleMention(fc, null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    */
}
