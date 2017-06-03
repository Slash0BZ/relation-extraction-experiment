/**
 * 
 */
package edu.illinois.cs.cogcomp.lbj.coref.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Eric
 *
 */
public class Debug {
    public Debug(String msg) {
	System.err.println(msg);
    }

    public static void p(String msg) {
	System.err.println(msg);
    }
    
    public static void adjust() throws IOException {
		ArrayList<String> lines = IOManager.readLines("files.txt");
		ArrayList<String> files = new ArrayList<String>();
		for (String line : lines) {
			line = line.split("\t")[0];
			if (!files.contains(line)) {
				files.add(line);
			}
		}
		BufferedWriter bw = IOManager.openWriter("files_adjusted.txt");
		for (String file : files) {
			bw.write(file + "\n");
		}
		bw.close();
		
		ArrayList<String> content = IOManager.readLines("conll12_coref_predmention.pred");
		ArrayList<String> content_new = IOManager.readLines("conll12_coref_goldmention.pred");
		for (String file : files) {
			file = "#begin document " + file;
			int p = content.indexOf(file);
			int q = content_new.indexOf(file);
			if (p == -1 || q == -1) {
				System.out.println("Error1: " + file);
				System.exit(-1);
			}
			int index = 1;
			while (!content.get(p + index).startsWith("#end document")) {
				content.set(p + index, content_new.get(q + index));
				index++;
			}
			if (!content_new.get(q + index).startsWith("#end document")) {
				System.out.println("Error2: " + file);
				System.exit(-1);
			}
		}
		bw = IOManager.openWriter("conll12_coref_predmention_new.pred");
		for (String line : content) {
			bw.write(line + "\n");
		}
		bw.close();
	}
}
