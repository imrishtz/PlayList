package com.music.playlist;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String TAG = "MainActivity";
    Button firstFragmentButton, secondFragmentButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment playListsFragment;
    Fragment allSongsFragment;
    SeekBar seekBar;
    FloatingActionButton fab;
    boolean wasPlaying = false;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    TextView totalTime;
    TextView songName;
    TextView artistAlbumName;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private MusicPlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        Log.v("imri", "imri onCreate");
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
// get the reference of Button's
        firstFragmentButton = (Button) findViewById(R.id.all_songs);
        secondFragmentButton = (Button) findViewById(R.id.play_lists);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        songName = findViewById(R.id.song_name_music_player);
        artistAlbumName = findViewById(R.id.artist_and_album_music_player);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seekbar);

        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        allSongsFragment = new AllSongs(mMediaPlayer);
        fragmentTransaction.add(R.id.container, allSongsFragment, "check");
        fragmentTransaction.commit();
// perform setOnClickListener event on First Button
        firstFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// load First Fragment
                getSupportFragmentManager()
                        .beginTransaction().replace(R.id.container, allSongsFragment)
                        .commit();
            }
        });
// perform setOnClickListener event on Second Button
        secondFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListsFragment = new PlayLists();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, playListsFragment)
                        .commit();
            }
        });

        final TextView seekBarHint = findViewById(R.id.curr_time);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress);
                seekBarHint.setText(getTime(x));


                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                int labelWidth = seekBarHint.getWidth();

                if (progress > 0 && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                   // clearMediaPlayer();
                   // MainActivity.this.seekBar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
          //TODO  musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    private void prepareSeekBar() {
        String path = sharedPreferences.getString("lastSong", null);
        if (path == null) {
           // showPlayList();
        } else {
            Song song = ((AllSongs)allSongsFragment).getSongByPath(path);
            prepareSong(song);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("imri", "imri onStart");
        if(playIntent==null){
            playIntent = new Intent(this, MusicPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            startService(playIntent);
// TODO
// TODO
// TODO
// TODO
// TODO
// TODO

           // prepareSeekBar();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(playIntent);
        musicSrv=null;
        Log.v("imri", "imri onDestroy");
    }

    public void run() {
        Log.v("imri", "imri run");
        if (musicBound) {
            int currentPosition;
            while (musicSrv.isPlaying()) {
                try {
                    Thread.sleep(1000);
                    currentPosition = musicSrv.getCurrentPosition();
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    return;
                }
                Log.v("imri", "imri isPlaying + " + currentPosition);
                seekBar.setProgress(currentPosition);
            }
        }
    }


    public void playSong(Song song) {
        if (musicBound) {
            musicSrv.playSong(song);
            int duration = musicSrv.getSongDuration();
            seekBar.setMax(duration);
            String time = getTime(duration);
            totalTime.setText(time);
            songName.setText(song.getTitle());
            artistAlbumName.setText(song.getArtist() + " - " + song.getAlbum());
            new Thread(this).start();
        }
        /*
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                clearMediaPlayer();
                seekBar.setProgress(0);
                this.
                wasPlaying = true;
            }

            if (!wasPlaying) {

                prepareSong(song);

                mMediaPlayer.start();
                currentlyPlaying = song;
                editor.putString("lastSong", song.getPath());
                editor.commit();
                new Thread(this).start();
            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }

         */
    }

    private void prepareSong(Song song){
        try {
            mMediaPlayer.setDataSource(song.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.setVolume(0.5f, 0.5f);
            mMediaPlayer.setLooping(false);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String getTime(int timeInMilSec) {
        long second = (timeInMilSec / 1000) % 60;
        long minute = (timeInMilSec / (1000 * 60)) % 60;
        long hour = (timeInMilSec / (1000 * 60 * 60)) % 24;
        String time;
        if (hour > 0) {
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            time = String.format("%02d:%02d",minute, second);
        }
        return time;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.v("imri", "imri onPause");
    }
}
