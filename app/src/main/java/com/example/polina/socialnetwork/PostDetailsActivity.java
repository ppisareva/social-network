package com.example.polina.socialnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_post_details)
public class PostDetailsActivity extends AppCompatActivity {

    @App
    SNApp snApp;
    @ViewById(R.id.comments)
    ListView commentsList;
    @ViewById(R.id.edit_comment)
    EditText newComment;


    NetworkImageView imageUser;
    TextView userName;
    TextView postDate;
    TextView postText;
    NetworkImageView imagePost;
    CheckBox checkBoxLike;
    TextView likeCount;
    ImageView location;
    TextView commentsCount;

    Intent intent;
    private Post post;
    private ImageLoader mImageLoader;
    private CommentsAdapter adapter;
    ViewGroup header;
    ArrayList<Comment> comments = new ArrayList<Comment>();
    int countComments;
    int countLikes;
    int INTENT_EDIT = 0;
    boolean myPost = false;
    SharedPreferences sharedPreferences;
    android.view.ActionMode actionMode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        post = (Post) intent.getSerializableExtra(Utils.POST);
        if (TextUtils.equals(post.getUserId(), snApp.getUserId())) {
            myPost = true;
        }
        mImageLoader = snApp.mImageLoader;
        adapter = new CommentsAdapter(this, mImageLoader, comments);
        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.post_details_layout, commentsList, false);
        imageUser = (NetworkImageView) header.findViewById(R.id.user_image_details);
        userName = (TextView) header.findViewById(R.id.user_name_details);
        postDate = (TextView) header.findViewById(R.id.time_stamp_details);
        postText = (TextView) header.findViewById(R.id.post_text_details);
        imagePost = (NetworkImageView) header.findViewById(R.id.attached_image_details);
        checkBoxLike = (CheckBox) header.findViewById(R.id.like_chack_box_details);
        likeCount = (TextView) header.findViewById(R.id.like_count_details);
        location = (ImageView) header.findViewById(R.id.image_lication_details);
        commentsCount = (TextView) header.findViewById(R.id.comment_count_details);
    }


    class ActionBarCallBack implements ActionMode.Callback {

        Comment comment;
        int position;

        public ActionBarCallBack(Comment comment, int position) {
            this.comment = comment;
            this.position = position;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.delete_id:
                    comments.remove(position - 1);
                    deleteComment(post.getPostId(), comment.getCommentId());
                    adapter.notifyDataSetChanged();
                    commentsCount.setText("" + (--countComments));
                    post.commentsCount = countComments;
                    if(countComments==0){
                        post.lastComment = null;
                    } else{
                        post.lastComment = comments.get(comments.size() - 1);
                    }
                    intent.putExtra(Utils.POST, post);
                    setResult(RESULT_CANCELED, intent);
                    mode.finish();
                    break;
                case R.id.edit_id:
                    Intent intent = new Intent(PostDetailsActivity.this, CommentDetailsActivity_.class);
                    intent.putExtra(Utils.COMMENT_ID, comment.getCommentId());
                    intent.putExtra(Utils.POST_ID, post.getPostId());
                    intent.putExtra(Utils.POSITION, position - 1);
                    intent.putExtra(Utils.COMMENT, comment.getComment());
                    startActivityForResult(intent, INTENT_EDIT);
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            if (TextUtils.equals(comment.userID, snApp.userId)) {
                mode.getMenuInflater().inflate(R.menu.comment_edit_delete, menu);
            } else if (TextUtils.equals(post.userId, snApp.userId)){
                mode.getMenuInflater().inflate(R.menu.comment_delete, menu);
            } else {
                return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (myPost) {
            getMenuInflater().inflate(R.menu.manu_post_details, menu);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_delete:
                deletePost();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Background
    public void deletePost() {
        JSONObject o = snApp.api.deletePost(post.getPostId());
        System.err.println("delet post " + o);
        back();

    }

    @UiThread
    public void back() {
        setResult(Utils.RESULT, intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_EDIT && Utils.RESULT == resultCode) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String commentFromIntent = bundle.getString(Utils.COMMENT);
                int position = bundle.getInt(Utils.POSITION);
                comments.get(position).comment = commentFromIntent;
                adapter.notifyDataSetChanged();
                if(position==(comments.size()-1)) {
                    post.lastComment = comments.get(position);
                    intent.putExtra(Utils.POST, post);
                    setResult(RESULT_CANCELED, intent);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Background
    public void deleteComment(String postId, String commentID) {
        JSONObject o = snApp.api.deleteComment(postId, commentID);
        System.out.println(o);
    }

    @AfterViews
    protected void init() {
        commentsList.addHeaderView(header, null, false);
        commentsList.setAdapter(adapter);
        commentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Comment comment = (Comment) adapterView.getItemAtPosition(i);
                actionMode = PostDetailsActivity.this.startActionMode(new ActionBarCallBack(comment, i));

            }
        });
        loadPost();
        loadComments();
    }




    @Background
    public void loadComments() {
        System.err.println(post.getPostId());
        JSONObject o = snApp.api.getComments(post.getPostId());
        System.err.println("comments list" + o);
        if (o != null) {
            viewComments(o);
        }
    }

    @UiThread
    public void viewComments(JSONObject o) {
        JSONArray jsonArray = o.optJSONArray(Utils.COMMENTS);
        System.err.println(jsonArray);
        System.err.println(jsonArray.length());
        comments.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject commentJson = jsonArray.optJSONObject(i);
            comments.add(Comment.parse(commentJson));
        }
        if(!comments.isEmpty()) {
            post.lastComment=comments.get(comments.size() - 1);
            intent.putExtra(Utils.POST, post);
        }
        setResult(RESULT_CANCELED, intent);
        adapter.notifyDataSetChanged();
    }

    @UiThread
    public void updateAdapter(ArrayList<Comment> comments) {
        adapter.clear();
        adapter.addAll(comments);
        adapter.notifyDataSetChanged();
    }

    public void loadPost() {
        postDate.setText(Utils.parseDate(post.getCreatedAt()));
        userName.setText(post.getName());
        imageUser.setImageUrl(post.getProfileImage(), mImageLoader);
        postText.setVisibility(View.GONE);
        if(!TextUtils.isEmpty(post.getMessage())){
            postText.setText(post.getMessage());
            postText.setVisibility(View.VISIBLE);
        }
        commentsCount.setText("" + post.getCommentsCount());
        countComments = post.getCommentsCount();
        likeCount.setText("" + post.getLikeCount());
        countLikes = post.getLikeCount();

        if (post.isOwnLike()) {
            checkBoxLike.setChecked(true);
        }
        if (!TextUtils.isEmpty(post.getLatitude())) {
            location.setVisibility(View.VISIBLE);
            location.setTag("geo: " + post.getLatitude() + "," + post.getLongitude() + "");
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri locationUri = android.net.Uri.parse((String) view.getTag());
                    Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            });
        }
        if (post.getImage() != null) {
            imagePost.setVisibility(View.VISIBLE);
            imagePost.setImageUrl(post.getImage(), mImageLoader);
        }

        checkBoxLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                compoundButton.setChecked(isChecked);
                likeCount.setText("" + (isChecked ? ++countLikes : --countLikes == 0 ? "" : countLikes));
                final String url = ServerAPI.HOST + "post/" + post.getPostId() + "/like";
                Volley.newRequestQueue(PostDetailsActivity.this).add(new StringRequest((isChecked ? Request.Method.POST : Request.Method.DELETE), url, LISTENER, ERROR_LISTENER) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        CookieManager cookieManager = CookieManager.getInstance();
                        headers.put("Cookie", cookieManager.getCookie(url));
                        return headers;
                    }
                });
                post.likeCount= countLikes;
                post.ownLike=isChecked;
                intent.putExtra(Utils.POST, post);
                setResult(RESULT_CANCELED, intent);
            }
        });
    }


    public void onCommentSend(View v) {
        String comment = newComment.getText().toString();
        sendComment(comment);
        newComment.setText("");
        commentsCount.setText("" + (++countComments));
        post.commentsCount= countComments;
        intent.putExtra(Utils.POST, post);
        setResult(RESULT_CANCELED, intent);
    }

    @Background
    public void sendComment(String comment) {
        snApp.api.sendComment(post.getPostId(), comment);
        loadComments();
    }

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
