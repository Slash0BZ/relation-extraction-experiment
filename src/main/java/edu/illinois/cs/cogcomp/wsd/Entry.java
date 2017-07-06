package edu.illinois.cs.cogcomp.wsd;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import edu.illinois.cs.cogcomp.wsd.annotators.WordSenseAnnotator;

import java.io.IOException;
import java.util.Properties;

import static edu.illinois.cs.cogcomp.wsd.WSD.VIEWNAME;

/**
 * Created by haowu4 on 1/14/17.
 */
public class Entry {
    public static void main(String[] args) throws IOException,
            AnnotatorException {

    }

    public static BasicAnnotatorService getPipeline() throws IOException, AnnotatorException {
//        String sentence = "Not content with bringing Rocky back to cinema " +
//                "screens , another Stallone character Vietnam vet " +
//                "John Rambo is coming out of hibernation , 19 years after " +
//                "the third film in the series .";
        Properties props = new Properties();
        props.setProperty("usePos", Configurator.TRUE);
        props.setProperty("useLemma",
                Configurator.TRUE);
        props.setProperty("useShallowParse",
                Configurator.TRUE);

        props.setProperty("useNerConll",
                Configurator.FALSE);
        props.setProperty("useNerOntonotes",
                Configurator.TRUE);
        props.setProperty("useStanfordParse",
                Configurator.FALSE);
        props.setProperty("useStanfordDep",
                Configurator.FALSE);

        props.setProperty("useSrlVerb",
                Configurator.FALSE);
        props.setProperty("useSrlNom",
                Configurator.FALSE);
        props.setProperty(
                "throwExceptionOnFailedLengthCheck",
                Configurator.FALSE);
        props.setProperty(
                "useJson",
                Configurator.FALSE);
        props.setProperty(
                "isLazilyInitialized",
                Configurator.FALSE);
//        props.setProperty(
//                PipelineConfigurator.USE_SRL_INTERNAL_PREPROCESSOR.key,
//                Configurator.FALSE);


        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key,
                Configurator.TRUE);
        props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key,
                "tmp/aswdtgffasdfasd");
        props.setProperty(
                AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key,
                Configurator.FALSE);
        props.setProperty(
                AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key,
                Configurator.TRUE);

        props.setProperty(
                "wsd-word-embedding-file",
                "finer_data/GoogleNews-vectors" +
                        "-negative300.combined_500k.txt");

        props.setProperty(
                "wsd-sense-embedding-file",
                "finer_data/synset_embeddings_300.txt");

        props.setProperty(
                "wsd-sense-mapping-file",
                "finer_data/word_pos_to_synsets.txt");

        BasicAnnotatorService processor = PipelineFactory
                .buildPipeline(new ResourceManager(props));

        ResourceManager resourceManager = new ResourceManager(props);

//        WordSenseAnnotator wsd = new WordSenseAnnotator(VIEWNAME, new String[]{""},
//                resourceManager);
//        processor.addAnnotator(wsd);
        return processor;
    }

    public static WordSenseAnnotator getDefaultWSD() {
        Properties props = new Properties();

        props.setProperty(
                "wsd-word-embedding-file",
                "finer_data/GoogleNews-vectors" +
                        "-negative300.combined_500k.txt");

        props.setProperty(
                "wsd-sense-embedding-file",
                "finer_data/synset_embeddings_300.txt");

        props.setProperty(
                "wsd-sense-mapping-file",
                "finer_data/word_pos_to_synsets.txt");

        ResourceManager resourceManager = new ResourceManager(props);

        WordSenseAnnotator wsd = new WordSenseAnnotator("", new String[]{""},
                resourceManager);
        return wsd;

    }

    public static WordSenseAnnotator getWithProperties(Properties props) {
        ResourceManager resourceManager = new ResourceManager(props);

        WordSenseAnnotator wsd = new WordSenseAnnotator("", new String[]{""},
                resourceManager);
        return wsd;

    }

}
