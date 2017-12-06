package com.panni.mymusicplayer2.controller.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;

public class AudioNoisyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // signal your service to stop playback
            Controller c = ControllerImpl.getInstance();
            if (c != null)
                c.stopPlayers();
        }
    }
}
