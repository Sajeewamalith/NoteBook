package com.fruity.notebook.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
     * * API Contract for the NoteBook app.
     */
public  final class NoteContract {

        // To prevent someone from accidentally instantiating the contract class,
        // give it an empty constructor.
        private NoteContract() {}

        /**
         * The "Content authority" is a name for the entire content provider, similar to the
         * relationship between a domain name and its website.  A convenient string to use for the
         * content authority is the package name for the app, which is guaranteed to be unique on the
         * device.
         */
        public static final String CONTENT_AUTHORITY = "com.fruity.notebook";


    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.notebook/notebook/ is a valid path for
     * looking at note data. content://com.example.notebook/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_NOTES = "notes";


    /**
     * Inner class that defines constant values for the notes database table.
     * Each entry in the table represents a single note.
     */
    public static final class NoteEntry implements BaseColumns {

        /** The content URI to access the note data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of notes.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single note.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;


        /** Name of database table for notes */
        public final static String TABLE_NAME = "notes";

        /**
         * Unique ID number for the note (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;


        /**
         * Name of the note.
         *
         * Type: TEXT
         */
        public final static String COLUMN_NOTE_NAME ="title";

        /**
         * * contain of the note.
         *
         * Type: TEXT
         */
        public final static String COLUMN_NOTE_CONTAIN = "contain";


    }

}
