package com.navlauncher.app;

import com.navlauncher.app.settings.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class DockEventReceiver extends BroadcastReceiver {
    private final Handler mHandler = new Handler();
    private static final long LAUNCH_DELAY = 3000;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Settings settings = new Settings(context);

        if (settings.isCarDockReplacement()) {
            final int dockState = intent.getExtras().getInt(Intent.EXTRA_DOCK_STATE);

            switch (dockState) {
            // case Intent.EXTRA_DOCK_STATE_DESK: //TxODO: remove this when not testing
            case Intent.EXTRA_DOCK_STATE_CAR:
                // Start Nav Launcher
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        final Intent startIntent = new Intent(context, MainActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        context.startActivity(startIntent);
                    }
                };

                mHandler.postDelayed(runnable, LAUNCH_DELAY);
                break;
            default:
                // Take no action
                break;
            }
        }
    }

}
