/*
 * Created on Dec 6, 2005
 *
 */
package edu.illinois.cs.cogcomp.lbj.coref.util.io;

import java.net.URL;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
/**
 * @author Eric
 */
public class myIO {

    /**
     * 
     */
    public myIO() {
	super();
	// TODO Auto-generated constructor stub
    }

    public static String readAll(URL url) {
	try {
	    return readAll(new File(url.toURI()));
	} catch (Exception e) {
	    System.err.println("Cannot construct reference to file.");
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Reads all the contents of the file.
     * Assumes system-default character encoding.
     * This method is not static because getResource does not
     * find the resources when used in a static context.
     * @param filename The name of a file, relative to the classpath.
     * @return The contents of the file, or null on exception.
     */
    public String readAll(String filename) {
	if (!filename.startsWith("/")) filename = "/" + filename;
	try {
	    InputStream s = this.getClass().getResourceAsStream(filename);
	    if (s == null) {
		System.err.println("Resource " + filename
		 + " could not be found.");
		return null;
	    }
	    return readAll(s);
	} catch (Exception e) {
	    System.err.println("Could not load file " + filename + " because:");
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Reads all the contents of the file.
     * Assumes system-default character encoding.
     * @param f a File
     * @return The contents of the file, or null on exception.
     */
    public static String readAll(File f) {
	InputStream s = null;
	try {
	    s = new FileInputStream(f);
	    return readAll(s);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    System.err.println("File "+f+" not found.");
	    return null;
	} finally {
	    if (s != null) {
		try {
		    s.close(); //Was not closed by readAll.
		} catch (IOException e) {
		    //If we cannot close the stream, so be it.
		}
	    }
	}
    }

    /**
     * Reads all the contents of the file.
     * Assumes system-default character encoding.
     * Does not close stream.
     * @param in a stream view of the data.
     * @return The contents of the file, or null on exception.
     */
    public static String readAll(InputStream in) {
	try {
	    StringBuffer buf = new StringBuffer();
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    
	    int c;
	    while ( (c = r.read()) != -1 ) {
		buf.append((char)c);
	    }
	    return buf.toString();			
	} catch (IOException e) {
	    e.printStackTrace();
	    System.err.println("Problem reading file");
	    return null;
	}
    }

    public static void writeLines(String filename, List<String> lines)
//    public static void writeLines(String filename, Iterable<String> lines)
    throws IOException {
	FileOutputStream fout = new FileOutputStream(filename);
	DataOutputStream dout = new DataOutputStream(fout);
	for (String s : lines) {
	    dout.writeBytes(s + "\n");
	}
	dout.close();
	fout.close();
    }

    /**
     * @param filename
     * @param objects must be serializable
     * @throws IOException
     */
    public static void writeList(String filename, List<Object> objects) 
    throws IOException {
	FileOutputStream fout = new FileOutputStream(filename);
	ObjectOutputStream oOut = new ObjectOutputStream(fout);
	oOut.writeObject(objects);		
	oOut.close();
	fout.close();
    }

    /**
     * @param filename containing Serialized List of objects.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static List<Object> readList(String filename) 
    throws IOException, ClassNotFoundException {
	FileInputStream fin = new FileInputStream(filename);
	ObjectInputStream oIn = new ObjectInputStream(fin);
	@SuppressWarnings("unchecked")
	List<Object> objects = (List<Object>) oIn.readObject();
	oIn.close();
	fin.close();
	return objects;
    }

    //FIXME: Decide whether to throw exception or return null or empty.
    /**
     * Read the lines from the file.
     * This method is not static because getResource does not work
     * in a static context in an applet.
     * @param filename relative to classpath.
     * @return List of strings NOT containing eol characters.
     */
    public List<String> readLines(String filename) {
	try {
	    if (!filename.startsWith("/")) filename = "/" + filename;
	    InputStream in = this.getClass().getResourceAsStream(filename);
	    return readLines(in);
	} catch (Exception e) {
	    System.err.println("Cannot open file " + filename + ".");
	    e.printStackTrace();
	    return null;
	}
    }
    public static List<String> readLines(InputStream in) {
	BufferedReader bin = new BufferedReader(new InputStreamReader(in));

	List<String> lines = new ArrayList<String>();
	String line;
	do {
	    try {
		line = bin.readLine();
	    } catch (IOException e) {
		System.err.println("Problem reading line");
		e.printStackTrace();
		return null;
	    }
	    if (line == null)
		break;
	    lines.add(line);
	} while (line != null);
	return lines;
    }

    /** Finds a file that is in the classpath.
     * @param filename The name of the file, relative to a directory in the
     classpath.
     */
    public String findFile(String filename) 
    throws FileNotFoundException {
	try {
	    if (!filename.startsWith("/"))
		filename = "/" + filename;
	    URL resourceURL = getClass().getResource(filename);
	    if (resourceURL == null)
		throw new FileNotFoundException(filename);
//	    String fqfn = resourceURL.getFile();
	    String fqfn = resourceURL.toURI().getPath();
	    return fqfn;
	} catch (Exception e) {
	    e.printStackTrace(); //TODO: Remove
	    throw new FileNotFoundException(filename);
	}
    }
    
    public static List<Doc> readSerializedDocs(String fileName) throws Exception {
		FileInputStream fileIn = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		List<Doc> docs = (List<Doc>) in.readObject();
		//System.out.println("Done reading the data from " + fileName);
		in.close();
		fileIn.close();
		return docs;
	}
}
