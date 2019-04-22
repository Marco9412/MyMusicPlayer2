package com.panni.mymusicplayer2.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.SongQueueItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Calendar;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import objects.DbObject;
import objects.Song;
import okhttp3.OkHttpClient;

/**
 * Created by marco on 21/05/16.
 */
public class Utils {

    public static void runInThread(Runnable runnable) {
        new Thread(runnable).start();
    }

//    public static void runInThread(String name, Runnable runnable) {
//        new Thread(runnable, name).start();
//    }

    public static DbObject[] merge(DbObject[]... arrays) {
        int totalSize = 0;
        int i = 0;

        // Calculate size
        for (DbObject[] arr : arrays) totalSize += arr.length;

        DbObject[] res = new DbObject[totalSize];
        for (DbObject[] arr : arrays) {
            System.arraycopy(arr, 0, res, i, arr.length);
            i += arr.length;
        }

        return res;
    }

//    public static MediaQueueItem songToMediaItem(Song song, String urlPattern) {
//        MediaMetadata mm = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
//        mm.putString(MediaMetadata.KEY_ARTIST, song.getArtist());
//        mm.putString(MediaMetadata.KEY_TITLE, song.getTitle());
//        mm.putString("songname", song.getName());
//        mm.putInt("songid", song.getOid());
//        mm.putInt("songfolder", song.getFolder());
//
//        MediaInfo info = new MediaInfo.Builder(String.format(urlPattern, song.getOid())) // TODO format?
//                .setContentType(song.getMimeType())
//                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                .setMetadata(mm)
//                .build();
//
//        return new MediaQueueItem.Builder(info).build();
//    }

    public static Song mediaItemToSong(MediaQueueItem item) {
        MediaMetadata mm = item.getMedia().getMetadata();

        return new Song(mm.getInt("songid"),
                mm.getString("songname"),
                mm.getInt("songfolder"),
                mm.getString(MediaMetadata.KEY_TITLE),
                mm.getString(MediaMetadata.KEY_ARTIST));
    }

    public static String timeIntToString(int time) {
        time = time / 1000;
        return String.format(Locale.ENGLISH, "%02d:%02d", time / 60, time % 60);
    }

    public static URL getDefaultURL() {
        try {
            return new URL("https://0.0.0.0:9999/songrpc"); // TODO fixit
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Nullable
    public static String getMusicPath() {
        File f = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .getAbsolutePath() + "/MyMusicPlayer");
        if (f.isDirectory() || f.mkdir())
            return f.getAbsolutePath();
        return null;
    }

    @Nullable
    public static File getSongFile(Song song) {
        String musicPath = getMusicPath();
        if (musicPath == null) return null;
        return new File(musicPath + '/' + song.getName());
    }

    @Nullable
    public static File getSongFile(MyQueueItem item) {
        String musicPath = getMusicPath();
        if (musicPath == null) return null;

        if (item.isYoutube()) {
            return new File(musicPath + '/' + item.getTitle());
        } else if (item.isCustom()) {
            return null; // ???
        } else { // Local
            return new File(musicPath + '/' + ((SongQueueItem)item).getName());
        }
    }

    public static String getCurrentInstance() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.ENGLISH, "%04d%02d%02d_%02d%02d%02d",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
    }

    public static int mimeTypeToIconResource(String mime) {
        switch (mime) {
            case "audio/mpeg3":
                return R.drawable.song_mp3;
            case "audio/x-ms-wma":
                return R.drawable.song_wma;
            case "audio/wav":
                return R.drawable.song_wav;
            case "audio/aac":
                return R.drawable.song_aac;
            case "audio/flac":
                return R.drawable.song_flac;
            //else if (mime.equals("audio/atrac3")) return R.drawable.ic_menu_song;
            case "audio/mp4":
                return R.drawable.song_m4a;
            default:
                Log.d("MimeTypeToIconResource", "Unknown mimetype icon " + mime);
                return R.drawable.ic_menu_song;
        }
    }

//    public static boolean isHeavySong(String mimeType) {
//        switch (mimeType) {
//            case "audio/mpeg3":
//            case "audio/aac":
//            case "audio/mp4":
//                return false;
//            //case "audio/x-ms-wma":
//            //case "audio/wav":
//            //case "audio/flac":
//            default:
//                return true;
//        }
//    }

    @Nullable
    public static Uri resolvContextUri(Context context, String uri) {
        Uri res = null;

        if (uri.contains("content://")) {
            // Resolv content uri
            Uri tmp = Uri.parse(uri);
            Cursor c = context.getContentResolver().query(tmp, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
            if (c.moveToFirst()) {
                res = Uri.parse(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            }
            c.close();
        } else if (uri.contains("file://")) {
            // Already a file uri
            res = Uri.parse(uri);
        }

        // Return result or null!
        return res;
    }

    @SuppressWarnings("deprecation")
    public static int playerStateToRemoteControlClientState(int currentPlayerState) {
        switch (currentPlayerState) {
            case Player.STATE_PAUSED:
                return RemoteControlClient.PLAYSTATE_PAUSED;
            case Player.STATE_PLAYING:
                return RemoteControlClient.PLAYSTATE_PLAYING;
            case Player.STATE_BUFFERING:
                return RemoteControlClient.PLAYSTATE_BUFFERING;
            case Player.STATE_STOPPED:
            case Player.STATE_UNINITIALIZED:
                return RemoteControlClient.PLAYSTATE_STOPPED;
            default:
                return -1;
        }
    }

    public static String readFullyText(File f) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        String tmp;
        BufferedReader br = new BufferedReader(new FileReader(f));
        while ((tmp = br.readLine()) != null)
            sb.append(tmp).append('\n');
        br.close();
        return sb.toString();
    }

    public static void copy(File src, File dst) throws IOException {
        if (!src.exists()) return;

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dst));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024 * 16];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static OkHttpClient getFreeOkHttpClient(final Context context) {
        return new OkHttpClient.Builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .sslSocketFactory(loadServerCertificate(context).getSocketFactory())
                .build();
    }

    public static SSLContext loadServerCertificate(Context context) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream input = context.getResources().openRawResource(R.raw.cert);
            Certificate ca = cf.generateCertificate(input);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;
        } catch (java.security.cert.CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isLocal(String serverHostName) {
        if (!serverHostName.matches("^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}$")) return false; // Is a dns name
        String[] numbersS = serverHostName.split("\\.");
        if (numbersS.length != 4) return false; // ???

        // Is an ip address
        try {
            int[] address = new int[]{
                    Integer.parseInt(numbersS[0]),
                    Integer.parseInt(numbersS[1]),
                    Integer.parseInt(numbersS[2]),
                    Integer.parseInt(numbersS[3])
            };

            return address[0] == 10  || // 10.X.X.X
                    (address[0] == 172 && address[1] >= 16 && address[1] <= 31) || // 172.16.X.X - 172.31.255.255
                    (address[0] == 192 && address[1] == 168); // 192.168.X.X
        } catch (NumberFormatException ex) {
            Log.d("Utils.isLocal", "Unknown serverHostName found!" + serverHostName);
            return false;
        }
    }
}


