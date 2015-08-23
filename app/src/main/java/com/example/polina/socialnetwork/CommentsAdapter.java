package com.example.polina.socialnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

/**
 * Created by polina on 14.08.15.
 */
public class CommentsAdapter extends ArrayAdapter<Comment>{
    LayoutInflater layoutInflater;
    Context context;
    ImageLoader mImageLoader;

    public CommentsAdapter(Context context, ImageLoader mImageLoader, List<Comment> objects) {
        super(context, R.layout.comment_list, objects);
        this.mImageLoader = mImageLoader;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Comment comment = getItem(position);
        ViewHolderComments holder= null;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.comment_list, parent, false);
            holder = new ViewHolderComments();
            holder.image = (NetworkImageView) view.findViewById(R.id.comment_image_list);
            holder.comment = (TextView) view.findViewById(R.id.comment_list);
            holder.name = (TextView) view.findViewById(R.id.comment_name_list);
            holder.date = (TextView) view.findViewById(R.id.comment_time_list);
            holder.comments = comment;
            view.setTag(holder);
        } else {
            holder = (ViewHolderComments) view.getTag();
        }

        holder.image.setImageUrl(comment.getProfileImage(), mImageLoader);
        holder.comment.setText(comment.getComment());
        holder.date.setText(Utils.parseDate(comment.getTimeStemp()));
        holder.name.setText(comment.getName());

        return view;
    }
}
