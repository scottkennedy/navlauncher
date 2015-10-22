package com.navlauncher.app;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;

// TODO: Since we no longer have multiple versions of this, can we build it in a way that it doesn't need a reference to an Activity or Fragment?
public class ContactSelector {
    private final Fragment mFragment;

    public ContactSelector(final Fragment fragment) {
        super();

        mFragment = fragment;
    }

    /**
     * Displays the select contact activity.
     * 
     * @param requestCode
     *            The requestCode to include in the result.
     */
    public void selectContact(final int requestCode) {
        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
        mFragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Parses the Intent from the select contact Activity and returns the specified data.
     * 
     * @param data
     *            The Intent received from the activity.
     * @return The specified data for the selected contact.
     */
    private String parseResultForField(final Intent data, final String columnName) {
        final Uri contactUri = data.getData();
        final Cursor cursor = mFragment.getActivity().getContentResolver().query(contactUri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final String value = cursor.getString(cursor.getColumnIndex(columnName));
                cursor.close();

                return value;
            }

            cursor.close();
        }

        return "";
    }

    /**
     * Parses the Intent from the select contact Activity and returns the address.
     * 
     * @param data
     *            The Intent received from the activity.
     * @return The address for the selected contact.
     */
    public String parseResultForAddress(final Intent data) {
        return parseResultForField(data, ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
    }

    /**
     * Parses the Intent from the select contact Activity and returns the name.
     * 
     * @param data
     *            The Intent received from the activity.
     * @return The name for the selected contact.
     */
    public String parseResultForName(final Intent data) {
        return parseResultForField(data, ContactsContract.Contacts.DISPLAY_NAME);
    }

    /**
     * Parses the Intent from the select contact Activity and returns a Bitmap of the contact's photo.
     * 
     * @param data
     *            The Intent received from the activity.
     * @return A Bitmap of the contact's photo, or null if the contact has no photo.
     */
    public Bitmap parseResultForPhoto(final Intent data) {
        final Uri contactUri = data.getData();
        final Cursor cursor = mFragment.getActivity().getContentResolver().query(contactUri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final long photoId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));

                cursor.close();

                return getContactPhoto(mFragment.getActivity().getContentResolver(), photoId);
            }

            cursor.close();
            return null;
        }

        return null;
    }

    /**
     * Finds the photo that belongs to the associated id, and returns its Bitmap.
     * 
     * @param cr
     *            A ContentResolver
     * @param photoId
     *            The photo ID to retrieve
     * @return The desired Bitmap, or null if the photo could not be loaded
     */
    private static Bitmap getContactPhoto(final ContentResolver cr, final long photoId) {
        Bitmap photo = null;

        final String where = ContactsContract.Data.PHOTO_ID + " = ? and " + ContactsContract.Data.MIMETYPE + " = ?";

        final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, where, new String[] {
                String.valueOf(photoId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO);
                    final byte[] blob = cursor.getBlob(index);
                    if (blob != null) {
                        photo = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                        break;
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        return photo;
    }
}
