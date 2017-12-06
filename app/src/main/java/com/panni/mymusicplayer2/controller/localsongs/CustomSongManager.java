package com.panni.mymusicplayer2.controller.localsongs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by marco on 05/08/16.
 */
public class CustomSongManager {

    final public static String CUSTOM_SONG_FILE = "customSongs.txt";

    private static CustomSongManager instance;

    public static CustomSongManager getInstance() {
        return instance;
    }

    public static CustomSongManager getInstance(Context context) {
        if (instance == null) instance = new CustomSongManager(context);
        return instance;
    }

    private Context context;
    private ConcurrentSkipListSet<MyQueueItem> customSongs;
    private boolean loaded;

    private CustomSongManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.customSongs = new ConcurrentSkipListSet<>();
        this.loaded = false;
        loadCustomSongs();
    }

    private void loadCustomSongs() {
        synchronized (this) {
            try {
                File customSongsFile = new File(context.getFilesDir(), CUSTOM_SONG_FILE);
                if (customSongsFile.exists()) {
                    BufferedReader input = new BufferedReader(new FileReader(customSongsFile));
                    MyQueueItem tmp;
                    while ((tmp = MyQueueItem.fromFileFormat(input)) != null)
                        this.customSongs.add(tmp);
                    input.close();

                    //Collections.sort(this.customSongs);
                }
                this.loaded = true;
                Log.d("CustomSongManager", "Loaded " + this.customSongs.size() + " custom songs");
            } catch (IOException ex) {
                Log.d("CustomSongManager", "Cannot load custom songs! Exception", ex);
                ex.printStackTrace();
            }
        }
    }

    private void writeCustomSongs() {
        synchronized (this) {
            if (!loaded) return;
            try {
                File customSongsFile = new File(context.getFilesDir(), CUSTOM_SONG_FILE);
                customSongsFile.delete();

                PrintWriter pw = new PrintWriter(new FileWriter(customSongsFile), true);
                for (MyQueueItem item : this.customSongs)
                    pw.print(item.toFileFormat());
                pw.close();

                Log.d("CustomSongManager", "Written " + this.customSongs.size() + " custom songs");
            } catch (IOException ex) {
                Log.d("CustomSongManager", "Cannot save custom songs! Exception", ex);
                ex.printStackTrace();
            }
        }
    }

//    private boolean appendCustomSong(CustomQueueItem item) {
//        try {
//            File customSongsFile = new File(context.getFilesDir(), CUSTOM_SONG_FILE);
//
//            PrintWriter pw = new PrintWriter(new FileWriter(customSongsFile, true), true);
//            pw.print(item.toFileFormat());
//            pw.close();
//
//            Log.d("CustomSongManager", "Custom song " + item.toString() + " is saved!");
//            return true;
//        } catch (IOException ex) {
//            Log.d("CustomSongManager", "Cannot append " + item.toString(), ex);
//            ex.printStackTrace();
//        }
//        return false;
//    }

    public void addCustomSong(CustomQueueItem item) {
        maybeLoad();
        if (customSongs.contains(item)) return;

        customSongs.add(item);
        //Collections.sort(customSongs);
        writeCustomSongs(); // Always rewrite entire file (keep data sorted)
    }

    public void deleteCustomSong(CustomQueueItem item) {
        maybeLoad();
        if (!customSongs.contains(item)) return;

        customSongs.remove(item);
        //Collections.sort(customSongs); // Useful?
        writeCustomSongs();
    }

    public MyQueueItem[] getCustomSongs() {
        maybeLoad();
        MyQueueItem[] res = new MyQueueItem[customSongs.size()];
        customSongs.toArray(res);
        return res;
    }

    /**
     * Loads custom songs if they aren't in memory
     */
    private void maybeLoad() {
        if (loaded) return;
        loadCustomSongs();
    }

    public void trimMemory() {
        writeCustomSongs();
        customSongs.clear();
        loaded = false;
    }
}
