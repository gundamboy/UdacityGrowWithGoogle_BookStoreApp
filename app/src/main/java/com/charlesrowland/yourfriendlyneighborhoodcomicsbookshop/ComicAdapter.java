package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicContract;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {
    public static final String TAG = ComicAdapter.class.getSimpleName();
    private Context mContext;
    private Cursor mCursor;

    public ComicAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comic_list_item, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ComicViewHolder holder, final int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        final String volume = mCursor.getString(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME));
        int issue = mCursor.getInt(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER));
        String title = mCursor.getString(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_COMIC_NAME));
        String full_title = volume + " #" + String.valueOf(issue);
        String publisher = mCursor.getString(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_PUBLISHER));
        String release_date = mCursor.getString(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_RELEASE_DATE));
        Double price = mCursor.getDouble(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_PRICE));
        String info = publisher + " - " + release_date + " · $" + String.valueOf(price);
        final int quantity = mCursor.getInt(mCursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_QUANTITY));
        final int comic_id = mCursor.getInt(mCursor.getColumnIndex(ComicContract.ComicEntry._ID));
        final Uri comicUpdateUri = ContentUris.withAppendedId(ComicContract.ComicEntry.CONTENT_URI, comic_id);

        // if the quantity is 10 or less that needs to be indicated. Nothing worse than running out of comics.
        holder.quantityOnHand.setTextColor(quantityColor(quantity));

        holder.book_title.setText(full_title);
        holder.comic_info.setText(info);

        if (quantity == 0) {
            holder.quantityOnHand.setText(mContext.getResources().getString(R.string.out_of_stock_message));
            holder.in_stock_message.setVisibility(View.GONE);
            holder.sell_button.setEnabled(false);
        } else {
            holder.quantityOnHand.setText(String.valueOf(quantity));
        }

        /**
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: clicked on the WHOLE item at position: " + position);
            }
        });
        **/
    }
    
    public int updateComic(Uri uri, int quantity) {
        ContentValues values = new ContentValues();
        values.put(ComicContract.ComicEntry.COLUMN_QUANTITY, quantity);
        
        return mContext.getContentResolver().update(uri, values, null, null);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class ComicViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout parentLayout;
        public TextView book_title;
        public TextView comic_info;
        public TextView quantityOnHand;
        public TextView in_stock_message;
        public Button sell_button;

        public ComicViewHolder(View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            book_title = itemView.findViewById(R.id.book_title);
            comic_info = itemView.findViewById(R.id.comic_info);
            quantityOnHand = itemView.findViewById(R.id.quantityOnHand);
            in_stock_message = itemView.findViewById(R.id.in_stock);
            sell_button = itemView.findViewById(R.id.sell_button);
        }
    }

    private int quantityColor(int quantity) {
        // if the quantity is less than 10 it gets a bright color to be
        // apparent that YOU ARE RUNNING LOW! ORDER MORE!

        int color;
        if (quantity <= 10) {
            color = ContextCompat.getColor(mContext, R.color.quantity_warning);
        } else {
            color = ContextCompat.getColor(mContext, R.color.comic_byline_text_color);
        }
        return color;
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

}
