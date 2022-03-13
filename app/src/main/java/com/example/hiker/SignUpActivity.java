package com.example.hiker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hiker.auth.AuthRedirectHandler;
import com.example.hiker.auth.FireBaseAuthHelper;

public class SignUpActivity extends AppCompatActivity implements AuthRedirectHandler {
    String username ;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void goToSignIn(){
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    public void onSignUpClick(View view) {
        EditText uname= findViewById(R.id.username_sign_up);
        EditText pwd = findViewById(R.id.password_sign_up);
        EditText cpwd = findViewById(R.id.c_password_sign_up);

        username = uname.getText().toString();
        password = pwd.getText().toString();
        String cpassword = cpwd.getText().toString();
        Log.d("signup",uname+password+cpassword);
        if (password!=cpassword ||username=="" ||password==""){

        }
        else {
            FireBaseAuthHelper.createUserWithEmailPw(username,password,this,this);
        }
    }

    @Override
    public void onAuthSuccess(String email, String uuid) {
        redirectHome();
    }

    @Override
    public void onAuthFail(String message) {
        Toast.makeText(this,"Error",Toast.LENGTH_SHORT);
    }

    private void redirectHome(){
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}