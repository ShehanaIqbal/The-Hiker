package com.example.hiker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.example.hiker.R;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

public class Utils {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    static final String AUTO_TRACK_BUTTON_TAG="auto_track_button_tag";

    public static Uri getImageUri(Context applicationContext, Bitmap selectedImage, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), selectedImage, title, null);
        return Uri.parse(path);
    }


    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }
    public static String getAutoTrackButtonTag(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(AUTO_TRACK_BUTTON_TAG, "1");
    }
    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }
    public static void setAutoTrackButtonTag(Context context, String tag) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(AUTO_TRACK_BUTTON_TAG, tag)
                .commit();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
}
