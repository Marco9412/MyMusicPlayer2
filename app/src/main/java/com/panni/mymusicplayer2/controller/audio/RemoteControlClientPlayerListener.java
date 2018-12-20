package com.panni.mymusicplayer2.controller.audio;

import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;

import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.PlayerListener;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.utils.Utils;

/**
 * Created by marco on 04/08/16.
 */

public class RemoteControlClientPlayerListener implements PlayerListener {

    private int lastTotal = -1;

    @SuppressWarnings("deprecation")
    private RemoteControlClient remoteControlClient;

    @SuppressWarnings("deprecation")
    public RemoteControlClientPlayerListener(RemoteControlClient remoteControlClient) {
        this.remoteControlClient = remoteControlClient;
    }

    @Override
    public void stateChanged() {
        Controller c = ControllerImpl.getInstance();

        MyQueueItem item = c.getCurrentQueue().getCurrentItem();

        remoteControlClient.setPlaybackState(
                Utils.playerStateToRemoteControlClientState(c.getCurrentPlayer().getCurrentState()),
                c.getCurrentPlayer().getCurrentPosition(),
                1.f);
        remoteControlClient.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, item.getTitle())
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, item.getArtist())
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, c.getCurrentPlayer().getDuration())
                .apply();
    }

//    @Override
//    public void songChanged() {
//        // Never used???
//    }

    @Override
    public void timeChanged(int current, int total) {
        Controller c = ControllerImpl.getInstance();

        remoteControlClient.setPlaybackState(
                Utils.playerStateToRemoteControlClientState(c.getCurrentPlayer().getCurrentState()),
                current,
                1.f);

        // Fix: with smart cache enabled when a new song is played the wrong total time remains
        // into the mediasession and causes a crash when the song ends
        if (total != lastTotal) {
            total = lastTotal;
            remoteControlClient.editMetadata(false)
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, total)
                    .apply();
        }
    }
}
