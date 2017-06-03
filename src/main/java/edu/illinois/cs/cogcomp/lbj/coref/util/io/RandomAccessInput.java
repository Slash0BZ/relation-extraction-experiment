package edu.illinois.cs.cogcomp.lbj.coref.util.io;

import java.io.IOException;

/**
 * Interface for reading bytes in an arbitrary order.
 * Intended as a partial drop-in replacement for read-only RandomAccessFile 
 * objects, although non-file data sources may be used.
 */
public interface RandomAccessInput extends RandomAccessData {
    
    /**
     * Reads a byte of data from the cursor position.
     * The byte is returned as an integer in the inclusive range 0-255.
     * This method blocks if no input is available.
     * This method behaves just as {@code InputStream}'s read method.
     * @return The next byte of data, or -1 if the end of file has been reached.
     * @throws IOException if an I/O error occurs, but NOT if end-of-file.
     */
    public int read() throws IOException;
}

