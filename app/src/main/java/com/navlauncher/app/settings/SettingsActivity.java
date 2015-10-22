package com.navlauncher.app.settings;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.navlauncher.app.FakeLocationListener;
import com.navlauncher.app.MainActivity;
import com.navlauncher.app.NewUiFeatures;
import com.navlauncher.app.R;

/**
 * An {@link Activity} for the settings, using the native {@link Activity} and {@link android.app.Fragment}. This class must not be used prior to Android 3.0.
 */
public class SettingsActivity extends Activity {
    private FakeLocationListener mFakeLocationListener;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mFakeLocationListener = new FakeLocationListener((LocationManager) getSystemService(LOCATION_SERVICE));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            NewUiFeatures.setHomeAsUp(this);
        }
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            /*
             * By default, go all the way up. Subclasses can override this, testing for the same menu id, to go somewhere else.
             */
            final Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
