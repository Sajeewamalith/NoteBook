package com.fruity.notebook.data;


import static com.fruity.notebook.data.NoteContract.NoteEntry.CONTENT_LIST_TYPE;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for NoteBook app.
 */
public class NoteProvider extends ContentProvider{

    /** Tag for the log messages */
    public static final String LOG_TAG = NoteProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the notes table */
    private static final int NOTES = 100;

    /** URI matcher code for the content URI for a single note in the notes table */
    private static final int NOTE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY,NoteContract.PATH_NOTES,NOTES);
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY,NoteContract.PATH_NOTES + "/#",NOTE_ID);

    }

    /** Database helper object */
    private NoteDbHelper mDbHelper;


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new NoteDbHelper(getContext());
        return true;

    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments,
     * and sort order.
     */
    @Override
    public Cursor query(Uri uri,String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case NOTE_ID:
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case NOTES:
                return CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NoteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);

        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri,ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        if (match == NOTES) {
            return insertNote(uri, contentValues);
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    /**
     * Insert a note into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertNote(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Note requires a title");
        }

        // No need to check the contain, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new note with the given values
        long id = database.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the note content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                // For the NOTE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

   /**
    * Update notes in the database with the given content values. Apply the changes to the rows
    * specified in the selection and selection arguments (which could be 0 or 1 or more notes).
    * Return the number of rows that were successfully updated.
    */
   private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       // If the {@link NoteEntry#COLUMN_NOTE_NAME} key is present,
       // check that the name value is not null.
       if (values.containsKey(NoteContract.NoteEntry.COLUMN_NOTE_NAME)) {
           String name = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_NAME);
           if (name == null) {
               throw new IllegalArgumentException("Note requires a title");
           }
       }

       // No need to check the contain, any value is valid (including null).

       // If there are no values to update, then don't try to update the database
       if (values.size() == 0) {
           return 0;
       }

       // Otherwise, get writeable database to update the data
       SQLiteDatabase database = mDbHelper.getWritableDatabase();

       // Perform the update on the database and get the number of rows affected
       int rowsUpdated = database.update(NoteContract.NoteEntry.TABLE_NAME, values, selection, selectionArgs);

       // If 1 or more rows were updated, then notify all listeners that the data at the
       // given URI has changed
       if (rowsUpdated != 0) {
           getContext().getContentResolver().notifyChange(uri, null);
       }

       // Return the number of rows updated
       return rowsUpdated;

   }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri,String selection,String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case NOTES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case NOTE_ID:
                // Delete a single row given by the ID in the URI
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

}
