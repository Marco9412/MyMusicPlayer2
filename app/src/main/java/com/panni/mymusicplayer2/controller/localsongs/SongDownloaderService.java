package com.panni.mymusicplayer2.controller.localsongs;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.panni.mymusicplayer2.utils.DbObjectParcellator;

import objects.Song;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SongDownloaderService extends IntentService {

    private static final String ACTION_DOWNLOAD_SONG = "com.panni.mymusicplayer2.controller.action.SONG";
    private static final String PARAM_URL = "com.panni.mymusicplayer2.controller.param.SONGURL";

    public SongDownloaderService() {
        super("SongDownloaderService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startDownload(Context context, Song song, String url) {
        Intent intent = new Intent(context, SongDownloaderService.class);
        intent.setAction(ACTION_DOWNLOAD_SONG);
        Bundle b = new Bundle();
        b.putString(PARAM_URL, url);
        DbObjectParcellator.pushToBundle(song, b);
        intent.replaceExtras(b);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_SONG.equals(action)) {
                Song song = (Song) DbObjectParcellator.createFromBundle(intent.getExtras());
                String url = intent.getExtras().getString(PARAM_URL);
                handleActionDownload(song, url);
            }
        }
    }

    /**
     * Handle action Download in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(Song song, String url) {
        if (LocalSongManager.getInstance(getApplicationContext()).localSongExists(song)) return;
        //if (new DbHelper(getApplicationContext()).songExists(song.getOid())) return;

        DownloadManager dm = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "/MyMusicPlayer/" + song.getName());
        request.setVisibleInDownloadsUi(true);
        long id = dm.enqueue(request);

        // Receiver TODO!
        getApplicationContext().registerReceiver(new DownloadReceiver(song, id), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
