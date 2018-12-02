package com.panni.mymusicplayer2.controller.player.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.view.MainActivity;

public class PlayerService extends Service {

    final public static String SERVICE_CMD = "com.panni.mymusicplayer2.CMD";

    final public static String START_PLAYER = "com.panni.mymusicplayer2.STARTP";
    final public static String PLAY = "com.panni.mymusicplayer2.PLAY";
    final public static String PAUSE = "com.panni.mymusicplayer2.PAUSE";
    final public static String NEXT = "com.panni.mymusicplayer2.NEXT";
    final public static String PREVIOUS = "com.panni.mymusicplayer2.PREV";
    final public static String STOP_PLAYER = "com.panni.mymusicplayer2.STOPP";
    final public static String UPDATE_NOTIFICATION = "com.panni.mymusicplayer2.UPDATEN";
    final public static String HIDE_NOTIFICATION = "com.panni.mymusicplayer2.HIDEN";
    final public static String CLOSE_ALL = "com.panni.mymusicplayer2.CLOSE";

    final private static int NOTIFICATION_ID = 478;
    final private static int NOTIFICATION_ID2 = 479;

    private Controller controller;

    public PlayerService() {
        this.controller = ControllerImpl.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder(); // Return a pointer to this service!
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String command = intent.getStringExtra(SERVICE_CMD);
            if (command != null)
                switch (command) {
                    case START_PLAYER:
                    case UPDATE_NOTIFICATION:
                        showNotification();
                        break;
                    case HIDE_NOTIFICATION:
                        stopForeground(true);
                        break;
                    case STOP_PLAYER:
                        stopForeground(true);
                        stopSelf();
                        break;
                    case PLAY:
                        play();
                        break;
                    case PAUSE:
                        pause();
                        break;
                    case NEXT:
                        next();
                        break;
                    case PREVIOUS:
                        prev();
                        break;
                    case CLOSE_ALL:
                        controller.stopPlayers();
                        break;
                }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {
        int playerState = this.controller.getCurrentPlayer().getCurrentState();
        boolean isPlaying = playerState == Player.STATE_PLAYING || playerState == Player.STATE_BUFFERING; // Fixed
        PlayerQueue currentPlaylist = this.controller.getCurrentQueue();

        // Open activity intent!
        Intent playerIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent intentOpenActivity = PendingIntent.getActivity(getApplicationContext(), 0,
                playerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Actions
        Intent pauseIntent = new Intent(getApplicationContext(), PlayerService.class);
        if (isPlaying) pauseIntent.putExtra(SERVICE_CMD, PAUSE);
        else pauseIntent.putExtra(SERVICE_CMD, PLAY);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent nextIntent = new Intent(getApplicationContext(), PlayerService.class);
        nextIntent.putExtra(SERVICE_CMD, NEXT);
        PendingIntent next = PendingIntent.getService(getApplicationContext(), 1, nextIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent prevIntent = new Intent(getApplicationContext(), PlayerService.class);
        prevIntent.putExtra(SERVICE_CMD, PREVIOUS);
        PendingIntent prev = PendingIntent.getService(getApplicationContext(), 2, prevIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent quitIntent = new Intent(getApplicationContext(), PlayerService.class);
        quitIntent.putExtra(SERVICE_CMD, CLOSE_ALL);
        PendingIntent quit = PendingIntent.getService(getApplicationContext(), 3, quitIntent, PendingIntent.FLAG_ONE_SHOT);

        MyQueueItem item = currentPlaylist.getCurrentItem();

        // Notification
        Notification notification = new Notification.Builder(getApplicationContext())
                .setTicker(item.getTitle())
                .setContentTitle(item.getTitle())
                .setContentText(item.getArtist())
                .setContentIntent(intentOpenActivity)
                //.setDeleteIntent(quit)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(
                        isPlaying ?
                                R.drawable.ic_pause_white_24dp :
                                R.drawable.ic_play_arrow_white_24dp,
                        "",
                        pause)
                //.addAction(R.drawable.ic_skip_previous_white_36dp, "", prev)
                .addAction(R.drawable.ic_skip_next_white_36dp, "", next)
                .addAction(R.drawable.ic_close_dark, "", quit)
                .setOngoing(false)
                .build();

        // Show notification
        startForeground(NOTIFICATION_ID, notification);
    }


    // ----------- Public methods

    /**
     * Only a public wrapper to showNotification method
     */
    public void updateNotification() {
        showNotification();
    }

    public void hideNotification() {
        stopForeground(true);
    }

    public void play() {
        this.controller.getCurrentPlayer().play();
        showNotification();
    }

    public void pause() {
        this.controller.getCurrentPlayer().pause();
        showNotification();
    }

    public void next() {
        this.controller.getCurrentPlayer().next();
        showNotification();
    }

    public void prev() {
        this.controller.getCurrentPlayer().prev();
        showNotification();
    }

    public void showError(String text) {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setTicker("Error")
                .setContentTitle("MyMusicPlayer")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(false)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID2, notification);
    }

    /**
     * My binder communicator class
     */
    public class MyBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
