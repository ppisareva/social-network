package com.example.polina.socialnetwork;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
public class CreatePostActivity extends ActionBarActivity {
    @App
    SNApp snApp;
    @ViewById(R.id.post_text)
    EditText text;
    @ViewById(R.id.post_image)
    ImageView postImage;
    @ViewById(R.id.post_file)
    ImageView post_file;
    @ViewById(R.id.post_location)
    ImageView post_location;
    Uri attachedImage;

    private static int RESULT_LOAD_IMAGE = 2;
    private static int RESULT_LOAD_FILE = 3;
    private static int TAKE_PICTURE = 1;
    private static int DIALOG_IMAGE = 1;

    String massage;
    String attachment;
    String account;
    String image;
    Location location;
    JSONObject jsonLocation;
    String data[];
    String connection_faild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new String[]{getResources().getString(R.string.gallary), getResources().getString(R.string.shot)};
        connection_faild = getResources().getString(R.string.connection_faild);
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
            ListView lv = ((AlertDialog) dialog).getListView();
            if (which == 0) {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } else {
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
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
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("longitude", location.getLongitude());
        jsonLocation.put("latitude", location.getLatitude());
        Log.d("Location", location.toString());
        post_location.setVisibility(View.VISIBLE);
    }

    public void onAttach(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, RESULT_LOAD_FILE);
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
        }
        if (requestCode == RESULT_LOAD_FILE && data != null) {
            attachment = FormActivity.getRealPathFromURI(CreatePostActivity.this, data.getData());
            post_file.setVisibility(View.VISIBLE);
        }
    }

    @Background
    public void loadImage(Intent data) {
        attachedImage = data.getData();
        String path = FormActivity.getRealPathFromURI(CreatePostActivity.this, attachedImage);
        loadImageInBackground(path);
    }

    @UiThread
    public void loadImageInBackground(String path) {
        image = S3Helper.uploadImage(path);
        postImage.setVisibility(View.VISIBLE);
        postImage.setImageURI(attachedImage);

    }

    @Background
    public void loadImage(Uri uri) {
        System.out.println(uri);
        image = S3Helper.uploadImage(uri.getPath());
    }


    @Background
    public void sandPost() {
        sendingPost();
        JSONObject o = snApp.api.newPost(this, massage, jsonLocation, attachment, image, account);
        System.err.println("Image not choose" + image);
        if (o != null) {
            Intent intent = new Intent(this, ProfileActivity_.class);
            startActivity(intent);
        }
    }

    @UiThread
    public void sendingPost(){
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
                    sandPost();
                } else {
                    Toast.makeText(CreatePostActivity.this, connection_faild, Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAccountAttach(View v) {
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
