package com.panni.mymusicplayer2.controller.localsongs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by marco on 02/08/16.
 * Implementation taken from BufferedOutputStream!
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

    private byte[] buffer;
    private int count;

    public BufferedRandomAccessFile(File file) throws FileNotFoundException {
        this(file, "rw");
    }

    public BufferedRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);

        if (65536 <= 0) throw new IllegalArgumentException("BufferSize can't be <= 0!");
        buffer = new byte[65536];
        count = 0;
    }

    private void flush() throws IOException {
        if (count > 0) {
            super.write(buffer, 0, count);
            count = 0;
        }
    }

    @Override
    public long length() throws IOException {
        return super.length() + count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len >= buffer.length) {
            /* If the request length exceeds the size of the output buffer,
               flush the output buffer and then write the data directly.
               In this way buffered streams will cascade harmlessly. */
            flush();
            super.write(b, off, len);
            return;
        }
        if (len > buffer.length - count) {
            flush();
        }
        System.arraycopy(b, off, buffer, count, len);
        count += len;
    }

    @Override
    public void seek(long offset) throws IOException {
        flush();
        super.seek(offset);
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }
}
