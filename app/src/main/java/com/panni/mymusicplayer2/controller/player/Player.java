package com.panni.mymusicplayer2.controller.player;

import com.panni.mymusicplayer2.controller.player.service.PlayerService;

import java.util.LinkedList;

/**
 * Created by marco on 28/05/16.
 */
public interface Player {

    // Player Kind
    int KIND_UNINITIALIZED = 0;
    int KIND_LOCAL = 1;
    int KIND_CHROMECAST = 2;

    // Player State
    int STATE_PLAYING = 0;
    int STATE_PAUSED = 1;
    int STATE_STOPPED = 2;
    int STATE_BUFFERING = 3;
    int STATE_UNINITIALIZED = 4;

    void play();

    void play(int position);

    /**
     * Starts to play current song and skips to positionMs time
     * @param positionMs the time to start (milliseconds)
     */
    void playAndSeek(int positionMs);

    void setNext(int position);

    void seekTo(int percentage);

    /**
     * @return the current position in ms of the media played, or 0 if it is stopped.
     */
    int getCurrentPosition();

    int getDuration();

    void next();

    void prev();

    void pause();

    void stop();

    LinkedList<PlayerListener> getPlayerListeners();

    void addPlayerListener(PlayerListener playerListener);

    void removePlayerListener(PlayerListener playerListener);

    void setService(PlayerService service);

    /**
     * Tells this player to deallocate itself (a player change event is called)
     */
    void delete();

    int getCurrentState();

    int getKind();

    void audioFocus(boolean status);
}
