package com.example.polina.socialnetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by polina on 10.07.15.
 */
public class Post implements Serializable {


    double createdAt;
    boolean ownLike;
    String name;
    String idUser;
    String idPost;
    String message;
    String latitude;
    String longitude;
    String image;
    String profileImage;


    public void setOwnLike(boolean ownLike) {
        this.ownLike = ownLike;
    }


    public String getIdPost() {
        return idPost;
    }


    public double getCreatedAt() {
        return createdAt;
    }

    public boolean isOwnLike() {
        return ownLike;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getImage() {
        return image;
    }

    public String getProfileImage() {
        return profileImage;
    }


    public Post(String idPost, Double createdAt, boolean ownLike, String name, String idUser, String message, String latitude, String longitude, String image, String profileImage) {
        this.createdAt = createdAt;
        this.ownLike = ownLike;
        this.name = name;
        this.idUser = idUser;
        this.idPost = idPost;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.profileImage = profileImage;
    }

    static Post parse(JSONObject o, String profileImage) throws JSONException {
        String idPost = o.getString(Utils.IDPOST);
        Double created_at = o.getDouble(Utils.TIMESTAMP);
        boolean ownLike = o.getBoolean(Utils.LIKE);
        JSONObject object = o.getJSONObject(Utils.CREATED_BY);
        String name = object.getString(Utils.NAME);
        String idUser = object.getString(Utils.IDUSER);
        String message = o.optString(Utils.MASSAGE);
        object = o.optJSONObject(Utils.LOCATION);
        String latitude = "";
        String longitude = "";
        if (object != null) {
            latitude = object.optString(Utils.LATITUDE);
            longitude = object.optString(Utils.LONGITUDE);
        }
        String image = o.optString(Utils.IMAGE, "");
        return new Post(idPost, created_at, ownLike, name, idUser, message, latitude, longitude, image, profileImage);
    }
}
