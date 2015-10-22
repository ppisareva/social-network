package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by polina on 22.09.15.
 */

public class SearchActivity extends AppCompatActivity {

    SNApp snApp;
    ArrayList<User> users = new ArrayList<>();
    UsersListAdapter adapter;
    EditText searchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.search_activity);
        snApp = (SNApp) getApplication();
        searchName = (EditText) findViewById(R.id.search_name);
        searchName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                System.err.println(" press onclick");
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    new LoadUsers().execute(searchName.getText().toString());
                    return true;
                }
                return false;
            }
        });

        ListView searchList = (ListView) findViewById(R.id.search_result);
        adapter = new UsersListAdapter(users, this, snApp.mImageLoader);
        searchList.setAdapter(adapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) adapterView.getItemAtPosition(i);
                System.err.println(" user ID" + user.userId);
                Intent intent = new Intent(SearchActivity.this, UserActivity.class);
                intent.putExtra(Utils.USER_ID, user.userId);
                startActivity(intent);
            }
        });


    }


    class LoadUsers extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            if (!TextUtils.isEmpty(strings[0])) {
                return snApp.api.findUsers(strings[0], 0);
            }
            System.err.println(" don't state user name");
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            System.out.println(jsonObject);
            if (jsonObject != null) {
                try {
                    users.clear();
                    JSONArray jsonArray = jsonObject.getJSONArray(Utils.USERS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        users.add(User.parse(jsonPost));
                    }
                    if (!users.isEmpty()) {
                        System.err.println("users + " + users.toString());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SearchActivity.this, getResources().getText(R.string.no_users), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
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
