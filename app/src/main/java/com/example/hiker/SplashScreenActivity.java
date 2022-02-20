package com.example.hiker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigation();
        finish();
    }
    private void initNavigation() {
        // stay 1 seconds in splash
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToLogin();
            }
        }, 1000);
    }

    private void navigateToLogin() {
        startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
    }
}
