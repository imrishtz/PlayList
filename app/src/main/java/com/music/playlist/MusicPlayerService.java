package com.music.playlist;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    //media player
    private MediaPlayer player = new MediaPlayer();
    //song list
    private List<Song> songList;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private int currentlyPlaying = -1;
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

    public void playSong(int index){

        Song song = songList.get(index % songList.size());
        try {
            player.reset();
            player.setDataSource(song.getPath());
            player.prepare();
            player.setVolume(0.5f, 0.5f);
            player.setLooping(false);
            player.start();
            isPlaying = true;
            currentlyPlaying = index;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopSong() {
        player.stop();
        isPlaying = false;
    }
    public void pauseSong() {
        player.pause();
        isPlaying = false;
    }
    public void playNextSong(int index) {
        playSong(index);
    }
    public boolean isPlaying() {
        return isPlaying;
    }
    public void resume() {
        player.start();
        isPlaying = true;
    }
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }
    public int getSongDuration(){
        return player.getDuration();
    }
    public void seekTo(int progress) {
        player.seekTo(progress);
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
        playNextSong(currentlyPlaying);
    }
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

    }
    public void setList(List<Song> theSongs){
        songList=theSongs;
    }
    public class MusicBinder extends Binder {
        MusicPlayerService getService() {
            Log.v("imri", "imri Binder");
            return MusicPlayerService.this;
        }
    }
    public void setLowerVolume(float volume) {
        player.setVolume(volume, volume);
    }
    public void setNoramlVolume(float volume) {
        player.setVolume(volume, volume);
    }
}
