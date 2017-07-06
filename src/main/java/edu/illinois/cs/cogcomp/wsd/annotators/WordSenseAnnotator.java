package edu.illinois.cs.cogcomp.wsd.annotators;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation
        .TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsd.embedding.DefaultSentenceEmbeddingGenerator;
import edu.illinois.cs.cogcomp.wsd.embedding.EmbeddingSpace;
import edu.illinois.cs.cogcomp.wsd.embedding.SentenceEmbeddingGenerator;
import edu.illinois.cs.cogcomp.wsd.math.DenseVector;
import edu.illinois.cs.cogcomp.wsd.math.Distance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.illinois.cs.cogcomp.wsd.WSD.VIEWNAME;

/**
 * Created by haowu4 on 1/13/17.
 */

public class WordSenseAnnotator extends Annotator {

    public static class WordAndPOS {
        private String word;
        private String pos;

        public WordAndPOS(String word, String pos) {
            this.word = word;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WordAndPOS that = (WordAndPOS) o;

            if (!word.equals(that.word)) return false;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            int result = word.hashCode();
            result = 31 * result + pos.hashCode();
            return result;
        }

        public String getWord() {
            return word;
        }

        public String getPos() {
            return pos;
        }
    }

    private EmbeddingSpace wordEmbeddings;
    private EmbeddingSpace senseEmbeddings;
    private Map<WordAndPOS, List<String>> candidatesMaps;
    private SentenceEmbeddingGenerator sentenceEmbeddingGenerator = new
            DefaultSentenceEmbeddingGenerator();

    public void setSentenceEmbeddingGenerator(SentenceEmbeddingGenerator
                                                      sentenceEmbeddingGenerator) {
        this.sentenceEmbeddingGenerator = sentenceEmbeddingGenerator;
    }

    private List<String> wordToSense(String w, String pos) {
        return candidatesMaps.getOrDefault(new WordAndPOS(w, pos), new
                ArrayList<String>());
    }

    public WordSenseAnnotator(String viewName, String[] requiredViews,
                              ResourceManager rm) {
        super(viewName, requiredViews, rm);
    }

    private static EmbeddingSpace readEmbeddingFile(String filename) {
        List<DenseVector> vectors = new ArrayList<DenseVector>();
        Map<String, Integer> toId = new HashMap<String, Integer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader
                    (filename));
            System.out.print("Reading embeddings from " + filename + " ...  ");
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter % 10000 == 0) {
//                    System.out.printf("Reading %s, progress %d \n", filename,
//                            counter);
                }
                String trimedLine = line.trim();
                if (trimedLine.isEmpty()) {
                    continue;
                }

                String[] parts = trimedLine.split("\t");
                if (parts.length != 2) {
//                    System.err.println(line);
                    continue;
                }
                String entry = parts[0];
                String[] vecParts = parts[1].split(" ");
                float[] vec = new float[vecParts.length];
                for (int i = 0; i < vecParts.length; i++) {
                    vec[i] = Float.valueOf(vecParts[i]);
                }
                vectors.add(new DenseVector(vec));
                toId.put(entry, counter);
                counter++;
            }
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EmbeddingSpace(toId, vectors);
    }

    @Override
    public void initialize(ResourceManager rm) {
        // Read word embedding.
        String wordEmbeddingFile = rm.getString("wsd-word-embedding-file");

        wordEmbeddings = readEmbeddingFile(wordEmbeddingFile);

        // Read sense embedding.
        String senseEmbeddingFile = rm.getString("wsd-sense-embedding-file");
        senseEmbeddings = readEmbeddingFile(senseEmbeddingFile);


        candidatesMaps = new HashMap<>();
        String senseMapping = rm.getString("wsd-sense-mapping-file");
        try (BufferedReader br = new BufferedReader(new FileReader
                (senseMapping))) {
            String line;

            while ((line = br.readLine()) != null) {
                String trimedLine = line.trim();
                if (trimedLine.isEmpty()) {
                    continue;
                }

                String[] parts = trimedLine.split("\t");
                String word = parts[0];
                String pos = parts[1];
                String[] senses = parts[2].split(" ");
                WordAndPOS key = new WordAndPOS(word, pos);
                List<String> candidates = candidatesMaps.getOrDefault(key, new
                        ArrayList<String>());
                for (String s : senses) {
                    candidates.add(s);
                }
                candidatesMaps.put(key, candidates);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        View v = new View(VIEWNAME, "EmbeddingWSD", ta, 1.0);
        for (Sentence sentence : ta.sentences()) {
            for (Constituent ct : ta.getView(ViewNames.TOKENS).getConstituentsCoveringSpan(sentence.getStartSpan(), sentence.getEndSpan())){
                Map<String, Double> result = predict(sentence, ct);
                if (result.isEmpty()) continue;
                v.addConstituent(new Constituent(result, VIEWNAME, ta, ct
                        .getStartSpan(), ct.getEndSpan()));
            }
        }
        ta.addView(VIEWNAME, v);
    }

    public String mapPOS(String pos) {
        switch (pos.toLowerCase().charAt(0)) {
            case 'v':
                return "v";
            case 'n':
                return "n";
            default:
                return "";
        }
    }

    public Map<String, Double> predict(Sentence sentence, Constituent ct) {
        Map<String, Double> result = new HashMap<>();

        String query = ct.getSurfaceForm();
        String pos = ct.getTextAnnotation().getView(ViewNames.POS)
                .getConstituentsCovering(ct).get(0).getLabel();
        List<String> senses = wordToSense(query, mapPOS(pos));

        if (senses.isEmpty()) {
            return result;
        }

        DenseVector sentenceEmbedding = sentenceEmbeddingGenerator
                .getEmbeddingOrNull(sentence, wordEmbeddings);


        if (sentenceEmbedding == null) {
            return result;
        }

        double normOfSentence = Distance.norm(sentenceEmbedding);
        for (String s : senses) {
            DenseVector senseEmbedding = senseEmbeddings.getEmbeddingOrNull(s);
            if (senseEmbedding == null) continue;
            double sim = Distance.cosine(sentenceEmbedding, senseEmbedding,
                    normOfSentence,
                    Distance.norm(senseEmbedding));
            result.put(s, sim);
        }

        return result;
    }


}
