package com.example.polina.socialnetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
;


@EActivity(R.layout.form_activity)
public class FormActivity extends Activity {

    @ViewById(R.id.form_name)
    public TextView tvName;
    @ViewById(R.id.form_birthday)
    public TextView tvBithday;
    @ViewById(R.id.form_sex)
    public TextView tvSex;
    @ViewById(R.id.form_image)
    public ImageView tvImage;
    @App
    SNApp snApp;

    private String name;
    private String birthday;
    private String sex;
    private String image;
    private String imageMini;
    private Uri selectedImage;
    SharedPreferences sharedPreferences;

    private static int DIALOG_DATE = 1;
    private static int DIALOG_SEX = 2;
   private static int RESULT_LOAD_IMAGE = 1;
    private final static String FEMALE = "female";
    private final static String MALE = "male";
    String data[] = {FEMALE, MALE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        loadProfInfo();
    }

    @Background
    public void loadProfInfo() {
        JSONObject o = snApp.api.getProfile(FormActivity.this);
        addProfileInfo(o);
    }

    @org.androidannotations.annotations.UiThread
    public void addProfileInfo(JSONObject o) {
        if (o != null) {
            try {
                tvName.setText(o.getString(Utils.NAME));
                int y = Utils.calculateAmountYears(o.getString(Utils.BIRTHDAY));
                String years = getResources().getQuantityString(R.plurals.years, y, y);
                tvBithday.setText(years);
                String profileURL = o.getString(Utils.PROF_URL);
                image = o.getString(Utils.PROF_URL);
                imageMini = o.getString(Utils.MINI_PROF_URL);
                birthday = o.getString(Utils.BIRTHDAY);
                sex = o.getString(Utils.SEX);
                tvSex.setText(sex);
                getBitmap(new URL(profileURL));
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

    @Background
    public void getBitmap(URL url) {
        try {
            InputStream in = url.openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            saveImage(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void saveImage(Bitmap bitmap) {
        tvImage.setImageBitmap(bitmap);
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
            birthday = String.format("%s/%s/%s", dayOfMonth, monthOfYear, year);
            tvBithday.setText(birthday);
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
                tvSex.setText(FEMALE);
            } else {
                tvSex.setText(MALE);
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
            tvImage.setImageURI(selectedImage);
        }
    }

    public void onSave(View v) {
        name = tvName.getText().toString();
        sex = tvSex.getText().toString();
        saveProf();
    }

    @Background
    void saveProf() {
        try {
            if (selectedImage != null) {
                String path = Utils.getRealPathFromURI(FormActivity.this, selectedImage);
                InputStream inputStream = Utils.getThumbnailImage(selectedImage, FormActivity.this);
                image = S3Helper.uploadImage(path);
                imageMini = S3Helper.uploadImage(inputStream);
            }
            JSONObject o = snApp.api.saveProfile(name, birthday, sex, image, imageMini, FormActivity.this);
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