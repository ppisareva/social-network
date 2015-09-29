package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by polina on 27.09.15.
 */
public class UsersListAdapter extends BaseAdapter {



        ArrayList<User> users;
        Context context;
        LayoutInflater inflater;
        ImageLoader imageLoader;
    SharedPreferences sharedPreferences;

        public UsersListAdapter(ArrayList<User> users, Context context, ImageLoader imageLoader ) {
            super();
            this.users = users;
            this.context = context;
            this.imageLoader = imageLoader;
            sharedPreferences = context.getSharedPreferences(Utils.PROFILE_PREFERENCES, context.MODE_PRIVATE);
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
                userName += " " + context.getResources().getString(R.string.my_profile);
            }
            ((TextView) view.findViewById(R.id.search_list_name)).setText(userName);
            ((NetworkImageView) view.findViewById(R.id.search_list_image)).setImageUrl(user.userURL, imageLoader);
            return view;
        }


}
