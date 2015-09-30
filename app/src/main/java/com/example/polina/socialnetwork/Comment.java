package com.example.polina.socialnetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by polina on 10.08.15.
 */
public class Comment  implements Serializable{

    String name;
    String profileImage;
    String userID;
    Double timestamp;
    String commentId;
    String comment;



    public Comment(String userID, String profileImage, String name, Double timestamp, String commentId, String comment) {
        this.userID = userID;
        this.profileImage = profileImage;
        this.name = name;
        this.timestamp = timestamp;
        this.commentId = commentId;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }


    public Double getTimestamp() {
        return timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getComment() {
        return comment;
    }

    public static Comment parse (JSONObject o)  {
        JSONObject object =o.optJSONObject(Utils.CREATED_BY);
        String profileImage = object.optString(Utils.MINI_PROF_URL);
        String userID = object.optString(Utils.ID);
        String name = object.optString(Utils.NAME);
        Double timestamp = o.optDouble(Utils.TIMESTAMP);
        String commentID = o.optString(Utils.ID);
        String comment=o.optString(Utils.COMMENT);
        return new Comment(userID, profileImage, name, timestamp, commentID, comment);
    }
}
