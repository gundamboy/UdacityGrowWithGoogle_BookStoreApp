package com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop;

import android.content.Context;
import android.database.Cursor;
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

import com.charlesrowland.yourfriendlyneighborhoodcomicsbookshop.data.ComicContract;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {
    public static final String TAG = ComicAdapter.class.getSimpleName();
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
        void onLongClick(int position, int db_id, String title);

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

        public ComicViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            item_id = itemView.findViewById(R.id.item_id);
            book_title = itemView.findViewById(R.id.book_title);
            comic_info = itemView.findViewById(R.id.comic_info);
            quantityOnHand = itemView.findViewById(R.id.quantityOnHand);
            in_stock_message = itemView.findViewById(R.id.in_stock);
            sell_button = itemView.findViewById(R.id.sell_button);

            sell_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        int db_id = Integer.parseInt(item_id.getText().toString());
                        int quantity = Integer.parseInt(quantityOnHand.getText().toString());
                        String title = book_title.getText().toString();

                        if (position != RecyclerView.NO_POSITION) {
                            //listener.onItemClick(position, db_id);
                            listener.onItemClick(position, db_id, quantity, title);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        int db_id = Integer.parseInt(item_id.getText().toString());
                        String title = book_title.getText().toString();

                        if (position != RecyclerView.NO_POSITION) {
                            //listener.onItemClick(position, db_id);
                            listener.onLongClick(position, db_id, title);
                        }
                    }
                    return true;
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

        /**
        holder.sell_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantityFromView;

                 //  get the quantity from the TextView because wtf why is the RecyclerView doing this to me?
                 //  seriously, I couldn't figure out another way to do this. Making a global quantity makes
                 //  EACH list item share the quantity. ... REALLY!? So fine, you don't tell me what to do
                 //  RecyclerView.. I DO WHAT I WANT!

                String quantityString = holder.quantityOnHand.getText().toString();

                // remove any alpha characters so it can be cast as an int without a crash
                String numberString = quantityString.replaceAll("[^\\d]", "");
                int stringLength = numberString.length();
                 //  now, you might be seeing this next bit of code and thinking to yourself... hmm,
                 //  why is he setting the value to be 1... shouldn't it be 0? Well, you can't be faulted
                 //  for thinking that, but you're still wrong. This click event is AFTER-FACT which means
                 //  if quantityFromView was set to 0, then the view would actually display "0 out of stock"
                 //  instead of going from 1, to just, Out of Stock. So, that's why it's set to 1. WORD UP!

                 // wait, what? you said 1.. yeah, i did. for quantityFromView. if the string has no length
                 // its empty and you can cast an empty string to an int. it will crash just like all the
                 // airplanes crash when Tom Hanks flies them in a movie. Seriously, they all crash. He's a
                 // terrible pretend pilot.

                if (stringLength == 0) {
                    quantityFromView = 1;
                } else {
                    quantityFromView = Integer.parseInt(numberString);
                }

                // the word visibility here should tell you all you need to know. now you see it, now you don't
                if (quantityFromView == 1) {
                    if (holder.in_stock_message.getVisibility() == View.VISIBLE) {
                        holder.in_stock_message.setVisibility(View.GONE);
                        holder.quantityOnHand.setText(mContext.getResources().getString(R.string.out_of_stock_message));
                        holder.sell_button.setEnabled(false);

                        // TODO: update the value in the database to be 0
                    }
                } else {
                    quantityFromView--;
                    holder.quantityOnHand.setTextColor(quantityColor(quantityFromView));
                    holder.quantityOnHand.setText(String.valueOf(quantityFromView));

                    // TODO: update the value in the database
                }
            }
        });
         **/
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

    public void swapCursorDeleteSingleItem(Cursor newCursor, int position) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyItemRemoved(position);
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