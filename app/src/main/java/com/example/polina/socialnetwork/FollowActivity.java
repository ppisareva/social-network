package com.example.polina.socialnetwork;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class FollowActivity extends AppCompatActivity {

    ArrayList<User> users = new ArrayList<>();
    ListView listFollow;
    UsersListAdapter adapter;
    SNApp snApp;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_follow);
        snApp = (SNApp) getApplication();
        listFollow = (ListView) findViewById(R.id.list_follow);
        adapter = new UsersListAdapter(users, this, snApp.mImageLoader);
        listFollow.setAdapter(adapter);
        listFollow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) adapterView.getItemAtPosition(i);
                System.err.println(" user ID" + user.userId);
                Intent intent = new Intent(FollowActivity.this, UserActivity.class);
                intent.putExtra(Utils.USER_ID, user.userId);
                startActivity(intent);
            }
        });

        userId =  getIntent().getStringExtra(Utils.USER_ID);
        System.err.println(" id User after check follow" + userId);
        boolean isFollower = getIntent().getBooleanExtra(Utils.FOLLOWER, false);
        new LoadFollow().execute(isFollower);

    }

    class LoadFollow extends AsyncTask <Boolean, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Boolean... booleans) {
            if(booleans[0]){
                return snApp.api.getFollowers(userId, 0);
            }
            return snApp.api.getFollowing(userId, 0);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {

                    JSONArray jsonArray = null;
                    jsonArray = jsonObject.getJSONArray(Utils.USERS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        users.add(User.parse(jsonPost));
                    }
                        adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(FollowActivity.this, getResources().getText(R.string.no_users), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_follow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
