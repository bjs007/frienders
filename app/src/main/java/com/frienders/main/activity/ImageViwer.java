package com.frienders.main.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.frienders.main.R;
import com.frienders.main.config.UsersFirebaseFields;

public class ImageViwer extends AppCompatActivity {

    private String videoLink = null;
    private ImageView imageView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viwer);
        if(getIntent().getExtras().get(UsersFirebaseFields.imagelink) != null)
        {
            videoLink = getIntent().getExtras().get(UsersFirebaseFields.imagelink).toString();
        }

        if(videoLink != null)
        {
            progressBar = findViewById(R.id.image_display_progressbar);
            progressBar.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.image_full_display);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(getApplicationContext())
                    .load(videoLink)
                    .into(imageView);
        }
    }
}
