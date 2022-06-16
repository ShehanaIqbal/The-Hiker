package com.example.hiker.auth;

public interface AuthRedirectHandler {
    void onAuthSuccess(String email, String uuid);
    void onAuthFail(String message);
}

