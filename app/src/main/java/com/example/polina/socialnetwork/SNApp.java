package com.example.polina.socialnetwork;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.webkit.CookieSyncManager;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.EApplication;

/**
 * Created by polina on 04.06.15.
 */
@EApplication
public class SNApp extends Application {
    API api = new ServerAPI();
    ImageLoader mImageLoader;


    @Override
    public void onCreate() {
        CookieSyncManager.createInstance(getApplicationContext());
        super.onCreate();
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
