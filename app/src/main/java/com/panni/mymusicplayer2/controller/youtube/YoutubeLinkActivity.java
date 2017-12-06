package com.panni.mymusicplayer2.controller.youtube;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.panni.mymusicplayer2.settings.Settings;

import pymusicmanagerconnector.PyMusicManagerConnector;

public class YoutubeLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Download from Youtube
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            String uri = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (uri != null && uri.contains("youtu")) {
                new YtDownloader().execute(uri);
            }
        }
        finish();
    }

    private class YtDownloader extends AsyncTask<String,Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            PyMusicManagerConnector connector = Settings.loadSettings(YoutubeLinkActivity.this).getConnector();
            return connector.downloadSong(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(getApplicationContext(), "Song is downloading...", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
            } else {
                Toast.makeText(getApplicationContext(), "Error! Cannot download song...", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    public static void setEnabled(Context context, boolean status) {
        ComponentName receiver = new ComponentName(context, YoutubeLinkActivity.class);

        PackageManager pm = context.getPackageManager();
        if (status) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

}
