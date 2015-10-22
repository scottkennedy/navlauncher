package com.navlauncher.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.navlauncher.app.favorites.AddFavoriteActivity;
import com.navlauncher.app.favorites.AddFavoriteFragment;
import com.navlauncher.app.favorites.Favorite;
import com.navlauncher.app.favorites.FavoritesActivity;
import com.navlauncher.app.settings.Settings;

public class NavLauncherFragment extends Fragment {
    private static final int SELECT_CONTACT = 1;
    private static final int VOICE_SEARCH = 2;

    private static final int MENU_ADD_MODIFY = 1;
    private static final int MENU_REMOVE = 2;

    private final ImageButton[] mBtnQuickFav = new ImageButton[Settings.QUICK_FAV_MAX + 1];
    private final TextView[] mTxtQuickFav = new TextView[Settings.QUICK_FAV_MAX + 1];

    // TODO: there must be a better way to pass this around than its own variable
    private int mQuickFavPressedId = 0;

    private EditText mTxtAddress;

    private boolean mStartupScreenLaunched = false; // true when startup screen has been launched

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.navlauncher_fragment, container);

        // Grab references
        mBtnQuickFav[0] = (ImageButton) rootView.findViewById(R.id.btnQuickFav0);
        mBtnQuickFav[1] = (ImageButton) rootView.findViewById(R.id.btnQuickFav1);

        mTxtQuickFav[0] = (TextView) rootView.findViewById(R.id.txtQuickFav0);
        mTxtQuickFav[1] = (TextView) rootView.findViewById(R.id.txtQuickFav1);

        final View contactsButton = rootView.findViewById(R.id.btnContacts);
        final View favoritesButton = rootView.findViewById(R.id.btnFavorites);
        final View historyButton = rootView.findViewById(R.id.btnHistory);

        final View voiceSearchButton = rootView.findViewById(R.id.btnVoiceSearch);
        final View navigateButton = rootView.findViewById(R.id.btnNavigate);

        mTxtAddress = (EditText) rootView.findViewById(R.id.txtAddress);

        // Set OnClickListeners
        mBtnQuickFav[0].setOnClickListener(mQuickFavOnClickListener);
        mBtnQuickFav[1].setOnClickListener(mQuickFavOnClickListener);
        registerForContextMenu(mBtnQuickFav[0]);
        registerForContextMenu(mBtnQuickFav[1]);

        contactsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new ContactSelector(NavLauncherFragment.this).selectContact(SELECT_CONTACT);
            }
        });

        voiceSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                startActivityForResult(intent, VOICE_SEARCH);
            }
        });

        navigateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                navigate();
            }
        });

        favoritesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showFavorites();
            }
        });

        historyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showHistory();
            }
        });

        // Fix for voice recognition issues
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> info = packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (info.isEmpty()) { // Can't do voice recognition
            voiceSearchButton.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Open settings activity if settings are missing
        final Settings settings = new Settings(getActivity());

        // Go to desired starting screen if settings screen isn't going to pop up
        if (!mStartupScreenLaunched) {
            final String startupScreen = settings.getStartupScreen();

            if (startupScreen.equals(Settings.STARTUP_SCREEN_CONTACTS)) {
                new ContactSelector(NavLauncherFragment.this).selectContact(SELECT_CONTACT);
            } else if (startupScreen.equals(Settings.STARTUP_SCREEN_FAVORITES)) {
                showFavorites();
            } else if (startupScreen.equals(Settings.STARTUP_SCREEN_HISTORY)) {
                showHistory();
            }

            mStartupScreenLaunched = true;
        }

        /*
         * Since even in onPostResume(), .getWidth() and .getHeight() can return 0, we'll add use a Runnable and post it to the thread, so that it gets run
         * after the layout has completed.
         */
        final Runnable quickFavReloader = new Runnable() {
            @Override
            public void run() {
                reloadQuickFavs();
            }
        };

        mBtnQuickFav[Settings.QUICK_FAV_MIN].post(quickFavReloader);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.navlauncher_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuSettings:
            final Intent settingsIntent = new Intent(getActivity(), Settings.getSettingsActivityClass());
            startActivity(settingsIntent);

            return true;
        case R.id.menuAbout:
            final Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
            startActivity(aboutIntent);

            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case (SELECT_CONTACT):
            if (resultCode == Activity.RESULT_OK) {
                final String address = new ContactSelector(NavLauncherFragment.this).parseResultForAddress(data);
                mTxtAddress.setText(address);

                if (!"".equals(address)) {
                    navigate();
                }
            }
            break;
        case (VOICE_SEARCH):
            if (resultCode == Activity.RESULT_OK) {
                final ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (results != null) {
                    if (results.size() > 1) {
                        // TODO: display a list in an AlertDialog if there are multiple results
                        mTxtAddress.setText(results.get(0));
                    } else if (results.size() == 1) {
                        final String result = results.get(0);

                        // If the result matches the name of a favourite, use the favourite
                        final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                        final List<Favorite> favorites = dbHelper.getFavorites();

                        boolean wasFav = false;

                        for (final Favorite fav : favorites) {
                            if (fav.getName().equalsIgnoreCase(result)) {
                                wasFav = true;
                                mTxtAddress.setText(fav.getAddress());
                            }
                        }

                        if (!wasFav) {
                            mTxtAddress.setText(result);
                        }
                    }
                }
            }
            break;
        }
    }

    private void navigate() {
        final String address = mTxtAddress.getText().toString();

        // Launch Navigation
        final Launcher launcher = new Launcher(getActivity());
        launcher.tryLaunchNavigation(address);
    }

    private void showFavorites() {
        final Intent intent = new Intent(getActivity(), FavoritesActivity.class);
        startActivity(intent);
    }

    private void showHistory() {
        final Intent intent = new Intent(getActivity(), HistoryActivity.class);
        startActivity(intent);
    }

    private static void setDefaultAddImage(final ImageButton button) {
        button.setImageResource(android.R.drawable.ic_menu_add);
        button.setScaleType(ScaleType.CENTER);
    }

    private void reloadQuickFavs() {
        for (int id = Settings.QUICK_FAV_MIN; id <= Settings.QUICK_FAV_MAX; id++) {
            reloadQuickFav(id);
        }
    }

    private void reloadQuickFav(final int id) {
        final Settings settings = new Settings(getActivity());

        final String name = settings.getQuickFavName(id);

        if ("".equals(name)) {
            mTxtQuickFav[id].setText(R.string.add);
            setDefaultAddImage(mBtnQuickFav[id]);
            mBtnQuickFav[id].setContentDescription(getString(R.string.add));
        } else {
            mTxtQuickFav[id].setText(name);
            mBtnQuickFav[id].setContentDescription(name);

            try {
                // Get dimensions
                InputStream inputStream = getActivity().openFileInput(Settings.getQuickFavImageFileName(id));

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                // Get scaling factors for width and height
                final int scaleWidth = (int) Math.ceil((double) options.outWidth / mBtnQuickFav[id].getWidth());
                final int scaleHeight = (int) Math.ceil((double) options.outHeight / mBtnQuickFav[id].getHeight());

                options.inJustDecodeBounds = false;
                options.inSampleSize = Math.max(scaleWidth, scaleHeight);

                inputStream = getActivity().openFileInput(Settings.getQuickFavImageFileName(id));
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                if (bitmap != null) {
                    mBtnQuickFav[id].setImageBitmap(bitmap);
                    mBtnQuickFav[id].setScaleType(ScaleType.FIT_CENTER);
                } else {
                    setDefaultAddImage(mBtnQuickFav[id]);

                    if (Settings.LOGGING_ENABLED) {
                        Log.w(getClass().getSimpleName(), "bitmap was null for id: " + id);
                    }
                }
            } catch (final FileNotFoundException e) {
                if (Settings.LOGGING_ENABLED) {
                    e.printStackTrace();
                }

                setDefaultAddImage(mBtnQuickFav[id]);
            } catch (final IOException e) {
                if (Settings.LOGGING_ENABLED) {
                    e.printStackTrace();
                }

                setDefaultAddImage(mBtnQuickFav[id]);
            }
        }
    }

    private void quickFav(final int id) {
        if ((id < Settings.QUICK_FAV_MIN) || (id > Settings.QUICK_FAV_MAX)) {
            return;
        }

        final Settings settings = new Settings(getActivity());

        final String quickFavAddress = settings.getQuickFavAddress(id);

        final String intentPrefix = "intent:";

        if ("".equals(quickFavAddress)) {
            // Add a new quick favorite
            final Intent addQuickFavIntent = new Intent(getActivity(), AddFavoriteActivity.class);
            addQuickFavIntent.putExtra(AddFavoriteFragment.EXTRA_ID, id);
            addQuickFavIntent.putExtra(AddFavoriteFragment.EXTRA_ADDRESS, mTxtAddress.getText().toString());
            addQuickFavIntent.putExtra(AddFavoriteFragment.EXTRA_QUICK, true);
            startActivity(addQuickFavIntent);
        } else if (quickFavAddress.startsWith(intentPrefix)) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(quickFavAddress.substring(intentPrefix.length())));

            startActivity(intent);
        } else {
            // Navigate to the quick favorite
            final Launcher launcher = new Launcher(getActivity());
            launcher.tryLaunchNavigation(quickFavAddress);
        }
    }

    private final OnClickListener mQuickFavOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final int id = getQuickFavIdFromImageButtonView(v);

            if (id == -1) { // Something strange happened
                if (Settings.LOGGING_ENABLED) {
                    Log.e(getClass().getSimpleName(), "context menu was created from something other than a QuickFav button");
                }

                return;
            }

            quickFav(id);
        }
    };

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        final int id = getQuickFavIdFromImageButtonView(v);

        if (id == -1) { // Something strange happened
            if (Settings.LOGGING_ENABLED) {
                Log.e(getClass().getSimpleName(), "context menu was created from something other than a QuickFav button");
            }

            return;
        }

        mQuickFavPressedId = id;

        final Settings settings = new Settings(getActivity());

        final String name = settings.getQuickFavName(id);

        if ("".equals(name)) {
            menu.setHeaderTitle(R.string.addQuickFavorite);

            menu.add(Menu.NONE, MENU_ADD_MODIFY, MENU_ADD_MODIFY, getString(R.string.addQuickFavorite));
        } else {
            menu.setHeaderTitle(name);

            menu.add(Menu.NONE, MENU_ADD_MODIFY, MENU_ADD_MODIFY, getString(R.string.modify));
            menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, getString(R.string.remove));
        }
    }

    private int getQuickFavIdFromImageButtonView(final View view) {
        int id = -1;

        for (int i = Settings.QUICK_FAV_MIN; i <= Settings.QUICK_FAV_MAX; i++) {
            if (view == mBtnQuickFav[i]) {
                id = i;
            }
        }

        return id;
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final Settings settings = new Settings(getActivity());

        switch (item.getItemId()) {
        case MENU_ADD_MODIFY:
            final Intent intent = new Intent(getActivity(), AddFavoriteActivity.class);
            intent.putExtra(AddFavoriteFragment.EXTRA_ID, mQuickFavPressedId);
            intent.putExtra(AddFavoriteFragment.EXTRA_ADDRESS, mTxtAddress.getText().toString());
            intent.putExtra(AddFavoriteFragment.EXTRA_QUICK, true);
            startActivity(intent);
            return true;
        case MENU_REMOVE:
            settings.putQuickFavName(mQuickFavPressedId, "");
            settings.putQuickFavAddress(mQuickFavPressedId, "");
            settings.deleteQuickFavImageFile(mQuickFavPressedId);
            reloadQuickFavs();
            return true;
        }

        return false;
    }
}
