package com.panni.mymusicplayer2.controller.player;

import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.chromecast.CastConsumer;
import com.panni.mymusicplayer2.controller.chromecast.CastListener;
import com.panni.mymusicplayer2.controller.chromecast.VideoCastManagerHelper;
import com.panni.mymusicplayer2.controller.player.service.PlayerService;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marco on 28/05/16.
 */
public class ChromecastPlayer implements Player, CastListener {

    // TODO cannot play an icystream!

    private VideoCastManagerHelper chromecastPlayer;
    private int playerState;

    private PlayerQueue playlist;

    private LinkedList<PlayerListener> listeners;
    private PlayerService service;

    private Timer uiTimer;
    private boolean uiTimerWasRunning;

    public ChromecastPlayer() {
        playerState = Player.STATE_STOPPED;
        listeners = new LinkedList<>();
        uiTimerWasRunning = false;
        chromecastPlayer = new VideoCastManagerHelper();
        playlist = ControllerImpl.getInstance().getCurrentQueue();
        CastConsumer.getInstance().setListener(this);
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
                startPlayer(0);
            else if (playerState == Player.STATE_PAUSED) {
                if (!chromecastPlayer.play()) {
                    onError("Error while resuming!");
                    next();
                    return;
                }

                playerState = Player.STATE_PLAYING;

                if (service != null) service.updateNotification();
                updateListenersState();
            }
        }
    }

    @Override
    public void playAndSeek(int positionMs) {
        if (playlist.length() > 0) {
            if (playerState == Player.STATE_STOPPED)
                startPlayer(positionMs);
            else if (playerState == Player.STATE_PAUSED) {
                if (!chromecastPlayer.play()) {
                    onError("Error while resuming!");
                    next();
                    return;
                }

                playerState = Player.STATE_PLAYING;

                if (service != null) service.updateNotification();
                updateListenersState();
            }
        }
    }

    @Override
    public void play(int position) {
        if (position < playlist.length()) {
            playlist.setCurrentPosition(position);

            this.playerState = Player.STATE_STOPPED; // Fix for play (can't call stop, causes bug!)
            this.play();
        }
    }

    @Override
    public void setNext(int position) {
        playlist.setNext(position);
    }

    @Override
    public void seekTo(int percentage) {
        if (!this.chromecastPlayer.seek(this.chromecastPlayer.getMediaDuration() * percentage / 100))
            onError("Cannot seek!");
    }

    @Override
    public int getCurrentPosition() {
        if (playerState != Player.STATE_STOPPED && chromecastPlayer != null)
            return chromecastPlayer.getCurrentMediaPosition();
        return 0;
    }

    @Override
    public int getDuration() {
        if (playerState != Player.STATE_STOPPED && chromecastPlayer != null)
            return chromecastPlayer.getMediaDuration();
        return 0;
    }

    @Override
    public void next() {
        if (playlist.next()) play(playlist.getCurrentPosition());
        else if (playerState == Player.STATE_PAUSED || playerState == Player.STATE_PLAYING) {
            stop();
            ControllerImpl.getInstance().stopPlayers();
        }
    }

    @Override
    public void prev() {
        if (playlist.prev()) play(playlist.getCurrentPosition());
        else if (playerState == Player.STATE_PAUSED || playerState == Player.STATE_PLAYING) {
            stop();
            ControllerImpl.getInstance().stopPlayers();
        }
    }

    @Override
    public void pause() {
        if (playerState == Player.STATE_PLAYING) {
            if (!chromecastPlayer.pause()) onError("Cannot pause!");

            playerState = Player.STATE_PAUSED;

            if (service != null) service.updateNotification();
            updateListenersState();
        }
    }

    @Override
    public void stop() {
        if (playerState == Player.STATE_PLAYING || playerState == Player.STATE_PAUSED) {
            stopUiTimer();
            chromecastPlayer.stop();
            playerState = Player.STATE_STOPPED;

            if (service != null) service.hideNotification();
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
        stopUiTimer();

        playerState = Player.STATE_UNINITIALIZED;

        if (service != null) service.hideNotification();
        updateListenersState();
    }

    @Override
    public int getCurrentState() {
        return playerState;
    }

    @Override
    public int getKind() {
        return Player.KIND_CHROMECAST;
    }

    @Override
    public void audioFocus(boolean status) {
        if (playerState == Player.STATE_PLAYING) chromecastPlayer.setVolume(status ? 1.f : .1f);
    }

    private void startPlayer(int positionMs) {
        if (!chromecastPlayer.playAndSeek(playlist.getCurrentMediaQueueItem(), positionMs)) {
            onError("Cannot play item!");
            ControllerImpl.getInstance().stopPlayers();
            return;
        }

        playerState = Player.STATE_BUFFERING;

        if (service != null) service.updateNotification();
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

    private void onError(String err) {
        stopUiTimer();

        if (service != null) service.showError(err);
    }

    private void updateListenersState() {
        for (PlayerListener listener : listeners)
            listener.stateChanged();
    }

    private void updateListenersTime() {
        int time = chromecastPlayer.getCurrentMediaPosition();
        int total = chromecastPlayer.getMediaDuration();
        for (PlayerListener listener : listeners)
            listener.timeChanged(time, total);
    }

    @Override
    public void onCompleteSong() {
        if (playerState == Player.STATE_PLAYING) next();
    }

    @Override
    public void onPrepared() {
        playerState = Player.STATE_PLAYING;
        updateListenersState();
        if (service != null) service.updateNotification();

        startUiTimer();
    }
}
