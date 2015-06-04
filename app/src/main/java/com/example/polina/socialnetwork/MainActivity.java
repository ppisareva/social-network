package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;


public class MainActivity extends Activity {
     private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                   try {
                      JSONObject o = ((SNApp) getApplication()).api.getResponseMe(MainActivity.this);
                       if (o != null && !o.has(FormActivity.NAME)) {
                               intent = new Intent(MainActivity.this, FormActivity.class);
                               startActivity(intent);
                       }
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
    }
    public void logout (View v) {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        intent = new Intent(this, IntroActivity.class);
        startActivity(intent);


        }
    }


