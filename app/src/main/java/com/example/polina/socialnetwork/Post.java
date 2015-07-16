package com.example.polina.socialnetwork;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by polina on 10.07.15.
 */
public class Post {


    double createdAt;
    boolean own_like;
    String name;
    String _id;
    String message;
    String latitude;
    String longitude;
    String image;
    String profile_image;

    public void setOwn_like(boolean own_like) {
        this.own_like = own_like;
    }

    public double getCreatedAt() {
        return createdAt;
    }

    public boolean isOwn_like() {
        return own_like;
    }

    public String getName() {
        return name;
    }

    public String get_id() {
        return _id;
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


    public String getProfile_image() {
        return profile_image;
    }

    public static final String TIMESTAMP = "createdAt";
    public static final String LIKE = "own_like";
    public static final String CREATED_BY = "created_by";
    public static final String NAME = "name";
    public static final String ID = "_id";
    public static final String MASSAGE = "massage";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IMAGE = "image";


    public Post(Double createdAt, boolean own_like, String name, String _id, String message, String latitude, String longitude, String image, String profile_image) {
        this.createdAt = createdAt;
        this.own_like = own_like;
        this.name = name;
        this._id = _id;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.profile_image = profile_image;
    }

    static Post parse(JSONObject o, String profile_image) throws JSONException {
        Double created_at = o.getDouble(TIMESTAMP);
        boolean own_like = o.getBoolean(LIKE);
        JSONObject object = o.getJSONObject(CREATED_BY);
        String name = object.getString(NAME);
        String _id = object.getString(ID);
        String message = o.optString(MASSAGE);
        object = o.optJSONObject(LOCATION);
        String latitude = "";
        String longitude = "";
        if (object != null) {
            latitude = object.getString(LATITUDE);
            longitude = object.getString(LONGITUDE);
        }
        String image = o.optString(IMAGE, "");
        return new Post(created_at, own_like, name, _id, message, latitude, longitude, image, profile_image);
    }
}
