package edu.illinois.cs.cogcomp.lbj.coref.io.loaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.DocPlainText;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;

/**
 * Created by xuany on 5/31/2017.
 */
public class DocFromTaLoader extends DocLoader{
    public DocFromTaLoader(MentionDecoder mentionDetector,
                           Classifier mTyper){
        super(mentionDetector, mTyper);
    }
    protected Doc createDoc(Object ota){
        TextAnnotation ta = (TextAnnotation)ota;
        DocPlainText d = new DocPlainText(ta);
        d.setUsePredictedMentions(true);
        //d.loadFromTA(ta);
        return d;
    }
}
