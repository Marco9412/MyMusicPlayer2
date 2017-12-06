package com.panni.mymusicplayer2.controller;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.panni.mymusicplayer2.controller.audio.AudioFocusListener;
import com.panni.mymusicplayer2.controller.audio.InputButtonReceiver;
import com.panni.mymusicplayer2.controller.audio.RemoteControlClientPlayerListener;
import com.panni.mymusicplayer2.controller.chromecast.CastConsumer;
import com.panni.mymusicplayer2.controller.localsongs.CustomSongManager;
import com.panni.mymusicplayer2.controller.localsongs.LocalSongManager;
import com.panni.mymusicplayer2.controller.localsongs.SongDownloaderService;
import com.panni.mymusicplayer2.controller.player.ChromecastPlayer;
import com.panni.mymusicplayer2.controller.player.MyExoPlayer;
import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.controller.player.PlayerListener;
import com.panni.mymusicplayer2.controller.player.UninitializedPlayer;
import com.panni.mymusicplayer2.controller.player.service.PlayerService;
import com.panni.mymusicplayer2.controller.queue.QueueManager;
import com.panni.mymusicplayer2.logger.MyErrorLogger;
import com.panni.mymusicplayer2.model.buffer.DataBuffer;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;
import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.settings.Settings;
import com.panni.mymusicplayer2.utils.Utils;

import java.util.LinkedList;

import objects.DbObject;
import objects.Folder;
import objects.Song;
import pymusicmanagerconnector.PyMusicManagerConnector;

/**
 * Created by marco on 15/05/16.
 */
public class ControllerImpl implements Controller, ServiceConnection {

    final public static Folder FOLDER_PLACEHOLDER = new Folder(0, "...", 0, false);

    /// Singleton
    private static ControllerImpl instance;
    private static boolean initialized;

    /// Settings
    private Settings currentSettings;

    /// Requests buffer
    private DataBuffer buffer;

    /// Player in use
    private Player currentPlayer;

    /// Playlist in use
    private PlayerQueue currentQueue;

    /// Service binded to Player
    private PlayerService service;

    /// Application context
    private Context applicationContext;

    /// Tells if the service is binded
    private boolean serviceBinded;

    /// Tells if one player is running
    private boolean playerStarted;

    /// A registered audiofocuslistener
    private AudioFocusListener audioFocusListener;

    /// CastConsumer instance
    private CastConsumer castConsumer;

    /// RemoteControlClient
    @SuppressWarnings("deprecation")
    private RemoteControlClient remoteControlClient;
    private RemoteControlClientPlayerListener remoteControlClientListener;


    /**
     * Returns an existent instance of Controller, or creates a new one using context.
     * @param context application context
     * @return the created controller
     */
    public static ControllerImpl getInstance(Context context) {
        if (instance == null || !initialized) new ControllerImpl(context);
        return instance;
    }

    /**
     * Returns an existent instance of Controller, or null.
     * (To create it you must call getInstance(Context context)!)
     * @return
     */
    public static ControllerImpl getInstance() {
        return instance;
    }

    private ControllerImpl(Context context) {
        instance = this; // I need this pointer!
        initialized = true;

        this.applicationContext = context.getApplicationContext();

        // Fix for old versions
        this.applicationContext.deleteDatabase("LocalSongs.db");

        this.currentSettings = Settings.loadSettings(context);

        MyErrorLogger.getInstance(); // Initialize logger

        LocalSongManager.getInstance(context); // Initialize LocalSongManager
        CustomSongManager.getInstance(context); // Initialize CustomSongManager

        this.currentPlayer = new UninitializedPlayer(); // initialize null player
        loadAndBindService(context); // start playerservice
        playerStarted = false;

        buffer = DataBuffer.getInstance(); // load databuffer

        // Cast API
        this.castConsumer = CastConsumer.getInstance();
        VideoCastManager.getInstance().addVideoCastConsumer(this.castConsumer);

        // load last queue (or an empty one)
        this.currentQueue = new QueueManager(context).loadLastQueue();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void startAPlayer() {
        if (!playerStarted) {

            if (service != null) {
                AudioManager am = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
                audioFocusListener = new AudioFocusListener();
                am.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                am.registerMediaButtonEventReceiver(new ComponentName(applicationContext.getPackageName(), InputButtonReceiver.class.getName()));

                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.setComponent(new ComponentName(service.getApplicationContext().getPackageName(), InputButtonReceiver.class.getName()));
                PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0);
                remoteControlClient = new RemoteControlClient(pendingIntent);
                remoteControlClient.setOnGetPlaybackPositionListener(new RemoteControlClient.OnGetPlaybackPositionListener() {
                    @Override
                    public long onGetPlaybackPosition() {
                        return currentPlayer.getCurrentPosition();
                    }
                });
                remoteControlClient.setPlaybackPositionUpdateListener(new RemoteControlClient.OnPlaybackPositionUpdateListener() {
                    @Override
                    public void onPlaybackPositionUpdate(long l) {
                        // TODO seek
                    }
                });
                remoteControlClient.setTransportControlFlags(
                        RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                );
                remoteControlClientListener = new RemoteControlClientPlayerListener(remoteControlClient);
                am.registerRemoteControlClient(remoteControlClient);
                currentPlayer.addPlayerListener(remoteControlClientListener);
            }

            playerStarted = true;

            if (castConsumer.isConnected())
                changePlayer(Player.KIND_CHROMECAST);
            else
                changePlayer(Player.KIND_LOCAL);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void stopPlayers() {
        if (playerStarted) {
            playerStarted = false;
            moveToUninitialized();
        }

        if (service != null && audioFocusListener != null) {
            AudioManager am = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(audioFocusListener);
            am.unregisterMediaButtonEventReceiver(new ComponentName(applicationContext.getPackageName(), InputButtonReceiver.class.getName()));
            am.unregisterRemoteControlClient(remoteControlClient);
            currentPlayer.removePlayerListener(remoteControlClientListener);
            audioFocusListener = null;
        }
    }

    @Override
    public String getHttpSongUrl(Song song) {
        return String.format(currentSettings.getSongRequestUrl(), song.getOid());
    }

    @Override
    public void settingsChanged(Settings currentSettings) {
        this.currentSettings = currentSettings;
    }

    @Override
    public Settings getCurrentSettings() {
        return this.currentSettings;
    }

    @Override
    public void downloadSong(Context context, Song s) {
        // Done in intent-service
        SongDownloaderService.startDownload(context, s, getHttpSongUrl(s));
    }

    @Override
    public void downloadFolder(final Context context, Folder f) {
        listFolder(f, new DataCallback() { // TODO maybe doesn't work!
            @Override
            public void newData(DbObject[] objects) {
                for (DbObject d: objects)
                    if (d instanceof Song)
                        downloadSong(context, (Song) d);
            }
        }, false);
    }

    /**
     * Starts and connects to PlayerService
     * @param context
     */
    private void loadAndBindService(Context context) {
        Intent i = new Intent(context, PlayerService.class);
        context.startService(i);

        if (!context.bindService(i, this, Context.BIND_NOT_FOREGROUND))
            Toast.makeText(context, "Cannot bind to PlayerService! App won't work correctly!", Toast.LENGTH_LONG).show();

        serviceBinded = true;
    }

    /**
     * Removes service connection to PlayerService
     * (Used in activity to prevent exception)
     */
    public void unloadService(Context context) {
        if (serviceBinded) {
            context.unbindService(this);
            serviceBinded = false;
        }
    }

    @Override
    public void enqueueFolder(Folder folder, final boolean recursive, final boolean thenPlay) {
        // TODO recursive call! (Not implemented!)
        listFolder(folder, new DataCallback() {
            @Override
            public void newData(DbObject[] objects) {
                if (objects.length == 0) return;

                PlayerQueue pl = getCurrentQueue();
                int pos = pl.length();
                for (DbObject o: objects)
                    if (o instanceof Song)
                        pl.enqueue((Song) o);

                if (thenPlay) getCurrentPlayer().play(pos); // TODO play in looper thread? -> exception
                    //else if (o instanceof Folder && recursive)
                    //    enqueueFolder((Folder) o, recursive);
            }
        }, false);
    }

    @Override
    public void shareQueueItem(Context context, MyQueueItem item) {
        if (!currentSettings.canShareSongs()) {
            Toast.makeText(applicationContext,
                    "Cannot share songs, your music server must be reachable from the internet!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, item.getShareString());
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    @Override
    public void saveCustomSong(CustomQueueItem item) {
        //new DbHelper(service.getApplicationContext()).addCustomSongs(new MyQueueItem[]{item});
        CustomSongManager.getInstance().addCustomSong(item);
    }

    @Override
    public void deleteCustomSong(CustomQueueItem item) {
        CustomSongManager.getInstance().deleteCustomSong(item);
    }

    @Override
    public void removeLocalSong(Song song) {
        LocalSongManager.getInstance().removeLocalSong(song);
    }

    @Override
    public int getSongSize(int songid) {
        return currentSettings.getConnector().getSongSize(songid);
    }

    @Override
    public void listCustomSongs(final DataCallbackPlaylist callback) {
        Utils.runInThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.newData(CustomSongManager.getInstance().getCustomSongs());
                //if (forceUpdate || buffer.getCustomSongs() == null || buffer.getCustomSongs().length == 0) {
                //    buffer.setCustomSongs(new DbHelper(service.getApplicationContext()).getCustomSongs());
                //}
                //if (callback != null)
                //    callback.newData(buffer.getCustomSongs());
            }
        });
    }

    @Override
    public void listRootFolders(final DataCallback callback, final boolean force) {
        Utils.runInThread(new Runnable() {
            @Override
            public void run() {
                if (buffer.getRootFolders() != null && !force) {
                    if (callback != null)
                        callback.newData(buffer.getRootFolders());
                } else {
                    Folder[] f = currentSettings.getConnector().listRootFolders();
                    buffer.setRootFolders(f);

                    if (callback != null)
                        callback.newData(f);
                }
            }
        });
    }

    @Override
    public void listFolder(final Folder current, final DataCallback callback, final boolean force) {
        if (current.isRoot() || current.getParent() < 1 || current.getOid() < 1) {
            listRootFolders(callback, force);
            return;
        }

        Utils.runInThread(new Runnable() {
            @Override
            public void run() {
                if (buffer.getFolder(current.getOid()) != null && !force)
                    callback.newData(buffer.getFolder(current.getOid()));
                else {
                    //Folder par = connector.getFolder(current.getParent());
                    DbObject[] content =
                            Utils.merge(
                                    new DbObject[]{FOLDER_PLACEHOLDER},
                                    currentSettings.getConnector().listFoldersInto(current.getOid()),
                                    currentSettings.getConnector().listSongsInto(current.getOid()));
                    buffer.insertFolder(current.getOid(), content);

                    if (callback != null)
                        callback.newData(content);
                }
            }
        });
    }

    //@Override
    // TOO MUCH SONGS!
    //public void listSongs(final DataCallback callback, final boolean force) {
    //    Utils.runInThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            callback.newData(connector.listSongs());
    //        }
    //    });
    //}

    @Override
    public void listLocalSongs(final DataCallback callback, final boolean forceUpdate) {
        Utils.runInThread(new Runnable() {
            @Override
            public void run() {
                //if (forceUpdate || buffer.getLocalSongs() == null || buffer.getLocalSongs().length == 0) {
                //    buffer.setLocalSongs(new DbHelper(service.getApplicationContext()).getLocalSongs());
                //}
                if (callback != null)
                    //callback.newData(buffer.getLocalSongs());
                    callback.newData(LocalSongManager.getInstance().getLocalSongs());
            }
        });
    }

    @Override
    public void search(final String query, final DataCallback callback, final boolean force) {
        Utils.runInThread(new Runnable() {
            @Override
            public void run() {
                // Missing playlist search (and code xD)
                if (callback != null) {
                    PyMusicManagerConnector connector = currentSettings.getConnector();
                    callback.newData(Utils.merge(connector.searchFolder(query), connector.searchSong(query)));
                }
            }
        });
    }

    @Override
    public void quit() {
        if (playerStarted)
            stopPlayers();

        // Save Playlist
        new QueueManager(applicationContext).saveCurrentQueue(currentQueue);

        if (service != null) {
            service.hideNotification();
            service.stopSelf();
        }

        initialized = false;

        LocalSongManager.deleteInstance();
        MyErrorLogger.closeLog();
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public PlayerQueue getCurrentQueue() {
        return currentQueue;
    }

    @Override
    public void changePlayer(int whichPlayer) {
        if (currentPlayer.getKind() == whichPlayer) return;

        switch (whichPlayer) {
            case Player.KIND_CHROMECAST:
                moveToChromecast();
                break;
            case Player.KIND_LOCAL:
                moveToExo();
                break;
            case Player.KIND_UNINITIALIZED:
                moveToUninitialized();
                break;
        }
    }

    /**
     * Starts exo player, keeping currentplayer state
     */
    private void moveToExo() {
        // Save listener
        LinkedList<PlayerListener> current = this.currentPlayer.getPlayerListeners();
        int status = this.currentPlayer.getCurrentState();

        //if (currentPlayer.getKind() != PlayerKind.CHROMECAST_PLAYER)
        //    int pos = this.currentPlayer.getCurrentPosition();

        // Remove current
        this.currentPlayer.delete();

        // Start new
        currentPlayer = new MyExoPlayer();
        for (PlayerListener listener: current)
            currentPlayer.addPlayerListener(listener);
        currentPlayer.setService(service);
        if (status == Player.STATE_PLAYING || status == Player.STATE_BUFFERING) {
            this.currentPlayer.play(); // TODO play and seek
        }
    }

    /**
     * Starts chromecast player, keeping currentplayer state
     */
    private void moveToChromecast() {
        // Save listener
        LinkedList<PlayerListener> current = this.currentPlayer.getPlayerListeners();
        int status = this.currentPlayer.getCurrentState();
        int pos = this.currentPlayer.getCurrentPosition();

        // Remove current
        this.currentPlayer.delete();

        // Start new
        currentPlayer = new ChromecastPlayer();
        for (PlayerListener listener: current)
            currentPlayer.addPlayerListener(listener);
        currentPlayer.setService(service);
        if (status == Player.STATE_PLAYING || status == Player.STATE_BUFFERING) {
            this.currentPlayer.playAndSeek(pos);
        }
    }

    /**
     * Starts uninitialized player, keeping currentplayer state
     */
    private void moveToUninitialized() {
        // Save listener
        LinkedList<PlayerListener> current = this.currentPlayer.getPlayerListeners();

        // Remove current
        this.currentPlayer.delete();

        // Start new
        this.currentPlayer = new UninitializedPlayer();
        for (PlayerListener listener: current)
            currentPlayer.addPlayerListener(listener);
        this.currentPlayer.setService(service);

        // Stop players
        this.playerStarted = false;
        stopPlayers(); // only removes audio-focus
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((PlayerService.MyBinder) service).getService();
        this.currentPlayer.setService(this.service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
