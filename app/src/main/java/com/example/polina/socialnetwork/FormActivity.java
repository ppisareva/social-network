package com.example.polina.socialnetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FormActivity extends Activity {
    public static final String NAME = "name";
    private TextView twname;
    private TextView twbithday;
    private TextView twsex;
    private ImageView twimage;
    private String name;
    private String birthday;
    private String sex;
    private String image;
    private String imagemini;
    private Uri selectedImage = null;
    int DIALOG_DATE = 1;
    int DIALOG_SEX = 2;
    int RESULT_LOAD_IMAGE = 1;
    private String female = "female";
    private String male = "male";
    private Intent intent;


    String data[] = {female, male};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_activity);
        twname = (TextView) findViewById(R.id.form_name);
        twbithday = (TextView) findViewById(R.id.form_birthday);
        twsex = (TextView) findViewById(R.id.form_sex);
        twimage = (ImageView) findViewById(R.id.form_image);
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

    public void choseDate(View v) {
        showDialog(DIALOG_DATE);
    }

    DatePickerDialog.OnDateSetListener callBackDataDialog = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            twbithday.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
        }
    };


    public void choseSex(View v) {
        showDialog(DIALOG_SEX);
    }

    DialogInterface.OnClickListener sexClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            int pos = lv.getCheckedItemPosition();
            if (pos == 0) {
                twsex.setText(female);
            } else {
                twsex.setText(male);
            }

        }

    };


    public void addImage(View v) {
        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            twimage.setImageURI(selectedImage);
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void submit(View v) {
        name = twname.getText().toString();
        birthday = twbithday.getText().toString();
        sex = twsex.getText().toString();

        new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(Void... voids) {
                String path = getRealPathFromURI(FormActivity.this, selectedImage);
                InputStream inputStream = getThumbnailImage(selectedImage);
                String url = ConnectionTo3S.uploadImage(path);
                String urlSmall = ConnectionTo3S.uploadImage(inputStream);
                List<String> listOfUrls = new ArrayList<>();
                listOfUrls.add(url);
                listOfUrls.add(urlSmall);
                return listOfUrls;
            }

            @Override
            protected void onPostExecute(List<String> urls) {
                if (urls != null) {
                    image = urls.get(0);
                    imagemini = urls.get(1);
                }
            }
        }.execute();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    JSONObject o = ((SNApp) getApplication()).api.userInfo(name, birthday, sex, image, imagemini, FormActivity.this);
                    if(o!= null){
                        intent = new Intent(FormActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }


    public InputStream getThumbnailImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            System.out.println(bitmap.toString());
            Bitmap resizedBitmap = getResizedBitmap(bitmap, 100, 100);
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

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

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


    public String getThumbnailPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

        // This method was deprecated in API level 11
        Cursor cursor = managedQuery(uri, proj, null, null, null);

        System.err.println("NAMES: " + Arrays.toString(cursor.getColumnNames()));
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        cursor.moveToFirst();
        long imageId = cursor.getLong(column_index);
        //cursor.close();
        String result = "";
        cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), imageId,
                MediaStore.Images.Thumbnails.MINI_KIND, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            cursor.close();
        }
        return result;
    }
}
