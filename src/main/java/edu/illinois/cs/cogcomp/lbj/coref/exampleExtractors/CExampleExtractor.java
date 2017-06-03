package edu.illinois.cs.cogcomp.lbj.coref.exampleExtractors;

import edu.illinois.cs.cogcomp.lbj.coref.ir.examples.CExample;

/** 
 * This interface specifies a type of ExampleExtractor
 * that extracts {@code CExample}s from {@code Doc}s.
 * Make sure the desired document is set (using a constructor or {@code setDoc},
 * and then call {@code getExamples()}
 * @author Eric Bengtson
 */
public interface CExampleExtractor extends DocExampleExtractor<CExample> {
}
