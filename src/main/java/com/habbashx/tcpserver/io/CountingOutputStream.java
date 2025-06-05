package com.habbashx.tcpserver.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A decorator implementation of {@link OutputStream} that keeps track of the number of bytes written.
 * This class allows measuring the amount of data written to the underlying {@link OutputStream}.
 *
 * <p>Each write operation updates the internal count of bytes written by this stream.
 * The tracking behavior is transparent to the caller, as this class delegates all
 * actual write operations to the wrapped {@link OutputStream}.
 */
public class CountingOutputStream extends OutputStream {

    /**
     * The underlying {@link OutputStream} that this class decorates.
     * All data written through this stream is forwarded to the wrapped {@link OutputStream}.
     * This object serves as the target for all write operations and ensures
     * data is ultimately written to the intended destination.
     */
    private final OutputStream outputStream;

    /**
     * Tracks the total number of bytes written through the associated OutputStream.
     * This variable is updated atomically to ensure thread-safety in environments
     * where multiple threads may write to the stream concurrently.
     */
    private final AtomicLong bytesWritten = new AtomicLong(0L);

    public CountingOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Writes the specified byte to the underlying {@link OutputStream} and increments
     * the internal byte counter to track the total number of bytes written.
     *
     * @param b the byte to be written
     * @throws IOException if an I/O error occurs while writing the byte to the output stream
     */
    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        bytesWritten.incrementAndGet();
    }

    /**
     * Writes a portion of a byte array to the underlying {@link OutputStream}, starting at the specified offset
     * and writing the specified number of bytes. Updates the internal count of bytes written.
     *
     * @param b   the byte array containing the data to write
     * @param off the start offset in the array from where to begin writing
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs during the write operation
     */
    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        bytesWritten.addAndGet(len);
    }

    /**
     * Flushes the underlying {@link OutputStream}, ensuring that any buffered data is
     * written to its intended destination.
     * <p>
     * This method delegates the flush operation to the wrapped {@link OutputStream}.
     * It is typically used to ensure that all written data is fully propagated
     * and not retained in any intermediate buffers.
     *
     * @throws IOException if an I/O error occurs during the flush operation
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * Closes this {@code CountingOutputStream} and the underlying {@link OutputStream}.
     * <p>
     * This method releases any system resources associated with the stream, ensuring
     * that no further data can be written to the wrapped {@link OutputStream}. After the
     * {@code close} method is invoked, any attempt to write to the stream will result
     * in an {@link IOException}.
     *
     * @throws IOException if an I/O error occurs while closing the underlying {@link OutputStream}
     */
    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    /**
     * Returns the underlying {@link OutputStream} that this class decorates.
     * This method provides access to the original output stream to which all data
     * is written, enabling direct interaction if required by the caller.
     *
     * @return the underlying {@link OutputStream} instance used by this class
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Retrieves the total number of bytes written to the decorated {@link OutputStream}.
     * This method returns the cumulative count of bytes written through the stream
     * since its creation or the last reset, tracked atomically for thread-safety.
     *
     * @return the total number of bytes written as a long value
     */
    public long getBytesWritten() {
        return bytesWritten.get();
    }
}
