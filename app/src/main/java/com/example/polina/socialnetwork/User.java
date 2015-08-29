package com.example.polina.socialnetwork;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by polina on 31.07.15.
 */
public class User {
    String userId;
    String userURL;
    String userName;

    public User(String userId, String userURL, String userName) {
        this.userId = userId;
        this.userURL = userURL;
        this.userName = userName;
    }

    public static User parse(JSONObject o){
        try {
            return new User(o.getString(Utils.ID), o.getString(Utils.MINI_PROF_URL), o.getString(Utils.NAME));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
