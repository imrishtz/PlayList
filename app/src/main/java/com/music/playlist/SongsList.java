package com.music.playlist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongsList {

    private final String TAG = "SongsList";
    private static volatile SongsList sSongsListInstance = new SongsList();
    private List<Song> mSongsList;

    public static SongsList getInstance() {
        return sSongsListInstance;
    }

    private SongsList(){}

    public void setSongsList(List<Song> songsList) {
        mSongsList = songsList;
    }

    public List<Song> getAllSongs(Context context) {
        getAllAudioFromDevice(context);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                for (Song song : mSongsList) {
                    mmr.setDataSource(song.getPath());
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                    }
                }
            }
        });
        return mSongsList;
    }

    public List<Song> sortByAbc() {
        List<Song> sortedSongsList = mSongsList;
        Collections.sort(sortedSongsList, new Comparator<Song>() {
            @Override
            public int compare(final Song object1, final Song object2) {
                return object1.getTitle().compareTo(object2.getTitle());
            }
        });
        return sortedSongsList;
    }
    public Song getSongByIndex(int index) {
        return mSongsList.get(index % mSongsList.size());
    }
    private void getAllAudioFromDevice(final Context context) {
        final List<Song> tempAudioList = new ArrayList<>();
        final String [] STAR= {"*"};
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0",ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor= context.getContentResolver().query(uri, STAR, selectionMusic + selectionMp3 , selExtARGS, null);
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
                song.setTitle(titleStr);
                //song.setClipArt(getAlbumImage(data));
                Log.e(TAG, " song = :" +id + ". name = :" + name + ". album " + album
                        + ". artist" + artist + ". title =  " + titleStr + ".isMusic =" + isMusic);
                Log.e("Name :" + name, " data :" + data);

                tempAudioList.add(song);
            }
        }
        Log.e(TAG, "Cursor is not null");
        cursor.close();
        mSongsList = tempAudioList;
    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    public class SetAlbumImageThread extends Thread {

        public void run(){
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (Song song : mSongsList) {
                mmr.setDataSource(song.getPath());
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {
                    song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                }
            }
        }
    }
}
