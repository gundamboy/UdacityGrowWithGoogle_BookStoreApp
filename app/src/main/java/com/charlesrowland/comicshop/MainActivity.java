package com.charlesrowland.comicshop;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.charlesrowland.comicshop.data.ComicContract.ComicEntry;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int URL_LOADER= 0;
    View emptyView;
    private RecyclerView  recyclerView;
    private ComicAdapter mAdapter;
    private boolean mReload = false;
    static final String[] projection = {ComicEntry._ID, ComicEntry.COLUMN_COMIC_VOLUME, ComicEntry.COLUMN_COMIC_NAME, ComicEntry.COLUMN_ISSUE_NUMBER, ComicEntry.COLUMN_RELEASE_DATE, ComicEntry.COLUMN_COVER_TYPE, ComicEntry.COLUMN_PRICE, ComicEntry.COLUMN_QUANTITY, ComicEntry.COLUMN_ON_ORDER, ComicEntry.COLUMN_PUBLISHER, ComicEntry.COLUMN_SUPPLIER_NAME, ComicEntry.COLUMN_SUPPLIER_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emptyView = findViewById(R.id.empty_view);

        buildRecyclerView();
        getLoaderManager().initLoader(URL_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayData();
    }

    // the sole purpose of this is to check if the loader needs to restart
    // if we added a new item, it needs to restart. if an item was edited, it needs to restart.
    public void displayData() {
        if (mReload) {
            getLoaderManager().restartLoader(URL_LOADER,null,this);
            mReload = !mReload;
        }
        setEmptyView();
    }

    // checks for db count and shows/hides the empty items view
    // my empty items screen is awesome by the way.
    public void setEmptyView() {
        int count = getAllItems().getCount();
        if (count == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // creates the recyclerview
    public void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new ComicAdapter(this, null);
        recyclerView.setAdapter(mAdapter);

        // ok, this might look like a bunch of empty junk, but without it you can't click
        // the buttons on the list items. It is here for a reason so chill out.
        mAdapter.setOnClickListener(new ComicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int db_id, int quantity, String title) {

            }

            @Override
            public void onLongClick(int position, int db_id, String title) {

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
                setEmptyView();
                return true;

            case R.id.action_delete_all_entries:
                // Respond to a click on the "Delete all entries" menu option
                showDeleteAllConfirmationDialog();
                return true;

            case R.id.action_insert_new_comic:
                mReload = true;
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case URL_LOADER:
                CursorLoader cl = new CursorLoader(this, ComicEntry.CONTENT_URI, projection, null, null, null);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // these two things are what does the actual reloading of the info if it changed
        mAdapter.swapCursor(data);
        mReload = ! mReload;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

    // this is here for the advanced options of deleting everything.
    // i know you shouldn't do this, but it's for education purposes only
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
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

    // Kill 'em All
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
        setEmptyView();
    }

    // helper method to grab a row count from the database
    private Cursor getAllItems() {
        return getContentResolver().query(ComicEntry.CONTENT_URI, projection, null, null, ComicEntry._ID + " DESC");
    }
}