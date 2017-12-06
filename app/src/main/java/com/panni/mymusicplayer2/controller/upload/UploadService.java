package com.panni.mymusicplayer2.controller.upload;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.settings.Settings;
import com.panni.mymusicplayer2.utils.Utils;

import java.io.File;
import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class UploadService extends IntentService {

    private static int notification_id = 5000;

    private static final String UPLOAD_ACTION = "com.panni.mymusicplayer2.controller.upload.action.UPLOAD";
    private static final String EXTRA_PARAM = "com.panni.mymusicplayer2.controller.upload.extra.URI";

    private int notificationId;

    public UploadService() {
        super("UploadService");
        this.notificationId = ++UploadService.notification_id;
    }

    /**
     * Starts this service
     *
     * @see IntentService
     */
    public static void startUploadSong(Context context, String uri) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UPLOAD_ACTION);
        intent.putExtra(EXTRA_PARAM, uri);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction().equals(UPLOAD_ACTION)) {
            handleUpload(intent.getStringExtra(EXTRA_PARAM));
        }
    }

//    private void handleUpload(String uri) {
//        Uri fileUri = Utils.resolvContextUri(getApplicationContext(), uri);
//
//        if (fileUri != null) {
//            PyMusicManagerConnector connector = Settings.loadSettings(getApplicationContext()).getConnector();
//
//            File inputSong = new File(fileUri.getPath());
//
//            String name = inputSong.getName();
//            byte[] data = new byte[(int) inputSong.length()];
//
//            try {
//                DataInputStream dis = new DataInputStream(new FileInputStream(inputSong));
//                dis.readFully(data);
//                dis.close();
//
//                showNotificationUploading(name);
//                showNotificationUploaded(connector.addLocalSong(name, data), name);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//
//                showNotificationUploaded(false, name);
//            }
//        } else {
//            showNotificationUploaded(false, "Application error!");
//        }
//    }

    private void handleUpload(String uri) {
        Uri fileUri = Utils.resolvContextUri(getApplicationContext(), uri);

        if (fileUri != null) {
            File inputSong = new File(fileUri.getPath());
            String name = inputSong.getName();
            OkHttpClient client = Utils.getFreeOkHttpClient(this);

            Settings settings = Settings.loadSettings(getApplicationContext());

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", "upload")
                    .addFormDataPart("datafile", name,
                            RequestBody.create(MediaType.parse("audio/*"), inputSong))
                    .build();

            Request request = new Request.Builder()
                    .url(settings.getHttpsPostUrl())
                    .addHeader("Authorization", Credentials.basic(settings.getUsername(), settings.getPassword()))
                    .post(requestBody)
                    .build();

            showNotificationUploading(name);

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                showNotificationUploaded(true, name);
            } catch (IOException ex) {
                ex.printStackTrace();
                showNotificationUploaded(false, name);
            }

        } else {
            showNotificationUploaded(false, "Application error!");
        }
    }

    private void showNotificationUploading(String name) {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setTicker("Uploading")
                .setContentTitle("Uploading song")
                .setContentText(name)
                .setOngoing(false)
                .setProgress(0, 0, true) // TODO
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .build();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(this.notificationId, notification);
    }

    private void showNotificationUploaded(boolean result, String name) {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setTicker(result ? "Uploaded!" : "Error!")
                .setContentTitle(result ? "Song uploaded!" : "Cannot upload song")
                .setContentText(name)
                .setTicker(result ? "Song uploaded!" : "Cannot upload song")
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .build();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(this.notificationId, notification);
    }
}
