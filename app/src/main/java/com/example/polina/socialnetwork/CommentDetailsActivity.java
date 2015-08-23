package com.example.polina.socialnetwork;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_comment_details)
public class CommentDetailsActivity extends ActionBarActivity {

    @App
    SNApp snApp;

    @ViewById(R.id.edit_comment_details)
    EditText edit;

    Intent intent;
    String postId;
    String commentID;
    String comment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void init() {
        intent = getIntent();
        Bundle bundle= intent.getExtras();
        if(bundle!=null) {
            postId = bundle.getString(Utils.IDPOSTINTENG);
            commentID = bundle.getString(Utils.IDCOMMENTINTEND);
            comment = bundle.getString(Utils.COMMENT);
            edit.setText(comment);

            System.err.println("id post" + postId);
            System.err.println("id comm" + commentID);
        }

    }

@Background
    public void editComment(String comment) {
        try {
            snApp.api.editComment(this, postId, commentID, comment);


            intent.putExtra(Utils.COMMENT, comment);
            setResult(RESULT_OK, intent);
           finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comment_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            String comment = edit.getText().toString();
            editComment(comment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
