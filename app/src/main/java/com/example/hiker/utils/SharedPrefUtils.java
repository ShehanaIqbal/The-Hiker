package com.example.hiker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.hiker.model.CommentSerializable;
import com.google.gson.Gson;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.LatLangSerializable;
import com.example.hiker.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedPrefUtils {
    public static User saveUserInSP(User user, Context context) {
        SharedPreferences pref = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(user);

        editor.putString("user", json);
        editor.commit();

        return user;
    }

    public static User getUserFromSP(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode

        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");

        if (json.equals("")) return null;

        return gson.fromJson(json, User.class);
    }

    public static HikeSerializable onGoingHike(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode

        Gson gson = new Gson();
        String json = mPrefs.getString("onGoingHike", "");

        if (json.equals("")) return null;

        return gson.fromJson(json, HikeSerializable.class);
    }

    public static void deleteOnGoingHike(Context context) {
        SharedPreferences pref = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("onGoingHike", "");
        editor.commit();
    }

    public static void addLocToOnGoingHike(Context context,String hikeId, double latitude, double longitude) {
        SharedPreferences pref = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        HikeSerializable hike = onGoingHike(context);

        if (hike == null) {
            hike = new HikeSerializable(hikeId);
            List<LatLangSerializable> path = new ArrayList<>();
            path.add(new LatLangSerializable(latitude, longitude));
            hike.setPath(path);
        } else {
            List<LatLangSerializable> path = hike.getPath();
            path.add(new LatLangSerializable(latitude, longitude));
            hike.setPath(path);
        }

        Gson gson = new Gson();
        String json = gson.toJson(hike);

        editor.putString("onGoingHike", json);
        editor.commit();
    }
    public static String getOnGoingHikeId(Context context) {
        HikeSerializable hike = onGoingHike(context);
        if (hike == null) return UUID.randomUUID().toString();
        return hike.getId();
    }
    public static void addCommentToOnGoingHike(Context context, CommentSerializable comment, String hikeId) {
        SharedPreferences pref = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        HikeSerializable hike = onGoingHike(context);

        if (hike == null) {
            hike = new HikeSerializable(hikeId);
        }
        hike.addComment(comment);

        Gson gson = new Gson();
        String json = gson.toJson(hike);

        editor.putString("onGoingHike", json);
        editor.commit();
    }
    public  static List<CommentSerializable> getCommentsFromOnGoingHike(Context context, String hikeId) {
        SharedPreferences pref = context.getSharedPreferences("HikerPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        HikeSerializable hike = onGoingHike(context);

        if (hike == null) {
            hike = new HikeSerializable(hikeId);
        }
        return hike.getComments();
    }
}
