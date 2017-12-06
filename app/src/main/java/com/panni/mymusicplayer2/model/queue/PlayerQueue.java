package com.panni.mymusicplayer2.model.queue;

import com.google.android.gms.cast.MediaQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;

import objects.Song;

/**
 * Created by marco on 28/05/16.
 */
public interface PlayerQueue {

    Song getSongAt(int position);

    MediaQueueItem getMediaQueueItemAt(int position);

    MyQueueItem getItemAt(int position);

    Song getCurrentSong();

    MediaQueueItem getCurrentMediaQueueItem();

    MyQueueItem getCurrentItem();

    boolean next();

    boolean prev();

    int getCurrentPosition();

    void setCurrentPosition(int position);

    void setNext(int next);

    int getNext();

    void enqueue(Song song);

    //void enqueue(Song[] songs);

    void enqueue(MediaQueueItem song);

    void enqueue(MyQueueItem item);

    //void enqueue(Folder folder);

    //void enqueue(Folder folder, boolean recursive);

    //void removeFromQueue(Song song);

    void removeFromQueue(int position);

    int length();

    void clear();

    void setRepeat(boolean status);

    void setShuffle(boolean status);

    void addPlaylistListener(QueueListener listener);

    void removePlaylistListener(QueueListener listener);

    MyQueueItem[] getItems();

    boolean wasEdited();

    void moveUp(int position);

    void moveDown(int position);

    String serialize();

}
