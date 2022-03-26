package com.example.hiker.utils;

import com.example.hiker.model.Hike;
import com.example.hiker.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {
    public static User updateLikes(String hikeTitle, User user) {
        List<String> favHikes;
        if (user.getFavorites() == null) {
            favHikes = new ArrayList<>();
        } else {
            favHikes = user.getFavorites();
        }
        if (favHikes.contains(hikeTitle)) {
            return user;
        }
        favHikes.add(hikeTitle);
        user.setFavorites(favHikes);
        return user;
    }

    public static User updateDislikes(String hikeTitle, User user) {
        List<String> favHikes;
        if (user.getFavorites() == null) {
            favHikes = new ArrayList<>();
        } else {
            favHikes = user.getFavorites();
        }
        if (!favHikes.contains(hikeTitle)) {
            return user;
        }
        favHikes.remove(hikeTitle);
        user.setFavorites(favHikes);
        return user;
    }

    public static boolean userFav(User user, Hike hike) {
        if (user.getFavorites() == null || user.getFavorites().size() == 0) return false;
        return user.getFavorites().contains(hike.getTitle());
    }

    public static boolean userFav(User user, String hike) {
        if (user.getFavorites() == null || user.getFavorites().size() == 0) return false;
        return user.getFavorites().contains(hike);
    }
}
