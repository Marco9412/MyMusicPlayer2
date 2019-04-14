package com.panni.mymusicplayer2.model.queue;

import com.google.android.gms.cast.MediaQueueItem;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import objects.Song;

/**
 * Created by marco on 28/05/16.
 */
public class LinkedListQueue implements PlayerQueue {

    final public static Song DUMMY_SONG = new Song(0, "bo", -1, "-----", "-----");

    private List<MyQueueItem> queue;
    private int currentPosition;
    private int nextPosition;

    private boolean shuffle;
    private boolean repeat;
    private boolean edited;

    private List<QueueListener> listeners;

    private Random random;

    public LinkedListQueue(LinkedList<MyQueueItem> queue, int nextPosition){
        this.queue = Collections.synchronizedList(queue);
        this.currentPosition = 0;
        this.shuffle = false;
        this.repeat = true;
        this.edited = false;
        this.nextPosition = nextPosition;
        this.random = new Random();
        this.listeners = Collections.synchronizedList(new LinkedList<QueueListener>());
    }

    public LinkedListQueue() {
        this(new LinkedList<MyQueueItem>(), -1);
    }

    /**
     * Creates a queue from PlayerQueue.QUEUE_FILE_NAME data!
     * @param sourceData
     */
    public LinkedListQueue(String sourceData) {
        LinkedList<MyQueueItem> items = new LinkedList<>();
        BufferedReader br = new BufferedReader(new StringReader(sourceData));

        try { // read last position
            this.currentPosition = Integer.parseInt(br.readLine());
        } catch (IOException e) {
        }

        MyQueueItem tmp;
        while ((tmp = MyQueueItem.fromFileFormat(br)) != null)
            items.add(tmp);

        this.queue = items;
        this.shuffle = false;
        this.repeat = true;
        this.edited = false;
        this.nextPosition = -1;
        this.random = new Random();
        this.listeners = new LinkedList<>();
    }
    @Override
    public void addPlaylistListener(QueueListener listener) {
        if (!listeners.contains(listener))
            this.listeners.add(listener);
    }

    @Override
    public void removePlaylistListener(QueueListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public MyQueueItem[] getItems() {
        MyQueueItem[] tmp = new MyQueueItem[queue.size()];
        return queue.toArray(tmp);
    }

    @Override
    public Song getSongAt(int position) {
        return getItemAt(position).toSong();
    }

    @Override
    public MediaQueueItem getMediaQueueItemAt(int position) {
        return getItemAt(position).toMediaQueueItem();
    }

    @Override
    public MyQueueItem getItemAt(int position) {
        if (position < queue.size() && position > -1 && length() > 0)
            return queue.get(currentPosition);

        return MyQueueItem.create(DUMMY_SONG);
    }

    @Override
    public Song getCurrentSong() {
        return getSongAt(currentPosition);
    }

    @Override
    public MediaQueueItem getCurrentMediaQueueItem() {
        return getMediaQueueItemAt(currentPosition);
    }

    @Override
    public MyQueueItem getCurrentItem() {
        return getItemAt(currentPosition);
    }

    @Override
    public boolean next() {
        synchronized (this) {
            // Empty
            if (length() == 0) return false;

            edited = true;

            // Go to selected next song
            if (nextPosition != -1) {
                currentPosition = nextPosition;
                nextPosition = -1;
                update();
                return true;
            }

            // Shuffle
            if (shuffle) {
                currentPosition = random.nextInt(length());
                update();
                return true;
            }

            if (repeat) {
                // Next with repeat
                currentPosition = (currentPosition + 1 == length()) ? 0 : (currentPosition + 1);
                update();
                return true;
            } else if (currentPosition + 1 == length()) {
                // Next without repeat, stop
                currentPosition = 0;
                update();
                return false;
            } else {
                // Next without repeat
                currentPosition++;
                update();
                return true;
            }
        }
    }

    @Override
    public boolean prev() {
        synchronized (this) {
            // Empty
            if (length() == 0) return false;

            edited = true;

            // Shuffle
            if (shuffle) {
                currentPosition = random.nextInt(length());
                update();
                return true;
            }

            if (repeat) {
                // Prev with repeat
                currentPosition = (currentPosition - 1 > -1) ? (currentPosition - 1) : (length() - 1);
                update();
                return true;
            } else if (currentPosition == 0) {
                // Prev without repeat, stop
                return false;
            } else {
                // Prev without repeat
                currentPosition--;
                update();
                return true;
            }
        }
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        if (currentPosition < length() && currentPosition > -1) {
            edited = true;
            this.currentPosition = currentPosition;
            update();
        }
    }

    @Override
    public void setNext(int next) {
        if (next < length() && next > -1) {
            this.nextPosition = next;
            update();
        }
    }

    @Override
    public int getNext() {
        return nextPosition;
    }

    @Override
    public void enqueue(Song song) {
        enqueue(MyQueueItem.create(song));
        //enqueue(Utils.songToMediaItem(song, ControllerImpl.getInstance().getHttpSongUrl(song)));
    }

//    @Override
//    public void enqueue(Song[] songs) {
//        for (Song s: songs)
//            enqueue(s);
//    }

    @Override
    public void enqueue(MediaQueueItem song) {
        enqueue(MyQueueItem.create(Utils.mediaItemToSong(song)));
    }

    @Override
    public synchronized void enqueue(MyQueueItem item) {
        this.queue.add(item);

        if (length() == 1) currentPosition = 0;

        edited = true;
        update();
    }

//    @Override
//    public synchronized void removeFromQueue(Song song) {
//        removeFromQueue(queue.indexOf(song));
//    }

    @Override
    public synchronized void removeFromQueue(int position) {
        if (position< queue.size() && position > -1) {
            queue.remove(position);

            if (position == currentPosition) ControllerImpl.getInstance().stopPlayers();

            // Clear nextSong if not exists
            if (nextPosition != -1) {
                if (nextPosition <= currentPosition)
                    nextPosition = (nextPosition - 1 > -1) ? (currentPosition - 1) : 0;
                else if (nextPosition >= length()) nextPosition = length() - 1;
            }

            if (position <= currentPosition)
                currentPosition = (currentPosition - 1 > -1) ? (currentPosition - 1) : 0;

            if (length() == 1) currentPosition = 0;

            edited = true;
            update();
        }
    }

    @Override
    public int length() {
        return queue.size();
    }

    @Override
    public void clear() {
        queue.clear();
        this.currentPosition = -1;
        nextPosition = -1;
        edited = true;
        update();
    }

    @Override
    public boolean wasEdited() {
        return edited;
    }

    @Override
    public void setShuffle(boolean status) {
        this.shuffle = status;
    }

    @Override
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    private void update() {
        for (QueueListener listener: listeners)
            listener.playlistChanged();
    }

    @Override
    public void moveUp(int position) {
        if (position >= length() || position == 0) return;

        if (position == currentPosition) currentPosition--;
        if (position == nextPosition) nextPosition--;
        edited = true;

        Collections.swap(queue, position, position - 1);

        update();
    }

    @Override
    public void moveDown(int position) {
        if (position >= (length() - 1)) return;

        if (position == currentPosition) currentPosition++;
        if (position == nextPosition) nextPosition++;
        edited = true;

        Collections.swap(queue, position, position + 1);

        update();
    }

    @Override
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.currentPosition).append('\n');
        if (queue.size() == 0) return builder.toString();

        for (MyQueueItem item: this.queue)
            builder.append(item.toFileFormat());
        return builder.toString();
    }
}