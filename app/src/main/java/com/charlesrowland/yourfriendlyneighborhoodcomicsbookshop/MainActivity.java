package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicContract.*;
import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicDbHelper;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private ComicDbHelper mDbHelper;
    private Button button;
    private Button delete_button;
    private boolean dataInsertedToggle = false;
    private boolean deleteDataToggle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new ComicDbHelper(this);

        button = findViewById(R.id.button);
        delete_button = findViewById(R.id.button_delete);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertComic();
                displayDatabaseInfo();
                button.setEnabled(false);
                delete_button.setEnabled(true);

                dataInsertedToggle = false;
                deleteDataToggle = true;
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDummyData();
                displayDatabaseInfo();

                button.setEnabled(true);
                delete_button.setEnabled(false);

                dataInsertedToggle = true;
                deleteDataToggle = false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * query the database so we can see all the sweet sweet comic book goodness
     */
    private void displayDatabaseInfo() {
        // create / open a database instance to read/write from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // i did not define a projection for the columns because I want all the columns
        Cursor cursor = db.query(ComicEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // get the textview and some temporary display stuff so you, the reviewer, can
        // see that data is pulling. I know there in no UI for this, but i wanted to make
        // your life a little easier. Sue me.
        TextView comicInventoryHeader = findViewById(R.id.comic_total_inventory_header);
        TextView comicInfoTextView = findViewById(R.id.comic_info_view);

        if (cursor.getCount() == 0) {
            comicInfoTextView.setText("There are no Comic Books to display");
            delete_button.setEnabled(false);
        } else {
            comicInfoTextView.setText("");
            button.setEnabled(false);
        }

        try {
            String inventoryTotalHeader = String.format(getResources().getString(R.string.total_inventory_header), cursor.getCount());
            comicInventoryHeader.setText(inventoryTotalHeader);

            int idColumnIndex = cursor.getColumnIndex(ComicEntry._ID);
            int idVolume = cursor.getColumnIndex(ComicEntry.COLUMN_COMIC_VOLUME );
            int idName = cursor.getColumnIndex(ComicEntry.COLUMN_COMIC_NAME );
            int idIssue = cursor.getColumnIndex(ComicEntry.COLUMN_ISSUE_NUMBER );
            int idDate = cursor.getColumnIndex(ComicEntry.COLUMN_RELEASE_DATE );
            int idCover = cursor.getColumnIndex(ComicEntry.COLUMN_COVER_TYPE );
            int idPrice = cursor.getColumnIndex(ComicEntry.COLUMN_PRICE );
            int idQuantity = cursor.getColumnIndex(ComicEntry.COLUMN_QUANTITY );
            int idPublisher = cursor.getColumnIndex(ComicEntry.COLUMN_PUBLISHER );
            int idSupplier = cursor.getColumnIndex(ComicEntry.COLUMN_SUPPLIER_NAME );
            int idPhone = cursor.getColumnIndex(ComicEntry.COLUMN_SUPPLIER_PHONE );

            Resources res = getResources();

            while (cursor.moveToNext()) {
                String currentId = String.format(res.getString(R.string.comic_id), Integer.toString(cursor.getInt(idColumnIndex)));
                String currentVolume = String.format(res.getString(R.string.comic_volume), cursor.getString(idVolume));
                String currentName = String.format(res.getString(R.string.comic_name), cursor.getString(idName));
                String currentIssue = String.format(res.getString(R.string.comic_issue), Integer.toString(cursor.getInt(idIssue)));
                String currentDate = String.format(res.getString(R.string.comic_date), cursor.getString(idDate));
                String currentCover = String.format(res.getString(R.string.comic_cover), cursor.getString(idCover));
                String currentPrice = String.format(res.getString(R.string.comic_price), Double.toString(cursor.getDouble(idPrice)));
                String currentQuantity = String.format(res.getString(R.string.comic_quantity), Integer.toString(cursor.getInt(idQuantity)));
                String currentPublisher = String.format(res.getString(R.string.comic_publisher), cursor.getString(idPublisher));
                String currentSupplier = String.format(res.getString(R.string.comic_supplier), cursor.getString(idSupplier));
                String currentPhone = String.format(res.getString(R.string.comic_supplier_phone), cursor.getString(idPhone));
                String NL = "\n";

                // Id: 1, Volume: Justice League, Name: Trial by Combat, Issue: 1, Release Date: 08/15/2018
                // Cover Type: Original, Price: $3.99, Quantity: 1, Publisher: DC,
                // Supplier: Diamond Comic Distributors, Phone: 1 (443) 318–8500
                String infoTemplate = currentId + NL + currentVolume + NL + currentName + NL + currentIssue + NL  + currentDate + NL;
                infoTemplate += currentCover + NL + currentPrice + NL + currentQuantity + NL + currentPublisher + NL;
                infoTemplate += currentSupplier + NL + currentPhone + NL + NL;

                comicInfoTextView.append(infoTemplate);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * inserts data into the database
     */
    private void insertComic() {
        // Create database helper
        // this is used to get access to the database
        mDbHelper = new ComicDbHelper(this);
        String dc_comics = "DC";
        String marvel_comics = "Marvel";
        String darkhorse_comics = "Dark Horse";
        String image_comics = "Image";

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        buildContentValues("Aquaman", "Drowned Earth", 42, "11/21/2018", "Original", 3.99, 8, dc_comics);
        buildContentValues("Batman", "Dark Knight no More!", 53, "08/15/2018", "Original", 3.99, 10, dc_comics);
        buildContentValues("Future Quest Presents", "The Hurculoids", 10, "05/16/2018", "Original", 3.99, 6, dc_comics);
        buildContentValues("The Flash", "Grips of Strength", 52, "08/08/2018", "Original", 3.99, 3, dc_comics);
        buildContentValues("Wonder Woman", "Amazons Attacked", 45, "08/25/2018", "Original", 2.99, 14, dc_comics);
        buildContentValues("Venom", "", 5, "08/22/2018", "Original", 4.99, 10, marvel_comics);
        buildContentValues("Old Man Logan", "", 5, "08/22/2018", "Original", 4.99, 10, marvel_comics);
        buildContentValues("Avengers", "", 6, "08/22/2018", "Original", 4.99, 14, marvel_comics);
        buildContentValues("X-Men", "Reds", 7, "08/22/2018", "Original", 4.99, 14, marvel_comics);
        buildContentValues("Aliens", "Dust to Dust", 4, "10/31/2018", "Variant", 3.99, 14, darkhorse_comics);
        buildContentValues("Cold Spots", "", 1, "08/22/2018", "Original", 3.99, 14, image_comics);
    }

    private void deleteDummyData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete(ComicEntry.TABLE_NAME, null, null);
    }

    private void buildContentValues(String volume, String name, int number, String date, String cover, Double price, int quantity, String publisher) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        values.put(ComicEntry.COLUMN_COMIC_VOLUME, volume);
        values.put(ComicEntry.COLUMN_COMIC_NAME, name);
        values.put(ComicEntry.COLUMN_ISSUE_NUMBER, number);
        values.put(ComicEntry.COLUMN_RELEASE_DATE, date);
        values.put(ComicEntry.COLUMN_COVER_TYPE, cover);
        values.put(ComicEntry.COLUMN_PRICE, price);
        values.put(ComicEntry.COLUMN_QUANTITY, quantity);
        values.put(ComicEntry.COLUMN_PUBLISHER, publisher);
        values.put(ComicEntry.COLUMN_SUPPLIER_NAME, "Diamond Comic Distributors");
        values.put(ComicEntry.COLUMN_SUPPLIER_PHONE, "1 (443) 318–8500");

        long newRowId = db.insert(ComicEntry.TABLE_NAME, null, values );

        if (newRowId == -1) {
            Toast.makeText(this, "Error with saving comic book", Toast.LENGTH_SHORT).show();
        }
    }
}
