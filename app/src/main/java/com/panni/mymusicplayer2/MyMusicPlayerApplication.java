package com.panni.mymusicplayer2;

import android.app.Application;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.panni.mymusicplayer2.controller.localsongs.CustomSongManager;
import com.panni.mymusicplayer2.controller.localsongs.LocalSongManager;
import com.panni.mymusicplayer2.logger.MyErrorLogger;
import com.panni.mymusicplayer2.model.buffer.DataBuffer;

/**
 * Created by marco on 19/06/16.
 */
public class MyMusicPlayerApplication extends Application {

    private Thread.UncaughtExceptionHandler defaultHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        startLogger();
        initCast();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        DataBuffer.getInstance().trimData();
        CustomSongManager.getInstance().trimMemory();
        LocalSongManager.getInstance().trimMemory();
    }

    /**
     * Initializes chromecast companion library
     */
    private void initCast() {
        CastConfiguration options = new CastConfiguration.Builder(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                //.enableAutoReconnect()
                //.enableLockScreen()
                .enableNotification()
                //.enableWifiReconnection()
                //.enableDebug()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                //.addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .build();
        VideoCastManager.initialize(getApplicationContext(), options);
    }

    private void startLogger() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                MyErrorLogger.getInstance().log("------------------- UncaughtException!");
                throwable.printStackTrace();

                LocalSongManager.deleteInstance();
                MyErrorLogger.closeLog();

                defaultHandler.uncaughtException(thread, throwable); // Go to default
            }
        });
    }
}
