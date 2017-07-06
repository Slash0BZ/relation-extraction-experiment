package edu.illinois.cs.cogcomp.finer.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.illinois.cs.cogcomp.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finer.components.hyp_typer.SimpleHypernymTyper;
import edu.illinois.cs.cogcomp.finer.components.kb_typer.SimpleKBBiasTyper;
import edu.illinois.cs.cogcomp.finer.components.mention.BasicMentionDetection;
import edu.illinois.cs.cogcomp.finer.components.mention.TypeMapper;
import edu.illinois.cs.cogcomp.finer.components.pattern_typer.SimplePattern;
import edu.illinois.cs.cogcomp.finer.components.pattern_typer.SimplePatternBasedTyper;
import edu.illinois.cs.cogcomp.finer.config.FinerConfiguration;
import edu.illinois.cs.cogcomp.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.finer.datastructure.types.TypeSystem;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 5/16/17.
 */
public class FinerTyperFactory {
    private FinerConfiguration configuration;

    public FinerTyperFactory(FinerConfiguration configuration, boolean lazyInit) {
        this.configuration = configuration;
        this.typers = new ArrayList<>();
        if (!lazyInit) {
            this.init();
        }
    }

    public FinerTyperFactory(FinerConfiguration configuration) {
        this(configuration, false);
    }

    private void init() {

        try (InputStream is = new FileInputStream(configuration.getTypeSystemDBPath())) {
            this.typeSystem = TypeSystem.getFromYaml(is);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (InputStream is = new FileInputStream(configuration.getTypeMappingDBPath())) {
            this.mentionDetecter = this.getMentionDetecter(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (configuration.usingKBBiasTyper()) {
            try (InputStream is = new FileInputStream(configuration.getKBMentionDBPath())) {
                this.typers.add(this.getKBBiasTyper(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (configuration.usingHyponymTyper()) {
            try (InputStream is = new FileInputStream(configuration.getWordSenseDBPath())) {
                this.typers.add(this.getHypTyper(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (configuration.usingPatternTyper()) {
            try (InputStream is = new FileInputStream(configuration.getPatternDBPath())) {
                this.typers.add(this.getPatternTyper(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public FinerAnnotator getAnnotator() {
        if (this.mentionDetecter == null || this.typers.isEmpty()) {
            this.init();
        }
        return new FinerAnnotator(this.mentionDetecter, this.typers);
    }

    private MentionDetecter getMentionDetecter(InputStream is) {
        Gson gson = new GsonBuilder().create();
        Map<String, String> ret = new HashMap<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is))) {
            ret = gson.fromJson(reader, ret.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BasicMentionDetection(new TypeMapper(this.typeSystem, ret));
    }

    public IFinerTyper getKBBiasTyper(InputStream is) throws IOException {
        Map<String, Map<FinerType, Double>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\t");
            String pattern = parts[0];
            Map<FinerType, Double> scoreMap = new HashMap<>();
            try {
                for (String typeAndScore : parts[1].split(" ")) {
                    FinerType type = getTypeOrFail(typeAndScore.split(":")[0]);
                    double score = Double.parseDouble(typeAndScore.split(":")[1]);
                    scoreMap.put(type, score);
                }
                map.put(pattern, scoreMap);
            } catch (RuntimeException exp) {
                //System.err.println("[" + line + "] failed to process..");
            }

        }
        return new SimpleKBBiasTyper(map);
    }

    public IFinerTyper getHypTyper(InputStream is) throws IOException {
        Map<String, List<FinerType>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");
            String synsetId = parts[0];
            List<FinerType> types = Arrays.stream(parts[1].split(" "))
                    .map(this::getType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            map.put(synsetId, types);
        }
        return new SimpleHypernymTyper(map);
    }

    public IFinerTyper getPatternTyper(InputStream is) throws IOException {
        Map<SimplePattern, List<FinerType>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");

            int before = Integer.parseInt(parts[0]);
            String[] tokens = parts[1].split(" ");
            int after = Integer.parseInt(parts[2]);

            SimplePattern pattern = new SimplePattern(before, after, tokens);

            List<FinerType> types = Arrays.stream(parts[3].split(" "))
                    .map(this::getType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            map.put(pattern, types);
        }
        return new SimplePatternBasedTyper(map);

    }

    private FinerType getTypeOrFail(String name) {
        return this.typeSystem.getTypeOrFail(name);
    }

    private Optional<FinerType> getType(String name) {
        return this.typeSystem.getType(name);
    }


    private TypeSystem typeSystem = null;
    private MentionDetecter mentionDetecter = null;
    private List<IFinerTyper> typers = null;

    public void setMentionDetecter(MentionDetecter mentionDetecter) {
        this.mentionDetecter = mentionDetecter;
    }

    public void setTypers(List<IFinerTyper> typers) {
        this.typers = typers;
    }

    public List<IFinerTyper> getTypers() {
        return typers;
    }
}