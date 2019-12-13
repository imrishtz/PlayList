package com.music.playlist;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    //media player
    private MediaPlayer player = new MediaPlayer();
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    boolean wasPlaying = false;
    private final IBinder musicBind = new MusicBinder();
    private String currentlyPlaying = "";
    private boolean isPlaying = false;
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
    public void setSong(int songIndex){
        songPosn=songIndex;
    }
    public MediaPlayer getMediaPlayer() {
        return player;
    }
    private void clearMediaPlayer() {
        player.stop();
        player.release();
        player = null;
    }
    public void playSong(Song song){
            if (song.getPath().equals(currentlyPlaying) ) {
                stopSong();
            } else {
                try {
                    player.reset();
                    player.setDataSource(song.getPath());
                    player.prepare();
                    player.setVolume(0.5f, 0.5f);
                    player.setLooping(false);
                    player.start();
                    isPlaying = true;
                    currentlyPlaying = song.getPath();
                    //TODO  currentlyPlaying = song;
                    //  editor.putString("lastSong", song.getPath());
                    //  editor.commit();
                    //  new Thread(this).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
    public void stopSong() {
        player.stop();
        currentlyPlaying = "";
        isPlaying = false;
    }
    public boolean isPlaying() {
        return isPlaying;
    }
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }
    public int getSongDuration(){
        return player.getDuration();
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("imri", "imri onBind");
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

    }
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }


    public class MusicBinder extends Binder {
        MusicPlayerService getService() {
            Log.v("imri", "imri Binder");
            return MusicPlayerService.this;
        }
    }
}
