package com.example.polina.socialnetwork;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * Created by polina on 22.09.15.
 */
public class SearchFragment extends Fragment {

    SNApp snApp;
    Context thisContext;
    ArrayList<User> users = new ArrayList<>();
    SearchAdapter adapter;
    EditText searchName;



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, null);
        snApp= (SNApp) getActivity().getApplication();
        searchName = (EditText) v.findViewById(R.id.search_name);
        thisContext = container.getContext();
        ListView searchList = (ListView)v.findViewById(R.id.search_result);
        adapter = new SearchAdapter(users, thisContext, snApp.mImageLoader);
        searchList.setAdapter(adapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User)adapterView.getItemAtPosition(i);
                System.err.println(" user ID" + user.userId);
            }
        });
        ImageButton button = (ImageButton)v.findViewById(R.id.butt_search);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.err.println(" press onclick");
                new LoadUsers().execute(searchName.getText().toString());
            }
        });
        return v;
    }


    class LoadUsers extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            if(!strings[0].isEmpty()) {
                return snApp.api.findUsers(strings[0], 0);
            }
            System.err.println(" don't state user name");
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            System.out.println(jsonObject);
            if(jsonObject!=null){
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray(Utils.USERS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        users.add(User.parse(jsonPost));
                    }
                    adapter.notifyDataSetChanged();

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
            if(view==null) {
                view = inflater.inflate(R.layout.search_users_list, null);
            }
            User user = (User) getItem(i);
                    ((TextView) view.findViewById(R.id.search_list_name)).setText(user.userName);
                ((NetworkImageView)view.findViewById(R.id.search_list_image)).setImageUrl(user.userURL, imageLoader);
            return view;
        }
    }

}
