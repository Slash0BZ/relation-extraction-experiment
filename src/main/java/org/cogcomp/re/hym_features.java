// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057EC1BE62013C0600E751F259C112E4CC0472A54C0D1A2DD30457C5D7069EE298C6744FA51FEE83D234716A449FFBCF6B0573E31C97E9E360AECA8092EE254536B241D0AF00BD52CF04B527E426BFF33FAF562D5F694047028768847974203C60ED85D048CC5ABF6E4467893394C97ABB6CED5D0E578CE339DDDA63E7C6BE7A649FB9A6B0F2611C7397FE8502B31CA7B0FB4CB119D94B3BA1D4D4A26D461FAA1326EA1EE954D038B05A8A408DA97C7A00FF358530EB4A27C19EEFED25F92E76D1D758BED004D8CADA182100000

package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;


public class hym_features extends Classifier
{
  SupportFeatureExtractor sfe = null;
  public hym_features()
  {
    containingPackage = "org.cogcomp.re";
    name = "hym_features";
    sfe = new SupportFeatureExtractor();
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'hym_features(Constituent)' defined on line 38 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    List ret = sfe.getHypernymFeature(c);
    for (int i = 0; i < ret.size(); i++)
    {
      Pair p = (Pair) ret.get(i);
      __id = "" + ((String) p.getFirst());
      __value = (Double) p.getSecond();
      if (__value > 0.01) {
        __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'hym_features(Constituent)' defined on line 38 of PMS_sup.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "hym_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof hym_features; }
}

