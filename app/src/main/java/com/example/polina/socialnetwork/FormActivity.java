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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
;


@EActivity(R.layout.form_activity)
public class FormActivity extends Activity {
    public static final String NAME = "name";
    @ViewById(R.id.form_name)
    public TextView twname;
    @ViewById(R.id.form_birthday)
    public TextView twbithday;
    @ViewById(R.id.form_sex)
    public TextView twsex;
    @ViewById(R.id.form_image)
    public ImageView twimage;
    @App
    SNApp snApp;

    private String name;
    private String birthday;
    private String sex;
    private String image;
    private String imagemini;
    private Uri selectedImage;
    int DIALOG_DATE = 1;
    int DIALOG_SEX = 2;

    int RESULT_LOAD_IMAGE = 1;
    private final static String FEMALE = "female";
    private final static String MALE = "male";
    private final static int WIDTH = 100;
    private final static int HEIGHT = 100;
    String data[] = {FEMALE, MALE};

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(ProfileActivity.PROFILE_PREFERENCES, MODE_PRIVATE);
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

                twname.setText(o.getString(ServerAPI.NAME));

                int y = Utils.calculateAmountYears(o.getString(ServerAPI.BIRTHDAY));
                String years = getResources().getQuantityString(R.plurals.years, y, y);
                twbithday.setText(years);
                String profileURL = o.getString(ServerAPI.PROF_URL);
                image = o.getString(ServerAPI.PROF_URL);
                imagemini = o.getString(ServerAPI.MINI_PROF_URL);
                birthday = o.getString(ServerAPI.BIRTHDAY);
                sex = o.getString(ServerAPI.SEX);
                twsex.setText(sex);
                System.err.println("on load " + birthday);
                System.err.println("on load " + sex);
                getBitmap(new URL(profileURL));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ServerAPI.NAME, o.getString(ServerAPI.NAME));
                editor.putString(ServerAPI.BIRTHDAY, o.getString(ServerAPI.BIRTHDAY));
                editor.putString(ServerAPI.PROF_URL, o.getString(ServerAPI.PROF_URL));
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
        twimage.setImageBitmap(bitmap);
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
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            birthday = String.format("%s/%s/%s", dayOfMonth, monthOfYear, year);
            twbithday.setText(birthday);
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
                twsex.setText(FEMALE);
            } else {
                twsex.setText(MALE);
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
            twimage.setImageURI(selectedImage);
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] prof = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, prof, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onSave(View v) {
        name = twname.getText().toString();
        System.err.println("on save" + birthday);
        sex = twsex.getText().toString();
        System.err.println("on save" + sex);
        saveProf();
    }

    @Background
    void saveProf() {

        try {
            if (selectedImage != null) {
                String path = getRealPathFromURI(FormActivity.this, selectedImage);
                InputStream inputStream = getThumbnailImage(selectedImage);
                image = S3Helper.uploadImage(path);
                imagemini = S3Helper.uploadImage(inputStream);
            }
            JSONObject o = snApp.api.saveProfile(name, birthday, sex, image, imagemini, FormActivity.this);
            System.err.println(o);

            if (o != null) {
                Intent intent = new Intent(FormActivity.this, ProfileActivity_.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public InputStream getThumbnailImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            System.out.println(bitmap.toString());
            Bitmap resizedBitmap = getResizedBitmap(bitmap, HEIGHT, WIDTH);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(bitmapdata);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        Bitmap resizedBitmap;


        if (width >= height) {
            matrix.postScale(scaleHeight, scaleHeight);
            resizedBitmap = Bitmap.createBitmap(bm, width / 2 - height / 2, 0, height, height, matrix, false);
        } else {
            matrix.postScale(scaleWidth, scaleWidth);
            resizedBitmap = Bitmap.createBitmap(bm, 0, height / 2 - width / 2, width, width, matrix, false);
        }

        return resizedBitmap;

    }
}