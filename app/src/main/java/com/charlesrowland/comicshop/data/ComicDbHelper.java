package com.charlesrowland.comicshop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.charlesrowland.comicshop.data.ComicContract.*;

public class ComicDbHelper extends SQLiteOpenHelper {
    Context context;

    private static final String DATABASE_NAME = "comics.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COMMA_SEP = ", ";
    private SQLiteDatabase db;

    public ComicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        // if it isn't obvious, this string is the sql query that creates the table in the db
        final String CREATE_DB = "CREATE TABLE " + ComicEntry.TABLE_NAME + "(" +
                ComicEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                ComicEntry.COLUMN_COMIC_VOLUME + " TEXT NOT NULL" + COMMA_SEP +
                ComicEntry.COLUMN_COMIC_NAME + " TEXT" + COMMA_SEP +
                ComicEntry.COLUMN_ISSUE_NUMBER + " INTEGER NOT NULL" + COMMA_SEP +
                ComicEntry.COLUMN_RELEASE_DATE + " TEXT" + COMMA_SEP +
                ComicEntry.COLUMN_COVER_TYPE + " TEXT" + COMMA_SEP +
                ComicEntry.COLUMN_PRICE + " REAL" + COMMA_SEP +
                ComicEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP +
                ComicEntry.COLUMN_PUBLISHER + " TEXT" + COMMA_SEP +
                ComicEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL DEFAULT \"Diamond Comic Distributors\""  + COMMA_SEP +
                ComicEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL DEFAULT \"1 (443) 318–8500\"" +
                ")";

        // make the table. MAKE IT! WE CAN'T SAVE DATA WITHOUT THIS! ok im better now.
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion  > oldVersion ) {
            // table goes bye bye.
            dropComicsTable();
        }
    }

    private void dropComicsTable() {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + ComicEntry.TABLE_NAME;
        db.execSQL(DROP_TABLE);
    }
}
