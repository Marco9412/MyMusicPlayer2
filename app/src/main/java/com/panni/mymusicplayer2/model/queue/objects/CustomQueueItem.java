package com.panni.mymusicplayer2.model.queue.objects;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;

import org.json.JSONException;
import org.json.JSONObject;

import objects.Song;

/**
 * Created by marco on 26/07/16.
 */
public class CustomQueueItem extends MyQueueItem {

    final public static String FILE_ID = "C";

    public CustomQueueItem(String title, String url) {
        super(title, url);
    }

    @Override
    public int getType() {
        return TYPE_CUSTOM;
    }

    @Override
    public Song toSong() {
        throw new IllegalStateException("Cannot convert CustomQueueItem to Song!");
    }

    @Override
    public int compareTo(MyQueueItem item) {
        return getTitle().compareTo(item.getTitle());
    }

    @Override
    public String getArtist() {
        return "Custom source";
    }

    @Override
    public MediaQueueItem toMediaQueueItem() {
        MediaMetadata mm = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mm.putString(MediaMetadata.KEY_TITLE, getTitle());

        return new MediaQueueItem.Builder(
                new MediaInfo.Builder(getUrl())
                        .setMetadata(mm)
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setContentType("audio/*")
                        .build()
        ).build();
    }

    @Override
    public String toString() {
        return "CustomQueueItem: " + getTitle() + ", " + getUrl();
    }

    @Override
    public String toFileFormat() {
        return  FILE_ID + "\n" +
                getTitle() + "\n" +
                getUrl() + "\n";
                //SEPARATOR;
    }

    @Override
    public JSONObject toJson() {
        JSONObject res = new JSONObject();
        try {
            res.put("class", "CustomQueueItem");
            res.put("title", getTitle());
            res.put("url", getUrl());
        } catch (JSONException ex) {
        }
        return res;
    }
}
