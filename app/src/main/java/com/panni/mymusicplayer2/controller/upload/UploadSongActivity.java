package com.panni.mymusicplayer2.controller.upload;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by marco on 11/07/16.
 */
public class UploadSongActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            if (intent.getType().contains("audio/")) {
                Object tmp = intent.getExtras().get(Intent.EXTRA_STREAM);
                if (tmp != null && tmp instanceof Uri)
                    // start service
                    UploadService.startUploadSong(getApplicationContext(), tmp.toString());
            }
        }

        finish();
    }

    public static void setEnabled(Context context, boolean status) {
        ComponentName receiver = new ComponentName(context, UploadSongActivity.class);

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
