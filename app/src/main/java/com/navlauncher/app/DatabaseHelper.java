package com.navlauncher.app;

import java.util.ArrayList;
import java.util.List;

import com.navlauncher.app.favorites.Favorite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "NavLauncher";
    private static final int DB_VERSION = 1;

    private static final String FAVORITES_TABLE = "Favorites";
    private static final String HISTORY_TABLE = "History";

    public DatabaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FAVORITES_TABLE + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT);");
        db.execSQL("CREATE TABLE " + HISTORY_TABLE + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, address TEXT);");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // No new versions yet
    }

    public List<Favorite> getFavorites() {
        final SQLiteDatabase db = getReadableDatabase();
        final List<Favorite> favorites = new ArrayList<>();

        final String orderBy = FavoritesColumns.NAME;

        final Cursor cursor = db.query(FAVORITES_TABLE, null, null, null, null, null, orderBy);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final int id = cursor.getInt(cursor.getColumnIndex(FavoritesColumns._ID));
                    final String name = cursor.getString(cursor.getColumnIndex(FavoritesColumns.NAME));
                    final String address = cursor.getString(cursor.getColumnIndex(FavoritesColumns.ADDRESS));

                    final Favorite favorite = new Favorite(id, name, address);

                    favorites.add(favorite);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();

        return favorites;
    }

    public Favorite getFavorite(final int id) {
        final SQLiteDatabase db = getReadableDatabase();
        final Favorite favorite;

        final String selection = FavoritesColumns._ID + " = " + id;

        final Cursor cursor = db.query(FAVORITES_TABLE, null, selection, null, null, null, FavoritesColumns.NAME);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final String name = cursor.getString(cursor.getColumnIndex(FavoritesColumns.NAME));
                final String address = cursor.getString(cursor.getColumnIndex(FavoritesColumns.ADDRESS));

                favorite = new Favorite(id, name, address);
            } else {
                favorite = null;
            }

            cursor.close();
        } else {
            favorite = null;
        }

        db.close();

        return favorite;
    }

    /**
     * Checks if the specified address is already saved as a favorite.
     * 
     * @param address
     *            The address to check.
     * @return the Favorite if it exists, null otherwise.
     */
    public Favorite getFavorite(final String address) {
        final SQLiteDatabase db = getReadableDatabase();
        final Favorite favorite;

        final String selection = FavoritesColumns.ADDRESS + " LIKE ?";
        final String[] selectionArgs = { address };

        final Cursor cursor = db.query(FAVORITES_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int favId = cursor.getInt(cursor.getColumnIndex(FavoritesColumns._ID));
                final String favName = cursor.getString(cursor.getColumnIndex(FavoritesColumns.NAME));
                final String favAddress = cursor.getString(cursor.getColumnIndex(FavoritesColumns.ADDRESS));

                favorite = new Favorite(favId, favName, favAddress);
            } else {
                favorite = null;
            }

            cursor.close();
        } else {
            favorite = null;
        }

        db.close();

        return favorite;
    }

    public long addFavorite(final String name, final String address) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(FavoritesColumns.NAME, name);
        values.put(FavoritesColumns.ADDRESS, address);

        final long id = db.insert(FAVORITES_TABLE, null, values);

        db.close();

        return id;
    }

    public int updateFavorite(final int id, final String name, final String address) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(FavoritesColumns.NAME, name);
        values.put(FavoritesColumns.ADDRESS, address);

        final int result = db.update(FAVORITES_TABLE, values, "_id = ?", new String[] { String.valueOf(id) });

        db.close();

        return result;
    }

    public int deleteFavorite(final int id) {
        final SQLiteDatabase db = getWritableDatabase();
        final int result = db.delete(FAVORITES_TABLE, FavoritesColumns._ID + " = ?", new String[] { String.valueOf(id) });
        db.close();

        return result;
    }

    public List<Destination> getHistory() {
        final SQLiteDatabase db = getReadableDatabase();
        final List<Destination> destinations = new ArrayList<>();

        final String orderBy = HistoryColumns._ID + " DESC";

        final Cursor cursor = db.query(HISTORY_TABLE, null, null, null, null, null, orderBy);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final int id = cursor.getInt(cursor.getColumnIndex(HistoryColumns._ID));
                    final String address = cursor.getString(cursor.getColumnIndex(HistoryColumns.ADDRESS));

                    final Destination destination = new Destination(id, address);

                    destinations.add(destination);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();

        return destinations;
    }

    public long addHistory(final String address) {
        final SQLiteDatabase db = getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(HistoryColumns.ADDRESS, address);

        final long id = db.insert(HISTORY_TABLE, null, values);

        db.close();

        return id;
    }

    public int deleteHistory(final int id) {
        final SQLiteDatabase db = getWritableDatabase();
        final int result = db.delete(HISTORY_TABLE, HistoryColumns._ID + " = ?", new String[] { String.valueOf(id) });
        db.close();

        return result;
    }

    public int deleteHistory(final String address) {
        final SQLiteDatabase db = getWritableDatabase();
        final int result = db.delete(HISTORY_TABLE, HistoryColumns.ADDRESS + " LIKE ?", new String[] { address });
        db.close();

        return result;
    }

    public int deleteAllHistory() {
        final SQLiteDatabase db = getWritableDatabase();
        final int result = db.delete(HISTORY_TABLE, null, null);
        db.close();

        return result;
    }

    private interface DestinationColumns extends BaseColumns {
        String ADDRESS = "address";
    }

    public interface FavoritesColumns extends DestinationColumns {
        String NAME = "name";
    }

    public interface HistoryColumns extends DestinationColumns {
        // we don't need anything extra here
    }
}
