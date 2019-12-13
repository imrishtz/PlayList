package com.music.playlist;

import android.util.Log;

import androidx.annotation.NonNull;

public class Song {

    private String TAG = "Song";

    private String mPath;
    private String mTitle;
    private String mName;
    private String mAlbum;
    private String mArtist;
    private Integer mId;

    public String getId() {
        return mPath;
    }
    public void setId(String aPath) {
        this.mPath = aPath;
    }
    public String getPath() {
        return mPath;
    }
    public void setPath(String Path) {
        this.mPath = Path;
    }
    public String getName() {
        return mName;
    }
    public void setName(String Name) {
        Log.v(TAG, "imri1 name = " + Name);
        Name = Name.replace(".mp3", "");
        Name = Name.replace(".m4a", "");

        Log.v(TAG, "imri2 name = " + Name);
        this.mName = Name;
    }
    public void setTitle(String title) {
        mTitle = title;
    }
    public String getTitle() {
        return mTitle;
    }
    public String getAlbum() {
        return mAlbum;
    }
    public void setAlbum(String aAlbum) {
        this.mAlbum = aAlbum;
    }
    public String getArtist() {
        return mArtist;
    }
    public void setArtist(String aArtist) {
        this.mArtist = aArtist;
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
