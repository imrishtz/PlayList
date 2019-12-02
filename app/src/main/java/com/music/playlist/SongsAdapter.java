package com.music.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {
    private List<Song> mSongsData;
    private Integer row_index = -1;
    boolean isChecked = false;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View ViewRec;
        public TextView textView2;
        public TextView textView3;
        public Button button;
        public MyViewHolder(View v) {
            super(v);
            ViewRec = v;
            textView2 = v.findViewById(R.id.text1);
            textView3 = v.findViewById(R.id.text2);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SongsAdapter(List<Song> songsData) {
        mSongsData = songsData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_recycler_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String artistName = mSongsData.get(position).getArtist();
        boolean isNoArtistName = android.text.TextUtils.isDigitsOnly(artistName);
        String song = mSongsData.get(position).getName();
        holder.textView2.setText(song);
        holder.textView3.setText(artistName);

        holder.ViewRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastPosition != position) {
                    row_index = position;
                    lastPosition = position;
                } else {
                    row_index = -1;
                    lastPosition = -1;
                }
                notifyDataSetChanged();
            }
        });
        holder.ViewRec.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                row_index = lastPosition;

                notifyDataSetChanged();
                return true;
            }
        });

        if (row_index == position) {
            holder.ViewRec.setBackgroundResource(R.drawable.song_selected_boarder);
            row_index = -1;
        } else {
            holder.ViewRec.setBackgroundResource(R.drawable.songs_border);
        }
    }
    static private int lastPosition = -1;
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSongsData.size();
    }
}
