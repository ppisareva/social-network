package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.profile_activity)
public class ProfileActivity extends ActionBarActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.posts_list)
    public ListView postList;
    @ViewById(R.id.refresh_layout)
    public SwipeRefreshLayout refreshLayout;
    public TextView birthday;
    public NetworkImageView image;
    public TextView name;
    ViewGroup header;


    private String profileURL;
    private String connectionFailed;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferencesUserId;
    private ImageLoader mImageLoader;
    private String idUser;
    private String idPost;
    private PostAdapter adapter;
    private Handler handler = new Handler();
    ArrayList<Post> posts = new ArrayList<>();

    private static final int NORMAL_LIST_SIZE = 9;
    private int totalPost = 10;
    private boolean loadingNow = true;
    int INTENT_DELETE = 0;



    @AfterViews
    protected void init(){
        postList.setAdapter(adapter);
        postList.addHeaderView(header, null, false);
        postList.setOnScrollListener(myScrollListener);
        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Post post = (Post) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity_.class);
                intent.putExtra(Utils.POST, post);
                intent.putExtra(Utils.POSITION, i);
                startActivityForResult(intent, INTENT_DELETE);
            }
        });


        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        sharedPreferencesUserId = getSharedPreferences(Utils.USER_ID_PREFERENCES, MODE_PRIVATE);
        idUser = sharedPreferencesUserId.getString(Utils.ID, "");
        System.err.println("user id " + idUser);
        if (sharedPreferences.contains(Utils.NAME)) {
            loadProfileFromMemory(sharedPreferences);
        } else {
            loadProfile();
        }
        loadPost();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        loadProfile();
                        loadPost();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_DELETE && RESULT_OK == resultCode) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int position = bundle.getInt(Utils.POSITION);
                posts.remove(--position);
                adapter.clear();
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.profile_layout, postList, false);
        birthday = (TextView) header.findViewById(R.id.prof_bday);
        image = (NetworkImageView) header.findViewById(R.id.prof_image);
        name = (TextView) header.findViewById(R.id.prof_name);

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
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount >1) {
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
            JSONObject objectPosts = snApp.api.getLoadPosts(idUser, totalPost, idPost);
            System.out.println(objectPosts + "-------------------------------------------------");
            loadPostUIThread(objectPosts);
            return;
        }
    }

    @Background
    public void loadPost() {
        JSONObject objectPosts = snApp.api.getLoadPosts(idUser, totalPost, "");
        System.out.println("posrs = "+objectPosts);
        loadPostUIThread(objectPosts);
    }

    @org.androidannotations.annotations.UiThread
    public void loadPostUIThread(JSONObject o) {
        if (o != null) {
            try {
                JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                posts = new ArrayList<>();
                JSONObject jsonPost;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonPost = jsonArray.getJSONObject(i);
                    posts.add(Post.parse(jsonPost));
                }
                idPost = (posts.size() > NORMAL_LIST_SIZE ? posts.get(NORMAL_LIST_SIZE).getPostId() : "");
               updateAdapter(posts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @UiThread
    public void updateAdapter(ArrayList<Post> posts) {
        adapter.addAll(posts);
        adapter.notifyDataSetChanged();
        loadingNow = true;
    }

    @Background
    public void loadProfile() {
        JSONObject o = snApp.api.getProfile();
        System.out.println(o);
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


