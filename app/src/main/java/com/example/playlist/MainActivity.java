package com.example.playlist;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button firstFragmentButton, secondFragmentButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment playListsFragment;
    Fragment allSongsFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
// get the reference of Button's
        firstFragmentButton = (Button) findViewById(R.id.all_songs);
        secondFragmentButton = (Button) findViewById(R.id.play_lists);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

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
    }


}