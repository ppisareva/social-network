package com.example.polina.socialnetwork;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by polina on 10.07.15.
 */
public class Post {


    double createdAt;
    boolean ownLike;
    String name;
    String _idUser;

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

    public String get_idUset() {
        return _idUser;
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

    public static final String TIMESTAMP = "created_at";
    public static final String LIKE = "own_like";
    public static final String CREATED_BY = "created_by";
    public static final String NAME = "name";
    public static final String IDUSER = "_id";
    public static final String IDPOST = "_id";
    public static final String MASSAGE = "massage";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IMAGE = "image";



    public Post(String idPost, Double createdAt, boolean ownLike, String name, String _idUser, String message, String latitude, String longitude, String image, String profileImage) {
        this.createdAt = createdAt;
        this.ownLike = ownLike;
        this.name = name;
        this._idUser = _idUser;
        this.idPost = idPost;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.profileImage = profileImage;
    }

    static Post parse(JSONObject o, String profileImage) throws JSONException {
        String idPost = o.getString(IDPOST);
        Double created_at = o.getDouble(TIMESTAMP);
        boolean ownLike = o.getBoolean(LIKE);
        JSONObject object = o.getJSONObject(CREATED_BY);
        String name = object.getString(NAME);
        String idUser = object.getString(IDUSER);
        String message = o.optString(MASSAGE);
        object = o.optJSONObject(LOCATION);
        String latitude = "";
        String longitude = "";
        if (object != null) {
            latitude = object.getString(LATITUDE);
            longitude = object.getString(LONGITUDE);
        }
        String image = o.optString(IMAGE, "");
        return new Post(idPost, created_at, ownLike, name, idUser, message, latitude, longitude, image, profileImage);
    }
}
