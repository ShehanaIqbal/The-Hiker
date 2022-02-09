package com.example.hiker.auth;

public interface AuthRedirectHandler {
    void onAuthComplete(String email, String uuid);
    void onAuthFail(String message);
}

