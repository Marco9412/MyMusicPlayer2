package com.panni.mymusicplayer2.controller.audio;

import android.media.AudioManager;

import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.Player;

/**
 * Created by marco on 11/06/16.
 */
public class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {

    private int lastPlayerState = -1;

    @Override
    public void onAudioFocusChange(int focusChange) {
        Controller c = ControllerImpl.getInstance();
        if (c == null) return;
        Player p = c.getCurrentPlayer();

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (lastPlayerState == Player.STATE_PLAYING) {
                    p.play();
                    p.audioFocus(true);
                    lastPlayerState = -1;
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                c.stopPlayers();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                // Fix: save previous state, if it was playing, then it should resume
                lastPlayerState = ControllerImpl.getInstance().getCurrentPlayer().getCurrentState();
                p.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                lastPlayerState = ControllerImpl.getInstance().getCurrentPlayer().getCurrentState();
                p.audioFocus(false);
                break;
        }
    }
}
