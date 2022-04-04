package com.example.hiker.api;

import android.util.Log;

import com.example.hiker.model.CommentSerializable;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.hiker.model.Hike;
import com.example.hiker.model.User;
import com.example.hiker.utils.UserUtils;
import com.google.gson.Gson;

import java.util.UUID;

public class FirebaseApi {
    public static Task<QuerySnapshot> getAllHikes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("hikes").get();
    }

    public static Task<QuerySnapshot> getUser(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").whereEqualTo("email", email).get();
    }

    public static Task<DocumentReference> saveUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").add(user);
    }

    public static Task<DocumentReference> saveHike(Hike hike) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("hikes").add(hike);
    }

    public static Task<Void> saveComment(CommentSerializable comment) {
        Log.d("FirebaseApi", new Gson().toJson(comment));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("comments").document(comment.getCommentId()).set(comment);
    }

    public static StorageReference saveProfilePicture() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child("profile-pics/" + UUID.randomUUID().toString());
        return ref;
    }
    public static StorageReference saveHikePictures() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child("hike-pics/" + UUID.randomUUID().toString());
        return ref;
    }

    public static Task<Void> saveUserLike(String hikeTitle, User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User updatedUser = UserUtils.updateLikes(hikeTitle, user);
        return db.collection("users").document(user.getId()).set(updatedUser);
    }

    public static Task<Void> saveUserDislike(String hikeTitle, User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User updatedUser = UserUtils.updateDislikes(hikeTitle, user);
        return db.collection("users").document(user.getId()).set(updatedUser);
    }

    public static Task<Void> updateUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").document(user.getId()).set(user);
    }
}
