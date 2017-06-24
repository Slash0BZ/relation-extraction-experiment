// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4A8B3A00803C004FA291A2828E0EEE6E14A4F39260AF196254B7B7B3A3EBF42A71DF4089584AB6E665AFD8AD543CC4180BA25BFC769CC327B1318B50E8D0C697470432DA22FA075F4F323ED39059C62EF4E702ABFFAEB76000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;


public class entity_subtype_classifier$$1 extends Classifier
{
  private static final bow_features __bow_features = new bow_features();
  private static final hym_features __hym_features = new hym_features();
  private static final additional_features __additional_features = new additional_features();

  public entity_subtype_classifier$$1()
  {
    containingPackage = "org.cogcomp.re";
    name = "entity_subtype_classifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'entity_subtype_classifier$$1(Constituent)' defined on line 61 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__bow_features.classify(__example));
    __result.addFeatures(__hym_features.classify(__example));
    //__result.addFeatures(__additional_features.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'entity_subtype_classifier$$1(Constituent)' defined on line 61 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "entity_subtype_classifier$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof entity_subtype_classifier$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__bow_features);
    result.add(__hym_features);
    result.add(__additional_features);
    return result;
  }
}

