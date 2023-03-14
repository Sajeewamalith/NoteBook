package com.fruity.notebook;


import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.fruity.notebook.data.NoteContract;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of notes that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the note data loader */
    private static final int NOTE_LOADER = 0;

    /** Adapter for the ListView */
    NoteCursorAdapter mCursorAdapter;

    public static SharedPref sharedPref;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme);
        } else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(intent);
        });

        // Find the ListView which will be populated with the note data
       ListView noteListView = findViewById(R.id.list);

        // Setup an Adapter to create a list item for each row of note data in the Cursor.
        // There is no note data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new NoteCursorAdapter(this, null);
        noteListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        noteListView.setOnItemClickListener((adapterView, view, position, id) -> {
            // Create new intent to go to {@link EditorActivity}
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

            // Form the content URI that represents the specific note that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // {@link NoteEntry#CONTENT_URI}.
            // For example, the URI would be "content://com.example.android.notes/notes/2"
            // if the note with ID 2 was clicked on.
            Uri currentPetUri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, id);

            // Set the URI on the data field of the intent
            intent.setData(currentPetUri);

            // Launch the {@link EditorActivity} to display the data for the current note.
            startActivity(intent);
        });

        // Kick off the loader
        getLoaderManager().initLoader(NOTE_LOADER, null, this);

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



    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Dark Mode" menu option
            case R.id.action_darkTheme:
                sharedPref.setNightModeState(true);
                restartApp();
                return true;
            // Respond to a click on the "Light Mode" menu option
            case R.id.action_lightTheme:
                sharedPref.setNightModeState(false);
                restartApp();
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restartApp(){
        Intent i = new Intent(getApplicationContext(),CatalogActivity.class);
        startActivity(i);
        finish();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_NOTE_NAME,
                NoteContract.NoteEntry.COLUMN_NOTE_CONTAIN };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                NoteContract.NoteEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                NoteContract.NoteEntry._ID + " DESC");    //  sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link NoteCursorAdapter} with this new cursor containing updated note data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


}