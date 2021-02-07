package com.example.mp3player.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.keystore.SecureKeyImportUnavailableException;
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

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

        m_index = index;
        initializeMediaPlayer();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                int globalState = 1;
                while (true) {
                    try {
                        while(!playingTrack){Thread.sleep(1000);}
                        if (mediaPlayer != null) {
                            if(!tracking){
                                int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                                if(trackSeek_sk != null) trackSeek_sk.setProgress(mCurrentPosition);
                                updateTimer(mediaPlayer.getCurrentPosition());
                            }
                        }

                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }

        }.execute();
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
        trackSeek_sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    tracking = false;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    tracking = true;
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer != null && fromUser){
                        mediaPlayer.seekTo(progress * 1000);
                        updateTimer(progress*1000);
                    }
                }
            });

        mPrefs = getActivity().getSharedPreferences(String.valueOf(m_index), 0);
        mEditor = mPrefs.edit();

        selectedfile = Uri.parse(mPrefs.getString("CurrentFileUri", ""));
        updateTimer(mPrefs.getInt("Time", 0));
        prepareMediaFile();

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
    private boolean tracking = false;

    private int m_index;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.importTrack_pb:
                Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select a File"), 123);
                break;
            case R.id.seek_pb:
                mediaPlayer.seekTo((((Integer.parseInt(String.valueOf(hour_tb.getText()))*60)+Integer.parseInt(String.valueOf(min_tb.getText())))*60 +Integer.parseInt(String.valueOf(sec_tb.getText()))) * 1000);
                break;
            case R.id.adv_pb:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + Integer.parseInt(String.valueOf(step_tb.getText())) *1000);
                break;
            case R.id.rew_pb:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - Integer.parseInt(String.valueOf(step_tb.getText())) *1000);
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
        }
    }

    private int time;

    private void updateTimer(int timeMS)
    {
        String seconds = String.valueOf(timeMS /1000 % 60) ;
        String minutes = String.valueOf(timeMS /1000 / 60 % 60) ;
        String hours = String.valueOf(timeMS /1000 / 60 / 60) ;
        if (seconds.length() == 1) seconds = "0" + seconds;
        if (minutes.length() == 1) minutes = "0" + minutes;
        if (hours.length() == 1) hours = "0" + hours;
        if(lastTime_tb != null) lastTime_tb.setText(hours + ":" + minutes + ":" + seconds);

        mEditor.putInt("Time", timeMS).commit();
        time = timeMS;
    }

    private Uri selectedfile;
    MediaPlayer mediaPlayer;

    private void prepareMediaFile()
    {
        initializeMediaPlayer();
        try {
            mediaPlayer.setDataSource(getActivity().getApplicationContext(), selectedfile);
            mediaPlayer.prepare();
            TrackName_tb.setText(selectedfile.getPath().substring(selectedfile.getPath().lastIndexOf('/')+1));

            initializeSeekbar();
        }
        catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void initializeMediaPlayer()
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
    }

    private void initializeSeekbar()
    {
        trackSeek_sk.setMax(mediaPlayer.getDuration()/1000);
        mediaPlayer.seekTo(time);
        trackSeek_sk.setProgress(time/1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == Activity.RESULT_OK) {
            selectedfile = data.getData(); //The uri with the location of the file

            getActivity().grantUriPermission(getActivity().getPackageName(), selectedfile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            // Check for the freshest data.
            //noinspection WrongConstant
            getActivity().getContentResolver().takePersistableUriPermission(selectedfile, takeFlags);
            //getPrefs().setAlarmSoundUri(selectedfile.toString());

            mEditor.putString("CurrentFileUri", selectedfile.toString()).commit();
            time = 0;
            prepareMediaFile();
        }
    }
}