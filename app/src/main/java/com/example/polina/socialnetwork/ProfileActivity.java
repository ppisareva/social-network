package com.example.polina.socialnetwork;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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

    private SharedPreferences sharedPreferences;
      private ActionBarDrawerToggle toggle;
    ProfileFragment profileFragment;
    FragmentTransaction ft;
    private final int LOG_OUT = 1;
    private final int PROFILE = 0;
    @AfterViews
    protected void init(){

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case PROFILE:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_profile, profileFragment).commit();
                        break;
                    case LOG_OUT:
                        CookieSyncManager.createInstance(ProfileActivity.this);
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeAllCookie();
                        sharedPreferences.edit().clear().commit();
                       Intent intent = new Intent(ProfileActivity.this, IntroActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);;
                        break;
                    case 2:
                        System.out.println("Search");
                        break;
                }
            }
        });
        ActionBar bar = getSupportActionBar();


        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       profileFragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_profile, profileFragment).commit();
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


