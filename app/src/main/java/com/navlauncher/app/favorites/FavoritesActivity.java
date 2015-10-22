package com.navlauncher.app.favorites;

import android.os.Bundle;

import com.navlauncher.app.HomeUpActivity;
import com.navlauncher.app.R;

public class FavoritesActivity extends HomeUpActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);
    }
}
