package com.example.hiker.model;

import android.location.Location;

import com.example.hiker.utils.Utils;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nullable;

public class CommentSerializable implements Serializable {
    private String commentId;
    private String comment;
    private String imageUrl;
    private GeoPoint location;
    private String hikeId;

    public CommentSerializable(String comment,String hikeId , @Nullable String imageUrl, @Nullable GeoPoint location) {
        this.commentId= UUID.randomUUID().toString();
        this.comment = comment;
        this.hikeId = hikeId;
        this.imageUrl = imageUrl;
        this.location = location;
    }
    public CommentSerializable(){}
    public String getComment() {
        return comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String setImageUrl(String imageUrl) {
        return this.imageUrl = imageUrl;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getHikeId() {
        return hikeId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

}
