package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicContract.*;
import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int URL_LOADER= 0;
    private ComicDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private ComicAdapter mAdapter;
    static final String[] projection = {ComicEntry._ID, ComicEntry.COLUMN_COMIC_VOLUME, ComicEntry.COLUMN_COMIC_NAME, ComicEntry.COLUMN_ISSUE_NUMBER, ComicEntry.COLUMN_RELEASE_DATE, ComicEntry.COLUMN_COVER_TYPE, ComicEntry.COLUMN_PRICE, ComicEntry.COLUMN_QUANTITY, ComicEntry.COLUMN_ON_ORDER, ComicEntry.COLUMN_PUBLISHER, ComicEntry.COLUMN_SUPPLIER_NAME, ComicEntry.COLUMN_SUPPLIER_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new ComicDbHelper(this);
        mDatabase = mDbHelper.getReadableDatabase();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new ComicAdapter(this, null);
        //mAdapter = new ComicAdapter(this, null);
        recyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                ContentValues values = new ContentValues();
                values.put(ComicEntry.COLUMN_COMIC_VOLUME, "Test Comic");
                values.put(ComicEntry.COLUMN_COMIC_NAME, "Blah Blah Blah");
                values.put(ComicEntry.COLUMN_ISSUE_NUMBER, 42);
                values.put(ComicEntry.COLUMN_RELEASE_DATE, "11/21/2018");
                values.put(ComicEntry.COLUMN_COVER_TYPE, "Original");
                values.put(ComicEntry.COLUMN_PRICE, 3.99);
                values.put(ComicEntry.COLUMN_QUANTITY, 8);
                values.put(ComicEntry.COLUMN_ON_ORDER, 0);
                values.put(ComicEntry.COLUMN_PUBLISHER, "DC Comics");
                values.put(ComicEntry.COLUMN_SUPPLIER_NAME, "Diamond Comic Distributors");
                values.put(ComicEntry.COLUMN_SUPPLIER_PHONE, "1 (443) 318–8500");

                Uri uri = getContentResolver().insert(ComicEntry.CONTENT_URI, values);

                return true;

            case R.id.action_delete_all_entries:
                // Respond to a click on the "Delete all entries" menu option
                showDeleteConfirmationDialog();
                //deleteDummyData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case URL_LOADER:
                String sortOrder = ComicEntry._ID + " DESC";
                CursorLoader cl = new CursorLoader(this, ComicEntry.CONTENT_URI, projection, null, null, null);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        Log.i(TAG, "onLoadFinished: Cursor count: " + data.getCount());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteComics();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteComics() {
        // Only perform the delete if this is an existing pet.
        int rowsDeleted = getContentResolver().delete(ComicEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_comic_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_comic_successful),
                    Toast.LENGTH_SHORT).show();
        }

        // Close the activity
        finish();
    }

}
