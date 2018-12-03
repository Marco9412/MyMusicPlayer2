package com.panni.mymusicplayer2.controller.fragments;

import android.support.v4.app.Fragment;

import com.panni.mymusicplayer2.view.fragments.BaseFragment;

/**
 * A class used to handle a stack view in fragments
 * Created by marco on 20/05/16.
 */
public interface FragmentController {

    /**
     * Shows a new fragment in current FrameView (passed on constructor).
     * @param fragment the fragment to show.
     */
    void newFragment(BaseFragment fragment);

    /**
     * Goes back on fragments timeline.
     * @return true if there was a fragment to return to, else otherwise.
     */
    boolean back();

    /**
     * Clears fragments stack, and shows this fragment.
     * @param fragment the fragment to show.
     */
    void reset(BaseFragment fragment);

    void addFragmentControllerListener(FragmentControllerListener listener);

    void removeFragmentControllerListener();

    /**
     * Returns the shown fragment.
     * @return the shown fragment.
     */
    Fragment getCurrent();
}
