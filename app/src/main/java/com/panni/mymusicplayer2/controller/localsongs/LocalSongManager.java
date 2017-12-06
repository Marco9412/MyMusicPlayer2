package com.panni.mymusicplayer2.controller.localsongs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.settings.Settings;
import com.panni.mymusicplayer2.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

import objects.Song;
import pymusicmanagerconnector.PyMusicManagerConnector;

/**
 * Created by marco on 17/06/16.
 */
public class LocalSongManager {

    final public static String LOCAL_SONGS_FILE = "localSongs.txt";

    private static LocalSongManager instance;

    public static LocalSongManager getInstance() {
        return instance;
    }

    public static LocalSongManager getInstance(Context context) {
        if (instance == null) instance = new LocalSongManager(context);
        return instance;
    }

    public static void deleteInstance() {
        if (instance != null) instance.delete();
        instance = null;
    }

    private Context context;

    //private ArrayList<MyQueueItem> localSongs;
    //private TreeSet<MyQueueItem> localSongs;
    private ConcurrentSkipListSet<MyQueueItem> localSongs;
    private boolean loaded;

    /// The songs files which have been opened
    private File pendingSong;

    // TODO if metadata updates on server?

    private LocalSongManager(Context context) {
        this.context = context.getApplicationContext();
        //localSongs = new ArrayList<>(10);
        localSongs = new ConcurrentSkipListSet<>();
        loaded = false;
        loadCustomSongs();
    }

    private void delete() {
        clearPendingSong();
    }

//    public void saveLocalSong(Song song, byte[] data) {
//        try {
//            File outputFile = Utils.getSongFile(song);
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
//            out.write(data, 0, data.length);
//            out.flush();
//            out.close();
//
//            this.dbHelper.addLocalSong(song);
//            this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
//        } catch (IOException ex) {
//            //MyErrorLogger.getInstance().log("LocalSongManager", ex.getMessage());
//            ex.printStackTrace();
//        }
//    }

    private void loadCustomSongs() {
        synchronized (this) {
            try {
                File localSongsFile = new File(context.getFilesDir(), LOCAL_SONGS_FILE);
                if (localSongsFile.exists()) {
                    BufferedReader input = new BufferedReader(new FileReader(localSongsFile));
                    MyQueueItem tmp;
                    while ((tmp = MyQueueItem.fromFileFormat(input)) != null)
                        this.localSongs.add(tmp);
                    input.close();
                    //Collections.sort(this.localSongs);
                }

                loaded = true;
                Log.d("LocalSongManager", "Loaded " + this.localSongs.size() + " local songs");
            } catch (IOException ex) {
                Log.d("LocalSongManager", "Cannot load local songs! Exception", ex);
                ex.printStackTrace();
            }
        }
    }

    private void writeLocalSongs() {
        synchronized (this) {
            if (!loaded) return;
            try {
                File customSongsFile = new File(context.getFilesDir(), LOCAL_SONGS_FILE);
                customSongsFile.delete();

                PrintWriter pw = new PrintWriter(new FileWriter(customSongsFile), true);
                for (MyQueueItem item : this.localSongs)
                    pw.print(item.toFileFormat());
                pw.close();

                Log.d("LocalSongManager", "Written " + this.localSongs.size() + " local songs");
            } catch (IOException ex) {
                Log.d("LocalSongManager", "Cannot save local songs! Exception", ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Deletes LocalSong file and removes it from LocalSongs list
     * (used in LocalSongListFragment)
     * @param song
     */
    public void removeLocalSong(Song song) {
        maybeLoad();

        MyQueueItem item = MyQueueItem.create(song);
        if (!localSongs.contains(item)) return;

        File outputFile = Utils.getSongFile(song);
        if (outputFile.delete() || !outputFile.exists()) {
            localSongs.remove(item);
            //Collections.sort(localSongs);
            writeLocalSongs();
        }
    }

    /**
     * Tells if this song is already saved locally.
     * (used in SongDownloaderService)
     * @param song
     * @return
     */
    public boolean localSongExists(Song song) {
        maybeLoad();

        return this.localSongs.contains(MyQueueItem.create(song));
    }

    /**
     * Adds this song to LocalSongs list (this song is already downloaded)
     * @param song
     */
    public void addExistentLocalSong(Song song) {
        maybeLoad();

        MyQueueItem item = MyQueueItem.create(song);
        if (localSongs.contains(item)) return;

        localSongs.add(item);
        //Collections.sort(localSongs);
        writeLocalSongs();
    }

    public void addExistentLocalSongs(Song... songs) {
        maybeLoad();

        for (Song s: songs) {
            MyQueueItem item = MyQueueItem.create(s);
            if (localSongs.contains(item)) continue;

            localSongs.add(item);
            this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(Utils.getSongFile(s))));
        }

        writeLocalSongs();
    }

    // *********************************************************************************************
    /*
        Smart cache methods!

        Ways to save song with smart cache! (one song at time!)
          when randomaccessfile is created -> setPendingSong()
            if song is downloaded
                -> saveLocalSong(song)
            else
                -> nothing -> file will be deleted on next setPendingSong()
                    (or when app closes)...
     */

    /**
     * Sets a song in downloading state (and playing).
     * @param song
     */
    public void setPendingSong(Song song) {
        clearPendingSong();

        this.pendingSong = Utils.getSongFile(song);
        Log.i("LocalSongManager", song.getName() + " is downloading");
    }

    /**
     * Sets a song in downloaded state and adds it to LocalSongs list
     * @param song
     */
    public void saveLocalSong(Song song) {
        this.addExistentLocalSong(song);

        this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(Utils.getSongFile(song))));

        Log.i("LocalSongManager", song.getName() + " is downloaded");
        this.pendingSong = null;
    }

    /**
     * Removes the song in downloading state.
     */
    public void clearPendingSong() {
        if (pendingSong != null) {
            pendingSong.delete();
            Log.i("LocalSongManager", "Removing incomplete song " + pendingSong.getName());
            pendingSong = null;
        }
    }
    // *********************************************************************************************

    //public MyQueueItem[] getLocalSongs() {
    //    MyQueueItem[] tmp = new MyQueueItem[localSongs.size()];
    //    localSongs.toArray(tmp);
    //    return tmp;
    //}

    public Song[] getLocalSongs() {
        maybeLoad();

        Song[] tmp = new Song[localSongs.size()];
        int i = 0;
        for (MyQueueItem item: localSongs)
            tmp[i++] = item.toSong();
        return tmp;
    }

    /**
     * Scans local music folder searching for already downloaded songs, and adds them to
     * the LocalSongs list.
     * @param context
     */
    public static void findLocalSongs(Context context) {
        // Initializing data
        PyMusicManagerConnector connector = Settings.loadSettings(context).getConnector();
        LocalSongManager lsm = LocalSongManager.getInstance(context);

        File[] songs = new File(Utils.getMusicPath()).listFiles();
        if (songs == null) {
            Log.d("LocalSongManager", "Cannot find local songs! Can't listing musicfolder");
            return;
        }

        ArrayList<Song> toAdd = new ArrayList<>(20);

        for (File song: songs) {
            Song[] res = connector.searchSong(song.getName());
            for (Song templateSong: res)
                if (templateSong.getName().equals(song.getName())) {
                    toAdd.add(templateSong);
                    break;
                }
        }

        Song[] r = new Song[toAdd.size()];
        toAdd.toArray(r);
        lsm.addExistentLocalSongs(r);

        Log.d("LocalSongManager", "Find new " + r.length + " existent local songs!");
    }

    private void maybeLoad() {
        if (loaded) return;
        loadCustomSongs();
    }

    public void trimMemory() {
        writeLocalSongs();
        localSongs.clear();
        loaded = false;
    }
}
