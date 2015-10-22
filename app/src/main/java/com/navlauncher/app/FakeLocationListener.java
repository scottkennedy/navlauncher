package com.navlauncher.app;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class FakeLocationListener implements LocationListener {
    private final LocationManager mLocationManager;

    public FakeLocationListener(final LocationManager locationManager) {
        super();
        mLocationManager = locationManager;
    }

    public void start() {
        if (mLocationManager.getProviders(true).contains(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void stop() {
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        // do nothing
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // do nothing
    }

    @Override
    public void onProviderEnabled(final String provider) {
        // do nothing
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        // do nothing
    }
}
