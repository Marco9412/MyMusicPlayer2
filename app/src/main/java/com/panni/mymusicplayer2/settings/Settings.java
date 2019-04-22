package com.panni.mymusicplayer2.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.panni.mymusicplayer2.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import pymusicmanagerconnector.PyMusicManagerConnector;

/**
 * Created by marco on 14/07/16.
 */
public class Settings {

    final private static String RPC_PATH = "/songrpc";

    public static Settings loadSettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String host = preferences.getString("remote_addr", "0.0.0.0"); // TODO
        //int httpport = Integer.parseInt(preferences.getString("remote_http_port", "9998"));
        //int httpsport = Integer.parseInt(preferences.getString("remote_https_port", "9999"));
        String user = preferences.getString("remote_username", "dummy");
        String pass = preferences.getString("remote_password", "dummy");
        String prefix = preferences.getString("remote_basepath", "");

        boolean ytDownload = preferences.getBoolean("yt_intent_enable", false);
        boolean upload = preferences.getBoolean("upload_enable", false);

        boolean smartCache = preferences.getBoolean("smart_cache_enable", true);

        // Check if it is correct!
        URL remote;
        try {
            remote = new URL("https://" + host + "/" + prefix + RPC_PATH);
        } catch (MalformedURLException ex) {
            remote = Utils.getDefaultURL();
            Toast.makeText(context, "Wrong settings! App may be not fully work!", Toast.LENGTH_LONG).show();
        }

        return new Settings(
                remote, prefix, user, pass, ytDownload, upload, smartCache
        );
    }

    private String remoteHost;
    private URL remoteHostUrl;
    private String songRequestUrl;
    private String httpsPostUrl;

    private String username;
    private String password;

    private boolean ytDownloadEnabled;
    private boolean songUploadEnabled;

    private boolean smartCacheEnabled;

    private PyMusicManagerConnector connector;

    private Settings(URL remoteHostUrl, String remoteBasePath, String username,
                    String password, boolean ytDownloadEnabled, boolean songUploadEnabled, boolean smartCacheEnabled) {
        this.remoteHostUrl = remoteHostUrl;
        this.remoteHost = this.remoteHostUrl.getHost();
        this.songRequestUrl = "http://" + this.remoteHost + "/" + remoteBasePath + "/getsong?id=%d";
        this.httpsPostUrl = "https://" + this.remoteHost + "/" + remoteBasePath + "/songs/";
        this.username = username;
        this.password = password;
        this.connector = new PyMusicManagerConnector(remoteHostUrl, username, password, false); // set to true if you use a custom signed certificate for the server

        this.ytDownloadEnabled = ytDownloadEnabled;
        this.songUploadEnabled = songUploadEnabled;

        this.smartCacheEnabled = smartCacheEnabled;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public URL getRemoteHostUrl() {
        return remoteHostUrl;
    }

    public String getHttpsPostUrl() {
        return httpsPostUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isYtDownloadEnabled() {
        return ytDownloadEnabled;
    }

    public boolean isSongUploadEnabled() {
        return songUploadEnabled;
    }

    public boolean isSmartCacheEnabled() {
        return smartCacheEnabled;
    }

    public PyMusicManagerConnector getConnector() {
        return connector;
    }

    public String getSongRequestUrl() {
        return this.songRequestUrl;
    }

    public boolean canShareSongs() {
        // Can share songs urls only if the server is reachable from the internet!
        return !Utils.isLocal(this.remoteHost);
    }
}
