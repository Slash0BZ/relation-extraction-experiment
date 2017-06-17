// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D59C1B90080301500D55EA11C41A308AD98318308488F3296E49BBF9A4CDDD65C7DEBDB872301D8C993B38B564465387B3F9AEC2CA05A4A0235727B834D129AF30C591D8BE515BD02380DA26CF6F2ABFF2F971EA356EA6B6000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class offset_features extends Classifier
{
  public offset_features()
  {
    containingPackage = "org.cogcomp.re";
    name = "offset_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'offset_features(Constituent)' defined on line 22 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    __id = "" + (c.getStartSpan());
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "" + (c.getEndSpan());
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'offset_features(Constituent)' defined on line 22 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "offset_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof offset_features; }
}

