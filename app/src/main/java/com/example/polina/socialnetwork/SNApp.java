package com.example.polina.socialnetwork;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

/**
 * Created by polina on 04.06.15.
 */
@EApplication
public class SNApp extends Application {
    API api = new ServerAPI();
}
