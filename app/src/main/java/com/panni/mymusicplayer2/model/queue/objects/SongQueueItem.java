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
public class SongQueueItem extends MyQueueItem {

    final public static String FILE_ID = "S";

    /// Remote song id
    private int oid;
    /// Folder which contains this song
    private int folder;
    /// Song filename
    private String name;
    /// Song artist
    private String artist;
    /// Mimetype
    private String mimeType;

    public SongQueueItem(Song song, String url) {
        super(song.getTitle(), url);
        this.oid = song.getOid();
        this.folder = song.getFolder();
        this.name = song.getName();
        this.artist = song.getArtist();
        this.mimeType = song.getMimeType();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ oid ^ folder ^ name.hashCode() ^ artist.hashCode() ^ mimeType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SongQueueItem &&
                super.equals(obj) &&
                oid == ((SongQueueItem) obj).oid &&
                folder == ((SongQueueItem) obj).folder &&
                name.equals(((SongQueueItem) obj).name) &&
                artist.equals(((SongQueueItem) obj).artist) &&
                mimeType.equals(((SongQueueItem) obj).mimeType);
    }

    @Override
    public int compareTo(MyQueueItem item) {
        int res = getTitle().compareTo(item.getTitle());
        return res != 0 ? res : getArtist().compareTo(item.getArtist());
    }

    public int getOid() {
        return oid;
    }

    public int getFolder() {
        return folder;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public int getType() {
        return TYPE_SONG;
    }

    @Override
    public Song toSong() {
        return new Song(oid, name, folder, getTitle(), artist);
    }

    @Override
    public MediaQueueItem toMediaQueueItem() {
        MediaMetadata mm = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mm.putString(MediaMetadata.KEY_ARTIST, artist);
        mm.putString(MediaMetadata.KEY_TITLE, getTitle());
        mm.putString("songname", name);
        mm.putInt("songid", oid);
        mm.putInt("songfolder", folder);

        return new MediaQueueItem.Builder(
                new MediaInfo.Builder(getUrl())
                .setContentType(mimeType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mm)
                .build()
        ).build();
    }

    @Override
    public String toString() {
        return "SongQueueItem:" + this.oid + " " + this.name + " " + this.folder + " " + this.getTitle() + " " + this.artist;
    }

    @Override
    public String toFileFormat() {
        return  FILE_ID + "\n" +
                oid + "\n" +
                name + "\n" +
                folder + "\n" +
                getTitle() + "\n" +
                artist + "\n";
                //getUrl() + "\n" +
                //mimeType + "\n" +
                //SEPARATOR;
    }

    @Override
    public JSONObject toJson() {
        JSONObject res = new JSONObject();
        try {
            res.put("class", "SongQueueItem");
            res.put("oid", oid);
            res.put("name", name);
            res.put("folder", folder);
            res.put("title", getTitle());
            res.put("artist", getArtist());
        } catch (JSONException ex) {
        }
        return res;
    }
}
