// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D80914A038030154FA23BC40A5F206BBB69224715071EA3D83A40CE446232A45DBB731735A0D58BDFFFDC78975BB0691507E6C4710F002C30EA0F47EB0AB83B2AA2FC5F5723FAEB340C943C9E8033492CE8A58136707E8176D2A42059EC1C530AE66ED82288C125377F322B5310312149ECCA7213E828A430A16950E7F0A41FDFE56F1CE9CF04B70D28A9E0DA06DFF7B57C7C2A98189639093CA08159C73DDC05A87DF00F8968ADD53100000

package edu.illinois.cs.cogcomp.lbj.coref.learned;

import edu.illinois.cs.cogcomp.lbj.coref.features.Gazetteers;
import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.WordExample;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscreteFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;


public class bioInDict extends Classifier
{
  public bioInDict()
  {
    containingPackage = "edu.illinois.cs.cogcomp.lbj.coref.learned";
    name = "bioInDict";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbj.coref.ir.examples.WordExample"; }
  public String getOutputType() { return "discrete"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    String result = discreteValue(__example);
    return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof WordExample))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bioInDict(WordExample)' defined on line 419 of mentionDetectionFeatures.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    String __cachedValue = _discreteValue(__example);

    if (valueIndexOf(__cachedValue) == -1)
    {
      System.err.println("Classifier 'bioInDict' defined on line 419 of mentionDetectionFeatures.lbj produced '" + __cachedValue  + "' as a feature value, which is not allowable.");
      System.exit(1);
    }

    return __cachedValue;
  }

  private String _discreteValue(Object __example)
  {
    WordExample ex = (WordExample) __example;

    String word = ex.getWord();
    if (Gazetteers.getLowercaseWords().contains(word) || Gazetteers.getStopWords().contains(word) || Gazetteers.getPronouns().contains(word) || Gazetteers.getInflectedWords().contains(word))
    {
      return "true";
    }
    else
    {
      return "false";
    }
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof WordExample[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bioInDict(WordExample)' defined on line 419 of mentionDetectionFeatures.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bioInDict".hashCode(); }
  public boolean equals(Object o) { return o instanceof bioInDict; }
}

