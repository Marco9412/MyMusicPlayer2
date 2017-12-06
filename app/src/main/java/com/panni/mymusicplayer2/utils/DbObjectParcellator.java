package com.panni.mymusicplayer2.utils;

import android.os.Bundle;

import objects.DbObject;
import objects.Folder;
import objects.Playlist;
import objects.Song;


/**
 * Created by marco on 15/05/16.
 */
public class DbObjectParcellator {

    final public static String CLASS = "class_name";
    final public static String OID = "dbobject_oid";
    final public static String NAME = "dbobject_name";
    final public static String PARENT = "folder_parentid";
    final public static String ISROOT = "folder_isroot";
    final public static String SONGS = "playlist_songs";
    final public static String FOLDER = "song_folder";
    final public static String TITLE = "song_title";
    final public static String ARTIST = "song_artist";

    final public static String SONG_CLASS = "class_song";
    final public static String FOLDER_CLASS = "class_folder";
    final public static String PLAYLIST_CLASS = "class_playlist";

    final private static String ERR_STRING = "Wrong bundle!";

    public static void pushToBundle(DbObject object, Bundle bundle) {
        bundle.putInt(OID, object.getOid());
        bundle.putString(NAME, object.getName());

        if (object instanceof Song) {
            Song s = (Song) object;
            bundle.putString(CLASS, SONG_CLASS);
            bundle.putInt(FOLDER, s.getFolder());
            bundle.putString(TITLE, s.getTitle());
            bundle.putString(ARTIST, s.getArtist());
        } else if (object instanceof Playlist) {
            Playlist p = (Playlist) object;
            bundle.putString(CLASS, PLAYLIST_CLASS);
            bundle.putIntArray(SONGS, p.getItems());
        } else { // Folder
            Folder f = (Folder) object;
            bundle.putString(CLASS, FOLDER_CLASS);
            bundle.putInt(PARENT, f.getParent());
            bundle.putBoolean(ISROOT, f.isRoot());
        }
    }

    public static DbObject createFromBundle(Bundle bundle) {
        String classname = bundle.getString(CLASS, "Invalid");
        int oid = bundle.getInt(OID, -1);
        String name = bundle.getString(NAME, ERR_STRING);

        switch (classname) {
            case SONG_CLASS:
                return new Song(oid, name,
                        bundle.getInt(FOLDER, -1),
                        bundle.getString(TITLE, ERR_STRING),
                        bundle.getString(ARTIST, ERR_STRING));
            case FOLDER_CLASS:
                return new Folder(oid, name,
                        bundle.getInt(PARENT, -1),
                        bundle.getBoolean(ISROOT, false));
            case PLAYLIST_CLASS:
                return new Playlist(name, bundle.getIntArray(SONGS));
        }

        return null;
    }
}
