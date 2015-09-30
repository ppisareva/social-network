package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by polina on 29.09.15.
 */
public class FeedFragment extends Fragment {

    ListView feedList;
    SNApp snApp;
    PostAdapter adapter;
    Context thisContext;
    ArrayList<Post> posts = new ArrayList<>();
    private String lastPostId;
    private boolean loadingNow = true;
    SwipeRefreshLayout refreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, null);
        feedList = (ListView) v.findViewById(R.id.list_feed);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout_feed);
        snApp = (SNApp) getActivity().getApplication();
        thisContext  = container.getContext();
        adapter = new PostAdapter(thisContext, posts, snApp.mImageLoader);
        feedList.setAdapter(adapter);
        feedList.setOnScrollListener(myScrollListener);
        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Post post = (Post) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(thisContext, PostDetailsActivity_.class);
                intent.putExtra(Utils.POST, post);
                intent.putExtra(Utils.POSITION, i);
                startActivity(intent);
            }
        });
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
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 1) {
                if (loadingNow) {
                    loadingNow = false;
                    new LoadPost().execute(lastPostId);
                }
            }
        }
    };

    class LoadPost extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            return snApp.api.getFeed(strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject o) {
            if (o != null) {
                try {
                    JSONArray jsonArray = o.getJSONArray(Utils.POSTS_JSON);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        posts.add(Post.parse(jsonPost));
                    }
                   lastPostId = (posts.size() > 0 ? posts.get(posts.size() - 1).getPostId() : "");
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
}
