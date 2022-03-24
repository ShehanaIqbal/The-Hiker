package com.example.hiker.models;

import android.location.Location;

import java.util.List;

public class Trail {
    private final String country;
    private final String city;
    private final String trailName;
    private final String dateCreated;
    private String description;
    private int rating;
    private List<BlobWithLocation> blobs;
    private List<Location> trailPoints;
    private Location startPoint;
    private Location endPoint;
    private List<TrailComment> trailComments;


    public Trail(String country, String city, String trailName, String dateCreated, String description,Location startPoint) {
        this.country = country;
        this.city = city;
        this.trailName = trailName;
        this.dateCreated = dateCreated;
        this.description = description;
        this.startPoint=startPoint;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<BlobWithLocation> getBlobs() {
        return blobs;
    }

    public void addBlobs(BlobWithLocation blobs) {
        this.blobs.add(blobs);
    }

    public List<Location> getTrailPoints() {
        return trailPoints;
    }

    public void addTrailPoints(Location trailPoints) {
        this.trailPoints.add(trailPoints);
    }


    public Location getEndPoint() {
        if (endPoint==null) {
            return trailPoints.get(-1);
        }else{
            return endPoint;
        }
    }

    public void setEndPoint(Location endPoint) {
        this.endPoint = endPoint;
    }

    public List<TrailComment> getTrailComments() {
        return trailComments;
    }
    public void addTrailComment(TrailComment trailComment){
        this.trailComments.add(trailComment);
    }

}
