package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


public class MainActivity extends Activity {
   private SharedPreferences sPref;
     private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//            intent = new Intent(this, FormActivity.class);
//            startActivity(intent);
    }
    public void logout (View v) {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();


        intent = new Intent(this, IntroActivity.class);
        startActivity(intent);


        }
    }


