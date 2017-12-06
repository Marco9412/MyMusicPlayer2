package com.panni.mymusicplayer2.view.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallback;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.utils.Utils;
import com.panni.mymusicplayer2.view.MainActivity;
import com.panni.mymusicplayer2.view.adapters.LocalSongAdapter;
import com.panni.mymusicplayer2.view.listeners.ClickOnMusicListViewListener;

import objects.DbObject;
import objects.Song;

/**
 * Created by marco on 15/05/16.
 */
public class LocalSongListFragment extends BaseFragment implements DataCallback, SwipeRefreshLayout.OnRefreshListener {

    // Objects
    private LocalSongAdapter adapter;
    private FragmentController fragmentController;

    // View data
    private SwipeRefreshLayout swipeRefreshLayout;

    private Parcelable listViewState;

    /**
     * Creates a new LocalSongListFragment which will show local songs.
     *
     * @return
     */
    public static BaseFragment create() {
        return new LocalSongListFragment();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.longclicklocalsonglistmenu, menu);

        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Song current= (Song)adapter.getItem(infos.position);
        menu.setHeaderTitle(current.getTitle());
        menu.setHeaderIcon(Utils.mimeTypeToIconResource(current.getMimeType()));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final Song current= (Song)adapter.getItem(infos.position);
        Controller controller = ControllerImpl.getInstance();

        switch (item.getItemId()) {
            case R.id.enqueuesong:
                controller.getCurrentQueue().enqueue(current);
                break;
            case R.id.enqueueplaysong:
                controller.getCurrentQueue().enqueue(current);
                controller.getCurrentPlayer().play(controller.getCurrentQueue().length() - 1);
                break;
            case R.id.playextsong:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(controller.getHttpSongUrl(current)), "audio/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case R.id.deletesong:
                new AlertDialog.Builder(getContext())
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this song?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //LocalSongManager.getInstance().removeLocalSong(current);
                                ControllerImpl.getInstance().removeLocalSong(current);
                                onRefresh();
                            }
                        })
                        .create().show();
                break;
            case R.id.sharesong:
                controller.shareQueueItem(getActivity(), MyQueueItem.create(current));
                break;
        }

        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState); // TODO efficient method?
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.fragmentController = ((MainActivity) getActivity()).getFragmentController();

        // Set listener
        this.swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swyperefreshlist);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        ListView listView = (ListView) getView().findViewById(R.id.fragmentlistview);
        registerForContextMenu(listView);

        // Load data to show
        loadData(false);
    }

    @Override
    public void onPause() {
        ListView listView = (ListView) getView().findViewById(R.id.fragmentlistview);
        this.listViewState = listView.onSaveInstanceState();

        super.onPause();
    }

    private void loadData(boolean force) {
        this.swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        Controller controller = ControllerImpl.getInstance();
        controller.listLocalSongs(this, force);
    }

    @Override
    public void newData(final DbObject[] objects) {
        // Respond to data received
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    View view = getView();
                    if (view == null) return;

                    ListView listView = (ListView) view.findViewById(R.id.fragmentlistview);
                    if (listView == null) return;

                    adapter = new LocalSongAdapter(getContext(), objects);

                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(
                            new ClickOnMusicListViewListener(fragmentController, objects));

                    if (listViewState != null) {
                        listView.onRestoreInstanceState(listViewState);
                    }
                }
            });
    }

    @Override
    public void onRefresh() {
        this.loadData(true);
    }

    @Override
    public int getType() {
        return BaseFragment.TYPE_LOCAL_SONG;
    }
}
