package com.example.hiker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.example.hiker.R;
import com.example.hiker.model.LatLangSerializable;
import com.google.firebase.firestore.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class Utils {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    static final String AUTO_TRACK_BUTTON_TAG="auto_track_button_tag";

    public static Uri getImageUri(Context applicationContext, Bitmap selectedImage, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), selectedImage, title, null);
        return Uri.parse(path);
    }

    public static float getHikeDistance(List<LatLangSerializable> path) {
        float distance = 0;
        if (path.size() <= 1) {
            return distance;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            distance += getDistance(path.get(i), path.get(i + 1));
        }
        return distance;
    }
    public static double getShortestDistance(List<LatLangSerializable> path, Location location) {
        double shortestDistance = Double.MAX_VALUE;
        for (LatLangSerializable geoPoint : path) {
            double distance = getDistance(geoPoint, new LatLangSerializable(location.getLatitude(), location.getLongitude()));
            if (distance < shortestDistance) {
                shortestDistance = distance;
            }
        }
        return shortestDistance;
    }

    public static double getDistance(LatLangSerializable geoPoint1, LatLangSerializable geoPoint2) {
//        Distance d in meters = 3963.0 * 1000 * arccos[(sin(lat1) * sin(lat2)) + cos(lat1) * cos(lat2) * cos(long2 – long1)]
        double lat1 = geoPoint1.getLatitude();
        double lon1 = geoPoint1.getLongitude();
        double lat2 = geoPoint2.getLatitude();
        double lon2 = geoPoint2.getLongitude();

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);


        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371;  //radius of the earth in km
        // calculate the result
        return(c * r * 1000);
//        return (float) Math.sqrt(Math.pow(geoPoint.getLatitude() - geoPoint1.getLatitude(), 2) + Math.pow(geoPoint.getLongitude() - geoPoint1.getLongitude(), 2));
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
