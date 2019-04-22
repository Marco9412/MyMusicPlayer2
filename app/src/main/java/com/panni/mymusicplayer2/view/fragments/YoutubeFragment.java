package com.panni.mymusicplayer2.view.fragments;

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

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallbackPlaylist;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.view.adapters.CustomSongAdapter;

/**
 * Created by marco on 15/05/16.
 */
public class YoutubeFragment extends BaseFragment implements DataCallbackPlaylist, SwipeRefreshLayout.OnRefreshListener {

    //private FragmentController fragmentController;

    private EditText text;
    private Button button;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Parcelable listViewState;

    private CustomSongAdapter adapter;


    public static BaseFragment create() {
        return new YoutubeFragment();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater menuInflater = getActivity().getMenuInflater();

        MyQueueItem current = adapter.getItem(infos.position);

        menuInflater.inflate(R.menu.longclickyoutubesonglistmenu, menu);
        menu.setHeaderTitle(current.getTitle());
        //menu.setHeaderIcon(Utils.mimeTypeToIconResource(current.getMimeType())); // TODO youtube icon!
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MyQueueItem current = adapter.getItem(infos.position);

        switch (item.getItemId()) {
            // Folder
            /*case R.id.enqueuefolder:
                ControllerImpl.getInstance().enqueueFolder((Folder)current, false, false);
                break;
            case R.id.enqueueplayfolder:
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                //ControllerImpl.getInstance().enqueueFolder((Folder)objects[position], false, true);
                break;
            case R.id.downloadfolder:
                ControllerImpl.getInstance().downloadFolder(getActivity(), (Folder)current);
                break;
            case R.id.getm3u:
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                break;*/

            // Song
            case R.id.enqueuesong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue(current);
                break;
            case R.id.enqueueplaysong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue(current);
                ControllerImpl.getInstance().getCurrentPlayer().play(ControllerImpl.getInstance().getCurrentQueue().length() - 1);
                break;
            /*case R.id.playextsong:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(ControllerImpl.getInstance().getHttpSongUrl(current)), "audio/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;*/
            /*case R.id.downloadsong:
                ControllerImpl.getInstance().downloadSong(getActivity(), current);
                break;
            case R.id.sharesong:
                ControllerImpl.getInstance().shareQueueItem(getActivity(), MyQueueItem.create((Song) current));
                break;*/
        }

        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.youtube_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //this.fragmentController = ((MainActivity)getActivity()).getFragmentController();

        this.swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.ytswyperefreshsearch);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        ListView list = (ListView) getView().findViewById(R.id.ytsearchlistView);
        registerForContextMenu(list);

        this.text = (EditText) getView().findViewById(R.id.ytsearchtext);
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

        this.button = (Button) getView().findViewById(R.id.ytsearchbutton);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doYtSearch(false, true);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        doYtSearch(false, false);
    }

    @Override
    public void onPause() {
        ListView list = (ListView) getView().findViewById(R.id.ytsearchlistView);
        this.listViewState = list.onSaveInstanceState();

        super.onPause();
    }

    private void doYtSearch(boolean force, boolean fromButton) {
        this.button.setEnabled(false);
        this.swipeRefreshLayout.setRefreshing(true);

        if (fromButton) listViewState = null;

        // If it is a normal search (button click) or lastSearch isn't initialized yet
        if (text.getText() != null && !text.getText().toString().equals("")) {
            ControllerImpl.getInstance().ytSearch(text.getText().toString(), YoutubeFragment.this, force);
        } else {
            this.button.setEnabled(true);
            this.swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        this.doYtSearch(true, true);
    }

    @Override
    public int getType() {
        return BaseFragment.TYPE_YOUTUBE;
    }

    @Override
    public void newData(final MyQueueItem[] items) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);

                    View view = getView();
                    if (view == null) return;

                    ListView list = (ListView) view.findViewById(R.id.ytsearchlistView);
                    if (list == null) return;

                    adapter = new CustomSongAdapter(getContext(), items);
                    list.setAdapter(adapter);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Controller controller = ControllerImpl.getInstance();
                            // Enqueue and play
                            controller.getCurrentQueue().enqueue(items[position]);
                            controller.getCurrentPlayer().play(controller.getCurrentQueue().length() - 1);
                        }
                    });
                    //list.setOnItemLongClickListener(new LongClickOnMusicListViewListener(getActivity(), objects));

                    if (listViewState != null)
                        list.onRestoreInstanceState(listViewState);
                }
            });
    }
}
