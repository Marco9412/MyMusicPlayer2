package com.panni.mymusicplayer2.controller.localsongs;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import objects.Song;

/**
 * Created by marco on 19/06/16.
 */
public class DownloadReceiver extends BroadcastReceiver {

    private Song downloading;
    private long downloadId;

    public DownloadReceiver(Song song, long id) {
        this.downloading = song;
        this.downloadId = id;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1);
        if (id == downloadId) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = dm.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    LocalSongManager.getInstance(context).saveLocalSong(downloading);
                    //new DbHelper(context).addLocalSong(downloading);
                    context.unregisterReceiver(this);
                }
            }
        }
    }
}
