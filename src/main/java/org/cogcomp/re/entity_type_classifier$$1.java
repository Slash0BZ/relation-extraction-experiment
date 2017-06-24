// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000BCDCCA84D4155584DCB29CC29AC8F29AC284D8F4EC94C2E2ECC4BCC4D22515134D07ECFCB260AC5920558242B6A28D8EA2425E797C7A5A626949615A61BE8246456E221F004C30B1B3F4000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;


public class entity_type_classifier$$1 extends Classifier
{
  private static final bow_features __bow_features = new bow_features();
  private static final hym_features __hym_features = new hym_features();

  public entity_type_classifier$$1()
  {
    containingPackage = "org.cogcomp.re";
    name = "entity_type_classifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'entity_type_classifier$$1(Constituent)' defined on line 49 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__bow_features.classify(__example));
    __result.addFeatures(__hym_features.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'entity_type_classifier$$1(Constituent)' defined on line 49 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "entity_type_classifier$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof entity_type_classifier$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__bow_features);
    result.add(__hym_features);
    return result;
  }
}

