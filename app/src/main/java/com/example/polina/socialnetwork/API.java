package com.example.polina.socialnetwork;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by polina on 04.06.15.
 */
public interface API {
    public JSONObject logIn (String email, String password, Context context);
    public JSONObject signUp (String email, String password,  Context context);
    public JSONObject saveProfile(String name, String birthday, String sex, String imageUrl, String imageMiniUrl, Context context);
    public  JSONObject getProfile(Context context);
    public JSONObject newPost (Context context, String massage, JSONObject location,  String image, String account);
    public JSONObject getPosts(Context context, String id);

}
