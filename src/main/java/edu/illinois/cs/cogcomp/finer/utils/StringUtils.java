package edu.illinois.cs.cogcomp.finer.utils;

/**
 * Created by haowu4 on 1/10/17.
 */
public class StringUtils {

    public static String pad(String str, int size, char padChar) {
        StringBuffer padded = new StringBuffer(str);
        while (padded.length() < size) {
            padded.append(padChar);
        }
        return padded.toString();
    }
}
