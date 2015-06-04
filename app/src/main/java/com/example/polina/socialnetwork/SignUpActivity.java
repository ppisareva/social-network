package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;


public class SignUpActivity extends Activity {
    private String email;
    private String password;
    private String passwordToConfirm;
    private TextView e;
    private TextView pw1;
    private TextView pw2;
    private TextView registrationFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_registration);
        e = (TextView) findViewById(R.id.email_sign_up);
        pw1 = (TextView) findViewById(R.id.password_sign_up1);
        pw2 = (TextView) findViewById(R.id.password_sign_up2);
        registrationFail = (TextView) findViewById(R.id.registration_feild);
    }

    public void signUp(View v) {
        email = e.getText().toString();
        password = pw1.getText().toString();
        passwordToConfirm = pw2.getText().toString();
        if (password.equals(passwordToConfirm)) {

            new AsyncTask<Void, Void, JSONObject>() {

                @Override
                protected JSONObject doInBackground(Void... voids) {
                    return ((SNApp) getApplication()).api.signUp(email, password, SignUpActivity.this);
                }

                @Override
                protected void onPostExecute(JSONObject o) {
                    if (o == null) {
                        registrationFail.setText(R.string.registration_feild);
                        registrationFail.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }.execute();

        } else {
            registrationFail.setText(R.string.password_wrong);
            registrationFail.setVisibility(View.VISIBLE);
        }
    }
}
