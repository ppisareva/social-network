package com.example.polina.socialnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by polina on 15.09.15.
 */

public class ProfileFragment extends Fragment implements View.OnClickListener {

    SNApp snApp;
    public ListView postList;
    public SwipeRefreshLayout refreshLayout;
    public TextView birthday;
    public NetworkImageView image;
    public TextView name;
    FloatingActionButton postSend;
    TextView following;
    TextView followingCount;
    TextView followers;
    TextView followersCount;

    ViewGroup header;
    Context thisContext;
    CheckBox checkBoxFollow;


    private String profileURL;
    private String connectionFailed;
    private SharedPreferences sharedPreferences;
    private String userId;
    private String postId;
    private PostAdapter adapter;
    final ArrayList<Post> posts = new ArrayList<>();
    boolean myProfile = false;

    private boolean loadingNow = true;
    int INTENT_DELETE = 0;
    int followerAmount = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, null);
        sharedPreferences = this.getActivity().getSharedPreferences(Utils.PROFILE_PREFERENCES, thisContext.MODE_PRIVATE);
        snApp = (SNApp) getActivity().getApplication();
        Bundle bundle = this.getArguments();
        userId = bundle.getString(Utils.USER_ID);
        if (userId.equals(snApp.getUserId())) {
            myProfile = true;
        }
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        thisContext = container.getContext();

        header = (ViewGroup) inflater.inflate(R.layout.profile_header, postList, false);
        followers = (TextView) header.findViewById(R.id.button_followers);
        followers.setOnClickListener(this);
        followersCount = (TextView) header.findViewById(R.id.follower_count);
        following = (TextView) header.findViewById(R.id.button_followings);
        following.setOnClickListener(this);
        followingCount = (TextView) header.findViewById(R.id.following_count);
        checkBoxFollow = (CheckBox) header.findViewById(R.id.checkBoxFollow);
        checkBoxFollow.setOnCheckedChangeListener(myChangeListener);
        postSend = (FloatingActionButton) v.findViewById(R.id.button_post_activity);
        birthday = (TextView) header.findViewById(R.id.prof_bday);
        image = (NetworkImageView) header.findViewById(R.id.prof_image);
        name = (TextView) header.findViewById(R.id.prof_name);

        adapter = new PostAdapter(thisContext, posts, snApp.mImageLoader);
        connectionFailed = getResources().getString(R.string.connection_faild);
        postList = (ListView) v.findViewById(R.id.posts_list);
        postList.setAdapter(adapter);
        postList.addHeaderView(header, null, false);
        postList.setOnScrollListener(myScrollListener);
        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Post post = (Post) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(thisContext, PostDetailsActivity_.class);
                intent.putExtra(Utils.POST, post);
                intent.putExtra(Utils.POSITION, i);
                startActivityForResult(intent, INTENT_DELETE);
            }
        });

        postSend.attachToListView(postList);


        System.err.println("user id " + userId);
        if (sharedPreferences.contains(Utils.NAME) && myProfile) {
            loadProfileFromMemory(sharedPreferences);
        }
        new LoadPost().execute("");

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                adapter.clear();
                new LoadPost().execute("");
                refreshLayout.setRefreshing(false);
            }
        });

        if(!myProfile){
            HashSet<String> users = snApp.getFollowerIds();
            if(users.contains(userId)){
                checkBoxFollow.setOnCheckedChangeListener(null);
                checkBoxFollow.setChecked(true);
                checkBoxFollow.setText(R.string.unfollow);
                checkBoxFollow.setOnCheckedChangeListener(myChangeListener);
            }
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==INTENT_DELETE){
            int position = data.getIntExtra(Utils.POSITION, 0);
            switch (resultCode){
                case Utils.RESULT:
                    new LoadPost().execute("");
                    break;
                case Activity.RESULT_CANCELED:
                    if(data!=null) {
                        Post post = (Post) data.getSerializableExtra(Utils.POST);
                        posts.set(position-1, post);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }

        }

    }





    CompoundButton.OnCheckedChangeListener myChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                followersCount.setText(String.valueOf(++followerAmount));
                checkBoxFollow.setText(getResources().getString(R.string.unfollow));
            } else {
                followersCount.setText(String.valueOf(--followerAmount));
                checkBoxFollow.setText(getResources().getString(R.string.follow));
            }

            final String url = ServerAPI.HOST + "user/" + userId + "/follow";
            System.err.println(url);
            RequestQueue queue = Volley.newRequestQueue(thisContext);
            queue.add(new StringRequest((isChecked ? Request.Method.POST : Request.Method.DELETE), url, LISTENER, ERROR_LISTENER) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    CookieSyncManager.createInstance(thisContext);
                    CookieManager cookieManager = CookieManager.getInstance();
                    headers.put("Cookie", cookieManager.getCookie(url));
                    return headers;
                }
            });
        }
    };

    private final static Response.Listener<String> LISTENER = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
        }
    };

    private final static Response.ErrorListener ERROR_LISTENER = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            System.err.println("VOLLY ERROR: " + error);
        }
    };

    AbsListView.OnScrollListener myScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            System.err.println("id" + firstVisibleItem + "items" + visibleItemCount + "totalItemCount" + totalItemCount);
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 1) {
                if (loadingNow) {
                    loadingNow = false;
                    new LoadPost().execute(postId);
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(thisContext, FollowActivity.class);
        intent.putExtra(Utils.USER_ID, userId);

        switch (view.getId()){
            case R.id.button_followers:
               intent.putExtra(Utils.FOLLOWER, true);
                break;
            case R.id.button_followings:
                intent.putExtra(Utils.FOLLOWER, false);
                break;
        }
        startActivity(intent);
    }

    class LoadPost extends AsyncTask<String, Void, JSONObject> {

        private boolean refresh;
        @Override
        protected JSONObject doInBackground(String... strings) {
            refresh = TextUtils.isEmpty(strings[0]);
            return snApp.api.getLoadPosts(userId,  strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject o) {
            if (o != null) {
                try {
                    if (refresh) posts.clear();
                    JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        posts.add(Post.parse(jsonPost));
                    }
                    postId = (posts.size() > 0 ? posts.get(posts.size() - 1).getPostId() : "");
                    updateAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void updateAdapter() {
        adapter.notifyDataSetChanged();
        loadingNow = true;
    }

    class LoadProfile extends AsyncTask<Void, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(Void... strings) {
            return snApp.api.getUser(userId);
        }

        @Override
        protected void onPostExecute(JSONObject o) {
            if (o != null) {
                try {
                    if (myProfile) {
                        postSend.setVisibility(View.VISIBLE);
                    } else {
                        checkBoxFollow.setVisibility(View.VISIBLE);
                    }
                    name.setText(o.getString(Utils.NAME));
                    followerAmount = Integer.parseInt(o.getString(Utils.FOLLOWERS_COUNT));
                    followersCount.setText(o.getString(Utils.FOLLOWERS_COUNT));
                    followingCount.setText(o.getString(Utils.FOLLOWING_COUNT));
                    int y = Utils.calculateAmountYears(o.getString(Utils.BIRTHDAY));
                    String years = getResources().getQuantityString(R.plurals.years, y, y);
                    birthday.setText(years);
                    System.err.println(o.getString(Utils.PROF_URL));
                    image.setImageUrl(o.getString(Utils.PROF_URL), snApp.mImageLoader);

                    if (myProfile) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Utils.FOLLOWERS_COUNT, o.getString(Utils.FOLLOWERS_COUNT));
                        editor.putString(Utils.FOLLOWING_COUNT, o.getString(Utils.FOLLOWING_COUNT));
                        editor.putString(Utils.NAME, o.getString(Utils.NAME));
                        editor.putString(Utils.BIRTHDAY, o.getString(Utils.BIRTHDAY));
                        editor.putString(Utils.PROF_URL, o.getString(Utils.PROF_URL));
                        editor.commit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(thisContext, connectionFailed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadProfileFromMemory(SharedPreferences sharedPreferences) {
        name.setText(sharedPreferences.getString(Utils.NAME, ""));
        postSend.setVisibility(View.VISIBLE);
        int y = Utils.calculateAmountYears(sharedPreferences.getString(Utils.BIRTHDAY, ""));
        birthday.setText(getResources().getQuantityString(R.plurals.years, y, y));
        profileURL = sharedPreferences.getString(Utils.PROF_URL, "");
        image.setImageUrl(profileURL, snApp.mImageLoader);
        System.out.println(profileURL + " --------------------------");
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadProfile().execute();
    }

}
