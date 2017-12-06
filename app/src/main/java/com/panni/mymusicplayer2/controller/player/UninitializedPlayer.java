package com.panni.mymusicplayer2.controller.player;

import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.service.PlayerService;

import java.util.LinkedList;

/**
 * Created by marco on 07/06/16.
 */
public class UninitializedPlayer implements Player {
    // Default player -> null

    private LinkedList<PlayerListener> listeners = new LinkedList<>();

    @Override
    public void play() {
            if (ControllerImpl.getInstance().getCurrentQueue().length() > 0) {
                ControllerImpl.getInstance().startAPlayer();
                ControllerImpl.getInstance().getCurrentPlayer().play();
            }
    }

    @Override
    public void play(int position) {
            ControllerImpl.getInstance().getCurrentQueue().setCurrentPosition(position);
            ControllerImpl.getInstance().startAPlayer();
            ControllerImpl.getInstance().getCurrentPlayer().play();
    }

    @Override
    public void playAndSeek(int positionMs) {
        play(); // Not used!
    }

    @Override
    public void setNext(int position) {
        ControllerImpl.getInstance().getCurrentQueue().setNext(position);
    }

    @Override
    public void seekTo(int percentage) {
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public void next() {
        ControllerImpl.getInstance().getCurrentQueue().next();
    }

    @Override
    public void prev() {
        ControllerImpl.getInstance().getCurrentQueue().prev();
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public LinkedList<PlayerListener> getPlayerListeners() {
        return listeners;
    }

    @Override
    public void addPlayerListener(PlayerListener playerListener) {
        if (!listeners.contains(playerListener))
            listeners.add(playerListener);
    }

    @Override
    public void removePlayerListener(PlayerListener playerListener) {
        listeners.remove(playerListener);
    }

    @Override
    public void setService(PlayerService service) {
    }

    @Override
    public void delete() {
    }

    @Override
    public void audioFocus(boolean status) {

    }

    @Override
    public int getCurrentState() {
        return Player.STATE_UNINITIALIZED;
    }

    @Override
    public int getKind() {
        return Player.KIND_UNINITIALIZED;
    }
}
