package com.music.playlist;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Runnable, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "MainActivity";
    Button firstFragmentButton, secondFragmentButton;
    Button playPauseButton, skipNextButton, resartOrLastButton, menu;

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
    private int duration;
    private MusicPlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private SongsList mSongListInstance;
    private final int TIME_TO_GO_LAST_SONG = 3000;
    private AudioManager audioManager;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        Log.v("imri", "imri onCreate");
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
        mSongListInstance = SongsList.getInstance();
        sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        //mediaSession.setCallback(MySessionCallback);

// get the reference of Button's
        menu = findViewById(R.id.menu_button);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(view);
            }
        });
        playPauseButton = (Button) findViewById(R.id.play_pause);
        playPauseButton.setBackgroundResource(R.drawable.ic_play);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicSrv.isPlaying()) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_play);
                    musicSrv.pauseSong();
                } else {
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                    resumeSong();
                }
            }
        });
        skipNextButton = (Button) findViewById(R.id.skip_next_song);
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        });

        resartOrLastButton = (Button) findViewById(R.id.restart_or_last_song);
        resartOrLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartOrLastSong();
            }
        });

        firstFragmentButton = (Button) findViewById(R.id.all_songs);
        secondFragmentButton = (Button) findViewById(R.id.play_lists);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        songName = findViewById(R.id.song_name_music_player);
        artistAlbumName = findViewById(R.id.artist_and_album_music_player);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seekbar);

        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        allSongsFragment = new AllSongs();
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
                //musicSrv.seekTo(seekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress);
                seekBarHint.setText(getTime(x));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicSrv.seekTo(seekBar.getProgress());
            }
        });
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
        registerReceiver(KeyEventListener, intentFilter);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, this, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0));
    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicBound = true;
            List<Song> songList = ((AllSongs)allSongsFragment).getAllSongs();
            musicSrv.setList(songList);
            setSongList(songList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    private void setSongList(List<Song> songList) {
        mSongListInstance.setSongsList(songList);
    }

    private void prepareSeekBar() {
        String path = sharedPreferences.getString("lastSong", null);
        if (path == null) {
            songName.setText(R.string.app_name);
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
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(playIntent);
        musicSrv=null;
        unregisterReceiver(KeyEventListener);
        Log.v(TAG, "imri unregistered");
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
                if ((duration != 0) && (currentPosition >= duration)) {
                    currentPosition = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicSrv.stopSong();
                            playSong(currentlyPlaying + 1);
                        }
                    });
                }
                // Make sure music is still playing
                if (musicSrv.isPlaying()) {
                    seekBar.setProgress(currentPosition);
                }
            }
        }
    }

    private int currentlyPlaying = -1;
    private void nextSong() {
        playSong(currentlyPlaying + 1);
    }
    private void restartOrLastSong() {
        if ((musicSrv.getCurrentPosition() < TIME_TO_GO_LAST_SONG) && (currentlyPlaying > 0)) {
            playSong(currentlyPlaying - 1);
        } else {
            playSong(currentlyPlaying);
        }
    }
    public void playSong(int index) {
        // Start playback
        seekBar.setProgress(0);
        duration = 0;
        Song song = mSongListInstance.getSongByIndex(index);
        Log.v("imri ", "imri3 currentlyPlaying = " + currentlyPlaying);
        if (musicBound && requestAudio()) {
            if (musicSrv.isPlaying()) {
                musicSrv.stopSong();
            }
            musicSrv.playSong(index);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            currentlyPlaying = index;
            duration = musicSrv.getSongDuration();
            seekBar.setMax(duration);
            String time = getTime(duration);
            totalTime.setText(time);
            songName.setText(song.getTitle());
            artistAlbumName.setText(song.getArtist() + " - " + song.getAlbum());
            new Thread(this).start();
        }

    }
    public void resumeSong() {

        if (musicBound && requestAudio()) {
            musicSrv.resume();
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            new Thread(this).start();
        }
    }
    public void stopSong() {
        playPauseButton.setBackgroundResource(R.drawable.ic_play);
        musicSrv.stopSong();
    }
    public void pauseSong() {
        playPauseButton.setBackgroundResource(R.drawable.ic_play);
        musicSrv.pauseSong();
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


    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.v("imri", "imri + AUDIOFOCUS_GAIN");
                    if (isLoweredVolume) {
                        setNoramlVolume();
                        isLoweredVolume = false;
                    }
                    resumeSong();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.v("imri", "imri + AUDIOFOCUS_LOSS");
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.v("imri", "imri + AUDIOFOCUS_LOSS_TRANSIENT");
                    pauseSong();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.v("imri", "imri + AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    isLoweredVolume = true;
                    lowerVolume();
                    break;
            }
        }
    };
    boolean isLoweredVolume = false;
    private boolean requestAudio() {
        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void lowerVolume() {
        float current = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.v("imri", "imri lower");
        musicSrv.setLowerVolume((float)(current * 0.3));
    }
    private void setNoramlVolume() {
        float current = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        musicSrv.setNoramlVolume((float)(current * 3.3333333333));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                    nextSong();
                    Log.v(TAG, "imri KEYCODE_MEDIA_SKIP_FORWARD ");
                    break;
                case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                    Log.v(TAG, "imri KEYCODE_MEDIA_SKIP_BACKWARD ");
                    restartOrLastSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Log.v(TAG, "imri KEYCODE_MEDIA_PLAY ");
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Log.v(TAG, "imri KEYCODE_MEDIA_PAUSE ");
                    if (musicSrv.isPlaying()) {
                        pauseSong();
                    } else {
                        playSong(currentlyPlaying);
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }
    BroadcastReceiver KeyEventListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() != KeyEvent.ACTION_DOWN) return;

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    // stop music
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    // pause music
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    // next track
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // previous track
                    break;
            }
        }
    };
    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.all_songs_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }
}
