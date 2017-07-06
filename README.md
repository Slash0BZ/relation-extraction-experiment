# relation-extraction-experiment

## Requirements

- Maven

- JDK 8

- 16 GBs or more of RAM

## Install and run

This current version only supports experimenting relation-extraction with gold ACE documents. Other tests need specific configurations.

 - Clone the code

 - `cd` into the working folder

 - `wget http://ddns.meiguo.work/public/relation-extraction-supp.zip`
 
 - `unzip relation-extraction-supp.zip`
 
 - `mvn lbjava:generate`
 
 - `mvn install`
 
 - `mvn exec:java -Dexec.mainClass="org.cogcomp.ACERelationTester"`
