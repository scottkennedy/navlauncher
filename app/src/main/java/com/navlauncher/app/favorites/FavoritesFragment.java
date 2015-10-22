package com.navlauncher.app.favorites;

import java.util.List;

import android.app.Activity;
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

import com.navlauncher.app.DatabaseHelper;
import com.navlauncher.app.Launcher;
import com.navlauncher.app.R;

public class FavoritesFragment extends ListFragment {
    private static final int MENU_MODIFY = 1;
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
        final List<Favorite> favorites = dbHelper.getFavorites();

        setListAdapter(new ArrayAdapter<Favorite>(getActivity(), android.R.layout.simple_list_item_1, favorites.toArray(new Favorite[favorites.size()])));
    }

    private void launchNavigation(final Favorite destination) {
        // launch Navigation
        final Launcher launcher = new Launcher(getActivity());
        launcher.tryLaunchNavigation(destination.getAddress());
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.favorites_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuAdd:
            final Intent intent = new Intent(getActivity(), AddFavoriteActivity.class);
            startActivity(intent);

            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);

        final Favorite destination = (Favorite) getListAdapter().getItem(position);

        if (!mNeedsReturnVal) { // Navigate
            launchNavigation(destination);
        } else { // Return the favorite
            final Intent intent = new Intent();
            intent.putExtra(AddFavoriteFragment.DATA_NAME, destination.getName());
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

            final Favorite destination = (Favorite) getListAdapter().getItem(moreInfo.position);

            menu.setHeaderTitle(destination.getName());

            menu.add(Menu.NONE, MENU_MODIFY, MENU_MODIFY, getString(R.string.modify));
            menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, getString(R.string.remove));
        }
    };

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

        final int position = menuInfo.position;
        final Favorite destination = (Favorite) getListAdapter().getItem(position);

        switch (item.getItemId()) {
        case MENU_MODIFY:
            final Intent intent = new Intent(getActivity(), AddFavoriteActivity.class);
            intent.putExtra(AddFavoriteFragment.EXTRA_ID, destination.getId());
            startActivity(intent);
            return true;
        case MENU_REMOVE:
            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            dbHelper.deleteFavorite(destination.getId());
            repopulateList();
            return true;
        }

        return false;
    }
}
