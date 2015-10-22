package com.navlauncher.app;

import android.app.Activity;

public abstract class NewUiFeatures {
    public static void setHomeAsUp(final Activity activity) {
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
