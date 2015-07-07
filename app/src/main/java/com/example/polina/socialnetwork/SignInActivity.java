package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.sign_in)
public class SignInActivity extends Activity {
    @ViewById(R.id.email_sign_in)
    public TextView em;
    @ViewById(R.id.password_sign_in)
    public TextView pw;
    @ViewById(R.id.sign_in_faild)
    public TextView signInFaild;
    private String email;
    private String password;

    @App
    SNApp snApp;


    public void signIn(View v) {
        email = em.getText().toString();
        password = pw.getText().toString();
        logIn();
    }

    @Background
    void logIn(){
        JSONObject o = snApp.api.logIn(email, password, SignInActivity.this);
        checkData(o);
    }

   @ org.androidannotations.annotations.UiThread
    void checkData(JSONObject o){
        Intent intent;
        if (o == null) {
            signInFaild.setText("Wrong login or password");
        } else {
            if (!o.has(FormActivity.NAME)) {
                 intent = new Intent(SignInActivity.this, FormActivity_.class);
            } else {
                 intent = new Intent(SignInActivity.this, ProfileActivity_.class);
            }
            startActivity(intent);
        }

    }
}
