package com.example.polina.socialnetwork;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.webkit.CookieSyncManager;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.EApplication;

import java.util.HashSet;

/**
 * Created by polina on 04.06.15.
 */
@EApplication
public class SNApp extends Application {
    API api = new ServerAPI();
    ImageLoader mImageLoader;
    String userId;
    HashSet<String> followerIds;

    public HashSet<String> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(HashSet<String> followerIds) {
        this.followerIds = followerIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate() {
        CookieSyncManager.createInstance(getApplicationContext());
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(Utils.PROFILE_PREFERENCES, MODE_PRIVATE);
        userId = sharedPreferences.getString(Utils.ID, "");

        mImageLoader = new ImageLoader(Volley.newRequestQueue(this.getApplicationContext()), new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(40);

            @Override
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
        });
    }


}
