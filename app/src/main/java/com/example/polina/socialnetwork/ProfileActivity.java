package com.example.polina.socialnetwork;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.InputStream;
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

    View dialogLayout;
    String profileURL;

    private static final int DIALOGIMAGE = 1;

    @Override
    protected void onResume() {
        super.onResume();
        loadProfInfo();
    }

    public void onShowImage(View v){
        showDialog(DIALOGIMAGE);
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

    public void saveImage(Bitmap bitmap){
        image.setImageBitmap(bitmap);
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o){
        System.err.println(o);
        try{
            name.setText(o.getString(ServerAPI.NAME));
            int y = calculateAmountYears(o.getString(ServerAPI.BIRTHDAY));
            String years = getResources().getQuantityString(R.plurals.years, y, y);
            birthday.setText(years);
            profileURL = o.getString(ServerAPI.PROF_URL);
            getBitmap(new URL (profileURL));
        } catch (Exception e){
            e.printStackTrace();
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


