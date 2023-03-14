package com.fruity.notebook;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.fruity.notebook.data.NoteContract;

public class NoteCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link NoteCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public NoteCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the note data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current note can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.TitleName);
        TextView summaryTextView = view.findViewById(R.id.summary);

        // Find the columns of note attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_NAME);
        int containColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_CONTAIN);

        // Read the note attributes from the Cursor for the current note
        String noteName = cursor.getString(nameColumnIndex);
        String noteContain = cursor.getString(containColumnIndex);

        // If the note contain is empty string or null, then use some default text
        // that says "Empty contain", so the TextView isn't blank.
        if (TextUtils.isEmpty(noteContain)) {
            noteContain = context.getString(R.string.empty_contain);
        }

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(noteName);
        summaryTextView.setText(noteContain);

    }

}
