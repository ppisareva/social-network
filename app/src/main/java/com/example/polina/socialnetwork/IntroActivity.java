package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class IntroActivity extends AppCompatActivity {
    private Intent intent;
    CallbackManager callbackManager;
    SNApp snApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        snApp = (SNApp) getApplication();
        FacebookSdk.sdkInitialize(getApplicationContext());
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e){
            e.printStackTrace();
        }
        setContentView(R.layout.intro);
        CookieSyncManager.createInstance(this);
        if(CookieManager.getInstance().hasCookies()){
            Intent intent = new Intent(this, ProfileActivity_.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        callbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),

                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user, GraphResponse response) {
                                if (user != null) {
                                    String token = AccessToken.getCurrentAccessToken().getToken();
                                    System.out.println("88888888    " + token);
                                    new LogIn().execute(token);
                                }

                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            class LogIn extends AsyncTask<String, Void, JSONObject> {

                @Override
                protected JSONObject doInBackground(String... params) {
                    return snApp.api.logInWithFacebook(params[0]);
                }

                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    System.out.println(" json " + jsonObject);
                    if (jsonObject != null) {
                        try {
                            SharedPreferences sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
                            SharedPreferences.Editor ed = sharedPreferences.edit();
                            ed.putString(Utils.ID, jsonObject.getString(Utils.ID));
                            ed.putString(Utils.NAME, jsonObject.getString(Utils.NAME));
                            ed.putString(Utils.SEX, jsonObject.optString(Utils.SEX));
                            ed.putString(Utils.PROF_URL, jsonObject.getString(Utils.PROF_URL));
                            snApp.setUserId(jsonObject.getString(Utils.ID));
                            ed.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        intent = new Intent(IntroActivity.this, ProfileActivity_.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onError(FacebookException e) {

                Toast.makeText(IntroActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.sign_up:

               intent = new Intent(this, SignUpActivity_.class);
                break;
            case R.id.sign_in:

                intent = new Intent(this, SignInActivity_.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
