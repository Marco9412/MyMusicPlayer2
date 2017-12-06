package com.panni.mymusicplayer2.controller.player;


import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.localsongs.musicsource.IcyDataSource;
import com.panni.mymusicplayer2.controller.localsongs.musicsource.SongDataSource;
import com.panni.mymusicplayer2.controller.player.service.PlayerService;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marco on 28/05/16.
 */

public class MyExoPlayer implements Player, ExoPlayer.Listener {

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private int playerState;

    private PlayerQueue playlist;

    private LinkedList<PlayerListener> listeners;

    private PlayerService service;

    private Timer uiTimer;
    private boolean uiTimerWasRunning;

    public MyExoPlayer() {
        playerState = Player.STATE_STOPPED;
        listeners = new LinkedList<>();
        uiTimerWasRunning = false;
        exoPlayer = ExoPlayer.Factory.newInstance(1, 1000, 1000);
        exoPlayer.addListener(this);
        playlist = ControllerImpl.getInstance().getCurrentQueue();
    }

    public void setService(PlayerService service) {
        this.service = service;
        if ((playerState == Player.STATE_PLAYING || playerState == Player.STATE_PAUSED))
            service.updateNotification();
        if ((playerState == Player.STATE_STOPPED || playerState == Player.STATE_UNINITIALIZED))
            service.hideNotification();
    }

    @Override
    public void play() {
        if (playlist.length() > 0) {
            if (playerState == Player.STATE_STOPPED)
                startPlayer();
            else if (playerState == Player.STATE_PAUSED) {
                exoPlayer.setPlayWhenReady(true); // play

                playerState = Player.STATE_PLAYING;

                if (service != null)
                    service.updateNotification();

                updateListenersState();
            }
        }
    }

    @Override
    public void play(int position) {
        if (position < playlist.length()) {
            playlist.setCurrentPosition(position);
            this.stop();
            this.play();
        }
    }

    @Override
    public void playAndSeek(int positionMs) {
        play(); // TODO Not implemented! -> maybe unuseful
    }

    @Override
    public void setNext(int position) {
        playlist.setNext(position);
    }

    @Override
    public void seekTo(int percentage) {
        this.exoPlayer.seekTo(this.exoPlayer.getDuration() * percentage / 100);
    }

    @Override
    public int getCurrentPosition() {
        if (playerState != Player.STATE_STOPPED && exoPlayer != null)
            return (int) exoPlayer.getCurrentPosition();
        return 0;
    }

    @Override
    public int getDuration() {
        if (playerState != Player.STATE_STOPPED && exoPlayer != null)
            return (int) exoPlayer.getDuration();
        return 0;
    }

    @Override
    public void next() {
        if (playlist.next())
            startPlayer();
        else if (playerState == Player.STATE_PAUSED || playerState == Player.STATE_PLAYING) {
            stop();
            ControllerImpl.getInstance().stopPlayers();
        }
    }

    @Override
    public void prev() {
        if (playlist.prev())
            startPlayer();
        else if (playerState == Player.STATE_PAUSED || playerState == Player.STATE_PLAYING) {
            stop();
            ControllerImpl.getInstance().stopPlayers();
        }
    }

    @Override
    public void pause() {
        if (playerState == Player.STATE_PLAYING) {
            exoPlayer.setPlayWhenReady(false); // pause

            playerState = Player.STATE_PAUSED;
            if (service != null)
                service.updateNotification();

            updateListenersState();
        }
    }

    @Override
    public void stop() {
        if (playerState == Player.STATE_PLAYING || playerState == Player.STATE_PAUSED || playerState == Player.STATE_BUFFERING) {
            stopUiTimer();
            exoPlayer.stop();
            playerState = Player.STATE_STOPPED;

            if (service != null)
                service.hideNotification();
            updateListenersState();
        }
    }

    @Override
    public LinkedList<PlayerListener> getPlayerListeners() {
        return listeners;
    }

    @Override
    public void addPlayerListener(PlayerListener playerListener) {
        if (!listeners.contains(playerListener)) {
            listeners.add(playerListener);
            if (listeners.size() > 0 && uiTimerWasRunning) startUiTimer();
        }
    }

    @Override
    public void removePlayerListener(PlayerListener playerListener) {
        listeners.remove(playerListener);
        if (listeners.size() == 0) {
            uiTimer.cancel(); // TODO check
            uiTimer = null;
        }
    }

    @Override
    public void delete() {
        if (playerState != Player.STATE_STOPPED)
            stop();

        exoPlayer.release(); //already done in stop()
        exoPlayer = null;

        playerState = Player.STATE_UNINITIALIZED;

        updateListenersState();
    }

    @Override
    public int getCurrentState() {
        return playerState;
    }

    @Override
    public int getKind() {
        return Player.KIND_LOCAL;
    }

    @Override
    public void audioFocus(boolean status) {
        if (playerState == Player.STATE_PLAYING) {
            float vol = status ? 1.f : .1f;
            if (audioRenderer != null)
                exoPlayer.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, vol);
        }
    }

    private void startPlayer() {
        if (playerState == Player.STATE_PLAYING) {
            exoPlayer.stop();
            //exoPlayer.release();
        }

        DataSource currentDataSource;

        // If is remote stream, don't save!
        if (playlist.getCurrentItem().isCustom())
            currentDataSource = new IcyDataSource(System.getProperty("http.agent"), null);
        else //if (ControllerImpl.getInstance().getCurrentSettings().isSmartCacheEnabled())
            currentDataSource = new SongDataSource(System.getProperty("http.agent"), null)
                    //.setContext(context)
                    .setSong(playlist.getCurrentSong());
        //else // Use default data source if smart cache is disabled!
        //  currentDataSource = new DefaultHttpDataSource(System.getProperty("http.agent"), null);

        audioRenderer = new MediaCodecAudioTrackRenderer(
                new ExtractorSampleSource(
                        Uri.parse(playlist.getCurrentMediaQueueItem().getMedia().getContentId()),
                        currentDataSource,
                        new DefaultAllocator(64 * 1024),
                        64 * 1024 * 256
                ),
                MediaCodecSelector.DEFAULT);

        exoPlayer.seekTo(0);
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);

        playerState = Player.STATE_BUFFERING;

        if (service != null)
            service.updateNotification();
        updateListenersState();
    }

    private void startUiTimer() {
        stopUiTimer();
        uiTimerWasRunning = true;
        this.uiTimer = new Timer();
        uiTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (playerState == Player.STATE_PLAYING) {
                    updateListenersTime();
                } else {
                    uiTimer.cancel();
                    uiTimer = null;
                }
            }
        }, 0, 1000);
    }

    private void stopUiTimer() {
        if (uiTimer != null) {
            uiTimer.cancel();
            uiTimer = null;
            uiTimerWasRunning = false;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                Log.v("ExoPlayerState", "BUFFERING");
                break;
            case ExoPlayer.STATE_ENDED:
                Log.v("ExoPlayerState", "ENDED");
                stopUiTimer();
                next();
                break;
            case ExoPlayer.STATE_IDLE:
                Log.v("ExoPlayerState", "IDLE");
                break;
            case ExoPlayer.STATE_PREPARING:
                Log.v("ExoPlayerState", "PREPARING");
                break;
            case ExoPlayer.STATE_READY:
                Log.v("ExoPlayerState", "READY");
                if (playWhenReady) {
                    playerState = Player.STATE_PLAYING;
                    startUiTimer();
                    updateListenersState();
                    if (service != null)
                        service.updateNotification();
                } else {
                    stopUiTimer();
                }
                break;
            default:
                Log.v("ExoPlayerState", "UNKNOWN");
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Nothing
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        stopUiTimer();

        if (service != null) {
            service.showError(error.getLocalizedMessage());
        }

        error.printStackTrace();
        //MyErrorLogger.getInstance().log("MyExoPlayer", error.getMessage());

        // TODO should move to next song
    }

    private void updateListenersState() {
        for (PlayerListener listener : listeners)
            listener.stateChanged();
    }

    private void updateListenersTime() {
        int time = (int) exoPlayer.getCurrentPosition();
        int total = (int) exoPlayer.getDuration();
        for (PlayerListener listener : listeners)
            listener.timeChanged(time, total);
    }
}
