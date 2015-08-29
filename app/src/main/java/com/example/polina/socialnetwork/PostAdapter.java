package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


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

    public PostAdapter(Context context, List<Post> objects, ImageLoader mImageLoader) {
        super(context, R.layout.post_items, objects);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mImageLoader = mImageLoader;
        queue = Volley.newRequestQueue(context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Post post = getItem(position);
        ViewHolderPost holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.post_items, parent, false);
            holder = new ViewHolderPost();
            holder.checkBoxLike = (CheckBox) view.findViewById(R.id.like_chack_box);
            holder.imagePost = (NetworkImageView) view.findViewById(R.id.attached_image);
            holder.imageUser = (NetworkImageView) view.findViewById(R.id.user_image);
            holder.location = (ImageView) view.findViewById(R.id.image_lication);
            holder.postDate = (TextView) view.findViewById(R.id.time_stamp);
            holder.postText = (TextView) view.findViewById(R.id.post_text);
            holder.userName = (TextView) view.findViewById(R.id.user_name);
            holder.likeCount = (TextView) view.findViewById(R.id.like_count);
            holder.commentUserImage = (NetworkImageView) view.findViewById(R.id.comment_image);
            holder.commentUserName = (TextView) view.findViewById(R.id.comment_name);
            holder.commentTimestemp = (TextView) view.findViewById(R.id.comment_time);
            holder.commentLayout = (RelativeLayout) view.findViewById(R.id.layout_comment);
            holder.lastComment = (TextView) view.findViewById(R.id.last_comment);
            holder.commentsCount = (TextView) view.findViewById(R.id.comment_count);
            holder.post = post;
            view.setTag(holder);
        } else {
            holder = (ViewHolderPost) view.getTag();
        }
        holder.imageUser.setImageUrl(post.getProfileImage(), mImageLoader);
        holder.userName.setText(post.getName());
        holder.postDate.setText(Utils.parseDate(post.getCreatedAt()));
        holder.postText.setText(post.getMessage());
        holder.imagePost.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(post.getImage())) {
            holder.imagePost.setVisibility(View.VISIBLE);
            holder.imagePost.setImageUrl(post.getImage(), mImageLoader);
        }

        holder.checkBoxLike.setOnCheckedChangeListener(null);
        holder.checkBoxLike.setChecked(post.isOwnLike());
        holder.checkBoxLike.setOnCheckedChangeListener(myCheckChangeList);
        holder.checkBoxLike.setTag(position);

        holder.commentLayout.setVisibility(View.GONE);
        Comment comment = post.lastComment;
        if (comment != null) {
            holder.commentLayout.setVisibility(View.VISIBLE);
            holder.commentUserName.setText(comment.getName());
            holder.commentUserImage.setImageUrl(comment.getProfileImage(), mImageLoader);
            holder.commentTimestemp.setText(Utils.parseDate(comment.getTimestamp()));
            holder.lastComment.setText(comment.getComment());
        }
        holder.likeCount.setText("" + (post.getLikeCount() != 0 ? post.getLikeCount() : ""));
        holder.commentsCount.setText(post.getCommentsCount() != 0 ? "" + post.getCommentsCount() : "");
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

    CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            Post post = getItem((Integer) buttonView.getTag());
            post.setOwnLike(isChecked);
            if (isChecked)
                ++post.likeCount;
            else
                --post.likeCount;
            notifyDataSetChanged();
            final String url = ServerAPI.HOST + "post_items/" + post.getPostId() + "/like";
            System.err.println(url);
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

    public class ViewHolderPost {

        NetworkImageView imageUser;
        TextView userName;
        TextView postDate;
        TextView postText;
        NetworkImageView imagePost;
        CheckBox checkBoxLike;
        ImageView location;
        NetworkImageView commentUserImage;
        TextView commentUserName;
        TextView commentTimestemp;
        RelativeLayout commentLayout;
        TextView lastComment;
        TextView likeCount;
        Post post;
        TextView commentsCount;

    }
}


