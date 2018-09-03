package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicContract.*;
import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicDbHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int URL_LOADER= 0;
    private RecyclerView recyclerView;
    private ComicAdapter mAdapter;
    static final String[] projection = {ComicEntry._ID, ComicEntry.COLUMN_COMIC_VOLUME, ComicEntry.COLUMN_COMIC_NAME, ComicEntry.COLUMN_ISSUE_NUMBER, ComicEntry.COLUMN_RELEASE_DATE, ComicEntry.COLUMN_COVER_TYPE, ComicEntry.COLUMN_PRICE, ComicEntry.COLUMN_QUANTITY, ComicEntry.COLUMN_ON_ORDER, ComicEntry.COLUMN_PUBLISHER, ComicEntry.COLUMN_SUPPLIER_NAME, ComicEntry.COLUMN_SUPPLIER_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildRecyclerView();

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new ComicAdapter(this, null);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnClickListener(new ComicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int db_id, int quantity, String title) {
                Log.i(TAG, "onItemClick: TESTING position of clicked item: " + position);
                Log.i(TAG, "onItemClick: TESTING title of clicked item: " + db_id);
                Log.i(TAG, "onItemClick: TESTING title of clicked quantity: " + quantity);
                Log.i(TAG, "onItemClick: TESTING title of clicked title: " + title);
            }

            @Override
            public void onLongClick(int position, int db_id, String title) {
                showDeleteConfirmationDialog(db_id, position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // User clicked on a menu option in the app bar overflow menu
        switch(item.getItemId()){
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                String[]names={"Aquaman","Batman","Future Quest Presents","The Flash","Wonder Woman","Venom","Old Man Logan","Avengers","X-Men","Aliens","Cold Spots"};
                int[]issue_nums=new int[10];
                int[]stock_nums=new int[10];
                for(int i=0;i<issue_nums.length;i++){
                    issue_nums[i]=(int)(Math.random()*100);
                    stock_nums[i]=(int)(Math.random()*20);
                }

                int rnd_name_index=new Random().nextInt(names.length);
                int rnd_issue_index=new Random().nextInt(issue_nums.length);
                int rnd_stock_index=new Random().nextInt(stock_nums.length);

                ContentValues values=new ContentValues();
                values.put(ComicEntry.COLUMN_COMIC_VOLUME,names[rnd_name_index]);
                values.put(ComicEntry.COLUMN_COMIC_NAME,"Blah Blah Blah");
                values.put(ComicEntry.COLUMN_ISSUE_NUMBER,issue_nums[rnd_issue_index]);
                values.put(ComicEntry.COLUMN_RELEASE_DATE,"11/21/2018");
                values.put(ComicEntry.COLUMN_COVER_TYPE,"Original");
                values.put(ComicEntry.COLUMN_PRICE,3.99);
                values.put(ComicEntry.COLUMN_QUANTITY,stock_nums[rnd_stock_index]);
                values.put(ComicEntry.COLUMN_ON_ORDER,0);
                values.put(ComicEntry.COLUMN_PUBLISHER,"DC Comics");
                values.put(ComicEntry.COLUMN_SUPPLIER_NAME,"Diamond Comic Distributors");
                values.put(ComicEntry.COLUMN_SUPPLIER_PHONE,"1 (443) 318–8500");

                Uri uri=getContentResolver().insert(ComicEntry.CONTENT_URI,values);

                //getLoaderManager().restartLoader(URL_LOADER,null,this);
                mAdapter.swapCursorInsertNew(getAllItems());
                return true;

            case R.id.action_delete_all_entries:
                // Respond to a click on the "Delete all entries" menu option
                showDeleteAllConfirmationDialog();

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

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

    private void showDeleteConfirmationDialog(final int db_id, final int position) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the comic.
                deleteSingleComic(db_id, position);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the comic.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the comic.
                deleteAllComics();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the comic.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteSingleComic(int db_id, int position) {
        // Only perform the delete if this is an existing pet.
        Uri currentComicUri = ContentUris.withAppendedId(ComicEntry.CONTENT_URI, db_id);
        int rowsDeleted = getContentResolver().delete(currentComicUri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_comic_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            mAdapter.swapCursorDeleteSingleItem(getAllItems(), position);
        }
    }

    private void deleteAllComics() {
        // Only perform the delete if this is an existing pet.
        int rowsDeleted = getContentResolver().delete(ComicEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_comic_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            getLoaderManager().restartLoader(URL_LOADER, null, this);
        }
    }

    private Cursor getAllItems() {
        return getContentResolver().query(ComicEntry.CONTENT_URI, projection, null, null, ComicEntry._ID + " DESC");
    }

    private void updateQuantity(int db_id, int quantity, int position) {
        Uri currentComicUri = ContentUris.withAppendedId(ComicEntry.CONTENT_URI, db_id);

        ContentValues values = new ContentValues();
        values.put(ComicEntry.COLUMN_QUANTITY, quantity);

        int updateUri = getContentResolver().update(currentComicUri, values, null, null);

        if (updateUri != 0) {
            mAdapter.swapCursorItemChanged(getAllItems(), position);
        }
    }
}