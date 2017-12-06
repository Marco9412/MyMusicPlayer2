package com.panni.mymusicplayer2.logger;

import android.os.Environment;
import android.util.Log;

import com.panni.mymusicplayer2.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by marco on 01/07/16.
 */
public class MyErrorLogger {

    private static MyErrorLogger instance;

    public static MyErrorLogger getInstance() {
        if (instance == null) instance = new MyErrorLogger();
        return instance;
    }

    public static void closeLog() {
        if (instance != null) instance.close();
    }

    private PrintStream output;
    private PrintStream originalSysErr;

    private boolean opened;

    private MyErrorLogger() {
        File logfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MyMusicPlayerLog.txt");
        try {
            originalSysErr = System.err;

            output = new PrintStream(new FileOutputStream(logfile)) {
                @Override
                public PrintStream append(CharSequence charSequence) {
                    originalSysErr.append(charSequence);
                    return super.append(charSequence);
                }

                @Override
                public PrintStream append(char c) {
                    originalSysErr.append(c);
                    return super.append(c);
                }

                @Override
                public PrintStream append(CharSequence charSequence, int start, int end) {
                    originalSysErr.append(charSequence, start, end);
                    return super.append(charSequence, start, end);
                }
            };
            output.append("Logger started ").append(Utils.getCurrentInstance()).append("\n");
            System.setErr(output);
            opened = true;
        } catch (FileNotFoundException e) {
            Log.e("MyErrorLogger", "Cannot write to log file! Logging disabled!");
            opened = false;
        }
    }

    private void close() {
        if (output != null && opened) {
            System.setErr(originalSysErr);

            output.append("Logger closed ").append(Utils.getCurrentInstance()).append("\n");
            output.flush();
            output.close();

            opened = false;
        }
    }

    public void log(String message) {
        if (output != null && opened)
            output.append(Utils.getCurrentInstance()).append(":").append(message).append("\n");
    }
}
