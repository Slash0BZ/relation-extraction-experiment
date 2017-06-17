// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3BC14A02C0341481EBACC648482A700B57122E90AEE526AFA2F01E542990A02DBBB1C28BDFEF9994B4CC249D0E69E57D9520B6692EE49CA05955C888E13C61F1CC923C96305C10177771E8C0993E33893FD73D165D3BD4F7BEA3FDE954CA8CA1F22F6E1DC2130535BDCFC2D34CC9A7CE1CC55A7C285EBBE23882D89000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class bow_features extends Classifier
{
  public bow_features()
  {
    containingPackage = "org.cogcomp.re";
    name = "bow_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bow_features(Constituent)' defined on line 27 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    for (int i = c.getStartSpan(); i < c.getEndSpan(); i++)
    {
      __id = "" + (c.getTextAnnotation().getToken(i));
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
      System.err.println("Classifier 'bow_features(Constituent)' defined on line 27 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bow_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof bow_features; }
}

