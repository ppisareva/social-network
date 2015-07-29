package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
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
            Intent intent = new Intent(this, ProfileActivity_.class);
            startActivity(intent);
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.sign_up:
               intent = new Intent(this, SignUpActivity_.class);
                break;
            case R.id.sing_in:
                intent = new Intent(this, SignInActivity_.class);
                break;
        }
        startActivity(intent);
    }

}
