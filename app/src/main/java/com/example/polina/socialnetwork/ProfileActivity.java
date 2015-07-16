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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@EActivity(R.layout.profile_activity)
public class ProfileActivity extends ActionBarActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.prof_bday)
    public TextView birthday;
    @ViewById(R.id.prof_image)
    public ImageView image;
    @ViewById(R.id.prof_name)
    public TextView name;
    @ViewById(R.id.posts_list)
    public ListView postList;


    String profileURL;
    String connection_faild;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferencesUserId;
    public static final String PROFILE_PREFERENCES = "profile info";
    public static final String USER_ID_PREFERENCES = "User ID";
    public static final String USER_ID = "_id";
    private static final String POSTS_JSON = "posts";

    String iduser;


    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        connection_faild = getResources().getString(R.string.connection_faild);
        sharedPreferences = getSharedPreferences(PROFILE_PREFERENCES, MODE_PRIVATE);
        sharedPreferencesUserId = getSharedPreferences(USER_ID_PREFERENCES, MODE_PRIVATE);
        iduser = sharedPreferencesUserId.getString(USER_ID, "");
        adapter = new PostAdapter(this, new ArrayList<Post>());
        if (sharedPreferences.contains(ServerAPI.NAME)) {
            loadProfileFromMemory(sharedPreferences);
        } else {
            loadProfile();
        }
        loadPost();
    }


    @Background
    public void loadPost() {
        JSONObject objectPosts = snApp.api.getPosts(ProfileActivity.this, iduser);
        loadPostUIThread(objectPosts);


    }

    @org.androidannotations.annotations.UiThread
    public void loadPostUIThread(JSONObject o) {
        if (o != null) {
            try {
                JSONArray jsonArray = o.getJSONArray(POSTS_JSON);
                ArrayList<Post> posts = new ArrayList<>();
                JSONObject jsonPost;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonPost = jsonArray.getJSONObject(i);
                    posts.add(Post.parse(jsonPost, sharedPreferences.getString(ServerAPI.PROF_URL, "")));
                }
                updateAdapter(posts);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ProfileActivity.this, connection_faild, Toast.LENGTH_LONG).show();
        }
    }


    @Background
    public void loadProfile() {
        JSONObject o = snApp.api.getProfile(ProfileActivity.this);
        if (o != null) {
            addProfileInfo(o);
        } else {
            Toast.makeText(ProfileActivity.this, connection_faild, Toast.LENGTH_LONG).show();
        }
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o) {
        try {
            name.setText(o.getString(ServerAPI.NAME));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ServerAPI.NAME, o.getString(ServerAPI.NAME));
            int y = calculateAmountYears(o.getString(ServerAPI.BIRTHDAY));
            String years = getResources().getQuantityString(R.plurals.years, y, y);
            birthday.setText(years);
            editor.putString(ServerAPI.BIRTHDAY, o.getString(ServerAPI.BIRTHDAY));
            profileURL = o.getString(ServerAPI.PROF_URL);
            getBitmap(new URL(profileURL));
            editor.putString(ServerAPI.PROF_URL, o.getString(ServerAPI.PROF_URL));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void updateAdapter(ArrayList<Post> posts) {
        postList.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(posts);
        adapter.notifyDataSetChanged();
    }


    @Background
    public void getBitmap(URL url) {
        try {
            InputStream in = url.openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            saveImage(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void saveImage(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }

    private void loadProfileFromMemory(SharedPreferences sharedPreferences) {
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


    public void onPost(View v) {
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
                sharedPreferences.edit().clear().commit();
                sharedPreferencesUserId.edit().clear().commit();
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

    public static int calculateAmountYears(String birthday) {
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

            int year = nowYear - bYear - 1;

            if (bMonth < nowMonth) {
                year++;
            }
            if (bMonth == nowMonth) {
                if (bDay < nowDay) {
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


