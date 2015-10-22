package com.navlauncher.app;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class Launcher {
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";

    private final Context mContext;

    public Launcher(final Context context) {
        super();
        mContext = context;
    }

    public void tryLaunchNavigation(final String address) {
        try {
            launchNavigation(address);
        } catch (final ActivityNotFoundException e) {
            handleMapsNotInstalled();
        }
    }

    private void launchNavigation(final String address) throws ActivityNotFoundException {
        final ComponentName name = new ComponentName(MAPS_PACKAGE, "com.google.android.maps.driveabout.app.NavigationActivity");

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + address + "&dirflg=d&nav=1"));
        intent.setComponent(name);

        /*
         * History: To ensure the most recently used destinations appear first, remove it and then add it again to the table.
         */
        // remove entry from history
        final DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        dbHelper.deleteHistory(address);

        // add entry to history
        dbHelper.addHistory(address);

        mContext.startActivity(intent);
    }

    private void handleMapsNotInstalled() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(mContext.getString(R.string.installNav));
        builder.setMessage(mContext.getString(R.string.installInfo));
        builder.setPositiveButton(R.string.yes, mInstallNavListener);
        builder.setNegativeButton(R.string.no, mEmptyListener);
        builder.show();
    }

    private final DialogInterface.OnClickListener mInstallNavListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:com.google.android.apps.maps"));
            mContext.startActivity(intent);
        }
    };

    private final DialogInterface.OnClickListener mEmptyListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            // take no specific action, just let the dialog close
        }
    };
}
