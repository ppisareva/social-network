package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.registration)
public class SignUpActivity extends Activity {
    private String email;
    private String password;
    private String passwordToConfirm;
    @ViewById(R.id.email_sign_up)
    public TextView e;
    @ViewById(R.id.password_sign_up1)
    public TextView pw1;
    @ViewById(R.id.password_sign_up2)
    public TextView pw2;
    @ViewById(R.id.registration_feild)
    public TextView registrationFail;
    @App
    SNApp snApp;

    @Background
    void signUpData() {
        JSONObject o = snApp.api.signUp(email, password, SignUpActivity.this);
        check(o);
    }

    @UiThread
    void check(JSONObject o) {
        if (o == null) {
            registrationFail.setText(R.string.registration_feild);
            registrationFail.setVisibility(View.VISIBLE);
            return;
        } else {
            Intent intent = new Intent(SignUpActivity.this, FormActivity_.class);
            startActivity(intent);
        }
    }

    public void signUp(View v) {
        email = e.getText().toString();
        password = pw1.getText().toString();
        passwordToConfirm = pw2.getText().toString();
        if (password.equals(passwordToConfirm)) {
            signUpData();
        }
    }
}
