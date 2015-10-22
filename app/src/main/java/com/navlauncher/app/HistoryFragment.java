package com.navlauncher.app;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.navlauncher.app.favorites.AddFavoriteActivity;
import com.navlauncher.app.favorites.AddFavoriteFragment;
import com.navlauncher.app.favorites.Favorite;

// TODO: Don't directly control the Activity - use a listener
public class HistoryFragment extends ListFragment {
    private static final int MENU_ADD_FAV = 1;
    private static final int MENU_REMOVE = 2;

    private boolean mNeedsReturnVal = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnCreateContextMenuListener(mContextMenuListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();

        repopulateList();
    }

    private void parseIntent() {
        final Intent intent = getActivity().getIntent(); // TODO: Don't control the Activity

        if (intent != null) {
            final String action = intent.getAction();

            if (Intent.ACTION_PICK.equals(action)) {
                mNeedsReturnVal = true;
            }
        }
    }

    private void repopulateList() {
        final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        final List<Destination> destinations = dbHelper.getHistory();

        final HashMap<Integer, Destination> replacements = new HashMap<Integer, Destination>();

        for (final Destination destination : destinations) {
            final Favorite fav = dbHelper.getFavorite(destination.getAddress());

            if (fav != null) {
                final int index = destinations.indexOf(destination);

                replacements.put(index, fav);
            }
        }

        // Replace any favorites with their names (instead of address)
        for (final int index : replacements.keySet()) {
            destinations.remove(index);
            destinations.add(index, replacements.get(index));
        }

        setListAdapter(new ArrayAdapter<Destination>(getActivity(), android.R.layout.simple_list_item_1, destinations.toArray(new Destination[destinations
                .size()])));
    }

    private void launchNavigation(final Destination destination) {
        // Launch Navigation
        final Launcher launcher = new Launcher(getActivity());
        launcher.tryLaunchNavigation(destination.getAddress());
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);

        final Destination destination = (Destination) getListAdapter().getItem(position);

        if (!mNeedsReturnVal) { // Navigate
            launchNavigation(destination);
        } else { // Return the favorite
            final Intent intent = new Intent();
            if (destination instanceof Favorite) { // Add name
                intent.putExtra(AddFavoriteFragment.DATA_NAME, destination.toString());
            }
            intent.putExtra(AddFavoriteFragment.DATA_ADDRESS, destination.getAddress());

            getActivity().setResult(Activity.RESULT_OK, intent); // TODO: Don't control the Activity
        }

        // Close activity
        getActivity().finish(); // TODO: Don't control the Activity
    }

    private final OnCreateContextMenuListener mContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

            final AdapterContextMenuInfo moreInfo = (AdapterContextMenuInfo) menuInfo;

            final Destination destination = (Destination) getListAdapter().getItem(moreInfo.position);
            final String address = destination.getAddress();

            menu.setHeaderTitle(address);

            // Only allow adding as a favorite if it isn't already a favorite
            if (!(destination instanceof Favorite)) {
                menu.add(Menu.NONE, MENU_ADD_FAV, MENU_ADD_FAV, getString(R.string.addFavorite));
            }

            menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, getString(R.string.remove));
        }
    };

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

        final int position = menuInfo.position;
        final Destination destination = (Destination) getListAdapter().getItem(position);

        switch (item.getItemId()) {
        case MENU_ADD_FAV:
            final Intent addFavIntent = new Intent(getActivity(), AddFavoriteActivity.class);
            addFavIntent.putExtra(AddFavoriteFragment.EXTRA_ADDRESS, destination.getAddress());
            startActivity(addFavIntent);
            return true;
        case MENU_REMOVE:
            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            dbHelper.deleteHistory(destination.getId());
            repopulateList();
            return true;
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuClearHistory:
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // TODO: DialogFragment

            builder.setTitle(getString(R.string.clear));
            builder.setMessage(getString(R.string.clearConfirm));
            builder.setPositiveButton(R.string.yes, mClearListener);
            builder.setNegativeButton(R.string.no, mEmptyListener);
            builder.show();

            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    private final DialogInterface.OnClickListener mClearListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            dbHelper.deleteAllHistory();
            repopulateList();
        }
    };

    private final DialogInterface.OnClickListener mEmptyListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            // Take no specific action, just let the dialog close
        }
    };
}
