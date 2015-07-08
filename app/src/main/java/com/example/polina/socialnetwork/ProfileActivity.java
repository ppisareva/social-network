package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@EActivity(R.layout.activity_main)
public class ProfileActivity extends ActionBarActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.prof_bday)
    public TextView birthday;
    @ViewById(R.id.prof_image)
    public ImageView image;
    @ViewById(R.id.prof_name)
    public TextView name;
    String profileURL;
    String connection_faild;
    SharedPreferences sharedPreferences;
    public static final String PROFILE_PREFERENCES = "profile info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection_faild = getResources().getString(R.string.connection_faild);
        sharedPreferences = getSharedPreferences(PROFILE_PREFERENCES, MODE_PRIVATE);
        loadProfInfo();
    }

    @Background
    public void loadProfInfo() {
        JSONObject o = snApp.api.getProfile(ProfileActivity.this);

            addProfileInfo(o);
    }
    @Background
    public void getBitmap(URL url){
        try {
            InputStream in = url.openStream();
           Bitmap bitmap = BitmapFactory.decodeStream(in);
        saveImage(bitmap);
    } catch (Exception e) {
       e.printStackTrace();
        }
    }
@UiThread
    public void saveImage(Bitmap bitmap){
        image.setImageBitmap(bitmap);
    }

    private void loadProfileFromMemory(SharedPreferences sharedPreferences){
        name.setText(sharedPreferences.getString(ServerAPI.NAME, ""));
        int y = calculateAmountYears(sharedPreferences.getString(ServerAPI.BIRTHDAY, ""));
        String years = getResources().getQuantityString(R.plurals.years, y, y);
        birthday.setText(years);
        profileURL = sharedPreferences.getString(ServerAPI.PROF_URL, "");
        try {
            getBitmap(new URL(profileURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o){


        if(o!=null) {
            try {
                if(sharedPreferences.contains(ServerAPI.NAME)){
                    loadProfileFromMemory(sharedPreferences);
                } else {
                    name.setText(o.getString(ServerAPI.NAME));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ServerAPI.NAME, o.getString(ServerAPI.NAME));
                    int y = calculateAmountYears(o.getString(ServerAPI.BIRTHDAY));
                    String years = getResources().getQuantityString(R.plurals.years, y, y);
                    birthday.setText(years);
                    editor.putString(ServerAPI.BIRTHDAY, o.getString(ServerAPI.BIRTHDAY));
                    profileURL = o.getString(ServerAPI.PROF_URL);
                    getBitmap(new URL(profileURL));
                    editor.putString(ServerAPI.PROF_URL,o.getString(ServerAPI.PROF_URL));
                    editor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if(sharedPreferences.contains(ServerAPI.NAME)){
                loadProfileFromMemory(sharedPreferences);
            }
            Toast.makeText(ProfileActivity.this, connection_faild, Toast.LENGTH_LONG).show();
        }
    }

    public void onPost(View v){
        Intent intent = new Intent(this, CreatePostActivity_.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.log_out:
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                intent = new Intent(this, IntroActivity.class);
                startActivity(intent);
                break;
            case R.id.edit_profile:
                sharedPreferences.edit().clear().commit();
                intent = new Intent(this, FormActivity_.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private int calculateAmountYears(String birthday){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date bDate = format.parse(birthday);
            int bYear = bDate.getYear();
            int bMonth = bDate.getMonth();
            int bDay = bDate.getDay();

            Date nowDate = now.getTime();
            int nowYear = nowDate.getYear();
            int nowMonth = nowDate.getMonth();
            int nowDay = nowDate.getDay();

            int year = nowYear-bYear-1;

            if(bMonth<nowMonth){
                year++;
            }
            if(bMonth==nowMonth){
                if(bDay<nowDay){
                    year++;
                }
            }

            return year;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

    }

}


