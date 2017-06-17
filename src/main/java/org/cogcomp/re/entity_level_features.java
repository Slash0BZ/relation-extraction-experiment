// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4C813A02C044144FA2340484A0D3081D244C2DA41BD0ABE81612CF8CEF951288777751B0BD973FED53A78441760A92A6AE60E3834773E95931DBEDE86E56FC58224385DC1F4C1492A5F8FD7E4C08532C2A7A63A24E295CABADD7B6BFF4474B3E47765D4B07A93FFDB524923B5CBED407036DF78000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class entity_level_features extends Classifier
{
  public entity_level_features()
  {
    containingPackage = "org.cogcomp.re";
    name = "entity_level_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'entity_level_features(Constituent)' defined on line 13 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String featureVec = c.getAttribute("EntityMentionType");
    __id = "" + (featureVec);
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'entity_level_features(Constituent)' defined on line 13 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "entity_level_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof entity_level_features; }
}

