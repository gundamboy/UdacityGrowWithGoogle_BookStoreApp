package com.charlesrowland.comicshop;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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

import com.charlesrowland.comicshop.data.ComicContract;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private OnItemClickListener mListener;

    public ComicAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    // click listener interface;
    public interface OnItemClickListener {
        void onItemClick(int position, int db_id, int quantity, String title);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ComicViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout parentLayout;
        public TextView item_id;
        public TextView book_title;
        public TextView comic_info;
        public TextView quantityOnHand;
        public TextView in_stock_message;
        public Button sell_button;
        public Button edit_button;

        public ComicViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            item_id = itemView.findViewById(R.id.item_id);
            book_title = itemView.findViewById(R.id.book_title);
            comic_info = itemView.findViewById(R.id.comic_info);
            quantityOnHand = itemView.findViewById(R.id.quantityOnHand);
            in_stock_message = itemView.findViewById(R.id.in_stock);
            sell_button = itemView.findViewById(R.id.sell_button);
            edit_button = itemView.findViewById(R.id.edit_button);

            sell_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        int db_id = Integer.parseInt(item_id.getText().toString());
                        int quantity = Integer.parseInt(quantityOnHand.getText().toString());
                        String title = book_title.getText().toString();

                        int newQuantity = quantity-1;

                        if (newQuantity < 0) {
                            newQuantity = 0;
                        }

                        if (position != RecyclerView.NO_POSITION) {
                            //listener.onItemClick(position, db_id);
                            listener.onItemClick(position, db_id, newQuantity, title);
                        }
                    }
                }
            });

            edit_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int db_id = Integer.parseInt(item_id.getText().toString());

                        Intent intent = new Intent(itemView.getContext(), EditorActivity.class);
                        Uri currentComicUri = ContentUris.withAppendedId(ComicContract.ComicEntry.CONTENT_URI, db_id);

                        intent.setData(currentComicUri);
                        itemView.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comic_list_item, parent, false);
        return new ComicViewHolder(view, mListener);
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
        final int db_id = mCursor.getInt(mCursor.getColumnIndex(ComicContract.ComicEntry._ID));

        // if the quantity is 10 or less that needs to be indicated. Nothing worse than running out of comics.
        holder.quantityOnHand.setTextColor(quantityColor(quantity));

        holder.item_id.setText(String.valueOf(db_id));
        holder.book_title.setText(full_title);
        holder.comic_info.setText(info);

        if (quantity == 0) {
            holder.quantityOnHand.setText(mContext.getResources().getString(R.string.out_of_stock_message));
            holder.in_stock_message.setVisibility(View.GONE);
            holder.sell_button.setEnabled(false);
        } else {
            holder.quantityOnHand.setText(String.valueOf(quantity));
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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

    public void swapCursorInsertNew(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyItemInserted(0);
        }
    }

    public void swapCursorItemChanged(Cursor newCursor, int position) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyItemChanged(position);
        }
    }
}