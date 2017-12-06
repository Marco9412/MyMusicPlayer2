package com.panni.mymusicplayer2.model.buffer;

import android.util.SparseArray;

import objects.DbObject;
import objects.Folder;

/**
 * Created by marco on 27/05/16.
 */
public class DataBuffer {

    private static DataBuffer instance;

    public static DataBuffer getInstance() {
        if (instance == null)
            instance = new DataBuffer();
        return instance;
    }

    // Opened folders
    private SparseArray<DbObject[]> folders;

    // Local songs
    //private LinkedList<Song> localSongs;

    // Custom songs
    //private LinkedList<MyQueueItem> customSongs;

    // Root folders
    private Folder[] rootFolders;

    private DataBuffer() {
        folders = new SparseArray<>(10);
        //localSongs = new LinkedList<>();
        //customSongs = new LinkedList<>();
        rootFolders = null;
    }

    public void insertFolder(int folderid, DbObject[] content) {
        folders.put(folderid, content);
    }

    public DbObject[] getFolder(int folderid) {
        return folders.get(folderid);
    }

    public void setRootFolders(Folder[] rootsf) {
        this.rootFolders = rootsf;
    }

    public Folder[] getRootFolders() {
        return rootFolders;
    }

//    public void setLocalSongs(Song[] songs) {
//        localSongs.clear();
//        Collections.addAll(localSongs, songs);
//    }
//
//    public Song[] getLocalSongs() {
//        Song[] r = new Song[localSongs.size()];
//        return localSongs.toArray(r);
//    }
//
//    public void setCustomSongs(MyQueueItem[] items) {
//        customSongs.clear();
//        Collections.addAll(customSongs, items);
//    }
//
//    public MyQueueItem[] getCustomSongs() {
//        MyQueueItem[] res = new MyQueueItem[customSongs.size()];
//        return customSongs.toArray(res);
//    }

    public void trimData() {
        this.folders.clear();
        //this.localSongs.clear();
        //this.customSongs.clear();
        rootFolders = null;
    }
}
