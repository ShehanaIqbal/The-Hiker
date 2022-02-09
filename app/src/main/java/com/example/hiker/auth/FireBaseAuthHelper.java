package com.example.hiker.auth;

import android.app.Activity;
import android.util.Log;

import com.example.hiker.auth.AuthRedirectHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;

public class FireBaseAuthHelper {
    private static final FirebaseAuth mAuth =FirebaseAuth.getInstance();
    private static FirebaseUser fUser ;

    //Handle Exceptions
    private static String handleAuthException(Exception ex){
         if ( ex instanceof FirebaseAuthInvalidCredentialsException) {
             return "The verification Code is Invalid!";
         } else if (ex instanceof FirebaseTooManyRequestsException) {
             return "The SMS Quota for free firebase account has been exceed!";
         } else if (ex instanceof FirebaseException) {
             return ex.getMessage();
         } else {
             return ex.getMessage();
         }
    }
    public static FirebaseAuth getmAuth() {
        return mAuth;
    }

    public static FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }

    public static void logout() throws  Exception{
        if (getCurrentUser()!=null){
            mAuth.signOut();
            setUser(null);
        } else {
            throw new Exception("Failed");
        }
    }
    private static void setUser(FirebaseUser user){
        fUser=user;
    }
    public static void createUserWithEmailPw(String email , String password, Activity activity , AuthRedirectHandler authRedirectHandler){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(activity , task -> {
            if (task.isSuccessful()){
                Log.d(TAG ,"Created user : "+email);
                FirebaseUser user = task.getResult().getUser();
                setUser(user);
                assert user != null;
                authRedirectHandler.onAuthComplete(user.getEmail(),user.getUid());
            } else {
                authRedirectHandler.onAuthFail(handleAuthException(task.getException()));
            }
        });
    }
    public static void signInWIthEMailPw(String email , String password, Activity activity , AuthRedirectHandler authRedirectHandler){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(activity ,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG ,"Created user : "+email);
                    FirebaseUser user = task.getResult().getUser();
                    setUser(user);
                    assert user != null;
                    authRedirectHandler.onAuthComplete(user.getEmail(),user.getUid());
                } else {
                    authRedirectHandler.onAuthFail(handleAuthException(task.getException()));
                }
            }
        });
    }
}
