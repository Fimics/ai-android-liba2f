package com.noetix.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.libnoetix.demo.R;
import com.noetix.libnoetix.IRobotSDKManager;
import com.noetix.libnoetix.RobotConfig;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DataReceiver mDataReceiver;
    int status =0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        initSDK();
        IRobotSDKManager.getInstance().chatMode(true);

        AppCompatButton btnChat = this.findViewById(R.id.btn_chat);
        AppCompatButton btnTrack = this.findViewById(R.id.btn_track);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trackIntent = new Intent(SplashActivity.this, LiveFaceActivity.class);
                startActivity(trackIntent);
            }
        });

    }

    private void initSDK(){

        RobotConfig robotConfig = new RobotConfig.Builder(this)
                .enableLog(true)
                .setSerialPort("")
                .setCan("")
                .setZeroDuration(5.0f)
                .setUAppKey("")
                .setUChannel("")
                .setNeckType(2)
                .setExt("")
                .build();
        IRobotSDKManager.getInstance().init(robotConfig);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}