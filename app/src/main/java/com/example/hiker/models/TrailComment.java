package com.example.hiker.models;

import java.util.Date;

public class TrailComment {
    private final long commentId;
    private final long trailId;
    private final long commenterId;
    private final String commenterName;
    private final String commentText;
    private final Date commentDate;

    public TrailComment(long commentId, long trailId, long commenterId, String commenterName, String commentText, Date commentDate) {
        this.commentId = commentId;
        this.trailId = trailId;
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.commentDate = commentDate;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public long getCommenterId() {
        return commenterId;
    }

    public long getTrailId() {
        return trailId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public long getCommentId() {
        return commentId;
    }

    public String getCommentText() {
        return commentText;
    }
}
