package com.panni.mymusicplayer2.youtubedl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.panni.mymusicplayer2.utils.ProcessWithIO;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class NativePythonLinkGetter extends LinkGetter {

    @SuppressLint("SdCardPath")
    final private static String ROOTFS_PATH = "/data/data/com.panni.mymusicplayer2/files";
    final private static String ROOTFS_HOME = ROOTFS_PATH + "/home";
    final private static String ROOTFS_PREFIX = ROOTFS_PATH + "/usr";

    final private static String KEY_ROOTFS_READY_NATIVE = "com.panni.mymusicplayer2.keyrootfs_ready_native";

    @Override
    public boolean setup(Context context) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ROOTFS_READY_NATIVE, false)) {
            if (new RootFsExtractor().extract(context)) {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putBoolean(KEY_ROOTFS_READY_NATIVE, true)
                        .apply();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean available(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ROOTFS_READY_NATIVE, false);
    }

    @Override
    public String getLink(String url) {
        LinkedList<String> cmds = new LinkedList<>();
        cmds.add(ROOTFS_PREFIX + "/bin/sh");
        cmds.add("-c");
        cmds.add(ROOTFS_PREFIX + "/bin/python3 -m youtube_dl --format 'bestaudio/best' --no-check-certificate -g '" + url + "'");

        HashMap<String,String> env = new HashMap<>();
        env.put("LD_LIBRARY_PATH", ROOTFS_PREFIX + "/lib");
        env.put("PREFIX", ROOTFS_PREFIX);
        env.put("HOME", ROOTFS_HOME);
        env.put("PATH", ROOTFS_PREFIX + "/bin:" + ROOTFS_PREFIX + "/bin/applets");
        env.put("LD_PRELOAD", ROOTFS_PREFIX + "/lib/libtermux-exec.so");

        ProcessWithIO p = new ProcessWithIO(cmds, env, new File(ROOTFS_HOME));
        int exitCode = p.execute();

        if (exitCode != 0) {
            throw new RuntimeException(String.format("Unable to extract media url from video $url. \n" +
                            "Output data: %s" +
                            "Error data: %s\nExit code: %d", p.getOutputString(), p.getErrorString(), exitCode));
        }

        return p.getOutputString();
    }

    @Override
    public boolean updateModule() {
        LinkedList<String> cmds = new LinkedList<>();
        cmds.add(ROOTFS_PREFIX + "/bin/sh");
        cmds.add("-c");
        cmds.add(ROOTFS_PREFIX + "/bin/python3 -m pip install --upgrade youtube_dl");

        HashMap<String,String> env = new HashMap<>();
        env.put("LD_LIBRARY_PATH", ROOTFS_PREFIX + "/lib");
        env.put("PREFIX", ROOTFS_PREFIX);
        env.put("HOME", ROOTFS_HOME);
        env.put("PATH", ROOTFS_PREFIX + "/bin:" + ROOTFS_PREFIX + "/bin/applets");
        env.put("LD_PRELOAD", ROOTFS_PREFIX + "/lib/libtermux-exec.so");

        ProcessWithIO p = new ProcessWithIO(cmds, env, new File(ROOTFS_HOME));
        int exitCode = p.execute();

        if (exitCode != 0) {
            throw new RuntimeException(String.format("Unable to update youtube-dl module. \n" +
                            "Output data: %s" +
                            "Error data: %s\nExit code: %d", p.getOutputString(), p.getErrorString(), exitCode));
        }

        return true;
    }
}
