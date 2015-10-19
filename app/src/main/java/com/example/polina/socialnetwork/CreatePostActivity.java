package com.example.polina.socialnetwork;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EActivity(R.layout.activity_create_post)
public class CreatePostActivity extends AppCompatActivity {
    @App
    SNApp snApp;
    @ViewById(R.id.post_text)
    EditText text;
    @ViewById(R.id.post_image)
    ImageView postImage;
    @ViewById(R.id.location_button)
    ImageView post_location;
    @ViewById(R.id.image_button)
    ImageView imageButton;

    Uri attachedImage;

    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int TAKE_PICTURE = 1;
    private static final int DIALOG_IMAGE = 1;
    private static final String pictureName = "Pic.jpg";

    private String massage;
    private String account;
    private String image;
    private Location location;
    private JSONObject jsonLocation;
    private String data[];
    private String connection_failed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new String[]{getResources().getString(R.string.gallary), getResources().getString(R.string.shot)};
        connection_failed = getResources().getString(R.string.connection_faild);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_IMAGE) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setItems(data, imageClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    DialogInterface.OnClickListener imageClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Intent intent;
            if (which == 0) {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } else {
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                File photo = new File(Environment.getExternalStorageDirectory(), pictureName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                attachedImage = Uri.fromFile(photo);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        }

    };

    public void onImageAttach(View v) {
        showDialog(DIALOG_IMAGE);
    }

    public void onLocationAttach(View v) throws JSONException {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location loc) {
                if (loc != null) {
                    location = loc;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        post_location.setImageResource(R.drawable.ic_room_black_48dp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && data != null) {
            loadImage(data);
        }
        if (requestCode == TAKE_PICTURE) {
            loadImage(attachedImage);
            postImage.setVisibility(View.VISIBLE);
            postImage.setImageURI(attachedImage);
            imageButton.setImageResource(R.drawable.ic_local_see_black_48dp);
        }
    }

    @Background
    public void loadImage(Intent data) {
        attachedImage = data.getData();
        String path = Utils.getRealPathFromURI(CreatePostActivity.this, attachedImage);
        loadImageInBackground(path);
    }

    @UiThread
    public void loadImageInBackground(String path) {
        image = S3Helper.uploadImage(path);
        System.err.println("image " + image);
        postImage.setVisibility(View.VISIBLE);
        postImage.setImageURI(attachedImage);
        imageButton.setImageResource(R.drawable.ic_local_see_black_48dp);
    }

    @Background
    public void loadImage(Uri uri) {
        System.out.println(uri);
        image = S3Helper.uploadImage(uri.getPath());
    }


    @Background
    public void sendPost() {
        sendingPost();
        massage = text.getText().toString();
        if (location != null) {
            jsonLocation = new JSONObject();
            try {
                jsonLocation.put(Utils.LONGITUDE, location.getLongitude());
                jsonLocation.put(Utils.LATITUDE, location.getLatitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject o = snApp.api.newPost(massage, jsonLocation, image, account);
               if (o != null) {
            Intent intent = new Intent(this, ProfileActivity_.class);
            startActivity(intent);
        }
    }

    @UiThread
    public void sendingPost() {
        ProgressDialog progressDialog = new ProgressDialog(CreatePostActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.wait));
        progressDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_send:
                if (isNetworkAvailable()) {
                    sendPost();
                } else {
                    Toast.makeText(CreatePostActivity.this, connection_failed, Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
