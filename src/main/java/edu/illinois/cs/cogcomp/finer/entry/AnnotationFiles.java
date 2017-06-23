package edu.illinois.cs.cogcomp.finer.entry;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation
        .TextAnnotation;

import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finer.utils.PipelineUtils;
import net.sf.extjwnl.JWNLException;
import org.apache.commons.io.IOUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static edu.illinois.cs.cogcomp.finer.utils.PipelineUtils.getPipeline;

/**
 * Created by haowu4 on 1/17/17.
 */
public class AnnotationFiles {
    public static class AnnotationFilesParameters {
        @Parameter(names = "-input")
        public String input = "/home/haowu4/data/1blm/text/train/news.en-00001-of-00100";
        @Parameter(names = "-output")
        public String output = "/tmp/dumps";
        @Parameter(names = "-limit")
        public int limit = 5000000;
        @Parameter(names = "-offset")
        public int offset = 0;
    }

    private AnnotationFilesParameters parameters;
    DB db;
    HTreeMap<String, byte[]> store;
    BasicAnnotatorService processor;
    FinerAnnotator finerAnnotator;

    public AnnotationFiles(AnnotationFilesParameters parameters) {
        this.parameters = parameters;
    }

    public void init() throws IOException, AnnotatorException, JWNLException {
        processor = getPipeline();
//        finerAnnotator = new FinerAnnotator(PipelineUtils
//                .readFinerTypes("resources/type_to_wordnet_senses.txt"));

        db = DBMaker
                .fileDB(String.format("%s_%d", parameters.output, 0))
                .closeOnJvmShutdown()
                .make();

        store = db.hashMap("annotated")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

    int counter = 0;

    public void annotateAndSave(String id, String text) throws
            Exception {

        TextAnnotation ta = processor.createAnnotatedTextAnnotation("", id,
                text);
        finerAnnotator.addView(ta);
        if (ta.getView("FINER").getConstituents().isEmpty()) {
            return;
        }

        byte[] blob = compress(SerializationHelper.serializeTextAnnotationToBytes(ta));
        store.put(id, blob);
//        TextAnnotation recovered = SerializationHelper.deserializeTextAnnotationFromBytes(decompress(blob));
        counter++;

        if (counter % 50000 == 0) {
            db.commit();
            store.close();
            db.close();
            System.out.println("New DB made..");
            db = DBMaker
                    .fileDB(String.format("%s_%d", parameters.output, (counter / 50000)))
                    .closeOnJvmShutdown()
                    .make();

            store = db.hashMap("annotated")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.BYTE_ARRAY)
                    .createOrOpen();
        }

    }

    public static byte[] compress(byte[] content) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] decompress(byte[] contentBytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }


    public void startProcess() {
        int lineCounter = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(parameters
                    .input));
            String line;
            while ((line = br.readLine()) != null) {
                lineCounter++;
                if (lineCounter < parameters.offset) {
                    continue;
                }
                annotateAndSave(lineCounter + "", line);

                System.out.print(String.format("L" +
                        "ine processed with " +
                        "annotation %d/%d \r", counter, lineCounter));
                if (lineCounter > parameters.limit) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.commit();
        store.close();
        db.close();
        System.out.println(String.format("L" +
                "ine processed with " +
                "annotation %d/%d \n", counter, lineCounter));
        System.out.println("Finished ");
        System.exit(0);
    }


    public static void main(String[] args) throws JWNLException,
            AnnotatorException, IOException {
        AnnotationFilesParameters parameters = new AnnotationFilesParameters();

        new JCommander(parameters).parse(args);
        System.out.println(parameters);
        AnnotationFiles gd = new AnnotationFiles(parameters);
        gd.init();
        gd.startProcess();
    }
}
