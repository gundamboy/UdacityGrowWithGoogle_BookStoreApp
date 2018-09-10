package com.charlesrowland.comicshop;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.charlesrowland.comicshop.data.ComicContract;

import java.util.Calendar;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EditorActivity.class.getSimpleName();
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final int EXISTING_COMIC_LOADER = 0;
    private Uri mCurrentComicUri;
    private TextView mHowto;
    private EditText mVolume;
    private EditText mName;
    private EditText mIssueNumber;
    private ImageButton mCalendarButton;
    private Spinner mCoverType;
    private AutoCompleteTextView mPrice;
    private EditText mQuantity;
    private AutoCompleteTextView mPublisher;
    private AutoCompleteTextView mSupplierName;
    private AutoCompleteTextView mSupplierPhone;
    private Button mQuantity_add_button;
    private Button mQuantity_minus_button;
    private boolean mComicHasChanged = false;
    private boolean okToSave = false;
    private boolean editMode = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mComicHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mHowto = findViewById(R.id.howto_info);

        // check to see if there is data in the intent. It will be null if not
        Intent intent = getIntent();
        mCurrentComicUri = intent.getData();

        if (mCurrentComicUri == null) {
            setTitle(getString(R.string.action_insert_new_comic));
            mHowto.setText(getString(R.string.add_new_info));
            invalidateOptionsMenu();
        } else {
            // edit mode: i needed a way to do some fancy stuff later on. this helps with that. deal with it.
            editMode = true;
            mHowto.setText(getString(R.string.edit_book_info));
            setTitle(getString(R.string.action_edit_comic));
            getLoaderManager().initLoader(EXISTING_COMIC_LOADER, null, this);
        }

        // grab all the stuff... i mean views.
        mVolume = findViewById(R.id.edit_comic_volume);
        mName = findViewById(R.id.edit_comic_name);
        mIssueNumber = findViewById(R.id.edit_comic_issue_number);
        mCoverType = findViewById(R.id.cover_spinner);
        mPrice = findViewById(R.id.edit_comic_price);
        mQuantity = findViewById(R.id.edit_comic_quantity);
        mPublisher = findViewById(R.id.edit_comic_publisher);
        mSupplierName = findViewById(R.id.edit_comic_supplier);
        mSupplierPhone = findViewById(R.id.edit_comic_supplier_phone);
        mDisplayDate = findViewById(R.id.edit_comic_date);
        mCalendarButton = findViewById(R.id.calendar_button);
        mQuantity_add_button = findViewById(R.id.quantity_add_button);
        mQuantity_minus_button = findViewById(R.id.quantity_minus_button);

        // disables the date editextview which forces the user to use the calendar button.
        // why would you do this you ask.. because I can. Also, because now I dont have
        // do validity on the date format. see what i did there... its called winning.
        mDisplayDate.setEnabled(false);
        mDisplayDate.setFocusable(false);

        // setting up autocomplete text suggestions. it's 2018, typing full sentences is stupid.
        String[] suggested_price = getResources().getStringArray(R.array.suggested_prices_array);
        ArrayAdapter<String> suggested_price_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggested_price);
        mPrice.setAdapter(suggested_price_adapter);

        String[] publishers = getResources().getStringArray(R.array.publishers_array);
        ArrayAdapter<String> publishers_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, publishers);
        mPublisher.setAdapter(publishers_adapter);

        String[] distributors = getResources().getStringArray(R.array.distributors_array);
        ArrayAdapter<String> distributor_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, distributors);
        mSupplierName.setAdapter(distributor_adapter);

        String[] distributor_phones = getResources().getStringArray(R.array.distributors_phone_array);
        ArrayAdapter<String> phone_numbers_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, distributor_phones);
        mSupplierPhone.setAdapter(phone_numbers_adapter);

        // sets up touch listeners. I know the name is confusing which is why i explained it here.
        setTouchListeners();

        // sets up the spinner. not a cool spinner like on roller skates though
        setupSpinner();

        // super awesome calendar button.
        mCalendarButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                if (editMode) {
                    // this is some of the fancy stuff i was talking about earlier.
                    // now that I think about it i could just checked if mCurrentComicUri
                    // wasn't null, but its too late now, im not going back there, i will never
                    // go back there.
                    year = parseStringDate(mDisplayDate.getText().toString(), "y");
                    month = parseStringDate(mDisplayDate.getText().toString(), "m");
                    day = parseStringDate(mDisplayDate.getText().toString(), "d");
                }

                DatePickerDialog dateDialog = new DatePickerDialog(EditorActivity.this, 0, mDateSetListener, year, month, day);
                dateDialog.show();
            }
        });

        // the actual datepicker
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String formatted_date = String.valueOf(month) + "/" + String.valueOf(dayOfMonth)+ "/" + String.valueOf(year);
                mDisplayDate.setText(formatted_date);
            }
        };

        // these next to add and subtract from the quantity
        mQuantity_add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setStringQuantity("add");
            }
        });

        mQuantity_minus_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setStringQuantity("minus");
            }
        });
    }

    // this guy takes the string date from the DB and sends back the int needed to set
    // the datepicker if we are editing an entry.
    private int parseStringDate(String strDate, String section) {
        String[] parts = strDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        int monthOut = month - 1;
        int year = Integer.parseInt(parts[2]);

        int partRequested = 0;

        switch(section) {
            case "d":
                partRequested = day;
                break;
            case "m":
                partRequested = monthOut;
                break;
            case "y":
                partRequested = year;
                break;
        }
        return partRequested;
    }

    // touch listeners. if something here gets touched it enables the dialog for losing changes
    private void setTouchListeners() {
        mVolume.setOnTouchListener(mTouchListener);
        mName.setOnTouchListener(mTouchListener);
        mIssueNumber.setOnTouchListener(mTouchListener);
        mCoverType.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mCalendarButton.setOnTouchListener(mTouchListener);
        mQuantity_add_button.setOnTouchListener(mTouchListener);
        mQuantity_minus_button.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);
        mDisplayDate.setOnTouchListener(mTouchListener);
    }

    // this sets the quantity in the edittext view
    private void setStringQuantity(String method) {
        String stringQuantity = mQuantity.getText().toString();
        int currentQuantity;

        if (TextUtils.isEmpty(stringQuantity)) {
            currentQuantity = 0;
        } else {
            currentQuantity = Integer.parseInt(stringQuantity);
        }

        switch(method) {
            case "add":
                currentQuantity++;
                break;
            case "minus":
                if (currentQuantity != 0) {
                    currentQuantity--;
                }
                break;
        }

        mQuantity.setText(String.valueOf(currentQuantity));
    }

    // not a roller skate spinner. its for choosing the proper cover type.
    public void setupSpinner() {
        Spinner cover_spinner = findViewById(R.id.cover_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cover_types_array, R.layout.spinner_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cover_spinner.setAdapter(adapter);
    }

    // save to the database
    private void saveComic() {
        String volume = mVolume.getText().toString().trim();
        String name = mName.getText().toString().trim();
        String issue_number = mIssueNumber.getText().toString();
        String releaseDate = mDisplayDate.getText().toString().trim();
        String price = mPrice.getText().toString();
        String quantity = mQuantity.getText().toString();
        String publisher = mPublisher.getText().toString().trim();
        String supplier = mSupplierName.getText().toString().trim();
        String phone = mSupplierPhone.getText().toString().trim();
        String cover_type = mCoverType.getSelectedItem().toString();

        if (mCurrentComicUri == null && TextUtils.isEmpty(volume) && TextUtils.isEmpty(name) &&
                TextUtils.isEmpty(releaseDate) && TextUtils.isEmpty(publisher) && TextUtils.isEmpty(supplier) &&
                TextUtils.isEmpty(phone) && TextUtils.isEmpty(issue_number)&& TextUtils.isEmpty(price)&& TextUtils.isEmpty(quantity)) {
            okToSave = false;
            return;
        }

        okToSave = false;

        int issue_num = 0;
        double comic_price = 0.00;
        int comic_quantity = 0;

        if (!TextUtils.isEmpty(issue_number)) {
            issue_num = Integer.parseInt(issue_number);
        }

        if (!TextUtils.isEmpty(price)) {
            comic_price = Double.parseDouble(price);
        }

        if (!TextUtils.isEmpty(quantity)) {
            comic_quantity = Integer.parseInt(quantity);
        }

        ContentValues values = new ContentValues();
        values.put(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME, volume);
        values.put(ComicContract.ComicEntry.COLUMN_COMIC_NAME, name);
        values.put(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER, issue_num);
        values.put(ComicContract.ComicEntry.COLUMN_RELEASE_DATE, releaseDate);
        values.put(ComicContract.ComicEntry.COLUMN_COVER_TYPE, cover_type);
        values.put(ComicContract.ComicEntry.COLUMN_PRICE, comic_price);
        values.put(ComicContract.ComicEntry.COLUMN_QUANTITY, comic_quantity);
        values.put(ComicContract.ComicEntry.COLUMN_PUBLISHER, publisher);
        values.put(ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME, supplier);
        values.put(ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE, phone);


        if (mCurrentComicUri == null) {
            Uri newUri = getContentResolver().insert(ComicContract.ComicEntry.CONTENT_URI, values);

            if (newUri != null) {
                // comic saved successfully
                Toast.makeText(this, getString(R.string.editor_insert_comic_successful),
                        Toast.LENGTH_SHORT).show();

                okToSave = true;
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentComicUri, values, null, null);

            if (rowsAffected != 0) {
                // comic saved successfully
                Toast.makeText(this, getString(R.string.editor_insert_comic_successful),
                        Toast.LENGTH_SHORT).show();

                okToSave = true;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new comic, hide the delete menu item and the contact menu
        if (mCurrentComicUri == null) {
            MenuItem menuItemContact = menu.findItem(R.id.action_contact);
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemContact.setVisible(false);
            menuItemDelete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save to database
                saveComic();

                if (okToSave) {
                    // Exit activity
                    setResult(RESULT_OK);
                    finish();
                }

                return true;

            // call the supplier
            case R.id.action_contact:
                Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhoneNumber()));
                startActivity(call_intent);
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mComicHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // this strips out all non numeric characters from the phone number
    private String supplierPhoneNumber() {
        String raw_phone = mSupplierPhone.getText().toString();
        return raw_phone.replaceAll("[^\\d]", "");
    }

    @Override
    public void onBackPressed() {
        // If the comic hasn't changed, continue with handling back button press
        if (!mComicHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // start doing database stuff
        String[] projection = {ComicContract.ComicEntry._ID, ComicContract.ComicEntry.COLUMN_COMIC_VOLUME, ComicContract.ComicEntry.COLUMN_COMIC_NAME, ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER, ComicContract.ComicEntry.COLUMN_RELEASE_DATE, ComicContract.ComicEntry.COLUMN_COVER_TYPE, ComicContract.ComicEntry.COLUMN_PRICE, ComicContract.ComicEntry.COLUMN_QUANTITY, ComicContract.ComicEntry.COLUMN_ON_ORDER, ComicContract.ComicEntry.COLUMN_PUBLISHER, ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME, ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE};
        return new CursorLoader(this, mCurrentComicUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // we have a cursor so lets get to it. grab all the stuff and set the views
        if (cursor.moveToFirst()) {
            int volumeColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_COMIC_VOLUME );
            int nameColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_COMIC_NAME );
            int issueColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_ISSUE_NUMBER );
            int releaseDateColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_RELEASE_DATE );
            int coverTypeColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_COVER_TYPE );
            int priceColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_QUANTITY );
            int publisherColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_PUBLISHER );
            int supplierColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_SUPPLIER_NAME );
            int phoneColumnIndex = cursor.getColumnIndex(ComicContract.ComicEntry.COLUMN_SUPPLIER_PHONE );

            String volume = cursor.getString(volumeColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String issue_number = String.valueOf(cursor.getInt(issueColumnIndex));
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            String cover = cursor.getString(coverTypeColumnIndex);
            String price = String.valueOf(cursor.getDouble(priceColumnIndex));
            String quantity = String.valueOf(cursor.getInt(quantityColumnIndex));
            String publisher = cursor.getString(publisherColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            mVolume.setText(volume);
            mName.setText(name);
            mIssueNumber.setText(issue_number);
            mCoverType.setSelection(getSpinnerIndex(mCoverType, cover));
            mPrice.setText(price);
            mQuantity.setText(quantity);
            mPublisher.setText(publisher);
            mSupplierName.setText(supplier);
            mSupplierPhone.setText(phone);
            mDisplayDate.setText(releaseDate);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mVolume.setText("");
        mName.setText("");
        mIssueNumber.setText("");
        mCoverType.setSelection(0);
        mPrice.setText("");
        mQuantity.setText("");
        mPublisher.setText("");
        mSupplierName.setText("");
        mSupplierPhone.setText("");
        mDisplayDate.setText("");
    }

    private int getSpinnerIndex(Spinner spinner, String myString){
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    private void showUnsavedChangesDialog(
        DialogInterface.OnClickListener discardButtonClickListener) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changes_dialog_msg);
            builder.setPositiveButton(R.string.discard, discardButtonClickListener);
            builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the comic.
                deleteComic();
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

    private void deleteComic() {
        // Only perform the delete if this is an existing comic.
        if (mCurrentComicUri != null) {
            // Call the ContentResolver to delete the comic at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the comic that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentComicUri, null, null);

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
        }

        // Close the activity
        finish();
    }
}