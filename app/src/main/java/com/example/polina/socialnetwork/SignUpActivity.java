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

@EActivity(R.layout.registration)
public class SignUpActivity extends Activity {

    @ViewById(R.id.email_sign_up)
    public TextView tvEmail;
    @ViewById(R.id.password_sign_up1)
    public TextView tvPass1;
    @ViewById(R.id.password_sign_up2)
    public TextView tvPass2;
    @ViewById(R.id.registration_feild)
    public TextView registrationFail;
    @App
    SNApp snApp;

    SharedPreferences sharedPreferences;
    private String email;
    private String password;
    private String passwordToConfirm;

    @Background
    void signUpData() {
        JSONObject o = snApp.api.signUp(email, password);
        check(o);
    }

    @org.androidannotations.annotations.UiThread
    void check(JSONObject o) {
        sharedPreferences =  getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
            try {
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putString(Utils.ID, o.getString(Utils.ID));
                ed.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (o == null) {
            registrationFail.setText(R.string.registration_faild);
            registrationFail.setVisibility(View.VISIBLE);
            return;
        } else {
            Intent intent = new Intent(this, FormActivity_.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public void signUp(View v) {
        email = tvEmail.getText().toString();
        password = tvPass1.getText().toString();
        passwordToConfirm = tvPass2.getText().toString();
        if (password.equals(passwordToConfirm)) {
            signUpData();
        }
    }
}
