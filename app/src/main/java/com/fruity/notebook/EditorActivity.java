package com.fruity.notebook;

import static com.fruity.notebook.CatalogActivity.sharedPref;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.NavUtils;

import com.fruity.notebook.data.NoteContract;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

/**
 * Allows user to create a new note or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the note data loader */
    private static final int EXISTING_NOTE_LOADER = 0;

    /** Content URI for the existing note (null if it's a new note) */
    private Uri mCurrentNoteUri;

    /** EditText field to enter the note's  title */
    private EditText mTitleEditText;

    /** EditText field to enter the note's contain */
    private EditText mContentEditText;

    /** Boolean flag that keeps track of whether the note has been edited (true) or not (false) */
    private boolean mNoteHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mNoteHasChanged boolean to true.
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        mNoteHasChanged = true;
        return false;
    };

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new note or editing an existing one.
        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        // If the intent DOES NOT contain a note content URI, then we know that we are
        // creating a new note.
        if (mCurrentNoteUri == null) {
            // This is a new note, so change the app bar to say "Add a Note"
            setTitle(getString(R.string.editor_activity_title_new_note));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a note that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing note, so change app bar to say "Edit Note"
            setTitle(getString(R.string.editor_activity_title_edit_note));

            // Initialize a loader to read the note data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_title);
        mContentEditText = findViewById(R.id.edit_contain);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mContentEditText.setOnTouchListener(mTouchListener);

         //admob
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = new AdView(this);

        mAdView.setAdSize(AdSize.BANNER);

        mAdView.setAdUnitId("ca-app-pub-9792667030467157/4526786061");



        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });





    }


    /**
     * Get user input from editor and save  note into database.
     */
    private void saveNote() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space

    try {


        String titleString = mTitleEditText.getText().toString().trim();
        String containString = mContentEditText.getText().toString().trim();

        // Check if this is supposed to be a new note
        // and check if all the fields in the editor are blank
        if (mCurrentNoteUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(containString)) {
            // Since no fields were modified, we can return early without creating a new note.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and note attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_NAME, titleString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_CONTAIN, containString);

        // Determine if this is a new or existing note by checking if mCurrentNoteUri is null or not
        if (mCurrentNoteUri == null) {
            // This is a NEW note, so insert a new note into the provider,
            // returning the content URI for the new note.
            Uri newUri = getContentResolver().insert(NoteContract.NoteEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_note_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_note_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING note, so update the note with content URI: mCurrentNoteUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentNoteUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_note_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_note_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }catch (Exception e){
        Toast.makeText(this, getString(R.string.input_exception),
                Toast.LENGTH_SHORT).show();
    }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new note, hide the "Delete" menu item.
        if (mCurrentNoteUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save note to database
                saveNote();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the note hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mNoteHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialogInterface, i) -> {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
          
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the note hasn't changed, continue with handling back button press
        if (!mNoteHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                (dialogInterface, i) -> {
                    // User clicked "Discard" button, close the current activity.
                    finish();
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all note attributes, define a projection that contains
        // all columns from the note table
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_NOTE_NAME,
                NoteContract.NoteEntry.COLUMN_NOTE_CONTAIN};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentNoteUri,         // Query the content URI for the current note
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                NoteContract.NoteEntry._ID + " DESC");   //  sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of note attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_NAME);
            int containColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_CONTAIN);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String contain = cursor.getString(containColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mContentEditText.setText(contain);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mContentEditText.setText("");
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

    try {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, (dialog, id) -> {
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the note.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }catch (Exception e){
        Toast.makeText(this, getString(R.string.badToken_alertDialog_exception),
                Toast.LENGTH_SHORT).show();

    }
    }

    /**
     * Prompt the user to confirm that they want to delete this note.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

     try {


         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.delete_dialog_msg);
         builder.setPositiveButton(R.string.delete, (dialog, id) -> {
             // User clicked the "Delete" button, so delete the note.
             deleteNote();
         });
         builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
             // User clicked the "Cancel" button, so dismiss the dialog
             // and continue editing the note.
             if (dialog != null) {
                 dialog.dismiss();
             }
         });

         // Create and show the AlertDialog
         AlertDialog alertDialog = builder.create();
         alertDialog.show();

     }catch (Exception e){
         Toast.makeText(this, getString(R.string.badToken_alertDialog_exception),
                 Toast.LENGTH_SHORT).show();

     }
    }

    /**
     * Perform the deletion of the note in the database.
     */
    private void deleteNote() {
        // Only perform the delete if this is an existing note.
        if (mCurrentNoteUri != null) {
            // Call the ContentResolver to delete the note at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the note that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_note_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_note_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }


}