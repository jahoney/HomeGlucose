package com.example.homeglucose;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    private Handler myHandler;
    private Runnable myRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        myRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        };
        myHandler = new Handler();
        myHandler.postDelayed(myRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myHandler != null && myRunnable != null)
        myHandler.removeCallbacks(myRunnable);
    }
}
