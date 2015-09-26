package com.example.polina.socialnetwork;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by polina on 15.09.15.
 */

public class ProfileFragment extends Fragment{

    SNApp snApp;
    public ListView postList;
    public SwipeRefreshLayout refreshLayout;
    public TextView birthday;
    public NetworkImageView image;
    public TextView name;
    Button postSend;
    Button following;
    Button followers;
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

    private int totalPost = 10;
    private boolean loadingNow = true;
    int INTENT_DELETE = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, null);
        sharedPreferences = this.getActivity().getSharedPreferences(Utils.PROFILE_PREFERENCES, thisContext.MODE_PRIVATE);
        Bundle bundle = this.getArguments();
        userId = bundle.getString(Utils.USER_ID);
        if(userId.equals(sharedPreferences.getString(Utils.ID, ""))){
            myProfile = true;
            userId = sharedPreferences.getString(Utils.ID, "");
        }
        refreshLayout= (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        thisContext = container.getContext();

        header = (ViewGroup) inflater.inflate(R.layout.profile_layout, postList, false);
        followers = (Button) header.findViewById(R.id.button_followers);
        following = (Button) header.findViewById(R.id.button_followings);
        checkBoxFollow = (CheckBox) header.findViewById(R.id.checkBoxFollow);
        postSend= (Button) header.findViewById(R.id.button_post_activity);
        birthday = (TextView) header.findViewById(R.id.prof_bday);
        image = (NetworkImageView) header.findViewById(R.id.prof_image);
        name = (TextView) header.findViewById(R.id.prof_name);

        snApp= (SNApp) getActivity().getApplication();

        adapter = new PostAdapter(thisContext, posts, snApp.mImageLoader);
        connectionFailed = getResources().getString(R.string.connection_faild);
        postList =(ListView) v.findViewById(R.id.posts_list);
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




        System.err.println("user id " + userId);
        if (sharedPreferences.contains(Utils.NAME) && myProfile) {
            loadProfileFromMemory(sharedPreferences);
        } else {
            LoadProfile loadProfile = new LoadProfile();
            loadProfile.execute();
        }
        LoadPost loadPost = new LoadPost();
        loadPost.execute();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                adapter.clear();
                LoadPost lp = new LoadPost();
                lp.execute();
                refreshLayout.setRefreshing(false);
            }
        });

        return v;
    }







    AbsListView.OnScrollListener myScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            System.err.println("id" + firstVisibleItem + "items" + visibleItemCount + "totalItemCount" + totalItemCount);
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount >1) {
                if (loadingNow) {
                    loadingNow = false;

                   LoadPostList loadPostList = new LoadPostList();
                    loadPostList.execute();
                }
            }
        }
    };




    class LoadPostList extends AsyncTask<Void, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(Void... voids) {
            System.out.println("------------------------------" + userId + "    " + postId);
            if (!postId.isEmpty()) {
                    return snApp.api.getLoadPosts(userId, totalPost, postId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject o) {
            if (o != null) {
                try {
                    JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                    JSONObject jsonPost;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonPost = jsonArray.getJSONObject(i);
                        posts.add(Post.parse(jsonPost));
                    }
                    postId = (posts.size() > 0 ? posts.get(posts.size()-1).getPostId() : "");
                    updateAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


  class LoadPost extends AsyncTask<Void, Void, JSONObject>{

      @Override
      protected JSONObject doInBackground(Void... voids) {
              return snApp.api.getLoadPosts(userId, totalPost, "");
      }

      @Override
      protected void onPostExecute(JSONObject o) {
          if (o != null) {
              try {
                  posts.clear();
                  JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                  for (int i = 0; i < jsonArray.length(); i++) {
                      JSONObject jsonPost = jsonArray.getJSONObject(i);
                      posts.add(Post.parse(jsonPost));
                  }
                  postId = (posts.size() > 0 ? posts.get(posts.size()-1).getPostId() : "");
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

    class LoadProfile extends AsyncTask<Void, Void, JSONObject>{


        @Override
        protected JSONObject doInBackground(Void... strings) {
            return snApp.api.getUser(userId);
        }

        @Override
        protected void onPostExecute(JSONObject o) {
           if(o!=null){
               try {
                   if(myProfile){
                       postSend.setVisibility(View.VISIBLE);
                   } else {
                       checkBoxFollow.setVisibility(View.VISIBLE);
                   }
                   name.setText(o.getString(Utils.NAME));
                   followers.setText(o.getString(Utils.FOLLOWERS_COUNT) + "  " + getResources().getString(R.string.followers));
                   following.setText(o.getString(Utils.FOLLOWING_COUNT) + "  " + getResources().getString(R.string.followings));
                   int y = Utils.calculateAmountYears(o.getString(Utils.BIRTHDAY));
                   String years = getResources().getQuantityString(R.plurals.years, y, y);
                   birthday.setText(years);
                   System.err.println(o.getString(Utils.PROF_URL));
                   image.setImageUrl(o.getString(Utils.PROF_URL), snApp.mImageLoader);

                   if(myProfile) {
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
        following.setText(sharedPreferences.getString(Utils.FOLLOWING_COUNT, "") + "  " + getResources().getString(R.string.followings));
        followers.setText(sharedPreferences.getString(Utils.FOLLOWERS_COUNT, "") + "  " + getResources().getString(R.string.followers));
    }



}
