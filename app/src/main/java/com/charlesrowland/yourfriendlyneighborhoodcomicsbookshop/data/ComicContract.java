package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ComicContract {

    // none shall pass. prevent instantiation
    public ComicContract() {}

    public static final String CONTENT_AUTHORITY = "com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_COMICBOOKS = "comicbooks";

    public static final class ComicEntry implements BaseColumns {
        // uri for provider access
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COMICBOOKS);

        // MIME type for a list of comics
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMICBOOKS;

        // MIME type for a single comic
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMICBOOKS;

        public static final String TABLE_NAME = "comicbooks";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_COMIC_VOLUME = "volume";  // replaces Product Name
        public static final String COLUMN_COMIC_NAME = "name";
        public static final String COLUMN_ISSUE_NUMBER = "issue_number";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_COVER_TYPE = "cover_type";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_ON_ORDER = "on_order";
        public static final String COLUMN_PUBLISHER = "publisher";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";
    }
}