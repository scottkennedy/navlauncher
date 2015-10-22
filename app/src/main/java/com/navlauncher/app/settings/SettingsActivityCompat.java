package com.navlauncher.app.settings;

import android.app.Activity;
import android.app.Fragment;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.navlauncher.app.FakeLocationListener;
import com.navlauncher.app.R;

/**
 * An {@link Activity} for the settings, using the native {@link PreferenceActivity} and no {@link Fragment}s. This class should only be used prior to Android
 * 3.0.
 */
public class SettingsActivityCompat extends PreferenceActivity {
    private FakeLocationListener mFakeLocationListener;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

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
