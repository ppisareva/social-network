package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

@EActivity(R.layout.profile_activity)
public class ProfileActivity extends AppCompatActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.posts_list)
    public ListView postList;
    @ViewById(R.id.refresh_layout)
    public SwipeRefreshLayout refreshLayout;
    public TextView birthday;
    public NetworkImageView image;
    public TextView name;

    private SharedPreferences sharedPreferences;
    private ActionBarDrawerToggle toggle;
    ProfileFragment profileFragment;
    private final int LOG_OUT = 1;
    private final int PROFILE = 0;
    private final int SEARCH = 2;
    private final int EDIT_PROFILE = 3;
    private final int FEED = 4;


    Drawer result;

    @AfterViews
    protected void init(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        result = new DrawerBuilder()
                .withActivity(this)
                .withHasStableIds(true)
                .withToolbar(toolbar)
                .addDrawerItems(new PrimaryDrawerItem().withName(getResources().getString(R.string.profile)).withIcon(GoogleMaterial.Icon.gmd_account),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.log_out)).withIcon(GoogleMaterial.Icon.gmd_arrow_back),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.search)).withIcon(GoogleMaterial.Icon.gmd_search),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.edit_profile)).withIcon(GoogleMaterial.Icon.gmd_edit),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.feed)).withIcon(GoogleMaterial.Icon.gmd_collection_image)
                        )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent intent;
                        switch (position) {
                    case PROFILE:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_profile, profileFragment).commit();
                        break;
                    case LOG_OUT:
                        CookieSyncManager.createInstance(ProfileActivity.this);
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeAllCookie();
                        sharedPreferences.edit().clear().commit();
                        intent = new Intent(ProfileActivity.this, IntroActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        break;
                    case SEARCH:
                        intent = new Intent(ProfileActivity.this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    case EDIT_PROFILE:
                        intent = new Intent(ProfileActivity.this, FormActivity_.class);
                        startActivity(intent);
                        break;
                    case FEED:
                        FeedFragment feedFragment = new FeedFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_profile, feedFragment).commit();
                        break;
                }

                        return false;
                    }
                })
                .build();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        setTitle(sharedPreferences.getString(Utils.NAME, "OLOLO"));
        profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Utils.USER_ID, snApp.getUserId());
        profileFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_profile, profileFragment).commit();
        new LoadFollowing().execute();
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


    class LoadFollowing extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... voids) {
            return snApp.api.getFollowing(snApp.getUserId(), 0);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    HashSet<String> users = new HashSet<>();
                    JSONArray jsonArray = jsonObject.getJSONArray(Utils.USERS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        User user = User.parse(jsonArray.getJSONObject(i));
                        users.add(user.userId);
                    }
                    snApp.setFollowerIds(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
   }


