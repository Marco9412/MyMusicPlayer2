package com.panni.mymusicplayer2.view.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.utils.Utils;

import objects.DbObject;
import objects.Song;


/**
 * Created by marco on 14/05/16.
 */
public class LocalSongAdapter extends BaseAdapter {

    // Used in LocalSongListFragment

    final public static int TYPE_ARTIST = 0;
    final public static int TYPE_NO_ARTIST = 1;

    private DbObject[] objects;
    private Context context;

    public LocalSongAdapter(Context context, DbObject[] objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public DbObject getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return objects[position].getOid();
    }

    @Override
    public int getItemViewType(int position) {
        DbObject it = getItem(position);

        String artist = ((Song) it).getArtist();
        if (artist != null && !artist.equals(""))
            return TYPE_ARTIST;
        else return TYPE_NO_ARTIST;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Song it = (Song) getItem(position);

        //if (it instanceof Song) // Always a song!
        //    return getViewSong((Song) it, convertView, parent);
        //else return null;

        // Always a song!
        if (it.getArtist() != null && !it.getArtist().equals(""))
            return getViewSongWithArtist(it, convertView, parent);
        else
            return getViewSongWithoutArtist(it, convertView, parent);
    }

//    private View getViewSong(Song s, View convertView, ViewGroup parent) {
//        ViewHolderWithoutArtist holder;
//
//
//        if (convertView == null) {
//
//        }
//
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (s.getArtist() != null && !s.getArtist().equals("")) {
//            rowView = inflater.inflate(R.layout.song_folder_item_list_with_artist, parent, false);
//
//            AppCompatTextView textArtist = (AppCompatTextView) rowView.findViewById(R.id.item_artist_list);
//            textArtist.setText(s.getArtist());
//
//        } else {
//            rowView = inflater.inflate(R.layout.song_folder_item_list, parent, false);
//        }
//
//        AppCompatImageView image = (AppCompatImageView) rowView.findViewById(R.id.item_image_list);
//        AppCompatTextView text = (AppCompatTextView) rowView.findViewById(R.id.item_test_list);
//
//        //image.setImageResource(R.drawable.ic_menu_song);
//        image.setImageResource(Utils.mimeTypeToIconResource(s.getMimeType()));
//        text.setText(s.getTitle());
//
//        return convertView;
//    }

    private View getViewSongWithoutArtist(Song s, View convertView, ViewGroup parent) {
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

        holder.image.setImageResource(Utils.mimeTypeToIconResource(s.getMimeType()));
        holder.text.setText(s.getTitle());

        return convertView;
    }

    private View getViewSongWithArtist(Song s, View convertView, ViewGroup parent) {
        ViewHolderArtist holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.song_folder_item_list_with_artist, parent, false);
            holder = new ViewHolderArtist();
            holder.image = (AppCompatImageView) convertView.findViewById(R.id.item_image_list);
            holder.text = (AppCompatTextView) convertView.findViewById(R.id.item_test_list);
            holder.textArtist = (AppCompatTextView) convertView.findViewById(R.id.item_artist_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderArtist) convertView.getTag();
        }

        holder.image.setImageResource(Utils.mimeTypeToIconResource(s.getMimeType()));
        holder.text.setText(s.getTitle());
        holder.textArtist.setText(s.getArtist());

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
