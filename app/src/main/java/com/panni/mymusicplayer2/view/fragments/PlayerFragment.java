package com.panni.mymusicplayer2.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.Controller;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.controller.player.PlayerListener;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;
import com.panni.mymusicplayer2.model.queue.QueueListener;
import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.SongQueueItem;
import com.panni.mymusicplayer2.utils.Utils;
import com.panni.mymusicplayer2.view.adapters.PlaylistAdapter;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by marco on 15/05/16.
 */
public class PlayerFragment extends BaseFragment implements QueueListener, PlayerListener {

    private PlaylistAdapter adapter;

    public static BaseFragment create() {
        return new PlayerFragment();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        //menuInflater.inflate(R.menu.longclickplaylistmenu, menu);

        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;

        MyQueueItem current = adapter.getItem(infos.position);
        if (current.isCustom()) {
            menuInflater.inflate(R.menu.longclickplaylistmenu, menu);
            menu.setHeaderTitle(current.getTitle());
            menu.setHeaderIcon(R.drawable.ic_router_black_24dp);
            menu.removeItem(R.id.downloadfolder);
        } else if (current.isYoutube()) {
            menuInflater.inflate(R.menu.longclickplaylistmenu, menu); // TODO another xml!
            menu.setHeaderTitle(current.getTitle());
            menu.setHeaderIcon(R.drawable.ic_logo_of_youtube);
        } else {
            menuInflater.inflate(R.menu.longclickplaylistmenu, menu);
            menu.setHeaderTitle(current.getTitle());
            menu.setHeaderIcon(Utils.mimeTypeToIconResource(((SongQueueItem) current).getMimeType()));
        }

        if (infos.position == 0) menu.removeItem(R.id.moveupsong);
        if (infos.position == ControllerImpl.getInstance().getCurrentQueue().length() - 1) menu.removeItem(R.id.movedownsong);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = infos.position;

        MyQueueItem current = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.setnextsong:
                ControllerImpl.getInstance().getCurrentPlayer().setNext(position);
                break;
            case R.id.removesong:
                ControllerImpl.getInstance().getCurrentQueue().removeFromQueue(position);
                break;
            case R.id.downloadsong: // If custom never executed!
                ControllerImpl.getInstance().downloadSong(getContext(), current.toSong());
                break;
            case R.id.sharesong:
                ControllerImpl.getInstance().shareQueueItem(getActivity(), current);
                break;
            case R.id.moveupsong:
                ControllerImpl.getInstance().getCurrentQueue().moveUp(position);
                break;
            case R.id.movedownsong:
                ControllerImpl.getInstance().getCurrentQueue().moveDown(position);
                break;
        }

        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SeekBar perc = (SeekBar) getView().findViewById(R.id.playerseekbar);
        perc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ControllerImpl.getInstance().getCurrentPlayer().seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ListView songlist = (ListView) getView().findViewById(R.id.listPlayer);
        registerForContextMenu(songlist);

        ImageButton bplay = (ImageButton) getView().findViewById(R.id.buttonplay);
        bplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ControllerImpl.getInstance().getCurrentPlayer().getCurrentState() == Player.STATE_PLAYING)
                    ControllerImpl.getInstance().getCurrentPlayer().pause();
                else
                    ControllerImpl.getInstance().getCurrentPlayer().play();
            }
        });
        ImageButton bprev = (ImageButton) getView().findViewById(R.id.buttonprev);
        bprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControllerImpl.getInstance().getCurrentPlayer().prev();
            }
        });
        ImageButton bnext = (ImageButton) getView().findViewById(R.id.buttonnext);
        bnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControllerImpl.getInstance().getCurrentPlayer().next();
            }
        });
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        //setHasOptionsMenu(true); TODO different menus! (but ccl?)
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onStart() {
        super.onStart();

        playlistChanged();
        //songChanged();
        stateChanged();

        ControllerImpl.getInstance().getCurrentPlayer().addPlayerListener(this);
        ControllerImpl.getInstance().getCurrentQueue().addPlaylistListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        Controller con = ControllerImpl.getInstance();
        if (con != null) {
            con.getCurrentPlayer().removePlayerListener(this);
            con.getCurrentQueue().removePlaylistListener(this);
        }
    }

    private void setButtonsEnabled(boolean status) {
        View view = getView();
        if (view == null) return;

        ImageButton bplay = (ImageButton) view.findViewById(R.id.buttonplay);
        //ImageButton bprev = (ImageButton) view.findViewById(R.id.buttonprev);
        //ImageButton bnext = (ImageButton) view.findViewById(R.id.buttonnext);
        SeekBar perc = (SeekBar) view.findViewById(R.id.playerseekbar);

        bplay.setEnabled(status);
        //bprev.setEnabled(status);
        //bnext.setEnabled(status);
        perc.setEnabled(status);
    }

    @Override
    public void playlistChanged() {
        FragmentActivity activity = getActivity();
        if (activity != null) activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playlistChangedAux();
            }
        });
    }

    @Override
    public void stateChanged() {
        FragmentActivity activity = getActivity();
        if (activity != null) activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateChangedAux();
            }
        });
    }

//    @Override
//    public void songChanged() {
//        FragmentActivity activity = getActivity();
//        if (activity != null) activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                songChangedAux();
//            }
//        });
//    }

    @Override
    public void timeChanged(final int current, final int total) {
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeChangedAux(current, total);
                }
            });
    }

    private void playlistChangedAux() {
        Controller controller = ControllerImpl.getInstance();
        PlayerQueue playlist = controller.getCurrentQueue();

        ListView songlist = (ListView) getView().findViewById(R.id.listPlayer);
        //Parcelable listViewState = songlist.onSaveInstanceState();
        adapter = new PlaylistAdapter(
                getContext(),
                playlist.getItems(),
                controller.getCurrentPlayer().getCurrentState() == Player.STATE_STOPPED ?
                        -1 : playlist.getCurrentPosition(),
                playlist.getNext());
        songlist.setAdapter(adapter);

        songlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ControllerImpl.getInstance().getCurrentPlayer().play(position);
            }
        });

        //songlist.onRestoreInstanceState(listViewState);
        songlist.setSelection(playlist.getCurrentPosition());

        songChangedAux();
    }

    private void stateChangedAux() {
        View view = getView();
        if (view == null) return;

        GifImageView loadingImage = (GifImageView) view.findViewById(R.id.loadingimage);
        TextView currentTime = (TextView) view.findViewById(R.id.currentTimeText);
        ImageButton bplay = (ImageButton) view.findViewById(R.id.buttonplay);
        SeekBar perc = (SeekBar) view.findViewById(R.id.playerseekbar);

        if (currentTime == null || bplay == null || perc == null) return;

        switch (ControllerImpl.getInstance().getCurrentPlayer().getCurrentState()) {
            case Player.STATE_PLAYING:
                bplay.setImageResource(R.drawable.ic_pause_circle_grey_80dp);
                setButtonsEnabled(true);
                loadingImage.setVisibility(View.INVISIBLE);
                break;
            case Player.STATE_PAUSED:
                bplay.setImageResource(R.drawable.ic_play_circle_grey_80dp);
                setButtonsEnabled(true);
                perc.setEnabled(false);
                loadingImage.setVisibility(View.INVISIBLE);
                break;
            case Player.STATE_STOPPED:
                bplay.setImageResource(R.drawable.ic_play_circle_grey_80dp);
                currentTime.setText(Utils.timeIntToString(0));
                setButtonsEnabled(true);
                loadingImage.setVisibility(View.INVISIBLE);
                break;
            case Player.STATE_BUFFERING:
                setButtonsEnabled(false);
                loadingImage.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_UNINITIALIZED:
                bplay.setImageResource(R.drawable.ic_play_circle_grey_80dp);
                setButtonsEnabled(true);
                perc.setEnabled(false);
                timeChangedAux(0, 0);
                loadingImage.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void songChangedAux() {
        View view = getView();
        if (view == null) return;

        PlayerQueue playlist = ControllerImpl.getInstance().getCurrentQueue();

        TextView title = (TextView) view.findViewById(R.id.playerTitle);
        TextView artist = (TextView) view.findViewById(R.id.playerArtist);
        ListView songlist = (ListView) view.findViewById(R.id.listPlayer);

        MyQueueItem item = playlist.getCurrentItem();

        if (title == null || artist == null || songlist == null) return;

        // Update title and artist
        title.setText(item.getTitle());
        artist.setText(item.getArtist());

        // Update playlist
        ((PlaylistAdapter) songlist.getAdapter()).setCurrent(playlist.getCurrentPosition());

        //stateChangedAux();
    }

    private void timeChangedAux(int current, int total) {
        View view = getView();
        if (view == null) return;

        TextView currentTime = (TextView) view.findViewById(R.id.currentTimeText);
        TextView totalTime = (TextView) view.findViewById(R.id.totalTimeText);
        SeekBar perc = (SeekBar) view.findViewById(R.id.playerseekbar);

        if (currentTime == null || totalTime == null || perc == null) return;

        if (total > 0) {
            currentTime.setText(Utils.timeIntToString(current));
            totalTime.setText(Utils.timeIntToString(total));

            perc.setProgress(current * 100 / total);
        } else {
            currentTime.setText("--:--");
            totalTime.setText("--:--");
            perc.setProgress(0);
            perc.setEnabled(false);
        }
    }

    @Override
    public int getType() {
        return BaseFragment.TYPE_PLAYER;
    }
}
