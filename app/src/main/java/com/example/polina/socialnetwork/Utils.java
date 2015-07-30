package com.example.polina.socialnetwork;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by polina on 17.07.15.
 */
public class Utils {


    public static final String PROFILE_PREFERENCES = "profile info";
    public static final String NAME = "name";
    public static final String BIRTHDAY = "birthday";
    public static final String SEX = "sex";
    public static final String PROF_URL = "profile_url";
    public static final String MINI_PROF_URL = "mini_profile_url";
    public static final String POST_ACCOUNT = "account";
    public static final String POST_LOCATION = "location";
    public static final String POST_IMAGE = "image";
    public static final String POST_MASSAGE = "massage";
    public static final String TIMESTAMP = "created_at";
    public static final String LIKE = "own_like";
    public static final String CREATED_BY = "created_by";
    public static final String IDUSER = "_id";
    public static final String IDPOST = "_id";
    public static final String MASSAGE = "massage";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IMAGE = "image";
    public static final String USER_ID_PREFERENCES = "User ID";
    public static final String USER_ID = "_id";
    public static final String POSTS_JSON = "posts";
    private final static int WIDTH = 100;
    private final static int HEIGHT = 100;



    public static int calculateAmountYears(String birthday) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date bDate = format.parse(birthday);
            int bYear = bDate.getYear();
            int bMonth = bDate.getMonth();
            int bDay = bDate.getDay();

            Date nowDate = now.getTime();
            int nowYear = nowDate.getYear();
            int nowMonth = nowDate.getMonth();
            int nowDay = nowDate.getDay();

            int year = nowYear - bYear - 1;

            if (bMonth < nowMonth) {
                year++;
            }
            if (bMonth == nowMonth) {
                if (bDay < nowDay) {
                    year++;
                }
            }

            return year;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
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

    public static InputStream getThumbnailImage(Uri uri, Context context) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
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
