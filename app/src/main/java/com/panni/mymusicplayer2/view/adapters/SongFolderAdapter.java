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
import objects.Folder;
import objects.Song;


/**
 * Created by marco on 14/05/16.
 */
public class SongFolderAdapter extends BaseAdapter {

    // Used in SongListFragment, SearchFragment

    // TODO maybe a superclass adapter?

    final public static int TYPE_SONG_ARTIST = 0;
    final public static int TYPE_SONG_NO_ARTIST = 1;
    final public static int TYPE_FOLDER = 2;

    private DbObject[] objects;
    private Context context;

    public SongFolderAdapter(Context context, DbObject[] objects) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        DbObject it = getItem(position);

        if (it instanceof Song) {
            String artist = ((Song) it).getArtist();
            if (artist != null && !artist.equals(""))
                return getViewSongWithArtist((Song) it, convertView, parent);
            else return getViewSongWithoutArtist((Song) it, convertView, parent);
        } else if (it instanceof Folder)
            return getViewFolder((Folder) it, convertView, parent);
        else return null; // TODO playlist??
    }

    @Override
    public int getItemViewType(int position) {
        DbObject it = getItem(position);

        if (it instanceof Song) {
            String artist = ((Song) it).getArtist();
            if (artist != null && !artist.equals(""))
                return TYPE_SONG_ARTIST;
            else return TYPE_SONG_NO_ARTIST;
        } else if (it instanceof Folder)
            return TYPE_FOLDER;
        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

//    private View getViewSong(Song s, ViewGroup parent) {
//        View rowView;
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
//        return rowView;
//    }

    private View getViewFolder(Folder f, View convertView, ViewGroup parent) {
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

        if (f.getName().equals("..."))
            holder.image.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        else
            holder.image.setImageResource(R.drawable.ic_menu_folder);
        holder.text.setText(f.getName());

        return convertView;
    }

    private View getViewSongWithArtist(Song it, View convertView, ViewGroup parent) {
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
        holder.image.setImageResource(Utils.mimeTypeToIconResource(it.getMimeType()));

        return convertView;
    }

    private View getViewSongWithoutArtist(Song it, View convertView, ViewGroup parent) {
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
        holder.image.setImageResource(Utils.mimeTypeToIconResource(it.getMimeType()));

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
