package com.example.hiker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.hiker.auth.AuthRedirectHandler;
import com.example.hiker.auth.FireBaseAuthHelper;

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity implements AuthRedirectHandler {

    String username ;
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

    }


    public void onLoginClick(View view) {
        EditText uname= findViewById(R.id.username);
        EditText pwd = findViewById(R.id.password);
        username = uname.getText().toString();
        password = pwd.getText().toString();
        FireBaseAuthHelper.signInWIthEMailPw(username,password,this,this);
    }

    private void redirectHome(){
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAuthSuccess(String email, String uuid) {
        redirectHome();
    }

    @Override
    public void onAuthFail(String message) {
        Log.d("Error",message);

    }
}