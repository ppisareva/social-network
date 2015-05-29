package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends Activity {
   private SharedPreferences sPref;
     private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            intent = new Intent(this, FormActivity.class);
            startActivity(intent);
    }
    public void logout (View v) {
        sPref = getSharedPreferences(SignUpActivity.TOKEN, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(SignUpActivity.TOKEN);
        editor.commit();
        intent = new Intent(this, IntroActivity.class);
        startActivity(intent);


        }
    }


