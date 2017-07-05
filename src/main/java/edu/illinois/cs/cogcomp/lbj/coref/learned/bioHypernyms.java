// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000053D81BA02C034154F75EE224241B83B9A3A0E4D541A349E0A6F9DE3863909711A54AFFE6A517A33CD3C9BD2BC33054AD1E1CEE23B7A067E15455E24B7E9EE3AF180439641B7C707D818D675738E76F2831659A4A8737E609CB3A8559A4FACF76E7EB82A96B539DA4B63879B005C6328353E0621A8DA89F046BB8DB14691BE477246586B5A6E60744C0F623850BC71037959B1D4B000000

package edu.illinois.cs.cogcomp.lbj.coref.learned;

import edu.illinois.cs.cogcomp.lbj.coref.features.WordNetTools;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.WordExample;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;


public class bioHypernyms extends Classifier
{
  public bioHypernyms()
  {
    containingPackage = "edu.illinois.cs.cogcomp.lbj.coref.learned";
    name = "bioHypernyms";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbj.coref.ir.examples.WordExample"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof WordExample))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bioHypernyms(WordExample)' defined on line 503 of mentionDetectionFeatures.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    WordExample ex = (WordExample) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String[] hyps = WordNetTools.getWN().getHypernymStrings(ex.getWord());
    for (int i = 0; i < hyps.length; ++i)
    {
      __id = "" + (hyps[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof WordExample[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bioHypernyms(WordExample)' defined on line 503 of mentionDetectionFeatures.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bioHypernyms".hashCode(); }
  public boolean equals(Object o) { return o instanceof bioHypernyms; }
}

