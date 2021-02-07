package com.example.mp3player.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mp3player.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.res.Resources;

import org.w3c.dom.Text;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayer extends Fragment implements View.OnClickListener{

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static TrackPlayer newInstance(int index) {
        TrackPlayer fragment = new TrackPlayer();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

        initializeMediaPlayer();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //final TextView textView = view.findViewById(R.id.section_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        TrackName_tb = (TextView) view.findViewById(R.id.TrackName_tb);
        lastTime_tb = (TextView) view.findViewById(R.id.lastTime_tb);
        importTrack_pb = (Button) view.findViewById(R.id.importTrack_pb);
        seek_pb = (Button) view.findViewById(R.id.seek_pb);
        rew_pb = (FloatingActionButton) view.findViewById(R.id.rew_pb);
        playpause_pb = (FloatingActionButton) view.findViewById(R.id.playpause_pb);
        adv_pb = (FloatingActionButton) view.findViewById(R.id.adv_pb);
        trackSeek_sk = (SeekBar) view.findViewById(R.id.trackSeek_sk);
        step_tb = (EditText) view.findViewById(R.id.step_tb);
        hour_tb = (EditText) view.findViewById(R.id.hour_tb);
        min_tb = (EditText) view.findViewById(R.id.min_tb);
        sec_tb = (EditText) view.findViewById(R.id.sec_tb);

        importTrack_pb.setOnClickListener(this);
        seek_pb.setOnClickListener(this);
        rew_pb.setOnClickListener(this);
        playpause_pb.setOnClickListener(this);
        adv_pb.setOnClickListener(this);

        return view;
    }

    View view;
    private TextView TrackName_tb;
    private TextView lastTime_tb;
    private Button importTrack_pb;
    private Button seek_pb;
    private FloatingActionButton rew_pb;
    private FloatingActionButton playpause_pb;
    private FloatingActionButton adv_pb;
    private SeekBar trackSeek_sk;
    private EditText step_tb;
    private EditText hour_tb;
    private EditText min_tb;
    private EditText sec_tb;

    private boolean playingTrack = false;

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.importTrack_pb:
                Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a File"), 123);
                break;
            case R.id.seek_pb:

                break;
            case R.id.rew_pb:

                break;
            case R.id.playpause_pb:
                if(playingTrack)
                {
                    mediaPlayer.pause();
                    playpause_pb.setImageResource(android.R.drawable.ic_media_play);
                    playingTrack = false;
                }
                else
                {
                    mediaPlayer.start();
                    playpause_pb.setImageResource(android.R.drawable.ic_media_pause);
                    playingTrack = true;
                }
                break;
            case R.id.adv_pb:

                break;
        }
    }

    private Uri selectedfile;
    MediaPlayer mediaPlayer;

    private void prepareMediaFile()
    {
        try {
            mediaPlayer.setDataSource(getActivity().getApplicationContext(), selectedfile);
            mediaPlayer.prepare();
        }
        catch (Exception e) {}
    }

    private void initializeMediaPlayer()
    {
        mediaPlayer = new MediaPlayer();
        //mediaPlayer.setAudioAttributes();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == Activity.RESULT_OK) {
            selectedfile = data.getData(); //The uri with the location of the file
            TrackName_tb.setText(selectedfile.getPath().substring(selectedfile.getPath().lastIndexOf('/')+1));
            prepareMediaFile();
        }
    }
}