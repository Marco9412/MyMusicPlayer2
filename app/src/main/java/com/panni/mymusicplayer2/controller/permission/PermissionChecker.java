package com.panni.mymusicplayer2.controller.permission;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by marco on 17/06/16.
 */
public class PermissionChecker {

    final private static int REQUEST_ID = 50;
    final private static int REQUEST_ID2 = 51;

    private Activity activity;

    public PermissionChecker(Activity activity) {
        this.activity = activity;
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(activity)
                        .setTitle("Permissions")
                        .setMessage("You should grant music access permission to this app in order to download locally remote songs!")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ID);
                                Toast.makeText(activity, "You really should restart this application now!", Toast.LENGTH_LONG).show();
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ID);
            }
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(activity)
                        .setTitle("Permissions")
                        .setMessage("You should grant music access permission to this app in order to download locally remote songs!")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_ID2);
                                Toast.makeText(activity, "You really should restart this application now!", Toast.LENGTH_LONG).show();
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_ID2);
            }
        }
    }
}
