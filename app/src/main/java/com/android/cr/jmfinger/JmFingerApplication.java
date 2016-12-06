package com.android.cr.jmfinger;

import android.app.Application;

/**
 * Created by chaoranf on 16/12/6.
 */

public class JmFingerApplication extends Application {
    public static Application appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
