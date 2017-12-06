package com.panni.mymusicplayer2.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallback;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.utils.DbObjectParcellator;
import com.panni.mymusicplayer2.utils.Utils;
import com.panni.mymusicplayer2.view.MainActivity;
import com.panni.mymusicplayer2.view.adapters.SongFolderAdapter;
import com.panni.mymusicplayer2.view.listeners.ClickOnMusicListViewListener;

import objects.DbObject;
import objects.Folder;
import objects.Song;

/**
 * Created by marco on 15/05/16.
 */
public class SongListFragment extends BaseFragment implements DataCallback, SwipeRefreshLayout.OnRefreshListener {

    final public static String ROOT_VIEW_KEY = "root_view";

    // Parameters
    private boolean showsRootFolders;
    private Folder currentFolder;

    // Objects
    private SongFolderAdapter adapter;
    private FragmentController fragmentController;

    // View data
    private SwipeRefreshLayout swipeRefreshLayout;

    //private GestureDetector gestureDetector;

    private Parcelable listViewState;

    /**
     * Creates a new SongListFragment which will show root folders.
     *
     * @return
     */
    public static BaseFragment create() {
        SongListFragment fragment = new SongListFragment();
        Bundle b = new Bundle();
        b.putBoolean(SongListFragment.ROOT_VIEW_KEY, true);
        fragment.setArguments(b);

        return fragment;
    }


    // TODO swype left back button

    /**
     * Creates a new SongListFragment which will show "toShow" folder.
     *
     * @param toShow
     * @return
     */
    public static BaseFragment create(Folder toShow) {
        SongListFragment fragment = new SongListFragment();
        Bundle b = new Bundle();
        DbObjectParcellator.pushToBundle(toShow, b);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater menuInflater = getActivity().getMenuInflater();

        DbObject current = adapter.getItem(infos.position);

        if (current instanceof Song) {
            menuInflater.inflate(R.menu.longclicklistsongmenu, menu);
            menu.setHeaderTitle(((Song) current).getTitle());
            menu.setHeaderIcon(Utils.mimeTypeToIconResource(((Song) current).getMimeType()));
        } else if (current instanceof Folder) {
            menuInflater.inflate(R.menu.longclickfoldermenu, menu);
            menu.setHeaderTitle(current.getName());
            menu.setHeaderIcon(R.drawable.ic_menu_folder);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DbObject current = adapter.getItem(infos.position);

        switch (item.getItemId()) {
            // Folder
            case R.id.enqueuefolder:
                ControllerImpl.getInstance().enqueueFolder((Folder)current, false, false);
                break;
            case R.id.enqueueplayfolder:
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                //ControllerImpl.getInstance().enqueueFolder((Folder)objects[position], false, true);
                break;
            case R.id.downloadfolder:
                ControllerImpl.getInstance().downloadFolder(getActivity(), (Folder)current);
                break;
            case R.id.getm3u: // TODO
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                break;

            // Song
            case R.id.enqueuesong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue((Song)current);
                break;
            case R.id.enqueueplaysong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue((Song)current);
                ControllerImpl.getInstance().getCurrentPlayer().play(ControllerImpl.getInstance().getCurrentQueue().length() - 1);
                break;
            case R.id.playextsong:
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(ControllerImpl.getInstance().getHttpSongUrl((Song) current)), "audio/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case R.id.downloadsong:
                ControllerImpl.getInstance().downloadSong(getActivity(), (Song) current);
                break;
            case R.id.sharesong:
                ControllerImpl.getInstance().shareQueueItem(getActivity(), MyQueueItem.create((Song) current));
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
        //this.gestureDetector = ((MainActivity) getActivity()).getGestureDetector();

        // Set listener
        this.swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swyperefreshlist);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        // Context menu
        ListView listView = (ListView) getView().findViewById(R.id.fragmentlistview);
        registerForContextMenu(listView);

        // TODO swipe left as back
//        ListView listView = (ListView) getView().findViewById(R.id.fragmentlistview);
//        listView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        });

        // Loads data from bundle
        parseArguments();

        // Load data to show
        loadData(false);
    }

    @Override
    public void onPause() {
        ListView listView = (ListView) getView().findViewById(R.id.fragmentlistview);
        this.listViewState = listView.onSaveInstanceState();

        super.onPause();
    }

    private void parseArguments() {
        this.showsRootFolders = getArguments().getBoolean(ROOT_VIEW_KEY, false);
        if (!showsRootFolders) {
            this.currentFolder = (Folder) DbObjectParcellator.createFromBundle(getArguments());
        } else {
            this.currentFolder = null;
        }
    }

    private void loadData(boolean force) {
        this.swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        Controller controller = ControllerImpl.getInstance();
        if (showsRootFolders)
            controller.listRootFolders(this, force);
        else
            controller.listFolder(currentFolder, this, force);
    }

    @Override
    public void newData(final DbObject[] objects) {
        // Respond to data received
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Skip if only one root folder!
                    if (showsRootFolders && objects.length == 1 && listViewState == null) {
                        fragmentController.newFragment(SongListFragment.create((Folder) objects[0]));
                    }

                    View v = getView();
                    if (v == null) return;

                    swipeRefreshLayout.setRefreshing(false);
                    ListView listView = (ListView) v.findViewById(R.id.fragmentlistview);
                    if (listView == null) return;

                    adapter = new SongFolderAdapter(getContext(), objects);

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
        return BaseFragment.TYPE_FOLDER;
    }
}
