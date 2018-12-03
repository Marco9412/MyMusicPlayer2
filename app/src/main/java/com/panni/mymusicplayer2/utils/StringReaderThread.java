package com.panni.mymusicplayer2.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class StringReaderThread extends Thread {

    private StringWriter buffer = new StringWriter(256);
    private InputStream inputStream;

    public StringReaderThread(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            br.close();
        } catch (IOException ex) {
            // TODO
        }
    }

    public String getReadData() {
        try {
            this.join();
        } catch (InterruptedException e) {
        }
        return buffer.toString();
    }
}
