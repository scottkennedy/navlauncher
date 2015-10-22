package com.navlauncher.app;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {
    private FakeLocationListener mFakeLocationListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFakeLocationListener = new FakeLocationListener((LocationManager) getSystemService(LOCATION_SERVICE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFakeLocationListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mFakeLocationListener.stop();
    }
}
