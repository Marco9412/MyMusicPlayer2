package com.panni.mymusicplayer2.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;
import com.panni.mymusicplayer2.model.queue.objects.SongQueueItem;
import com.panni.mymusicplayer2.utils.Utils;

/**
 * Created by marco on 07/06/16.
 */
public class PlaylistAdapter extends BaseAdapter {

    final public static int TYPE_ARTIST = 0;
    final public static int TYPE_NO_ARTIST = 1;

    // Used in PlayerFragment

    private MyQueueItem[] data;
    private Context context;
    private int current;
    private int next;

    public PlaylistAdapter(Context context, MyQueueItem[] data) {
        this(context, data, -1, -1);
    }

    public PlaylistAdapter(Context context, MyQueueItem[] data, int current, int next) {
        this.context = context;
        this.data = data;
        this.current = current;
        this.next = next;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public MyQueueItem getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        //return data[position].toSong().getOid(); can't call getSong()
        return 0;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View rowView = null;
//
//        MyQueueItem it = getItem(position);
//        String artist;
//
//        if (it instanceof SongQueueItem && (artist = it.getArtist()) != null && !artist.equals("")) {
//            // Not custom -> song with title and artist
//            rowView = inflater.inflate(R.layout.song_folder_item_list_with_artist, parent, false);
//            AppCompatTextView textArtist = (AppCompatTextView) rowView.findViewById(R.id.item_artist_list);
//            textArtist.setText(artist);
//        } else {
//            // Song without artist, maybe custom
//            rowView = inflater.inflate(R.layout.song_folder_item_list, parent, false);
//        }
//
//        AppCompatImageView image = (AppCompatImageView) rowView.findViewById(R.id.item_image_list);
//        if (it.isCustom()) image.setImageResource(R.drawable.ic_router_black_24dp);
//        else image.setImageResource(Utils.mimeTypeToIconResource(it.toSong().getMimeType()));
//
//        AppCompatTextView text = (AppCompatTextView) rowView.findViewById(R.id.item_test_list);
//        text.setText(it.getTitle());
//        if (position == current)
//            text.setTypeface(null, Typeface.BOLD);
//        if (position == next)
//            text.setTypeface(null, Typeface.ITALIC);
//
//        return rowView;
//    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyQueueItem it = getItem(position);
        String artist = it.getArtist();

        if (it instanceof SongQueueItem && artist != null && !artist.equals(""))
            return getViewWithArtist(position, (SongQueueItem) it, convertView, parent);
        else
            return getViewWithoutArtist(position, it, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        MyQueueItem it = getItem(position);
        String artist = it.getArtist();
        return (it instanceof SongQueueItem && artist != null && !artist.equals("")) ?
                TYPE_ARTIST : TYPE_NO_ARTIST;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private View getViewWithArtist(int position, SongQueueItem it, View convertView, ViewGroup parent) {
        ViewHolderArtist holder;
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.song_folder_item_list_with_artist, parent, false);
            holder = new ViewHolderArtist();
            holder.textArtist = (AppCompatTextView) convertView.findViewById(R.id.item_artist_list);
            holder.image = (AppCompatImageView) convertView.findViewById(R.id.item_image_list);
            holder.text = (AppCompatTextView) convertView.findViewById(R.id.item_test_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderArtist) convertView.getTag();
        }

        holder.text.setText(it.getTitle());
        holder.textArtist.setText(it.getArtist());
        holder.image.setImageResource(Utils.mimeTypeToIconResource(it.toSong().getMimeType()));
        if (position == current)
            holder.text.setTypeface(null, Typeface.BOLD);
        else if (position == next)
            holder.text.setTypeface(null, Typeface.ITALIC);
        else holder.text.setTypeface(null, Typeface.NORMAL);

        return convertView;
    }

    private View getViewWithoutArtist(int position, MyQueueItem it, View convertView, ViewGroup parent) {
        ViewHolderWithoutArtist holder;
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.song_folder_item_list, parent, false);
            holder = new ViewHolderWithoutArtist();
            holder.image = (AppCompatImageView) convertView.findViewById(R.id.item_image_list);
            holder.text = (AppCompatTextView) convertView.findViewById(R.id.item_test_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderWithoutArtist) convertView.getTag();
        }

        holder.text.setText(it.getTitle());
        if (it.isCustom()) {
            holder.image.setImageResource(R.drawable.ic_router_black_24dp);
        } else if (it.isYoutube()) {
            holder.image.setImageResource(R.drawable.ic_logo_of_youtube);
        }
        else {
            holder.image.setImageResource(Utils.mimeTypeToIconResource(it.toSong().getMimeType()));
        }
        if (position == current)
            holder.text.setTypeface(null, Typeface.BOLD);
        if (position == next)
            holder.text.setTypeface(null, Typeface.ITALIC);

        return convertView;
    }

    private class ViewHolderArtist {
        public AppCompatTextView textArtist;
        public AppCompatImageView image;
        public AppCompatTextView text;
    }

    private class ViewHolderWithoutArtist {
        public AppCompatImageView image;
        public AppCompatTextView text;
    }
}
