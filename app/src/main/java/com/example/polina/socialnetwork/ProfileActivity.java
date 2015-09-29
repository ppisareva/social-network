package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
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
    @AfterViews
    protected void init(){


        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(toggle);

        ListView left_menu = (ListView) findViewById(R.id.lv_navigation_drawer);
        left_menu.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.left_menu)));
        left_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Intent intent;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case PROFILE:
                        Bundle bundle = new Bundle();
                        bundle.putString(Utils.USER_ID, snApp.getUserId());
                        profileFragment.setArguments(bundle);
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
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        ActionBar bar = getSupportActionBar();


        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(true);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
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
                    snApp.setUserIDHashSet(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.edit_profile) {
            Intent intent = new Intent(this, FormActivity_.class);
            startActivity(intent);
        }
        if (toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);

    }

   }


