package com.navlauncher.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

public abstract class NewUiFeatures {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setHomeAsUp(final Activity activity) {
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
