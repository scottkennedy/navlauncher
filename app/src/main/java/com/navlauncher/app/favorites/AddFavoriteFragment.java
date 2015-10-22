package com.navlauncher.app.favorites;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.navlauncher.app.ContactSelector;
import com.navlauncher.app.DatabaseHelper;
import com.navlauncher.app.HistoryActivity;
import com.navlauncher.app.R;
import com.navlauncher.app.settings.Settings;

public class AddFavoriteFragment extends Fragment {
    public static final String EXTRA_ID = "extraId";
    public static final String EXTRA_ADDRESS = "extraAddress";
    public static final String EXTRA_QUICK = "quick";

    private static final int SELECT_CONTACT = 1;
    private static final int SELECT_FAVORITE = 2;
    private static final int SELECT_HISTORY = 3;
    private static final int SELECT_IMAGE = 4;

    private static final int MENU_MODIFY = 0;
    private static final int MENU_REMOVE = 1;

    public static final String DATA_NAME = "name";
    public static final String DATA_ADDRESS = "address";

    private boolean mQuick = false;

    private EditText mTxtAddFavName;
    private EditText mTxtAddFavAddress;

    private View mPictureContainer;
    private View mTxtAddQuickFavPictureLabel;
    private ImageView mBtnAddQuickFavPicture;

    private TextView mBtnAddFav;
    private TextView mBtnRemoveFav;

    private int mFavoriteId = -1;

    private Bitmap mPhoto = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Just check if we're adding a Quick Favorite or not
        final Intent intent = getActivity().getIntent(); // TODO: Don't control the Activity

        if (intent != null) {
            mQuick = intent.getBooleanExtra(EXTRA_QUICK, false);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.addfavorite_fragment, container);

        mTxtAddFavName = (EditText) rootView.findViewById(R.id.txtAddFavName);
        mTxtAddFavAddress = (EditText) rootView.findViewById(R.id.txtAddFavAddress);

        mPictureContainer = rootView.findViewById(R.id.AddFavoriteFragment_pictureContainer);
        mTxtAddQuickFavPictureLabel = rootView.findViewById(R.id.txtAddQuickFavPictureLabel);
        mBtnAddQuickFavPicture = (ImageView) rootView.findViewById(R.id.btnAddQuickFavPicture);
        mBtnAddQuickFavPicture.setOnClickListener(mPictureOnClickListener);
        registerForContextMenu(mBtnAddQuickFavPicture);

        mBtnAddFav = (TextView) rootView.findViewById(R.id.btnAddFav);
        mBtnRemoveFav = (TextView) rootView.findViewById(R.id.btnRemoveFav);

        mBtnAddFav.setOnClickListener(mAddOnClickListener);
        mBtnRemoveFav.setOnClickListener(mRemoveOnClickListener);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        parseIntent();
    }

    /**
     * Parses the intent with which the Activity was started, to determine what data should be loaded.
     */
    private void parseIntent() {
        final Intent intent = getActivity().getIntent(); // TODO: Don't control the Activity

        if (intent == null) {
            return;
        }

        mFavoriteId = intent.getIntExtra(EXTRA_ID, -1);

        final String address = intent.getStringExtra(EXTRA_ADDRESS);
        mQuick = intent.getBooleanExtra(EXTRA_QUICK, false);

        if (mQuick) { // Quick favorite
            // Set appropriate title
            getActivity().setTitle(R.string.addQuickFavorite); // TODO: Don't control the Activity

            if ((mFavoriteId < Settings.QUICK_FAV_MIN) || (mFavoriteId > Settings.QUICK_FAV_MAX)) {
                // Something strange is going on
                if (Settings.LOGGING_ENABLED) {
                    Log.e(getClass().getSimpleName(), "mFavoriteId from intent was: " + mFavoriteId);
                }

                getActivity().finish(); // TODO: Don't control the Activity
            }

            final Settings settings = new Settings(getActivity());

            if ("".equals(settings.getQuickFavAddress(mFavoriteId))) { // New destination
                mBtnRemoveFav.setEnabled(false);

                if (address != null) {
                    mTxtAddFavAddress.setText(address);
                }
            } else { // Existing destination
                mBtnAddFav.setText(getString(R.string.modify));
                mBtnRemoveFav.setEnabled(true);

                postPopulateFields(true);
            }
        } else {
            // Hide picture label/button
            if (mPictureContainer != null) {
                mPictureContainer.setVisibility(View.GONE);
            }

            mTxtAddQuickFavPictureLabel.setVisibility(View.GONE);
            mBtnAddQuickFavPicture.setVisibility(View.GONE);
            mPhoto = null;

            if (mFavoriteId == -1) { // New destination
                mBtnRemoveFav.setEnabled(false);

                if (address != null) {
                    mTxtAddFavAddress.setText(address);
                }
            } else { // Existing destination
                mBtnAddFav.setText(getString(R.string.modify));
                mBtnRemoveFav.setEnabled(true);

                populateFields(true);
            }
        }

        getActivity().setIntent(null); // TODO: Don't control the Activity
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (mQuick) {
            inflater.inflate(R.menu.addquickfavorite_menu, menu);
        } else {
            inflater.inflate(R.menu.addfavorite_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuAddContact:
        case R.id.menuAddQuickFavContact:
            final ContactSelector contactSelector = new ContactSelector(this);
            contactSelector.selectContact(SELECT_CONTACT);

            return true;
        case R.id.menuAddQuickFavFavorite:
            final Intent favIntent = new Intent(getActivity(), FavoritesActivity.class);
            favIntent.setAction(Intent.ACTION_PICK);
            startActivityForResult(favIntent, SELECT_FAVORITE);

            return true;
        case R.id.menuAddQuickFavHistory:
            final Intent historyIntent = new Intent(getActivity(), HistoryActivity.class);
            historyIntent.setAction(Intent.ACTION_PICK);
            startActivityForResult(historyIntent, SELECT_FAVORITE);

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
                final ContactSelector contactSelector = new ContactSelector(this);
                final String name = contactSelector.parseResultForName(data);
                final String address = contactSelector.parseResultForAddress(data);

                mTxtAddFavName.setText(name);
                mTxtAddFavAddress.setText(address);

                if (mQuick) {
                    mPhoto = contactSelector.parseResultForPhoto(data);

                    // show the mPhoto
                    if (mPhoto != null) {
                        // display the mPhoto
                        postPopulateFields(false);
                    }
                }
            }
            break;
        case (SELECT_FAVORITE):
            if (resultCode == Activity.RESULT_OK) {
                final String name = data.getStringExtra(DATA_NAME);
                final String address = data.getStringExtra(DATA_ADDRESS);

                if (name != null) {
                    mTxtAddFavName.setText(name);
                }

                if (address != null) {
                    mTxtAddFavAddress.setText(address);
                }
            }
            break;
        case (SELECT_HISTORY):
            if (resultCode == Activity.RESULT_OK) {
                final String address = data.getStringExtra(DATA_ADDRESS);

                if (address != null) {
                    mTxtAddFavAddress.setText(address);
                }
            }
            break;
        case (SELECT_IMAGE):
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    mPhoto = data.getParcelableExtra("data");

                    if (mPhoto == null) {
                        // We didn't get the actual Bitmap data, so let's try to copy the file
                        final Uri imageUri = data.getData();

                        if (imageUri != null) {
                            try {
                                mPhoto = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                            } catch (final FileNotFoundException e) {
                                if (Settings.LOGGING_ENABLED) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                if (mPhoto != null) {
                    postPopulateFields(false);
                } else {
                    // TODO: Show error dialog
                }
            }
            break;
        }
    }

    private void setDefaultAddImage() {
        mPhoto = null; // This should already be null, but just in case, set it here
        mBtnAddQuickFavPicture.setImageResource(android.R.drawable.ic_menu_add);
        mBtnAddQuickFavPicture.setScaleType(ScaleType.CENTER);
    }

    /**
     * Saves the Bitmap in the local storage directory for later use.
     */
    private void saveBitmap() {
        // Delete old mPhoto
        final Settings settings = new Settings(getActivity());
        settings.deleteQuickFavImageFile(mFavoriteId);

        if (mPhoto == null) {
            return;
        }

        final String filename = Settings.getQuickFavImageFileName(mFavoriteId);

        try {
            final FileOutputStream fileOutputSream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);

            mPhoto.compress(Bitmap.CompressFormat.PNG, 100, fileOutputSream);
        } catch (final FileNotFoundException e) {
            if (Settings.LOGGING_ENABLED) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Populates the fields in the Activity after the ImageButton has loaded.
     * 
     * @param reloadData
     *            If this is a Quick Favorite, the text and image will only be reloaded from settings if this parameter is true, to prevent the text from being
     *            cleared on returning from the image select activity. This parameter has no effect for regular favorites.
     */
    private void postPopulateFields(final boolean reloadData) {
        /*
         * Since even in onPostResume(), .getWidth() and .getHeight() can return 0, we'll add use a Runnable and post it to the thread, so that it gets run
         * after the layout has completed.
         */
        final Runnable quickFavReloader = new Runnable() {
            @Override
            public void run() {
                populateFields(reloadData);
            }
        };

        mBtnAddQuickFavPicture.post(quickFavReloader);
    }

    /**
     * Populates the text fields with the data from the settings, and the image currently stored in the class variable (for Quick FavoritesActivity).
     * 
     * @param reloadData
     *            If this is a Quick Favorite, the text and image will only be reloaded from settings if this parameter is true, to prevent the text from being
     *            cleared on returning from the image select activity. This parameter has no effect for regular favorites.
     */
    private void populateFields(final boolean reloadData) {
        if (mQuick) {
            if (reloadData) {
                final Settings settings = new Settings(getActivity());

                mTxtAddFavName.setText(settings.getQuickFavName(mFavoriteId));
                mTxtAddFavAddress.setText(settings.getQuickFavAddress(mFavoriteId));

                // reload image
                try {
                    // get dimensions
                    InputStream inputStream = getActivity().openFileInput(Settings.getQuickFavImageFileName(mFavoriteId));

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();

                    // get scaling factors for width and height
                    final int scaleWidth = (int) Math.ceil((double) options.outWidth / mBtnAddQuickFavPicture.getWidth());
                    final int scaleHeight = (int) Math.ceil((double) options.outHeight / mBtnAddQuickFavPicture.getHeight());

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = Math.max(scaleWidth, scaleHeight);

                    inputStream = getActivity().openFileInput(Settings.getQuickFavImageFileName(mFavoriteId));
                    mPhoto = BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();
                } catch (final FileNotFoundException e) {
                    if (Settings.LOGGING_ENABLED) {
                        e.printStackTrace();
                    }
                } catch (final IOException e) {
                    if (Settings.LOGGING_ENABLED) {
                        e.printStackTrace();
                    }
                }
            }

            if (mPhoto != null) {
                mBtnAddQuickFavPicture.setImageBitmap(mPhoto);
                mBtnAddQuickFavPicture.setScaleType(ScaleType.FIT_CENTER);
            } else {
                setDefaultAddImage();
            }
        } else {
            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            final Favorite favorite = dbHelper.getFavorite(mFavoriteId);

            if (favorite != null) {
                mTxtAddFavName.setText(favorite.getName());
                mTxtAddFavAddress.setText(favorite.getAddress());
            }
        }
    }

    private final OnClickListener mAddOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final String name = mTxtAddFavName.getText().toString();
            final String address = mTxtAddFavAddress.getText().toString();

            if (name.length() == 0 || address.length() == 0) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.enterNameAddress);
                builder.setNeutralButton(getString(android.R.string.ok), mEmptyDialogOnClickListener);
                builder.show();
                return;
            }

            if (mQuick) {
                final Settings settings = new Settings(getActivity());

                settings.putQuickFavName(mFavoriteId, name);
                settings.putQuickFavAddress(mFavoriteId, address);

                saveBitmap();
            } else {
                final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

                if (mFavoriteId == -1) { // New destination
                    dbHelper.addFavorite(name, address);
                } else { // existing destination
                    dbHelper.updateFavorite(mFavoriteId, name, address);
                }
            }

            getActivity().finish(); // TODO: Don't control the Activity
        }
    };

    private final OnClickListener mRemoveOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (mQuick) {
                final Settings settings = new Settings(getActivity());

                settings.putQuickFavName(mFavoriteId, "");
                settings.putQuickFavAddress(mFavoriteId, "");
                settings.deleteQuickFavImageFile(mFavoriteId);
                mPhoto = null;
            } else {
                if (mFavoriteId >= 0) {
                    final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                    dbHelper.deleteFavorite(mFavoriteId);
                }
            }

            getActivity().finish(); // TODO: Don't control the Activity
        }
    };

    private final DialogInterface.OnClickListener mEmptyDialogOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            // Take no specific action, just let the dialog close
        }
    };

    private void choosePicture() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        // intent.putExtra("crop", "true"); // TODO: Figure this out
        intent.putExtra("return-data", true);
        startActivityForResult(intent, SELECT_IMAGE);
    }

    private final OnClickListener mPictureOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            choosePicture();
        }
    };

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (mPhoto != null) {
            menu.setHeaderTitle(R.string.favoritePicture);

            menu.add(Menu.NONE, MENU_MODIFY, MENU_MODIFY, getString(R.string.modify));
            menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, getString(R.string.remove));
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final Settings settings = new Settings(getActivity());

        switch (item.getItemId()) {
        case MENU_MODIFY:
            choosePicture();
            return true;
        case MENU_REMOVE:
            settings.deleteQuickFavImageFile(mFavoriteId);
            mPhoto = null;
            populateFields(false);
            return true;
        }

        return false;
    }
}
