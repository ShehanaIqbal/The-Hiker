package com.example.hiker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SubmitTrail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_trail);
    }

    public void goToSignUp(View view){
        Intent intent = new Intent(SubmitTrail.this, Home.class);
        startActivity(intent);
    }
}