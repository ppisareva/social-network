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
    String userId;
    String postId;
    String message;
    String latitude;
    String longitude;
    String image;
    String profileImage;
    int likeCount;
    Comment lastComment;
    int commentsCount;

    public void setOwnLike(boolean ownLike) {
        this.ownLike = ownLike;
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

    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
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

    public int getLikeCount() {
        return likeCount;
    }

    public Comment getLastComment() {
        return lastComment;
    }

    public int getCommentsCount() {
        return commentsCount;
    }



    public Post(String idPost, Double createdAt, boolean ownLike, String name, String idUser, String message,
                String latitude, String longitude, String image, String profileImage, int like_count, Comment lastComment, int commentsCount) {
        this.createdAt = createdAt;
        this.ownLike = ownLike;
        this.name = name;
        this.userId = idUser;
        this.postId = idPost;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.profileImage = profileImage;
        this.likeCount = like_count;
        this.lastComment = lastComment;
        this.commentsCount = commentsCount;
    }

    static Post parse(JSONObject o) throws JSONException {
        String postId = o.getString(Utils.ID);
        Double createdAt = o.getDouble(Utils.TIMESTAMP);
        boolean ownLike = o.getBoolean(Utils.LIKE);
        JSONObject object = o.getJSONObject(Utils.CREATED_BY);
        String name = object.getString(Utils.NAME);
        String userId = object.getString(Utils.ID);
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
        int likeCount = o.optInt(Utils.LIKES_COUNT);
        Comment comment=null;
        JSONObject ob =  o.optJSONObject(Utils.LAST_COMMENT);
        System.out.println(ob);
        if(ob!=null){
           comment = Comment.parse(o.optJSONObject((Utils.LAST_COMMENT)));
        }
        int commentsCount = o.optInt(Utils.COMMENTS_COUNT);
        return new Post(postId, createdAt, ownLike, name, userId, message, latitude, longitude, image, profileImage, likeCount, comment,  commentsCount);
    }
}
