package edu.illinois.cs.cogcomp.finer.entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finer.components.FinerTyperFactory;
import edu.illinois.cs.cogcomp.finer.config.FinerConfiguration;
import edu.illinois.cs.cogcomp.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wsd.Entry;
import edu.illinois.cs.cogcomp.wsd.annotators.WordSenseAnnotator;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.wsd.Entry.getPipeline;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.init;

/**
 * Created by haowu4 on 1/15/17.
 */
public class WebInterface {

    private static Gson GSON = new GsonBuilder().create();

    static class AnnotationTypeResponse {
        String type_name;
        String comment;

        public AnnotationTypeResponse(FineTypeConstituent c, String type_name) {
            this.type_name = type_name;
            List<String> comments = c.getReasons(type_name).
                    stream()
                    .map(r -> r.getComment())
                    .collect(Collectors.toList());
            this.comment = StringUtils.join(comments, "\n");
        }
    }

    public static class AnnotationMentionResponse {
        List<AnnotationTypeResponse> types;
        int start;
        int end;

        public AnnotationMentionResponse(FineTypeConstituent c) {
            this.start = c.getStartSpan();
            this.end = c.getEndSpan();
            this.types = c.getLabelsToScores()
                    .keySet()
                    .stream()
                    .map(t -> new AnnotationTypeResponse(c, t))
                    .collect(Collectors.toList());

        }
    }

    public static class AnnootationResultResponse {
        String[] tokens;
        List<AnnotationMentionResponse> mentions;

        public AnnootationResultResponse(String[] tokens, List<AnnotationMentionResponse> mentions) {
            this.tokens = tokens;
            this.mentions = mentions;
        }
    }


    public static String annotationToJson(TextAnnotation ta, List<FineTypeConstituent> mentions) {
        String[] tokens = ta.getTokens();

        AnnootationResultResponse arr = new AnnootationResultResponse(tokens,
                mentions
                        .stream()
                        .map(AnnotationMentionResponse::new)
                        .filter(a -> !a.types.isEmpty())
                        .collect(Collectors.toList()));
        return GSON.toJson(arr);
    }


    public static TextAnnotationBuilder tab;

    //    public static NERAnnotator coarseGrainNer;
    public static WordSenseAnnotator wordSenseAnnotator;
    public static BasicAnnotatorService bas;
    public static FinerAnnotator finerAnnotator;

    public static FinerConfiguration getConfig() {
        return new FinerConfiguration();
    }

    public static void loadAnnotators() {
        FinerTyperFactory factory = new FinerTyperFactory(getConfig());
        finerAnnotator = factory.getAnnotator();
    }

    public static NERAnnotator getAnnotator() throws IOException {
        NERAnnotator co = new NERAnnotator(ViewNames.NER_ONTONOTES);
        return co;
    }


    public static TextAnnotationBuilder getTABuilder() throws IOException {
        TextAnnotationBuilder tab;
        // don't split on hyphens, as NER models are trained this way
        tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        return tab;
    }

    public static void init() throws IOException, AnnotatorException {
        loadAnnotators();
//        tab = getTABuilder();
//        coarseGrainNer = getAnnotator();
        bas = getPipeline();
        wordSenseAnnotator = Entry.getDefaultWSD();
    }

    public static String getAllAnnotation(String text) {
        System.out.println("Annotating [" + text + "]");
        TextAnnotation ta = null;
        try {
//            ta = tab.createTextAnnotation("", "", text);
//            coarseGrainNer.addView(ta);
            ta = bas.createAnnotatedTextAnnotation("", "", text);
            wordSenseAnnotator.addView(ta);

        } catch (AnnotatorException e) {
            // TODO: return error status.
            e.printStackTrace();
        }

        List<FineTypeConstituent> constituents = finerAnnotator.getAllFineTypeConstituents(ta);
        return annotationToJson(ta, constituents);
    }

    public static String annotate(Request request, Response response) {
        String text = request.queryParams("text");
        String responsText = getAllAnnotation(text);
        System.out.println("Response: ");
        System.out.println(responsText);
        response.type("application/json");
        response.status(200);
//        response.body(responsText);
        return responsText;
    }


    public static void main(String[] args) throws IOException, AnnotatorException {
        init();
        externalStaticFileLocation("web/finer-app/build");

        System.out.println(getAllAnnotation("The 68-year-old muscle man revealed he is preparing to once again star as the troubled and misunderstood Vietnam veteran John Rambo"));
        get("/annotate", WebInterface::annotate);
    }

}
