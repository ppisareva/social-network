package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends Activity {
    public static final String PATH = "/user/register";
    public static final String HOST = "https://socialnetwork-core-rest.herokuapp.com";
   public static final String HOST_PATH = HOST + PATH;
    public static final String TOKEN = "token";
    public static final String MAIL = "email";
    public static final String PASSWORD = "password";


   private String email;
 private   String password;
   private String passwordToConfirm;
   private TextView e;
    private TextView pw1;
   private TextView pw2;
    private TextView registrationFail;


    public static JSONObject getJsonObject (String email, String password, String HOST_PATH, Context ctx) {


            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(HOST_PATH);
                List<NameValuePair> nameValuePairs = new ArrayList(2);
                nameValuePairs.add(new BasicNameValuePair(MAIL, email));
                nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse resp = client.execute(post);

                if (resp.getStatusLine().getStatusCode() < 400) {
                    List<Cookie> cookies = client.getCookieStore().getCookies();
                    CookieSyncManager.createInstance(ctx);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.removeAllCookie();
                    for (Cookie sessionCookie: cookies)
                        if (sessionCookie != null) {
                            String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
                            cookieManager.setCookie(sessionCookie.getDomain(), cookieString);
                        }
                    CookieSyncManager.getInstance().sync();
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
        if (password.equals(passwordToConfirm)) {

          new AsyncTask<Void, Void, JSONObject>() {


              @Override
              protected JSONObject doInBackground(Void... voids) {
                  return getJsonObject(email, password, HOST_PATH, SignUpActivity.this);

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


                } else{
                  registrationFail.setText(R.string.password_wrong);
                  registrationFail.setVisibility(View.VISIBLE);
                }
    }
}
