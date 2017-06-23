package edu.illinois.cs.cogcomp.finer.utils;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by haowu4 on 1/15/17.
 */
public class PipelineUtils {
    public static BasicAnnotatorService getPipeline() throws IOException,
            AnnotatorException {
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
                Configurator.TRUE);
//        props.setProperty(
//                PipelineConfigurator.USE_SRL_INTERNAL_PREPROCESSOR.key,
//                Configurator.FALSE);


        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key,
                Configurator.TRUE);
        props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key,
                "cache/db");
        props.setProperty(
                AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key,
                Configurator.FALSE);
        props.setProperty(
                AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key,
                Configurator.FALSE);
//
//        String embeddingFile =
//                "/home/haowu4/data/autoextend/GoogleNews-vectors" +
//                        "-negative300.combined_500k.txt";
//
//        if (!new File(embeddingFile).exists()) {
//            embeddingFile =
//                    "/shared/preprocessed/muddire2/Google/GoogleNews-vectors" +
//                            "-negative300.combined_500k.txt";
//        }
//
//        props.setProperty(
//                "wsd-word-embedding-file", embeddingFile
//        );
//
//        embeddingFile =
//                "/home/haowu4/data/autoextend/synset_embeddings_300.txt";
//
//        if (!new File(embeddingFile).exists()) {
//            embeddingFile =
//                    "/shared/preprocessed/muddire2/Google/synset_embeddings_300.txt";
//        }
//
//        props.setProperty(
//                "wsd-sense-embedding-file",
//                embeddingFile);
//
//        embeddingFile =
//                "/home/haowu4/data/autoextend/word_pos_to_synsets.txt";
//
//        if (!new File(embeddingFile).exists()) {
//            embeddingFile =
//                    "/shared/preprocessed/muddire2/Google/word_pos_to_synsets.txt";
//        }
//
//        props.setProperty(
//                "wsd-sense-mapping-file", embeddingFile
//        );

        ResourceManager resourceManager = new ResourceManager(props);
//        WordSenseAnnotator wsd = new WordSenseAnnotator("SENSE", new String[]{ViewNames.POS},
//                resourceManager);

        BasicAnnotatorService processor = PipelineFactory
                .buildPipeline(new ResourceManager(props));
//        processor.addAnnotator(wsd);

        return processor;
    }

//    public static List<FineNerType> readFinerTypes(String file) throws IOException, JWNLException {
//        List<FineNerType> types = new ArrayList<>();
//        List<String> typeStrs = FileUtils.readLines(new File(file));
//        for (String s : typeStrs) {
//            String[] parts = s.split("\\t");
//            String typName = parts[0];
//            String[] senseNames = parts[1].split(" ");
//            List<Synset> synsets = new ArrayList<>();
//            for (String sense : senseNames) {
//                synsets.add(WordNetUtils.getInstance().getSynsetOfNoun(sense));
//            }
//            types.add(new FineNerType(typName, synsets));
//        }
//        return types;
//    }

}
