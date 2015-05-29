package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;


public class IntroActivity extends Activity {
    private Intent intent;
    private   SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        sPref = getSharedPreferences(SignUpActivity.TOKEN, MODE_PRIVATE);

        if(sPref.contains(SignUpActivity.TOKEN)){
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
