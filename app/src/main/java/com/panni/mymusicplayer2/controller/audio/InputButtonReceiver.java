package com.panni.mymusicplayer2.controller.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.Player;

/**
 * Created by marco on 04/08/16.
 */
public class InputButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Controller c;

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() == KeyEvent.ACTION_DOWN) return; // Capture only action UP (releasing button)

            switch(event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Log.d("InputButtonReceiver","Play command");
                    ControllerImpl.getInstance(context.getApplicationContext()).getCurrentPlayer().play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Log.d("InputButtonReceiver","Pause command");
                    ControllerImpl.getInstance(context.getApplicationContext()).getCurrentPlayer().pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Log.d("InputButtonReceiver","Play/pause command");
                    c = ControllerImpl.getInstance(context.getApplicationContext());
                    if (c.getCurrentPlayer().getCurrentState() == Player.STATE_PLAYING)
                        c.getCurrentPlayer().pause();
                    else c.getCurrentPlayer().play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Log.d("InputButtonReceiver","Next command");
                    c = ControllerImpl.getInstance(context.getApplicationContext());
                    if (c.getCurrentPlayer().getCurrentState() == Player.STATE_PLAYING) c.getCurrentPlayer().next();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Log.d("InputButtonReceiver","Prev command");
                    c = ControllerImpl.getInstance(context.getApplicationContext());
                    if (c.getCurrentPlayer().getCurrentState() == Player.STATE_PLAYING) c.getCurrentPlayer().prev();
                    break;
            }
        }
    }
}
