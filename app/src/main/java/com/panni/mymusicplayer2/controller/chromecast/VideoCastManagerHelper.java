package com.panni.mymusicplayer2.controller.chromecast;

import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

/**
 * Created by marco on 19/06/16.
 */
public class VideoCastManagerHelper {

    private VideoCastManager chromecastPlayer = VideoCastManager.getInstance();

    public boolean play(MediaQueueItem item) {
        return playAndSeek(item, 0);
    }

    public boolean playAndSeek(MediaQueueItem item, int position) {
        try {
            chromecastPlayer.loadMedia(item.getMedia(), true, position);
            return true;
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean play() {
        try {
            chromecastPlayer.play();
            return true;
        } catch (CastException | TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean pause() {
        try {
            chromecastPlayer.pause();
            return true;
        } catch (CastException | TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean stop() {
        try {
            chromecastPlayer.stop();
            return true;
        } catch (CastException | TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean seek(int ms) {
        try {
            chromecastPlayer.seek(ms);
            return true;
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean setVolume(double vol) {
        // TODO actually a BUG in audiofocus!
        //try {
        //    chromecastPlayer.setVolume(vol);
            return true;
        //} catch (CastException | TransientNetworkDisconnectionException | NoConnectionException e) {
        //    e.printStackTrace();
        //    return false;
        //}
    }

    public int getMediaDuration() {
        try {
            return (int)chromecastPlayer.getMediaDuration();
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public int getCurrentMediaPosition() {
        try {
            return (int)chromecastPlayer.getCurrentMediaPosition();
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            //MyErrorLogger.getInstance().log("VideoCastManagerHelper", e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
