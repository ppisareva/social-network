package com.example.polina.socialnetwork;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
;


@EActivity(R.layout.form_activity)
public class FormActivity extends AppCompatActivity {

    @ViewById(R.id.form_name)
    public TextView name;
    @ViewById(R.id.form_birthday)
    public TextView birthday;
    @ViewById(R.id.form_sex)
    public TextView sex;
    @ViewById(R.id.form_image)
    public NetworkImageView profilePhoto;
    @App
    SNApp snApp;

    private String imageURI;
    private String miniImageURI;
    private Uri selectedImage;
    SharedPreferences sharedPreferences;

    private static int DIALOG_DATE = 1;
    private static int DIALOG_SEX = 2;
   private static int RESULT_LOAD_IMAGE = 1;
    private final static String FEMALE = "female";
    private final static String MALE = "male";
    String data[] = {FEMALE, MALE};
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        loadProfInfo();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Background
    public void loadProfInfo() {
        JSONObject o = snApp.api.getProfile();
        addProfileInfo(o);
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o) {
        if (o != null) {
            try {
                name.setText(o.getString(Utils.NAME));
                date = o.getString(Utils.BIRTHDAY);
                int y = Utils.calculateAmountYears(date);

                String years = getResources().getQuantityString(R.plurals.years, y, y);
                birthday.setText(years);
                String profileURL = o.getString(Utils.PROF_URL);
                imageURI = o.getString(Utils.PROF_URL);
                miniImageURI = o.getString(Utils.MINI_PROF_URL);
                sex.setText(o.getString(Utils.SEX));
                profilePhoto.setImageUrl(profileURL, snApp.mImageLoader);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Utils.NAME, o.getString(Utils.NAME));
                editor.putString(Utils.BIRTHDAY, o.getString(Utils.BIRTHDAY));
                editor.putString(Utils.PROF_URL, o.getString(Utils.PROF_URL));
                editor.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_DATE) {
            DatePickerDialog tpd = new DatePickerDialog(this, callBackDataDialog, 2000, 01, 01);
            return tpd;
        }
        if (id == DIALOG_SEX) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setSingleChoiceItems(data, -1, sexClickListener);
            adb.setPositiveButton(R.string.ok, sexClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    public void onDateChoose(View v) {
        showDialog(DIALOG_DATE);
    }

    DatePickerDialog.OnDateSetListener callBackDataDialog = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date = String.format("%s/%s/%s", dayOfMonth, monthOfYear, year);
            birthday.setText(date);
        }
    };

    public void onSexChoose(View v) {
        showDialog(DIALOG_SEX);
    }

    DialogInterface.OnClickListener sexClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            int pos = lv.getCheckedItemPosition();
            if (pos == 0) {
                sex.setText(FEMALE);
            } else {
                sex.setText(MALE);
            }
        }

    };

    public void onImageAdd(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            profilePhoto.setImageURI(selectedImage);
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
                    saveProf();
                    return true;
            }
            return super.onOptionsItemSelected(item);
    }

    @Background
    void saveProf() {
        try {
            if (selectedImage != null) {
                String path = Utils.getRealPathFromURI(FormActivity.this, selectedImage);
                InputStream inputStream = Utils.getThumbnailImage(selectedImage, FormActivity.this);
                imageURI = S3Helper.uploadImage(path);
                miniImageURI = S3Helper.uploadImage(inputStream);
            }
            JSONObject o = snApp.api.saveProfile(name.getText().toString(), date, sex.getText().toString(), imageURI, miniImageURI);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Utils.NAME, name.getText().toString());

            editor.putString(Utils.BIRTHDAY, date);
            editor.putString(Utils.PROF_URL, imageURI);
            editor.commit();

            System.err.println(o);
            if (o != null) {
                Intent intent = new Intent(FormActivity.this, ProfileActivity_.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}