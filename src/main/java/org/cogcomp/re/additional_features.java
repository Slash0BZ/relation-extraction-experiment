// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056C81BA020130150F75E532425C986BA2288F9076F2B42B72B82B194E50409BF775FA5BD91662B5B4559AB284EC64B2E2F8BE4A2CE55B58371F64367572254C160CB163124A5FD49325A27C7A8788832631FBEA9A735C26F2AF2E9CDB05E73D017165EEAEFF1F08D64CE0CAD57F8913F70FF1DFA5F49000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class additional_features extends Classifier
{
  public additional_features()
  {
    containingPackage = "org.cogcomp.re";
    name = "additional_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'additional_features(Constituent)' defined on line 33 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (c.getStartSpan() > 0)
    {
      __id = "" + (c.getTextAnnotation().getToken(c.getStartSpan() - 1));
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'additional_features(Constituent)' defined on line 33 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "additional_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof additional_features; }
}

