package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class SignInActivity extends Activity {
   private TextView em;
   private TextView pw;
   private TextView signInFaild;
   private String email;
   private String password;
   private String token;
    public static final String PATH = "/Login";
    public static final String HOST_PATH = SignUpActivity.HOST + PATH;
   private SharedPreferences sPref;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        em = (TextView) findViewById(R.id.email_sign_in);
        pw = (TextView) findViewById(R.id.password_sign_in);
        signInFaild = (TextView) findViewById(R.id.sign_in_faild);
    }

    public void signIn(View v) {
        email = em.getText().toString();
        password = pw.getText().toString();

        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... voids) {
                return SignUpActivity.getJsonObject(email, password, HOST_PATH);
            }
            @Override
            protected void onPostExecute(JSONObject o) {
                if (o == null){
                   signInFaild.setText("Wrong login or password");
                } else {

                    try {
                        token = o.getString(SignUpActivity.TOKEN);
                        sPref = getSharedPreferences(SignUpActivity.TOKEN, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putString(SignUpActivity.TOKEN, token);
                        editor.commit();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }

            }
        }.execute();


    }
}
