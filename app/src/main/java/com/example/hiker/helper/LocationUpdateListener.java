package com.example.hiker.helper;

import android.location.Location;

public interface LocationUpdateListener {


    /**
     *callback function when new location update is received
     *
     * @param location - the new location
     */
    public void onLocationUpdated(Location location);

}
