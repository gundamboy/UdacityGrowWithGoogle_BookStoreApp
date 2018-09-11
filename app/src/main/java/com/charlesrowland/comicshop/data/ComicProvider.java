package com.charlesrowland.comicshop.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.charlesrowland.comicshop.R;

public class ComicProvider extends ContentProvider {

    // URI matcher code for the content URI for the comicbookstable
    private static final int COMICBOOKS = 100;

    // URI matcher for the content URI for a single comicbooks row in the table
    private static final int COMICBOOKS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ComicContract.CONTENT_AUTHORITY, ComicContract.ComicEntry.TABLE_NAME, COMICBOOKS);
        sUriMatcher.addURI(ComicContract.CONTENT_AUTHORITY, ComicContract.ComicEntry.TABLE_NAME + "/#", COMICBOOKS_ID);
    }

    private ComicDbHelper mDbHelper;

    // empty constructor
    public ComicProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case COMICBOOKS:
                rowsDeleted = db.delete(ComicContract.ComicEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Delete all rows that match the selection and selection args
                return rowsDeleted;
            case COMICBOOKS_ID:
                // Delete a single row given by the ID in the URI
                selection = ComicContract.ComicEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = db.delete(ComicContract.ComicEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMICBOOKS:
                return ComicContract.ComicEntry.CONTENT_LIST_TYPE;
            case COMICBOOKS_ID:
                return ComicContract.ComicEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMICBOOKS:
                return insertComic(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertComic(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String volume = values.getAsString(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME );
        String name = values.getAsString(ComicContract.ComicEntry.COLUMN_COMIC_NAME );
        Integer issue_num = values.getAsInteger(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER );
        String release_date = values.getAsString(ComicContract.ComicEntry.COLUMN_RELEASE_DATE );
        String cover_type = values.getAsString(ComicContract.ComicEntry.COLUMN_COVER_TYPE );
        Double price = values.getAsDouble(ComicContract.ComicEntry.COLUMN_PRICE );
        Integer quantity = values.getAsInteger(ComicContract.ComicEntry.COLUMN_QUANTITY );
        String publisher = values.getAsString(ComicContract.ComicEntry.COLUMN_PUBLISHER );
        String supplier_name = values.getAsString(ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME );
        String supplier_phone = values.getAsString(ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE );

        if (volume == null || volume.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.volume_error));
            return null;
        }

        if (issue_num == null || issue_num < 0) {
            showErrorToast(getContext().getResources().getString(R.string.issue_num_error));
            return null;
        }

        if (release_date == null || release_date.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.date_error));
            return null;
        }

        if (cover_type == null || cover_type.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.volume_error));
            return null;
        }

        if (quantity == null || quantity < 0) {
            showErrorToast(getContext().getResources().getString(R.string.quantity_error));
            return null;
        }

        if (publisher == null || publisher.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.publisher_error));
            return null;
        }

        if (supplier_name == null || supplier_name.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.supplier_error));
            return null;
        }

        if (supplier_phone == null || supplier_phone.isEmpty()) {
            showErrorToast(getContext().getResources().getString(R.string.phone_error));
            return null;
        }

        long id = db.insert(ComicContract.ComicEntry.TABLE_NAME, null, values);

        if (id == -1) {
            showErrorToast(getContext().getResources().getString(R.string.editor_insert_comic_failed));
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public boolean onCreate() {
        // initialize a ComicDbHelper object
        mDbHelper = new ComicDbHelper((getContext()));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match= sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case COMICBOOKS:
                cursor = db.query(ComicContract.ComicEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case COMICBOOKS_ID:
                selection = ComicContract.ComicEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ComicContract.ComicEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Cannot query unknown URI: " + uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match= sUriMatcher.match(uri);

        switch (match) {
            case COMICBOOKS:
                return updateComic(uri, values, selection, selectionArgs);
            case COMICBOOKS_ID:
                selection = ComicContract.ComicEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateComic(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateComic(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME)) {
            String volume = values.getAsString(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME);
            if (volume == null) {
                showErrorToast(getContext().getResources().getString(R.string.volume_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER)) {
            Integer issue_number = values.getAsInteger(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER);
            if (issue_number == null) {
                showErrorToast(getContext().getResources().getString(R.string.issue_num_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_RELEASE_DATE)) {
            String release_date = values.getAsString(ComicContract.ComicEntry.COLUMN_RELEASE_DATE);
            if (release_date == null) {
                showErrorToast(getContext().getResources().getString(R.string.date_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(ComicContract.ComicEntry.COLUMN_PRICE);
            if (price == null) {
                showErrorToast(getContext().getResources().getString(R.string.price_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_PUBLISHER)) {
            String publisher = values.getAsString(ComicContract.ComicEntry.COLUMN_PUBLISHER);
            if (publisher == null) {
                showErrorToast(getContext().getResources().getString(R.string.publisher_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier_name = values.getAsString(ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME);
            if (supplier_name == null) {
                showErrorToast(getContext().getResources().getString(R.string.supplier_error));
            }
        }

        if (values.containsKey(ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE)) {
            String phone = values.getAsString(ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE);
            if (phone == null) {
                showErrorToast(getContext().getResources().getString(R.string.phone_error));
            }
        }

        if (values.size() == 0) {
            showErrorToast(getContext().getResources().getString(R.string.editor_update_comic_failed));
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ComicContract.ComicEntry.TABLE_NAME, values, selection, selectionArgs);

        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private void showErrorToast(String field_name) {
        //String error_message  = getContext().getResources().getString(R.string.error_toast, field_name);
        String error_message = field_name;
        Toast toast = Toast.makeText(getContext(), error_message, Toast.LENGTH_SHORT);
        toast.show();
    }
}