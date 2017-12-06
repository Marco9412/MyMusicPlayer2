package com.panni.mymusicplayer2.controller;


import android.content.Context;

import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;
import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.settings.Settings;

import objects.Folder;
import objects.Song;

/**
 * Created by marco on 15/05/16.
 */
public interface Controller {

// -------------------------------------------------------------------------------------------------

    /**
     * Returns the current player implementation
     * @return
     */
    Player getCurrentPlayer();

    /**
     * Returns the current playlist
     * @return
     */
    PlayerQueue getCurrentQueue();

    /**
     * Performs a playerchange, moving to whichPlayer and keeping current state
     * @param whichPlayer the player to move to
     */
    void changePlayer(int whichPlayer);

    /**
     * Starts one player (move to default)
     */
    void startAPlayer();

    /**
     * Stop player if it is running
     */
    void stopPlayers();

// -------------------------------------------------------------------------------------------------

    /**
     * Returns the String URL of song
     * @param song
     * @return
     */
    String getHttpSongUrl(Song song);

// -------------------------------------------------------------------------------------------------

    void settingsChanged(Settings currentSettings);

    Settings getCurrentSettings();

// -------------------------------------------------------------------------------------------------

    /**
     * Downloads song s into local app folder.
     * @param context
     * @param s
     */
    void downloadSong(Context context, Song s);

    void downloadFolder(Context context, Folder f);

// -------------------------------------------------------------------------------------------------

    /**
     * Drops service connection (but keeps service running!)
     */
    void unloadService(Context context);

// -------------------------------------------------------------------------------------------------


    void shareQueueItem(Context context, MyQueueItem item);

// -------------------------------------------------------------------------------------------------

    void saveCustomSong(CustomQueueItem item);

    void deleteCustomSong(CustomQueueItem item);

    void removeLocalSong(Song song);

    int getSongSize(int songid);

// -------------------------------------------------------------------------------------------------

    void enqueueFolder(Folder folder, boolean recursive, boolean thenPlay);

// -------------------------------------------------------------------------------------------------
//  List methods
// -------------------------------------------------------------------------------------------------
    //void listSongs(DataCallback callback, boolean forceUpdate);

    void listCustomSongs(DataCallbackPlaylist callback);

    void listLocalSongs(DataCallback callback, boolean forceUpdate);

    void listRootFolders(DataCallback callback, boolean forceUpdate);

    void listFolder(Folder current, DataCallback callback, boolean forceUpdate);

    void search(String query, DataCallback callback, boolean forceUpdate);
// -------------------------------------------------------------------------------------------------

    /**
     * Stops players, closes service and releases resources of this controller
     */
    void quit();
}
