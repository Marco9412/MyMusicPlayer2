package com.panni.mymusicplayer2.model.queue.objects;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;

import org.json.JSONException;
import org.json.JSONObject;

import objects.Song;

public class YoutubeQueueItem extends MyQueueItem {

    final public static String FILE_ID = "Y";

    public YoutubeQueueItem(String title, String url) {
        super(title, url);
    }

    @Override
    public String getArtist() {
        return "Yt"; // TODO implement by Yt video name
    }

    @Override
    public Song toSong() {
        throw new IllegalStateException("Cannot convertYoutubeQueueItem to Song!");
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
    public int getType() {
        return TYPE_YOUTUBE;
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
            res.put("class", "YoutubeQueueItem");
            res.put("title", getTitle());
            res.put("url", getUrl());
        } catch (JSONException ex) {
        }
        return res;
    }

    @Override
    public int compareTo(MyQueueItem o) {
        return getTitle().compareTo(o.getTitle());
    }
}
