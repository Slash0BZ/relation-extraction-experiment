// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055DC14A03804C0581EBA461350B0671DD7F41E504684FD04023E494230E1F51928ABDF8F97FEB2EC680C38C0A934AD233B66779220B49ED3C4F762B72D7E54A85583BEC5093AB1C742E6AAD8F86CF2CE16D93AFD2DA85E87835205F7BFE3E24B10EEDCE90829000000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import java.lang.*;
import java.util.List;


public class binary_relation_classifier$$1 extends Classifier
{
  private static final lexical_features __lexical_features = new lexical_features();
  private static final collocations_features __collocations_features = new collocations_features();
  private static final structual_features __structual_features = new structual_features();
  private static final mention_features __mention_features = new mention_features();
  private static final template_features __template_features = new template_features();

  public binary_relation_classifier$$1()
  {
    containingPackage = "org.cogcomp.re";
    name = "binary_relation_classifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'binary_relation_classifier$$1(Relation)' defined on line 144 of re.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__lexical_features.classify(__example));
    __result.addFeatures(__collocations_features.classify(__example));
    __result.addFeatures(__structual_features.classify(__example));
    __result.addFeatures(__mention_features.classify(__example));
    __result.addFeatures(__template_features.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'binary_relation_classifier$$1(Relation)' defined on line 144 of re.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "binary_relation_classifier$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof binary_relation_classifier$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__lexical_features);
    result.add(__collocations_features);
    result.add(__structual_features);
    result.add(__mention_features);
    result.add(__template_features);
    return result;
  }
}

