package com.example.polina.socialnetwork;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_post_deteils)
public class PostDeteilsActivity extends ActionBarActivity {

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
    private String postID;
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
        postID = intent.getStringExtra(Utils.IDPOST);
        System.out.println(postID);
        mImageLoader = Utils.getmImageLoader(this);


        adapter = new CommentsAdapter(this, mImageLoader, comments);
        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.post_deteils_layout, commentsList, false);
         imageUser = (NetworkImageView) header.findViewById(R.id.user_image_details);
         userName = (TextView)header.findViewById(R.id.user_name_details);
         postDate =(TextView) header.findViewById(R.id.time_stamp_details);
         postText= (TextView) header.findViewById(R.id.post_text_details);
         imagePost = (NetworkImageView)header.findViewById(R.id.attached_image_details);
         checkBoxLike =(CheckBox) header.findViewById(R.id.like_chack_box_details);
        likeCount= (TextView)header.findViewById(R.id.like_count_details);
         location = (ImageView)header.findViewById(R.id.image_lication_details);
        commentsCount= (TextView) header.findViewById(R.id.comment_count_details);
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
        Comment comment = ((ViewHolderComments)info.targetView.getTag()).comments;
        switch (item.getItemId()){
            case R.id.delete_id:
               comments.remove(info.position-1);
                deleteComment(postID, comment.getCommentID());
//                adapter.clear();
//                adapter.addAll(comments);
                adapter.notifyDataSetChanged();
                commentsCount.setText(""+(countComments -=1));

                return true;
            case R.id.edit_id:

                Intent intent = new Intent(PostDeteilsActivity.this, CommentDetailsActivity_.class);
                intent.putExtra(Utils.IDCOMMENTINTEND, comment.getCommentID());
                intent.putExtra(Utils.IDPOSTINTENG, postID);
                intent.putExtra(Utils.POSITION, info.position-1);
                intent.putExtra(Utils.COMMENT , comment.getComment());
                startActivityForResult(intent, INTENT_EDIT);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==INTENT_EDIT&&RESULT_OK==resultCode){
            Bundle bundle= data.getExtras();
            if(bundle!=null) {
                String commentFromIntent = bundle.getString(Utils.COMMENT);
                int position = bundle.getInt(Utils.POSITION);
                comments.get(position).comment = commentFromIntent;
//                adapter.clear();
//                adapter.addAll(comments);
                adapter.notifyDataSetChanged();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Background
    public void deleteComment(String postId, String commentID) {
        JSONObject o = snApp.api.deleteComment(PostDeteilsActivity.this, postId, commentID);
    }

    @AfterViews
    protected void init() {
        commentsList.addHeaderView(header, null, false);
        commentsList.setAdapter(adapter);
        registerForContextMenu(commentsList);
        loadPost();
        loadLike();
        loadComments();
    }


    @Background
   public void loadComments() {
        JSONObject o = snApp.api.getComments(PostDeteilsActivity.this, postID);
        System.err.println("comments list" + o);
        if(o!=null){
            viewComments(o);
        }
    }
@UiThread
    public void viewComments(JSONObject o) {
    try {
        JSONArray jsonArray = o.getJSONArray(Utils.COMMENTS);
        System.err.println(jsonArray);
        System.err.println(jsonArray.length());
//        comments = new ArrayList<>();
        comments.clear();
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            comments.add(Comment.parse(json));
        }

//      updateAdapter(comments);
        adapter.notifyDataSetChanged();
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    @UiThread
    public void updateAdapter(ArrayList<Comment> comments) {
        adapter.clear();
        adapter.addAll(comments);
        adapter.notifyDataSetChanged();
    }

    @Background
    public void loadLike() {
        JSONObject o = snApp.api.getLike(PostDeteilsActivity.this, postID);
        System.out.println(o + "-----");
        if (o != null) {
            viewLikes(o);
        }
    }

    @UiThread
    public void viewLikes(JSONObject o) {
        usersLiked = Utils.loadLikes(o);
        if (!usersLiked.isEmpty()) {
            countLikes = usersLiked.size();
            likeCount.setText("" + countLikes);
        }
        for (User user : usersLiked) {
            if (user.userId.equals(idUser)) {
                checkBoxLike.setChecked(true);
                break;
            }
        }
    }

    @Background
    public void loadPost() {
        JSONObject o = snApp.api.getPost(PostDeteilsActivity.this, postID);
        System.out.println(o);
        if (o != null) {
            viewPost(o);
        }
    }

    @UiThread
    public void viewPost(JSONObject o) {
        try {
            System.out.println(o);
            postDate.setText(Utils.parseDate(o.getDouble(Utils.TIMESTAMP)));
            JSONObject object = o.getJSONObject(Utils.CREATED_BY);
            userName.setText(object.getString(Utils.NAME));
            idUser = object.getString(Utils.IDUSER);
            imageUser.setImageUrl(object.getString(Utils.MINI_PROF_URL), mImageLoader);
            postText.setText(o.optString(Utils.MASSAGE));
            object = o.optJSONObject(Utils.LOCATION);
            countComments = o.optInt(Utils.COMMENTS_COUNT);
            commentsCount.setText("" + countComments);
            if (object != null) {
                String latitude = object.optString(Utils.LATITUDE);
                String longitude = object.optString(Utils.LONGITUDE);
                location.setVisibility(View.VISIBLE);
                location.setTag("geo: " + latitude + "," + longitude + "");
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
            if (!o.optString(Utils.IMAGE, "").isEmpty()) {
                imagePost.setVisibility(View.VISIBLE);
                imagePost.setImageUrl(o.optString(Utils.IMAGE, ""), mImageLoader);
            }

            checkBoxLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    compoundButton.setChecked(isChecked);

                    likeCount.setText("" + (isChecked ? (countLikes += 1) : (countLikes -= 1)));

                    final String url = ServerAPI.HOST + "post/" + postID + "/like";
                    Volley.newRequestQueue(PostDeteilsActivity.this).add(new StringRequest((isChecked ? Request.Method.POST : Request.Method.DELETE), url, LISTENER, ERROR_LISTENER) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            CookieSyncManager.createInstance(PostDeteilsActivity.this);
                            CookieManager cookieManager = CookieManager.getInstance();
                            headers.put("Cookie", cookieManager.getCookie(url));
                            return headers;
                        }
                    });
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onCommentSend(View v) {
        String comment = newComment.getText().toString();
        sendComment(comment);
        newComment.setText("");
        commentsCount.setText("" +(countComments +=1));



    }

    @Background
    public void sendComment(String comment) {
        JSONObject o = snApp.api.sendComment(PostDeteilsActivity.this, postID, comment);
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
