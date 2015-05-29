package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class SignUpActivity extends Activity {
    public static final String PATH = "/Register";
    public static final String HOST = "https://slabo-staging.appspot.com";
   public static final String HOST_PATH = HOST + PATH;
    public static final String TOKEN = "token";
    public static final String MAIL = "mail";
    public static final String PASSWORD = "password";

    private SharedPreferences sheredPref;
   private String email;
 private   String password;
   private String passwordToConfirm;
   private TextView e;
    private TextView pw1;
   private TextView pw2;
    private TextView registrationFail;
   private String token;

    public static JSONObject getJsonObject (String email, String password, String HOST_PATH) {
        try {
            JSONObject o = new JSONObject();
            o.put(MAIL, email);
            o.put(PASSWORD, password);
            String data = o.toString();
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(HOST_PATH);
            post.setEntity(new StringEntity(data));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                System.err.println("ERROR: " + resp.getStatusLine());
                return null;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_registration);
        e = (TextView) findViewById(R.id.email_sign_up);
        pw1 = (TextView) findViewById(R.id.password_sign_up1);
        pw2= (TextView) findViewById(R.id.password_sign_up2);
        registrationFail = (TextView) findViewById(R.id.registration_feild);



    }

    public void signUp (View v) {
        email = e.getText().toString();
        password = pw1.getText().toString();
        passwordToConfirm = pw2.getText().toString();
        System.out.println(HOST_PATH);
        if (password.equals(passwordToConfirm)) {

          new AsyncTask<Void, Void, JSONObject>() {


              @Override
              protected JSONObject doInBackground(Void... voids) {

                  return getJsonObject(email, password, HOST_PATH);

              }

              @Override
              protected void onPostExecute(JSONObject o) {
                  if (o == null) {
                      registrationFail.setText(R.string.registration_feild);
                      registrationFail.setVisibility(View.VISIBLE);
                      return;
                  } else {

                      try {

                          token = o.getString(TOKEN);
                          sheredPref = getSharedPreferences(TOKEN, MODE_PRIVATE);
                          SharedPreferences.Editor editor = sheredPref.edit();
                          editor.putString(TOKEN, token);
                          editor.commit();
                          Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                          startActivity(intent);


                      } catch (JSONException e1) {
                          e1.printStackTrace();
                      }
                  }
              }
          }.execute();


                } else{

                  registrationFail.setText(R.string.password_wrong);
                  registrationFail.setVisibility(View.VISIBLE);


                }


    }
}
