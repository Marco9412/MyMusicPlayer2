package com.panni.mymusicplayer2.controller.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.panni.mymusicplayer2.view.fragments.BaseFragment;

import java.util.Stack;

/**
 * Created by marco on 20/05/16.
 */
public class StackFragmentController implements FragmentController {

    private Stack<BaseFragment> fragmentsStack = new Stack<>();
    private FragmentManager fragmentManager;

    private FragmentControllerListener listener;

    private int resId;

    public StackFragmentController(FragmentManager manager, int resId, BaseFragment current) {
        this.fragmentManager = manager;
        this.resId = resId;

        if (manager.getFragments() == null || manager.getFragments().size() == 0)
            this.showFirstFragment(current);
    }

    private void showFirstFragment(BaseFragment current) {
        this.fragmentManager.beginTransaction().add(this.resId, current).commit();
        this.fragmentsStack.push(current);
    }

    private int doTransactionReplace(BaseFragment current, boolean open) {
        return this.fragmentManager.beginTransaction()
                .setTransition(open ? FragmentTransaction.TRANSIT_FRAGMENT_OPEN : FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(this.resId, current)
                .commit();
    }

    @Override
    public void newFragment(BaseFragment fragment) {
        doTransactionReplace(fragment, true);
        this.fragmentsStack.push(fragment);

        update(fragment);
    }

    @Override
    public boolean back() {
        if (this.fragmentsStack.size() > 1) {
            this.fragmentsStack.pop();
            doTransactionReplace(this.fragmentsStack.peek(), false);

            update(this.fragmentsStack.peek());
            return true;
        }
        return false;
    }

    @Override
    public void reset(BaseFragment fragment) {
        this.fragmentsStack.clear();
        this.fragmentManager.beginTransaction().replace(this.resId, fragment).commit();
        this.fragmentsStack.push(fragment);

        update(fragment);
    }

    @Override
    public void addFragmentControllerListener(FragmentControllerListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeFragmentControllerListener() {
        this.listener = null;
    }

    @Override
    public Fragment getCurrent() {
        return this.fragmentsStack.peek();
    }

    private void update(BaseFragment fragment) {
        if (listener != null) {
            listener.fragmentChanged(fragment.getType());
            /*
            if (fragment instanceof PlayerFragment)
                listener.fragmentChanged(BaseFragment.TYPE_PLAYER);
            else if (fragment instanceof SongListFragment)
                listener.fragmentChanged(BaseFragment.TYPE_FOLDER);
            else if (fragment instanceof LocalSongListFragment)
                listener.fragmentChanged(BaseFragment.TYPE_LOCAL_SONG);
            else if (fragment instanceof CustomSongListFragment)
                listener.fragmentChanged(BaseFragment.TYPE_CUSTOM_SONG);
            else if (fragment instanceof SearchFragment)
                listener.fragmentChanged(BaseFragment.TYPE_SEARCH);
            */
        }
    }
}
