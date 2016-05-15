package com.cyanflxy.matrix.geometry;

import android.app.Application;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Settings.init(this.getApplicationContext());
    }
}
