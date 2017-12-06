package com.panni.mymusicplayer2.controller;

import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;

/**
 * Created by marco on 28/06/16.
 */
public interface DataCallbackPlaylist {

    void newData(MyQueueItem[] items);
}
