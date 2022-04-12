package com.iljak.android_bluetooth_serial;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application {

    private static final String LOG_TAG = MainApplication.class.getSimpleName();

    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "onCreate");


    }
}
