package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data;

import android.provider.BaseColumns;

public final class ComicContract {

    // none shall pass. prevent instantiation
    public ComicContract() {}

    public static final class ComicEntry implements BaseColumns {
        public static final String TABLE_NAME = "comicbooks";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_COMIC_VOLUME = "volume";  // replaces Product Name
        public static final String COLUMN_COMIC_NAME = "name";
        public static final String COLUMN_ISSUE_NUMBER = "issue_number";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_COVER_TYPE = "cover_type";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PUBLISHER = "publisher";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";
    }
}