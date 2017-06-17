// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4A8D4A008020164FA2E2C0A0A61DEBD570113DF60401D076C4CBD7D62079FE7C7067502894129409E64AFD03E2A56E041054BED7E3372EFA5DFE05E6157C6FFB11F02A12859A50CBAA69B8F1033113460175E630DB0043697CDB7000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class entity_type_classifier$$1 extends Classifier
{
  private static final entity_level_features __entity_level_features = new entity_level_features();
  private static final word_features __word_features = new word_features();
  private static final offset_features __offset_features = new offset_features();
  private static final bow_features __bow_features = new bow_features();

  public entity_type_classifier$$1()
  {
    containingPackage = "org.cogcomp.re";
    name = "entity_type_classifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'entity_type_classifier$$1(Constituent)' defined on line 40 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__entity_level_features.classify(__example));
    __result.addFeatures(__word_features.classify(__example));
    __result.addFeatures(__offset_features.classify(__example));
    __result.addFeatures(__bow_features.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'entity_type_classifier$$1(Constituent)' defined on line 40 of PMS_sup.lbj received '" + type + "' as input.");
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
    result.add(__entity_level_features);
    result.add(__word_features);
    result.add(__offset_features);
    result.add(__bow_features);
    return result;
  }
}

