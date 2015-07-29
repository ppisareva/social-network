package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by polina on 11.07.15.
 */
public class PostAdapter extends ArrayAdapter<Post> {
    LayoutInflater layoutInflater;
    Context context;
    ImageLoader mImageLoader;
    RequestQueue queue;
    ViewHolder holder;
    SNApp snApp = new SNApp();

    public PostAdapter(Context context, List<Post> objects, ImageLoader mImageLoader) {
        super(context, R.layout.post_list, objects);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mImageLoader = mImageLoader;
        queue = Volley.newRequestQueue(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Post post = getItem(position);
        if (view == null) {
            view = layoutInflater.inflate(R.layout.post_list, parent, false);
            holder = new ViewHolder();
            holder.checkBoxLike = (CheckBox) view.findViewById(R.id.like_chack_box);
            holder.imagePost = (ImageView) view.findViewById(R.id.attached_image);
            holder.imageUser = (ImageView) view.findViewById(R.id.user_image);
            holder.location = (ImageView) view.findViewById(R.id.image_lication);
            holder.postDate = (TextView) view.findViewById(R.id.time_stamp);
            holder.postText = (TextView) view.findViewById(R.id.post_text);
            holder.userName = (TextView) view.findViewById(R.id.user_name);
            holder.post = post;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        mImageLoader.get(post.getProfileImage(), ImageLoader.getImageListener(holder.imageUser, 0, 0));
        holder.userName.setText(post.getName());
        Date date = new Date((long) post.getCreatedAt() * 1000);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
        holder.postDate.setText(format.format(date));
        holder.postText.setText(post.getMessage());
        holder.imagePost.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(post.getImage())) {
            holder.imagePost.setVisibility(View.VISIBLE);
            mImageLoader.get(post.getImage(), ImageLoader.getImageListener(holder.imagePost, 0, 0));
        }

        holder.checkBoxLike.setOnCheckedChangeListener(null);
        holder.checkBoxLike.setChecked(post.isOwnLike());
        holder.checkBoxLike.setOnCheckedChangeListener(myCheckChangList);
        holder.checkBoxLike.setTag(position);

        holder.location.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(post.getLatitude()) && !TextUtils.isEmpty(post.getLongitude())) {
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setTag("geo: " + post.getLatitude() + "," + post.getLongitude() + "");
            holder.location.setOnClickListener(onClickListener);
        }
        return view;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Uri locationUri = Uri.parse((String) view.getTag());
            Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
            intent.setPackage("com.google.android.apps.maps");
            context.startActivity(intent);
        }
    };

    CompoundButton.OnCheckedChangeListener myCheckChangList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getItem((Integer) buttonView.getTag()).setOwnLike(isChecked);
            final String url = ServerAPI.HOST + "post/" + getItem((int) buttonView.getTag()).getIdPost() + "/like";
            queue.add(new StringRequest((isChecked ? Request.Method.POST : Request.Method.DELETE), url, LISTENER, ERROR_LISTENER) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    CookieSyncManager.createInstance(context);
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
}


