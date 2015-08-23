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
    int like_count;
    Comment last_comment;
    int comments_count;


    public int getComments_count() {
        return comments_count;
    }

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

    public int getLike_count() {
        return like_count;
    }

    public Comment getLast_comment() {
        return last_comment;
    }

    public Post(String idPost, Double createdAt, boolean ownLike, String name, String idUser, String message,
                String latitude, String longitude, String image, String profileImage, int like_count, Comment last_comment, int comments_count) {
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
        this.like_count = like_count;
        this.last_comment = last_comment;
        this.comments_count = comments_count;
    }

    static Post parse(JSONObject o) throws JSONException {
        String idPost = o.getString(Utils.IDPOST);
        Double created_at = o.getDouble(Utils.TIMESTAMP);
        boolean ownLike = o.getBoolean(Utils.LIKE);
        JSONObject object = o.getJSONObject(Utils.CREATED_BY);
        String name = object.getString(Utils.NAME);
        String idUser = object.getString(Utils.IDUSER);
        String profileImage = object.getString(Utils.MINI_PROF_URL);
        String message = o.optString(Utils.MASSAGE);
        object = o.optJSONObject(Utils.LOCATION);
        String latitude = "";
        String longitude = "";
        if (object != null) {
            latitude = object.optString(Utils.LATITUDE);
            longitude = object.optString(Utils.LONGITUDE);
        }
        String image = o.optString(Utils.IMAGE, "");
        int like_count = o.optInt(Utils.LIKES_COUNT);
        Comment comment=null;
        JSONObject ob =  o.optJSONObject(Utils.LAST_COMMENT);
        System.out.println(ob);
        if(ob!=null){
           comment = Comment.parse(o.optJSONObject((Utils.LAST_COMMENT)));
        }
        int comments_count = o.optInt(Utils.COMMENTS_COUNT);
        return new Post(idPost, created_at, ownLike, name, idUser, message, latitude, longitude, image, profileImage, like_count, comment,  comments_count);
    }
}
