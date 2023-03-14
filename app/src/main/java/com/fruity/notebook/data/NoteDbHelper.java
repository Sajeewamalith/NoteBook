package com.fruity.notebook.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for NoteBook app. Manages database creation and version management.
 */
public class NoteDbHelper  extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link NoteDbHelper}.
     *
     * @param context of the app
     */
    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the notes table
        String SQL_CREATE_NOTES_TABLE =  "CREATE TABLE " + NoteContract.NoteEntry.TABLE_NAME + " ("
                + NoteContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NoteContract.NoteEntry.COLUMN_NOTE_NAME + " TEXT NOT NULL, "
                + NoteContract.NoteEntry. COLUMN_NOTE_CONTAIN + " TEXT );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_NOTES_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.

    }
}
