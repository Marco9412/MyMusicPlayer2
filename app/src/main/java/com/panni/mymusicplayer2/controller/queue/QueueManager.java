package com.panni.mymusicplayer2.controller.queue;

import android.content.Context;

import com.panni.mymusicplayer2.model.queue.LinkedListQueue;
import com.panni.mymusicplayer2.model.queue.PlayerQueue;
import com.panni.mymusicplayer2.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by marco on 05/08/16.
 */
public class QueueManager {

    final public static String QUEUE_FILE_NAME = "lastQueue.json";

    private Context context;

    public QueueManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void saveCurrentQueue(PlayerQueue currentQueue) {
        if (currentQueue.wasEdited()) {
            try {
                File queueFile = new File(context.getFilesDir(), QUEUE_FILE_NAME);
                queueFile.delete();

                PrintWriter pw = new PrintWriter(new FileWriter(queueFile), true);
                pw.print(currentQueue.serializeJSON().toString());
                pw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public PlayerQueue loadLastQueue() {
        try {
            File queueFile = new File(context.getFilesDir(), QUEUE_FILE_NAME);
            if (queueFile.exists()) {
                return new LinkedListQueue(new JSONObject(Utils.readFullyText(queueFile))); // Last queue found!
            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

        return new LinkedListQueue(); // No last queue! Loading an empty one
    }
}
