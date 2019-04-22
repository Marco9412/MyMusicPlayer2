package com.panni.mymusicplayer2.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProcessWithIO {

    private List<String> cmds;
    private Map<String,String> env;
    private File path;

    private StringReaderThread outputReader;
    private StringReaderThread errorReader;

    private int exitCode = -1;

    public ProcessWithIO(List<String> cmds, Map<String,String> env, File path) {
        this.cmds = cmds;
        this.env = env;
        this.path = path;
    }

    public int execute() {
        ProcessBuilder pb = new ProcessBuilder(this.cmds).directory(path);
        Map<String,String> pEnv = pb.environment();
        for (String key: env.keySet()) {
            if (key.equals("PATH")) {
                pEnv.put("PATH", env.get("PATH") + ":" + pEnv.get("PATH"));
            } else {
                pEnv.put(key, env.get(key));
            }
        }

        try {
            Process p = pb.start();

            outputReader = new StringReaderThread(p.getInputStream());
            errorReader = new StringReaderThread(p.getErrorStream());

            outputReader.start();
            errorReader.start();

            exitCode = p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
        }

        return exitCode;
    }

    public String getOutputString() {
        return outputReader.getReadData();
    }

    public String getErrorString() {
        return errorReader.getReadData();
    }
}
