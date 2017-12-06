package com.panni.mymusicplayer2.view.listeners;

import android.view.View;
import android.widget.AdapterView;

import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.view.fragments.SongListFragment;

import objects.DbObject;
import objects.Folder;
import objects.Song;

/**
 * Click listener used in SongListFragment or SearchFragment
 * -> when clicking on a song default action is enqueue and play!
 */
public class ClickOnMusicListViewListener implements AdapterView.OnItemClickListener {

    private FragmentController fragmentController;
    private DbObject[] objects;

    public ClickOnMusicListViewListener(FragmentController fragmentController, DbObject[] objects) {
        this.fragmentController = fragmentController;
        this.objects = objects;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DbObject current = objects[position];

        if (current instanceof Folder) {
            if (current.equals(ControllerImpl.FOLDER_PLACEHOLDER)) {
                fragmentController.back(); // go back!
            } else {
                // Change folder!
                fragmentController.newFragment(SongListFragment.create((Folder) current));
            }
        } else { // Is a Song!
            // Enqueue and play!
            ControllerImpl.getInstance().getCurrentQueue().enqueue((Song) current);
            ControllerImpl.getInstance().getCurrentPlayer().play(ControllerImpl.getInstance().getCurrentQueue().length() - 1);
        }
    }
}
