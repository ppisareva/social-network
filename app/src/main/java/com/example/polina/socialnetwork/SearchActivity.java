package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

public class SearchActivity extends Activity {

    SNApp snApp;
    ArrayList<User> users = new ArrayList<>();
    SearchAdapter adapter;
    EditText searchName;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
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
        adapter = new SearchAdapter(users, this, snApp.mImageLoader);
        searchList.setAdapter(adapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) adapterView.getItemAtPosition(i);
                System.err.println(" user ID" + user.userId);
                Intent intent = new Intent(SearchActivity.this, UserActivity.class);
                if(user.userId.equals(sharedPreferences.getString(Utils.ID, ""))){
                    intent.putExtra(Utils.USER_ID, Utils.MY_PROFILE);
                } else {
                    intent.putExtra(Utils.USER_ID, user.userId);
                }
                startActivity(intent);
            }
        });


    }


    class LoadUsers extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            if (!strings[0].isEmpty()) {
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

    class SearchAdapter extends BaseAdapter {

        ArrayList<User> users;
        Context context;
        LayoutInflater inflater;
        ImageLoader imageLoader;

        public SearchAdapter(ArrayList<User> users, Context context, ImageLoader imageLoader) {
            super();
            this.users = users;
            this.context = context;
            this.imageLoader = imageLoader;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int i) {
            return users.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.search_users_list, null);
            }
            User user = (User) getItem(i);
            String userName = user.userName;
            if (user.userId.equals(sharedPreferences.getString(Utils.ID, ""))) {
                userName += " " + getResources().getString(R.string.my_profile);
            }
            ((TextView) view.findViewById(R.id.search_list_name)).setText(userName);
            ((NetworkImageView) view.findViewById(R.id.search_list_image)).setImageUrl(user.userURL, imageLoader);
            return view;
        }
    }

}
