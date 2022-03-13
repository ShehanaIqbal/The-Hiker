package com.example.hiker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.hiker.auth.FireBaseAuthHelper;

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
                navigateToRoot();
            }
        }, 3000);
    }

    private void navigateToRoot() {
        Intent rootPage;
        if (FireBaseAuthHelper.getCurrentUser()!= null){
            rootPage = new Intent(this ,HomeActivity.class);
        } else {
            rootPage = new Intent(this , SignInActivity.class);
        }
        finish();
        startActivity(rootPage);
    }
}
