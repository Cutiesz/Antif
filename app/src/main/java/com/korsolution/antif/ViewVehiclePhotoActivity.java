package com.korsolution.antif;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ViewVehiclePhotoActivity extends AppCompatActivity {

    private String pathImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vehicle_photo);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        pathImage = getIntent().getStringExtra("pathImage");

        ImageView imgVehicle = (ImageView) findViewById(R.id.imgVehicle);

        Glide.with(this)
                .load(pathImage)
                .error(R.drawable.blank_img)
                .into(imgVehicle);
    }
}
