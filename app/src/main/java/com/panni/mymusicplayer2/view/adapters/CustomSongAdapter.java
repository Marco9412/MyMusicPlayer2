package com.panni.mymusicplayer2.view.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.model.queue.objects.MyQueueItem;

/**
 * Created by marco on 07/06/16.
 */
public class CustomSongAdapter extends BaseAdapter {

    // Used in CustomSongListFragment

    private MyQueueItem[] data;
    private Context context;

    public CustomSongAdapter(Context context, MyQueueItem[] data) {
        this.context = context;
        this.data = data;
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
        // return data[position].toSong().getOid(); -> cannot call toSong()
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse old view
        //View rowView =
        //        convertView == null ?
        //                ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        //                        .inflate(R.layout.song_folder_item_list, parent, false) :
        //                convertView;

        ViewHolder holder;
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.song_folder_item_list, parent, false);
            holder = new ViewHolder();
            holder.image = (AppCompatImageView) convertView.findViewById(R.id.item_image_list);
            holder.text = (AppCompatTextView) convertView.findViewById(R.id.item_test_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //AppCompatImageView image = (AppCompatImageView) rowView.findViewById(R.id.item_image_list);
        //AppCompatTextView text = (AppCompatTextView) rowView.findViewById(R.id.item_test_list);

        MyQueueItem it = getItem(position);
        if (it.isCustom()) {
            holder.image.setImageResource(R.drawable.ic_router_black_24dp);
        } else if (it.isYoutube()) {
            holder.image.setImageResource(R.drawable.ic_logo_of_youtube);
        }

        holder.text.setText(it.getTitle());

        return convertView;
    }

    private class ViewHolder {
        public AppCompatImageView image;
        public AppCompatTextView text;
    }
}
