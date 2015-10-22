package com.navlauncher.app;

import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;

public class HomeUpActivity extends BaseActivity {
    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            NewUiFeatures.setHomeAsUp(this);
        }
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
