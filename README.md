# Relation Extraction

See the [parent package reademe](https://github.com/CogComp/cogcomp-nlp/blob/master/README.md) first

## Introduction

A relation is a relationship between a pair of entity mentions.
Currently we only detect relation pairs within the same sentence.
There are often many relations within a sentence. 
For example, In sentence 

`Coalition forces in Iraq have captured a member of a terrorist group with links to al Qaeda .`

There are the following relations:

```
Located_In([forces], [Iraq])
Employment([Coalition], [forces])
Membership([member], [terrorist group])
Affiliation([terrorist group], [al Qaeda])
```

Knowing these relations is helpful for many tasks in NLP.

This application uses [Mention Detection](https://github.com/CogComp/cogcomp-nlp/blob/master/md/README.md) to identify mentions first, and uses a model trained with supervised learning and feature engineering on ACE2005 data to identify relations among the mentions detected. 
Since the model is built on ACE2005, the model identifes the following types:

| Coarse Type | Fine Type                            |
|------------|-------------------------------------|
| PHYS       | Located,Near|
| PART-WHOLE | Geographical,Subsidiary,Artifact|
| PER-SOC    | Lasting-Personal,Business,Family|
| ORG-AFF    | Employment,Ownership,Founder,Student-Alum,Sports-Affiliation,Investor-Shareholder,Membership|
| ART        | User-Owner-Inventor-Manufacturer|
| GEN-AFF    | Citizen-Resident-Religion-Ethnicity,Org-Location|

## Results

We test results on both Coarse Type and Fine Type, and on both gold mention data (i.e. the mentions are given) and predicted mention data (i.e. use MD to detect mentions).

## Usage

### Install with Maven

If you want to use the illinois-relation-extraction package independently, you can add a maven dependency in your pom.xml. Please replace the `VERSION` with the latest version of the parent package.

```xml
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-relation-extraction</artifactId>
    <version>VERSION</version>
</dependency>
```

### Using Annotator

Using the annotator `RelationAnnotator()` is the preferred way to use relation extraction package. This annotator annotates mentions, and then annotate relations. If you have a pre-defined mention set, please refer to [Using Relation Classifier Only](#using-relation-classifier-only)

Using the annotator is easy.

```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import org.cogcomp.re.RelationAnnotator;

import java.io.IOException;
import java.util.List;

public class app
{
    public static void main( String[] args ) throws IOException, AnnotatorException
    {
        String text = "Good afternoon, gentlemen. I am a HAL-9000 "
                + "computer. I was born in Urbana, Il. in 1992";

        String corpus = "2001_ODYSSEY";
        String textId = "001";

        // Create a TextAnnotation From Text
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotation(corpus, textId, text);
        
        RelationAnnotator relationAnnotator = new RelationAnnotator();
        relationAnnotator.addView(ta);

        View mentionView = ta.getView(ViewNames.MENTION);
        List<Constituent> predictedMentions = mentionView.getConstituents();
        List<Relation> predictedRelations = mentionView.getRelations();
        
    }
}
```

As the sample indicates, the annotator annotates the view `ViewNames.MENTION`, which contains both predicted mentions and predicted relations. Please refer to the structure of [Relation](https://github.com/CogComp/cogcomp-nlp/blob/master/core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/Relation.java).

### Using Relation Classifier Only

## Run Tests

### Data Pre-process

There is a handy `IOHelper` class which pre-annotates a large corpus and save them into single files. 

### Custom training/testing

## Citation
If you use this tool, please cite the following works.

```
@inproceedings{ChanRo10,
    author = {Y. Chan and D. Roth},
    title = {Exploiting Background Knowledge for Relation Extraction},
    booktitle = {COLING},
    month = {8},
    year = {2010},
    address = {Beijing, China},
    url = "http://cogcomp.org/papers/ChanRo10.pdf",
    funding = {MR},
    projects = {NLP, IE},
    comment = {Relation extraction, background knowledge, constraints, information extraction},
}
@inproceedings{ChanRo11,
    author = {Y. Chan and D. Roth},
    title = {Exploiting Syntactico-Semantic Structures for Relation Extraction},
    booktitle = {ACL},
    year = {2011},
    address = {Portland, Oregon},
    url = "http://cogcomp.org/papers/ChanRo11.pdf",
    funding = {MR},
    projects = {NLP, IE},
}
```
