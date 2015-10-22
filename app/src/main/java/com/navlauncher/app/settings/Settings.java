package com.navlauncher.app.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.navlauncher.app.R;

public class Settings {
    public static final boolean LOGGING_ENABLED = true;

    private static final String STARTUP_SCREEN_MAIN = "0";
    public static final String STARTUP_SCREEN_CONTACTS = "1";
    public static final String STARTUP_SCREEN_FAVORITES = "2";
    public static final String STARTUP_SCREEN_HISTORY = "3";

    private static final String QUICK_FAV_NAME = "quickFavName";
    private static final String QUICK_FAV_ADDRESS = "quickFavAddress";
    private static final String QUICK_FAV_IMAGE_FILE = "quickFav";

    public static final int QUICK_FAV_MIN = 0;
    public static final int QUICK_FAV_MAX = 1;

    private final Context mContext;

    public Settings(final Context context) {
        super();
        mContext = context.getApplicationContext();
    }

    /**
     * Gets the proper Settings {@link Activity} to launch, based on the device's version of Android.
     * 
     * @return A {@link Class} reference to the proper {@link Activity}
     */
    public static Class<? extends Activity> getSettingsActivityClass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return SettingsActivity.class;
        }

        return SettingsActivityCompat.class;
    }

    public String getStartupScreen() {
        final String setting = getStringSetting(mContext.getString(R.string.startupScreenSetting), STARTUP_SCREEN_MAIN);

        if (setting.equals(STARTUP_SCREEN_MAIN) || setting.equals(STARTUP_SCREEN_CONTACTS) || setting.equals(STARTUP_SCREEN_FAVORITES)
                || setting.equals(STARTUP_SCREEN_HISTORY)) {
            return setting;
        }

        return STARTUP_SCREEN_MAIN;
    }

    public String getQuickFavName(final int id) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return "";
        }

        return getStringSetting(QUICK_FAV_NAME + id, mContext.getString(R.string.add));
    }

    public void putQuickFavName(final int id, final String name) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return;
        }

        putStringSetting(QUICK_FAV_NAME + id, name);
    }

    public String getQuickFavAddress(final int id) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return "";
        }

        return getStringSetting(QUICK_FAV_ADDRESS + id, "");
    }

    public void putQuickFavAddress(final int id, final String address) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return;
        }

        putStringSetting(QUICK_FAV_ADDRESS + id, address);
    }

    public static String getQuickFavImageFileName(final int id) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return null;
        }

        return QUICK_FAV_IMAGE_FILE + id;
    }

    /**
     * Deletes the image file for a Quick Favorite.
     * 
     * @param id
     *            The ID of the Quick Favorite to delete.
     */
    public void deleteQuickFavImageFile(final int id) {
        if ((id < QUICK_FAV_MIN) || (id > QUICK_FAV_MAX)) {
            return;
        }

        mContext.deleteFile(QUICK_FAV_IMAGE_FILE + id);
    }

    /**
     * Checks if Nav Launcher is configured to replace the CarDock application
     */
    public boolean isCarDockReplacement() {
        return getBooleanSetting(mContext.getString(R.string.replaceCarDockSetting), false);
    }

    private String getStringSetting(final String key, final String defValue) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

        return settings.getString(key, defValue);
    }

    private void putStringSetting(final String key, final String value) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putString(key, value);

        editor.commit();
    }

    private boolean getBooleanSetting(final String key, final boolean defValue) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

        return settings.getBoolean(key, defValue);
    }
}
