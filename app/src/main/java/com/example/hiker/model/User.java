package com.example.hiker.model;

import java.util.List;

public class User {

    private String id;
    private String email;
    private String name;
    private String image;
    private List<String> favorites;

    public User() {
    }

    public User(String id, String title, String distance, String image, List<String> favorites) {
        this.id = id;
        this.email = title;
        this.name = distance;
        this.image = image;
        this.favorites = favorites;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

}
