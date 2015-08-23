package com.example.polina.socialnetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by polina on 10.08.15.
 */
public class Comment {
    String name;
    String profileImage;
    String userID;
    Double timeStemp;
    String commentID;
    String comment;



    public Comment(String userID, String profileImage, String name, Double timeStemp, String commentID, String comment) {
        this.userID = userID;
        this.profileImage = profileImage;
        this.name = name;
        this.timeStemp = timeStemp;
        this.commentID = commentID;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getUserID() {
        return userID;
    }

    public Double getTimeStemp() {
        return timeStemp;
    }

    public String getCommentID() {
        return commentID;
    }

    public String getComment() {
        return comment;
    }

    public static Comment parse (JSONObject o) throws JSONException {
        JSONObject object =o.getJSONObject(Utils.CREATED_BY);
        String prifileImage = object.getString(Utils.MINI_PROF_URL);
        String userID = object.getString(Utils.USER_ID);
        String name = object.getString(Utils.NAME);
        Double timeStemp = o.getDouble(Utils.TIMESTAMP);
        String commentID = o.getString(Utils.COMMENT_ID);
        String comment=o.getString(Utils.COMMENT);
        return new Comment(userID, prifileImage, name, timeStemp, commentID, comment);
    }
}
