package com.example.hiker.model;

import java.io.Serializable;

public class LatLangSerializable implements Serializable {
    private Double latitude;
    private Double longitude;

    public LatLangSerializable(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLangSerializable() {
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
