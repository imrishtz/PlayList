package com.example.playlist;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class MusicPlayer {

    static boolean isPlaying = false;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private String currentlyPlayingPath = "";

    MusicPlayer() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    void playStopSong(String path) {

        if (path != null && !path.isEmpty()) {
            if (currentlyPlayingPath.equals(path)) {
                mMediaPlayer.stop();
                isPlaying = false;
                currentlyPlayingPath = "";
            } else {
                Uri sampleUri = Uri.parse("file:///" + path);
                try {
                    mMediaPlayer.reset();
                    Log.v("imri", "path imri = " + path);
                    mMediaPlayer.setDataSource(path); //to set media source and send the object to the initialized state
                    mMediaPlayer.prepare(); //to send the object to prepared state
                    mMediaPlayer.start(); //to start the music and send the object to the started state
                    currentlyPlayingPath = path;
                    isPlaying = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
