package com.example.playlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class SongInfoBox extends PopupWindow {

    Context context;
    View layout;
    TextView songInfoName;
    public SongInfoBox (Context context) {
        super(context);
        this.context = context;
    }

    public void show(Song song, float yPosition) {

        if (context == null)
            return;

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = layoutInflater.inflate(R.layout.song_info_dialog, null);
        songInfoName = layout.findViewById(R.id.song_info_name);
        songInfoName.setText(("Name: " + song.getName() +
                              "\nArtist: " + song.getArtist() +
                                "\nAlbum: " + song.getAlbum()));

        setContentView(layout);
        setFocusable(true);

        Log.v("imri","x = "  + " round = " + Math.round(yPosition));
        /**
         * Displaying the pop-up at the specified location, + offsets.
         */
        showAtLocation(layout, Gravity.NO_GRAVITY, 100, Math.round(yPosition) + 20);

    }

}