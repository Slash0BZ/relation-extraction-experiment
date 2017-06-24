package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.finer.components.FinerTyperFactory;
import edu.illinois.cs.cogcomp.finer.components.IFinerTyper;
import edu.illinois.cs.cogcomp.finer.components.hyp_typer.SimpleHypernymTyper;
import edu.illinois.cs.cogcomp.finer.components.kb_typer.SimpleKBBiasTyper;
import edu.illinois.cs.cogcomp.finer.config.FinerConfiguration;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;

import java.io.*;
import java.util.*;

/**
 * Created by xuany on 6/20/2017.
 */
public class SupportFeatureExtractor {
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

}
