package com.panni.mymusicplayer2.controller.player;

/**
 * Created by marco on 07/06/16.
 */
public interface PlayerListener {

    void stateChanged();

    //void songChanged(); // useless

    void timeChanged(int current, int total);
}
