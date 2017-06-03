package edu.illinois.cs.cogcomp.lbj.coref.util.io;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

/**
 * Loads and stores a buffer of bytes from a source such as
 * a {@code InputStream}, and provides methods to read
 * in a random access fashion.
 * Suitable as a drop in replacement for a read-only RandomAccessFile,
 * with any data source.
 */
public class RandomAccessByteBuffer implements RandomAccessInput {
    protected long cursor = 0;
    protected byte[] bytes = new byte[0];

    /**
     * Creates a data-buffer by reading all input from the input
     * stream, and storing it in a byte buffer.
     * Data is read until -1, the end of the stream, or an IOException
     * is reached.
     * This constructor blocks until all data has been read from the stream.
     * @param in The stream from which to read the data.
     */
    public RandomAccessByteBuffer(InputStream in) {
	List<Byte> list = new ArrayList<Byte>();
	try {
	    int c = in.read();
	    while (c != -1) {
		list.add((byte)c);
		c = in.read();
	    }
	} catch (IOException e) {
	    //Do nothing but stop reading.
	}
	//Finally:
	bytes = new byte[list.size()];
	for (int i = 0, n = list.size(); i < n; ++i) {
	    bytes[i] = list.get(i);
	}
	if (bytes.length > 0)
	    cursor = 0;
    }

    public long getFilePointer() throws IOException {
	return cursor;
    }

    public void seek(long pos) throws IOException {
	/* Defer changing the size of the byte array until write,
	 * as this class has no write methods and thus will not allow
	 * writing without extension.
	 */
	cursor = pos;
    }

    public long length() throws IOException {
	return bytes.length;
    }

    public int read() {
	if (cursor < 0 || cursor >= bytes.length)
	    return -1;
	int result = bytes[(int)cursor];
	cursor++;
	return result;
    }
}
