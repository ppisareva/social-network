package com.example.polina.socialnetwork;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
public class PostDetailsActivity extends ActionBarActivity {

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
    String idUser;
    private ImageLoader mImageLoader;
    ArrayList<User> usersLiked;
    private CommentsAdapter adapter;
    ViewGroup header;
    ArrayList<Comment> comments = new ArrayList<Comment>();
    int countComments;
    int countLikes;
    int INTENT_EDIT = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        post = (Post)intent.getSerializableExtra(Utils.POST);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manu_post_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            deletePost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
@Background
    public void deletePost() {
    JSONObject o = snApp.api.deletePost(post.getPostId());
    System.err.println(o);
        back();

    }
    @UiThread
    public void back() {
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comment_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Comment comment = ((CommentsAdapter.ViewHolderComments) info.targetView.getTag()).comments;
        switch (item.getItemId()) {
            case R.id.delete_id:
                comments.remove(info.position - 1);
                deleteComment(post.getPostId(), comment.getCommentId());
                adapter.notifyDataSetChanged();
                commentsCount.setText("" + (--countComments));
                return true;
            case R.id.edit_id:
                Intent intent = new Intent(PostDetailsActivity.this, CommentDetailsActivity_.class);
                intent.putExtra(Utils.COMMENT_ID, comment.getCommentId());
                intent.putExtra(Utils.POST_ID, post.getPostId());
                intent.putExtra(Utils.POSITION, info.position - 1);
                intent.putExtra(Utils.COMMENT, comment.getComment());
                startActivityForResult(intent, INTENT_EDIT);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_EDIT && RESULT_OK == resultCode) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String commentFromIntent = bundle.getString(Utils.COMMENT);
                int position = bundle.getInt(Utils.POSITION);
                comments.get(position).comment = commentFromIntent;
                adapter.notifyDataSetChanged();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Background
    public void deleteComment(String postId, String commentID) {
        JSONObject o = snApp.api.deleteComment(postId, commentID);
    }

    @AfterViews
    protected void init() {
        commentsList.addHeaderView(header, null, false);
        commentsList.setAdapter(adapter);
        registerForContextMenu(commentsList);
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
            adapter.notifyDataSetChanged();
    }

    @UiThread
    public void updateAdapter(ArrayList<Comment> comments) {
        adapter.clear();
        adapter.addAll(comments);
        adapter.notifyDataSetChanged();
    }

//    @Background
//    public void loadLike() {
//        JSONObject o = snApp.api.getLike(PostDetailsActivity.this, post_items.getPostId());
//        System.out.println(o + "-----");
//        if (o != null) {
//            viewLikes(o);
//        }
//    }
//
//    @UiThread
//    public void viewLikes(JSONObject o) {
//        usersLiked = Utils.loadUsersLiked(o);
//        if (!usersLiked.isEmpty()) {
//            countLikes = usersLiked.size();
//            likeCount.setText("" + countLikes);
//        }
//        for (User user : usersLiked) {
//            if (user.userId.equals(idUser)) {
//                checkBoxLike.setChecked(true);
//                break;
//            }
//        }
//    }


    public void loadPost() {
            postDate.setText(Utils.parseDate(post.getCreatedAt()));
            userName.setText(post.getName());
            imageUser.setImageUrl(post.getProfileImage(), mImageLoader);
            postText.setText(post.getMessage());
            commentsCount.setText(""+post.getCommentsCount());
            likeCount.setText("" + post.getLikeCount());
        if(post.isOwnLike()){
            checkBoxLike.setChecked(true);
        }
            if (post.getLatitude()!=null) {
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
            if (post.getImage()!=null) {
                imagePost.setVisibility(View.VISIBLE);
                imagePost.setImageUrl(post.getImage(), mImageLoader);
            }

            checkBoxLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    compoundButton.setChecked(isChecked);
                    likeCount.setText("" + (isChecked ? ++countLikes : --countLikes==0 ? "" : countLikes));
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
                }
            });
    }



    public void onCommentSend(View v) {
        String comment = newComment.getText().toString();
        sendComment(comment);
        newComment.setText("");
        commentsCount.setText("" + (++countComments));


    }

    @Background
    public void sendComment(String comment) {
        JSONObject o = snApp.api.sendComment(post.getPostId(), comment);
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
