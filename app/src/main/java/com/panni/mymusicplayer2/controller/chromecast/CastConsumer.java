package com.panni.mymusicplayer2.controller.chromecast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.Player;

/**
 * Created by marco on 19/06/16.
 */
public class CastConsumer extends VideoCastConsumerImpl {

    private static CastConsumer instance;

    public static CastConsumer getInstance() {
        if (instance == null) instance = new CastConsumer();
        return instance;
    }

    private boolean connected = false;

    private boolean wasPlaying = false;

    private CastListener listener;

    private CastConsumer() {}

    public boolean isConnected() {
        return connected;
    }

    public void setListener(CastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
        connected = true;

        // Switch to chromecast-player
        int playerState = ControllerImpl.getInstance().getCurrentPlayer().getCurrentState();
        if (playerState != Player.STATE_UNINITIALIZED)
            ControllerImpl.getInstance().changePlayer(Player.KIND_CHROMECAST);
    }

    @Override
    public void onDisconnected() {
        connected = false;

        // Switch to local-player
        int playerState = ControllerImpl.getInstance().getCurrentPlayer().getCurrentState();
        if (playerState != Player.STATE_UNINITIALIZED)
            //ControllerImpl.getInstance().changePlayer(PlayerKind.LOCAL_PLAYER);
            ControllerImpl.getInstance().stopPlayers();
    }

    @Override
    public void onRemoteMediaPlayerStatusUpdated() {
        if (!connected) return;

        switch (VideoCastManager.getInstance().getPlaybackStatus()) {
            case MediaStatus.PLAYER_STATE_BUFFERING:
                wasPlaying = false;
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                wasPlaying = true;
                if (listener != null) listener.onPrepared();
                break;
            case MediaStatus.PLAYER_STATE_IDLE:
                // Used to go to next song in queue!
                if (wasPlaying) {
                    wasPlaying = false;
                    if (listener != null) listener.onCompleteSong();
                }
                break;
            case MediaStatus.PLAYER_STATE_UNKNOWN:
                break;
        }
    }
}
