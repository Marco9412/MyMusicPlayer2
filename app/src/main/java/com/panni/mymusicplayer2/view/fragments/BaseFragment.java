package com.panni.mymusicplayer2.view.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by marco on 13/07/16.
 */
public abstract class BaseFragment extends Fragment {

    // DONT'T change these numbers! The order is the same of the navigation menu's one
    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_LOCAL_SONG = 1;
    public static final int TYPE_CUSTOM_SONG = 2;
    public static final int TYPE_FOLDER = 3;
    public static final int TYPE_SEARCH = 4;
    public static final int TYPE_YOUTUBE = 5;

    public abstract int getType();
}
