package com.kf5sdk.exam.app;

import android.app.Application;

import com.kf5.sdk.system.init.KF5SDKInitializer;

public class KF5Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KF5SDKInitializer.init(getApplicationContext());
    }
}
