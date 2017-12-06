package com.panni.mymusicplayer2.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallback;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
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
public class SearchFragment extends BaseFragment implements DataCallback, SwipeRefreshLayout.OnRefreshListener {

    private FragmentController fragmentController;

    private EditText text;
    private Button button;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Parcelable listViewState;

    private SongFolderAdapter adapter;


    public static BaseFragment create() {
        return new SearchFragment();
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
        // TODO efficient method?
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.fragmentController = ((MainActivity)getActivity()).getFragmentController();

        this.swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swyperefreshsearch);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        ListView list = (ListView) getView().findViewById(R.id.searchlistView);
        registerForContextMenu(list);

        this.text = (EditText) getView().findViewById(R.id.searchtext); // TODO search?
        this.text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    button.performClick();
                    return true;
                }
                return false;
            }
        });

        this.button = (Button) getView().findViewById(R.id.searchbutton);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch(false, true);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        doSearch(false, false);
    }

    @Override
    public void onPause() {
        ListView list = (ListView) getView().findViewById(R.id.searchlistView);
        this.listViewState = list.onSaveInstanceState();

        super.onPause();
    }

    private void doSearch(boolean force, boolean fromButton) {
        this.button.setEnabled(false);
        this.swipeRefreshLayout.setRefreshing(true);

        if (fromButton) listViewState = null;

        // If it is a normal search (button click) or lastSearch isn't initialized yet
        if (text.getText() != null && !text.getText().toString().equals("")) {
            ControllerImpl.getInstance().search(text.getText().toString(), SearchFragment.this, force);
        } else {
            this.button.setEnabled(true);
            this.swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void newData(final DbObject[] objects) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                swipeRefreshLayout.setRefreshing(false);

                View view = getView();
                if (view == null) return;

                ListView list = (ListView) view.findViewById(R.id.searchlistView);
                if (list == null) return;

                adapter = new SongFolderAdapter(getContext(), objects);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new ClickOnMusicListViewListener(fragmentController, objects));
                //list.setOnItemLongClickListener(new LongClickOnMusicListViewListener(getActivity(), objects));

                if (listViewState != null)
                    list.onRestoreInstanceState(listViewState);
            }
        });
    }

    @Override
    public void onRefresh() {
        this.doSearch(true, true);
    }

    @Override
    public int getType() {
        return BaseFragment.TYPE_SEARCH;
    }
}
