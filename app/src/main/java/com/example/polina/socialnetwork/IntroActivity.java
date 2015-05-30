package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


public class IntroActivity extends Activity {
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        CookieSyncManager.createInstance(this);
        if(CookieManager.getInstance().hasCookies()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.sign_up:
               intent = new Intent(this, SignUpActivity.class);

                break;
            case R.id.sing_in:
                intent = new Intent(this, SignInActivity.class);
                break;
        }
        startActivity(intent);
    }

}
