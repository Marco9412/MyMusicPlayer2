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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallbackPlaylist;
import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.view.adapters.CustomSongAdapter;

/**
 * Created by marco on 15/05/16.
 */
public class CustomSongListFragment extends BaseFragment implements DataCallbackPlaylist, SwipeRefreshLayout.OnRefreshListener {

    // Objects
    private CustomSongAdapter adapter;

    // View data
    private SwipeRefreshLayout swipeRefreshLayout;

    private Parcelable listViewState;

    /**
     * Creates a new CustomSongListFragment which will show local custom songs.
     * @return
     */
    public static BaseFragment create() {
        return new CustomSongListFragment();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.longclickcustomsonglistmenu, menu);

        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;

        MyQueueItem current= adapter.getItem(infos.position - 1);
        menu.setHeaderTitle(current.getTitle());
        menu.setHeaderIcon(R.drawable.ic_router_black_24dp);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final MyQueueItem current = adapter.getItem(infos.position - 1);

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
                intent.setDataAndType(Uri.parse(current.getUrl()), "audio/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case R.id.deletesong:
                new AlertDialog.Builder(getContext())
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this custom song?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ControllerImpl.getInstance().deleteCustomSong((CustomQueueItem) current);
                                onRefresh();
                            }
                        })
                        .create().show();
                break;
            case R.id.sharesong:
                controller.shareQueueItem(getActivity(), current);
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
        controller.listCustomSongs(this);
    }

    @Override
    public void newData(final MyQueueItem[] objects) {
        // Respond to data received
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                View view = getView();
                if (view == null) return;

                final ListView listView = (ListView) view.findViewById(R.id.fragmentlistview);
                if (listView == null) return;

                if (listView.getHeaderViewsCount() == 0) {
                    View header = getLayoutInflater(null).inflate(R.layout.customsonglist_header, null);
                    header.findViewById(R.id.customsonglist_header_text).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final View view = getLayoutInflater(null).inflate(R.layout.addcustomsong_layout, null);

                            new AlertDialog.Builder(getContext())
                                    .setView(view)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String title = ((EditText)view.findViewById(R.id.addcustomsong_title)).getText().toString();
                                            String url = ((EditText)view.findViewById(R.id.addcustomsong_url)).getText().toString();
                                            if (!title.equals("") && !url.equals("")) {
                                                ControllerImpl.getInstance().saveCustomSong((CustomQueueItem) MyQueueItem.create(title, url));
                                                Toast.makeText(getContext(), "Custom source saved!", Toast.LENGTH_SHORT).show();
                                                onRefresh();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create().show();
                        }
                    });
                    listView.addHeaderView(header);
                }

                adapter = new CustomSongAdapter(getContext(), objects);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Controller controller = ControllerImpl.getInstance();
                        // Enqueue and play
                        controller.getCurrentQueue().enqueue(objects[position-1]);
                        controller.getCurrentPlayer().play(controller.getCurrentQueue().length() - 1);
                    }
                });

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
        return BaseFragment.TYPE_CUSTOM_SONG;
    }
}
