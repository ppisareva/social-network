package com.example.polina.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by polina on 11.07.15.
 */
public class PostAdapter extends ArrayAdapter<Post> {
    LayoutInflater layoutInflater;
    Context context;

    public PostAdapter(Context context, List<Post> objects) {
        super(context, R.layout.post_list, objects);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ImageLoader mImageLoader;


        if (view == null) {
            view = layoutInflater.inflate(R.layout.post_list, parent, false);
        }

        final Post post = (Post) getItem(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.user_image);
        ImageLoader.ImageCache imageCache = new BitmapLruCache();
        mImageLoader = new ImageLoader(Volley.newRequestQueue(context), imageCache);


        mImageLoader.get(post.getProfile_image(), ImageLoader.getImageListener(imageView,
                0, 0));


        ((TextView) view.findViewById(R.id.user_name)).setText(post.getName());
        Date date = new Date((long)post.getCreatedAt()*1000);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  HH:mm");



        ((TextView) view.findViewById(R.id.time_stamp)).setText(format.format(date));
        ((TextView) view.findViewById(R.id.post_text)).setText(post.getMessage());
        ImageView imageViewPost = (ImageView) view.findViewById(R.id.attached_image);
        imageViewPost.setVisibility(View.GONE);
        if (post.getImage() != "") {
            imageViewPost.setVisibility(View.VISIBLE);
            mImageLoader.get(post.getImage(), ImageLoader.getImageListener(imageViewPost,
                    0, 0));
        }

        CheckBox checkBoxLike = (CheckBox) view.findViewById(R.id.like_chack_box);
        checkBoxLike.setOnCheckedChangeListener(myCheckChangList);
        checkBoxLike.setChecked(post.isOwn_like());
        checkBoxLike.setTag(position);

        final ImageView location = (ImageView) view.findViewById(R.id.image_lication);
        location.setVisibility(View.GONE);
        if (post.getLatitude() != "" && post.getLongitude() != "") {
            location.setVisibility(View.VISIBLE);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Uri locationUri = Uri.parse("geo: " + post.getLatitude() + "," + post.getLongitude() + "");
                    Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
                    intent.setPackage("com.google.android.apps.maps");
                    context.startActivity(intent);
                }


            });
        }

        return view;
    }

    CompoundButton.OnCheckedChangeListener myCheckChangList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getItem((Integer) buttonView.getTag()).setOwn_like(isChecked);
        }


    };


}
