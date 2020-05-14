package com.frienders.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.frienders.main.activity.group.GroupChatActivity;
import com.frienders.main.config.ActivityParameters;

public class SplashActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private ProgressDialog loading;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        String videoLink = "";
        if(getIntent().getExtras().get(ActivityParameters.videoLink) != null)
        {
            videoLink = getIntent().getExtras().get(ActivityParameters.videoLink).toString();
        }
        loading = new ProgressDialog(this);
        loading.setMessage("Loading.");
        loading.show();
        video.setVideoPath(videoLink);
        video.start();
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                loading.dismiss();
            }
        });
        video.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {

        Intent intent = new Intent(this, GroupChatActivity.class);
        startActivity(intent);
        finish();
    }
}
