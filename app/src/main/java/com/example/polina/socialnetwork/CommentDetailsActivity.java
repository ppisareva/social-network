package com.example.polina.socialnetwork;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

@EActivity(R.layout.activity_comment_details)
public class CommentDetailsActivity extends AppCompatActivity {

    @App
    SNApp snApp;

    @ViewById(R.id.edit_comment_details)
    EditText edit;

    Intent intent;
    String postId;
    String commentId;
    String comment;

    @AfterViews
    protected void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();
        Bundle bundle= intent.getExtras();
        if(bundle!=null) {
            postId = bundle.getString(Utils.POST_ID);
            commentId = bundle.getString(Utils.COMMENT_ID);
            comment = bundle.getString(Utils.COMMENT);
            edit.setText(comment);

            System.err.println("id post_items" + postId);
            System.err.println("id comm" + commentId);
        }

    }

@Background
    public void editComment(String comment) {
        try {
            snApp.api.editComment( postId, commentId, comment);


            intent.putExtra(Utils.COMMENT, comment);
            setResult(Utils.RESULT, intent);
           finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_send:
                String comment = edit.getText().toString();
                editComment(comment);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
