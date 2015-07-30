package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.LruCache;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

@EActivity(R.layout.profile_activity)
public class ProfileActivity extends ActionBarActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.prof_bday)
    public TextView birthday;
    @ViewById(R.id.prof_image)
    public NetworkImageView image;
    @ViewById(R.id.prof_name)
    public TextView name;
    @ViewById(R.id.posts_list)
    public ListView postList;


    private String profileURL;
    private String connectionFailed;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferencesUserId;
    private ImageLoader mImageLoader;
    private String idUser;
    private String idPost;
    private PostAdapter adapter;

    private static final int NORMAL_LIST_SIZE = 9;
    private int totalPost = 10;
    private boolean loadingNow = true;

    @AfterViews
    protected void init(){
        postList.setAdapter(adapter);
        postList.setOnScrollListener(myScrollListener);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        sharedPreferencesUserId = getSharedPreferences(Utils.USER_ID_PREFERENCES, MODE_PRIVATE);
        idUser = sharedPreferencesUserId.getString(Utils.USER_ID, "");
        if (sharedPreferences.contains(Utils.NAME)) {
            loadProfileFromMemory(sharedPreferences);
        } else {
            loadProfile();
        }
        loadPost();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(40);

            @Override
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
        });
        adapter = new PostAdapter(this, new ArrayList<Post>(), mImageLoader);
        connectionFailed = getResources().getString(R.string.connection_faild);
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
                if (loadingNow) {
                    loadingNow = false;
                    loadPostList();
                }
            }
        }
    };

    @Background
    public void loadPostList() {
        System.out.println("------------------------------" + idUser + "    " + idPost);
        if (!idPost.isEmpty()) {
            JSONObject objectPosts = snApp.api.getLoadPosts(ProfileActivity.this, idUser, totalPost, idPost);
            System.out.println(objectPosts + "-------------------------------------------------");
            loadPostUIThread(objectPosts);
            return;
        }
    }

    @Background
    public void loadPost() {
        JSONObject objectPosts = snApp.api.getLoadPosts(ProfileActivity.this, idUser, totalPost, "");
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
                    posts.add(Post.parse(jsonPost));
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
            Toast.makeText(ProfileActivity.this, connectionFailed, Toast.LENGTH_LONG).show();
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
            image.setImageUrl(o.getString(Utils.PROF_URL), mImageLoader);
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
        loadingNow = true;
    }

    private void loadProfileFromMemory(SharedPreferences sharedPreferences) {
        name.setText(sharedPreferences.getString(Utils.NAME, ""));
        int y = Utils.calculateAmountYears(sharedPreferences.getString(Utils.BIRTHDAY, ""));
        String years = getResources().getQuantityString(R.plurals.years, y, y);
        birthday.setText(years);
        profileURL = sharedPreferences.getString(Utils.PROF_URL, "");
        image.setImageUrl(profileURL, mImageLoader);
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


