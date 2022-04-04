package com.example.hiker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HikeSerializable implements Serializable {

    private String id;
    private String title;
    private String distance;
    private String image;
    private List<LatLangSerializable> path;
    private Boolean popular;
    private Boolean featured;
    private List<CommentSerializable> comments;

    public HikeSerializable(String hikeId) {
        this.id = hikeId;
        this.comments = new ArrayList<>();
    }

    public HikeSerializable(String id, String title, String distance, String image, List<LatLangSerializable> path, Boolean popular, Boolean featured , List<CommentSerializable> comments) {
        this.id = id;
        this.title = title;
        this.distance = distance;
        this.image = image;
        this.path = path;
        this.popular = popular;
        this.featured = featured;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDistance() {
        return distance;
    }

    public String getImage() {
        return image;
    }

    public List<LatLangSerializable> getPath() {
        return path;
    }

    public Boolean getPopular() {
        return popular;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPath(List<LatLangSerializable> path) {
        this.path = path;
    }

    public void setPopular(Boolean popular) {
        this.popular = popular;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
    public List<CommentSerializable> getComments() {
        return comments;
    }
    public void addComment(CommentSerializable comment) {
        this.comments.add(comment);
    }
}
