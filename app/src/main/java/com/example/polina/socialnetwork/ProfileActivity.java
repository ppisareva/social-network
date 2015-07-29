package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;

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


    private String profileURL;
    private String connectionFaild;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferencesUserId;
    private ImageLoader mImageLoader;
    private String idUser;
    private String idPost;
    private PostAdapter adapter;

    private static final int NORMAL_LIST_SIZE = 9;
    private int totalPost = 10;
    private ArrayList<Post> postsToLoad;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postsToLoad = new ArrayList<>();
        ImageLoader.ImageCache imageCache = new BitmapLruCache();
        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), imageCache);
        connectionFaild = getResources().getString(R.string.connection_faild);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        sharedPreferencesUserId = getSharedPreferences(Utils.USER_ID_PREFERENCES, MODE_PRIVATE);
        idUser = sharedPreferencesUserId.getString(Utils.USER_ID, "");
        adapter = new PostAdapter(this, new ArrayList<Post>(), mImageLoader);
        postList.setAdapter(adapter);
        postList.setOnScrollListener(myScrollListener);
        if (sharedPreferences.contains(Utils.NAME)) {
            loadProfileFromMemory(sharedPreferences);
        } else {
            loadProfile();
        }
        loadPost();
    }

    AbsListView.OnScrollListener myScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            System.err.println("id" + firstVisibleItem + "itams" + visibleItemCount + "totalItemCount" + totalItemCount);
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                if (count == 0) {
                    count++;
                    loadPostList();
                }
            }
        }
    };

    @Background
    public void loadPostList() {
        System.out.println("------------------------------" + idUser + "    " + idPost);
        if (!idPost.isEmpty()) {
            JSONObject objectPosts = snApp.api.getLoadPosts(ProfileActivity.this, idUser, String.valueOf(totalPost), idPost);
            System.out.println(objectPosts + "-------------------------------------------------");
            loadPostUIThread(objectPosts);
            return;
        }
    }

    @Background
    public void loadPost() {
        JSONObject objectPosts = snApp.api.getPosts(ProfileActivity.this, idUser);
        loadPostUIThread(objectPosts);
    }

    @org.androidannotations.annotations.UiThread
    public void loadPostUIThread(JSONObject o) {
        if (o != null) {
            try {
                JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                ArrayList<Post> posts = new ArrayList<>();
                JSONObject jsonPost;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonPost = jsonArray.getJSONObject(i);
                    posts.add(Post.parse(jsonPost, sharedPreferences.getString(Utils.PROF_URL, "")));
                }
                idPost = (posts.size() > NORMAL_LIST_SIZE ? posts.get(NORMAL_LIST_SIZE).getIdPost() : "");
                updateAdapter(posts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Background
    public void loadProfile() {
        JSONObject o = snApp.api.getProfile(ProfileActivity.this);
        if (o != null) {
            addProfileInfo(o);
        } else {
            Toast.makeText(ProfileActivity.this, connectionFaild, Toast.LENGTH_LONG).show();
        }
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o) {
        try {
            name.setText(o.getString(Utils.NAME));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Utils.NAME, o.getString(Utils.NAME));
            int y = Utils.calculateAmountYears(o.getString(Utils.BIRTHDAY));
            String years = getResources().getQuantityString(R.plurals.years, y, y);
            birthday.setText(years);
            editor.putString(Utils.BIRTHDAY, o.getString(Utils.BIRTHDAY));
            System.err.println(o.getString(Utils.PROF_URL));
            mImageLoader.get(o.getString(Utils.PROF_URL), ImageLoader.getImageListener(image, 0, 0));
            editor.putString(Utils.PROF_URL, o.getString(Utils.PROF_URL));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void updateAdapter(ArrayList<Post> posts) {
        adapter.addAll(posts);
        adapter.notifyDataSetChanged();
        postsToLoad.addAll(posts);
        count = 0;
    }

    private void loadProfileFromMemory(SharedPreferences sharedPreferences) {
        name.setText(sharedPreferences.getString(Utils.NAME, ""));
        int y = Utils.calculateAmountYears(sharedPreferences.getString(Utils.BIRTHDAY, ""));
        String years = getResources().getQuantityString(R.plurals.years, y, y);
        birthday.setText(years);
        profileURL = sharedPreferences.getString(Utils.PROF_URL, "");
        mImageLoader.get(profileURL, ImageLoader.getImageListener(image, R.drawable.load, R.drawable.error));
        mImageLoader.get(profileURL, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("------------------------- " + error);
            }
        });
        System.out.println(profileURL + " --------------------------");
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
}


