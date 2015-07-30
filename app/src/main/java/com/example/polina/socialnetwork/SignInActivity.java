package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.sign_in)
public class SignInActivity extends Activity {
    @ViewById(R.id.email_sign_in)
    public TextView em;
    @ViewById(R.id.password_sign_in)
    public TextView pw;
    @ViewById(R.id.sign_in_faild)
    public TextView signInFailed;
    @App
    SNApp snApp;

    private String email;
    private String password;
    SharedPreferences sharedPreferencesUserId;

    public void signIn(View v) {
        email = em.getText().toString();
        password = pw.getText().toString();
        logIn();
    }

    @Background
    void logIn() {
        JSONObject o = snApp.api.logIn(email, password, SignInActivity.this);
        checkData(o);
    }

    @org.androidannotations.annotations.UiThread
    void checkData(JSONObject o) {
        sharedPreferencesUserId = getSharedPreferences(Utils.USER_ID_PREFERENCES, MODE_PRIVATE);
        try {
            SharedPreferences.Editor ed = sharedPreferencesUserId.edit();
            ed.putString(Utils.USER_ID, o.getString(Utils.USER_ID));
            ed.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent;
        if (o == null) {
            signInFailed.setText(R.string.wrong_login_password);
        } else {
            if (!o.has(Utils.NAME)) {
                intent = new Intent(SignInActivity.this, FormActivity_.class);
            } else {
                intent = new Intent(SignInActivity.this, ProfileActivity_.class);
            }
            startActivity(intent);
        }
    }
}
