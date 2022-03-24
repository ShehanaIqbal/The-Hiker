package com.example.hiker.models;

import android.location.Location;

import java.sql.Blob;

public class BlobWithLocation {
    private final Blob[] blob;
    private final Location location;

    public BlobWithLocation(Blob[] blob, Location location) {
        this.blob = blob;
        this.location = location;
    }


    public Blob[] getBlob() {
        return blob;
    }

    public Location getLocation() {
        return location;
    }
}
