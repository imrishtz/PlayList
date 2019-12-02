package com.music.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class AllSongs extends Fragment {
    AlertDialog.Builder songInfoDialog;
    String TAG = "AllSongs";
    View view;
    private float[] lastTouchDownXY = new float[2];
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mSongsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Song> mSongList;
    MusicPlayer musicPlayer = new MusicPlayer();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        final Context context = getContext();
        view = inflater.inflate(R.layout.all_songs, container, false);
        mSongList = getAllAudioFromDevice(context);
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        mSongsAdapter = new SongsAdapter(mSongList);
        recyclerView.setAdapter(mSongsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                Toast.makeText(context, "Single Click on position        :"+position,
                        Toast.LENGTH_SHORT).show();
                musicPlayer.playStopSong(mSongList.get(position).getPath());
            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(context, "Long press on position :"+position,
                        Toast.LENGTH_LONG).show();
                SongInfoBox songInfoBox = new SongInfoBox(context);
                float x = lastTouchDownXY[0];
                songInfoBox.show(mSongList.get(position), x);

                /*
                new AlertDialog.Builder(context)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        }


                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                        */

            }

        }));
        return view;


    }

    private List<Song> getAllAudioFromDevice(final Context context) {
        final List<Song> tempAudioList = new ArrayList<>();
        final String [] STAR= {"*"};
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0",ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor= getActivity().getContentResolver().query(uri, STAR, selectionMusic + selectionMp3 , selExtARGS, null);
        cursor.moveToFirst();
        for(int r= 0; r<cursor.getCount(); r++, cursor.moveToNext()){
            Log.e(TAG, "Cursor " + r);
            int i = cursor.getInt(0);
            int l = cursor.getString(1).length();
            if(l>0){
                Log.e(TAG, "Cursor song");
                // keep any playlists with a valid data field, and let me know
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int nameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int isMusicIndex = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                Song song = new Song();
                String id = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                String album = cursor.getString(albumIndex);
                String artist = cursor.getString(artistIndex);
                String titleStr = cursor.getString(titleIndex);
                String isMusic = cursor.getString(isMusicIndex);
                String data = cursor.getString(dataIndex);
                song.setName(name);
                song.setAlbum(album);
                song.setArtist(artist);
                song.setId(id);
                song.setPath(data);

                Log.e(TAG, " song = :" +id + ". name = :" + name + ". album " + album
                        + ". artist" + artist + ". title =  " + titleStr + ".isMusic =" + isMusic);
                Log.e("Name :" + name, " data :" + data);

                tempAudioList.add(song);
            }
        }
        Log.e(TAG, "Cursor is not null");
        cursor.close();

        return tempAudioList;
    }



    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            lastTouchDownXY[0] = e.getY();

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent event) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}

