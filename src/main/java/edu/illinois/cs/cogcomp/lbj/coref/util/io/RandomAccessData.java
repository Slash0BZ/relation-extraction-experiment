package edu.illinois.cs.cogcomp.lbj.coref.util.io;

import java.io.IOException;

/**
 * Interface for the random access related methods of RandomAccessFile.
 * Names relating to files are preserved to allow drop-in use.
 */
public interface RandomAccessData {
    /**
     * Returns the current offset in the data, in bytes.
     * @return The offset in bytes relative to the beginning of the data.
     * @throws IOException if an I/O error is encountered.
     */
    public long getFilePointer() throws IOException;

    /**
     * Sets the offset in the data, in bytes.
     * The offset may be set beyond the end of the data,
     * and this does not change the data length.
     * Only writing after setting the pointer beyond the end of the data
     * will change the data size.
     * @param pos The desired offset, in bytes.
     * @throws IOException if {@code pos} is negative, or if an I/O error
     * is encountered.
     */
    public void seek(long pos) throws IOException;

    /**
     * Gets the number of bytes of data.
     * @return The number of bytes of data.
     * @throws IOException if an I/O error is encountered.
     */
    public long length() throws IOException;

    //For use in writable version only:
    /**
     * Sets the number of bytes of data.
     * If the new length is smaller than the current length,
     * data will be discarded (from the end), and the offset (file pointer)
     * will be equal to {@code newLength}.
     * If the new length is larger than the current length,
     * the data will be enlarged (at the end)
     * and the new data will be undefined.
     * @param newLength The desired size of the data.
     * @throws IOException if an I/O error is encountered.
     */
    //public void setLength(long newLength) throws IOException;
}
