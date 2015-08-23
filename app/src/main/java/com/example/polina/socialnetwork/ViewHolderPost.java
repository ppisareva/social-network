package com.example.polina.socialnetwork;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by polina on 20.07.15.
 */
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
    LinearLayout commentLayout;
    TextView lastComment;
    TextView likeCount;
    Post post;
    TextView commentsCount;

 }
