package com.panni.mymusicplayer2.model.queue.objects;

import com.google.android.gms.cast.MediaQueueItem;
import com.panni.mymusicplayer2.controller.ControllerImpl;

import java.io.BufferedReader;
import java.io.IOException;

import objects.Song;

/**
 * Created by marco on 26/07/16.
 */
public abstract class MyQueueItem implements Comparable<MyQueueItem> {

    final public static int TYPE_SONG = 0;
    final public static int TYPE_CUSTOM = 1;

    //final protected static String SEPARATOR = "MEDIA_ITEM_END";

    private String title;
    private String url;

    protected MyQueueItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Override
    public int hashCode() {
        return title.hashCode() ^ url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MyQueueItem &&
                title.equals(((MyQueueItem) obj).title) &&
                url.equals(((MyQueueItem) obj).url);
    }

    public String getTitle() {
        return title;
    }

    public abstract String getArtist();

    public String getUrl() {
        return url;
    }

    public boolean isCustom() {
        return getType() == TYPE_CUSTOM;
    }

    public abstract Song toSong();

    public abstract MediaQueueItem toMediaQueueItem();

    public abstract int getType();

    public abstract String toFileFormat();

    public String getShareString() {
        return "Listen to " + getTitle() + " - " + getArtist() + " on " + getUrl();
    }

    public static MyQueueItem create(Song song) {
        return new SongQueueItem(song, ControllerImpl.getInstance().getHttpSongUrl(song));
    }

    public static MyQueueItem create(String title, String url) {
        return new CustomQueueItem(title, url);
    }

    public static MyQueueItem fromFileFormat(BufferedReader input) {
        try {
            String tmp = input.readLine();
            if (tmp == null) return null;
            switch (tmp) {
                case SongQueueItem.FILE_ID:
                    return create(new Song(
                            Integer.parseInt(input.readLine()),
                            input.readLine(),
                            Integer.parseInt(input.readLine()),
                            input.readLine(),
                            input.readLine()
                    ));
                case CustomQueueItem.FILE_ID:
                    return create(
                            input.readLine(),
                            input.readLine()
                    );
            }
        } catch (IOException ex) {
        }
        return null;
    }
}
