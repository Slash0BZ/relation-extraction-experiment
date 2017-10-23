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

| CoarseType | FineType                            |
|------------|-------------------------------------|
| PHYS       | Located,Near|
| PART-WHOLE | Geographical,Subsidiary,Artifact|
| PER-SOC    | Lasting-Personal,Business,Family|
| ORG-AFF    | Employment,Ownership,Founder,Student-Alum,Sports-Affiliation,Investor-Shareholder,Membership|
| ART        | User-Owner-Inventor-Manufacturer|
| GEN-AFF    | Citizen-Resident-Religion-Ethnicity,Org-Location|

## Results

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

### Using package to train/test

## Run Tests

## Citation
If you use this tool, please cite the following works.
