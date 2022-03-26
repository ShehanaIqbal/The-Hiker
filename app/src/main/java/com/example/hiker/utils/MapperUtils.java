package com.example.hiker.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.example.hiker.model.Hike;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.LatLangSerializable;

import java.util.ArrayList;
import java.util.List;

public class MapperUtils {
    public static HikeSerializable convertToSerializable(Hike hike) {
        HikeSerializable h = new HikeSerializable();
        h.setId(hike.getId());
        h.setTitle(hike.getTitle());
        h.setDistance(hike.getDistance());
        h.setImage(hike.getImage());
        h.setPopular(hike.getPopular());
        h.setFeatured(hike.getFeatured());
        h.setPath(convertToSerializableLatLang(hike.getPath()));
        return h;
    }

    private static List<LatLangSerializable> convertToSerializableLatLang(List<GeoPoint> path) {
        List<LatLangSerializable> output = new ArrayList<>();
        for (GeoPoint point : path) {
            output.add(new LatLangSerializable(point.getLatitude(), point.getLongitude()));
        }
        return output;
    }

    public static List<GeoPoint> convertToGeoPoint(List<LatLangSerializable> path) {
        List<GeoPoint> output = new ArrayList<>();
        for (LatLangSerializable point : path) {
            output.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
        }
        return output;
    }

    public static List<LatLng> convertToLatLang(List<LatLangSerializable> path) {
        List<LatLng> latLngs = new ArrayList<>();
        for (LatLangSerializable geoPoint : path) {
            latLngs.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
        }
        return latLngs;
    }
}
