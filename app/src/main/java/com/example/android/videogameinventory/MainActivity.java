/*
    I would like to give credit to two individuals that helped improve my app.
    From GitHub: ankurg22
    From Udacity one-on-one coach: Otieno Rowland
*/

package com.example.android.videogameinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;
import com.example.android.videogameinventory.data.VideoGameDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the Video Game data loader
     */
    private static final int VIDEO_GAME_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    VideoGameCursorAdapter mAdapter;

    // Database projection
    String[] projection = {
            VideoGameEntry._ID,
            VideoGameEntry.COLUMN_GAME_TITLE,
            VideoGameEntry.COLUMN_GENRE,
            VideoGameEntry.COLUMN_CURRENT_INVENTORY,
            VideoGameEntry.COLUMN_PRICE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the GridView which will be populated with the Video Game data
        ListView videoGameListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the GridView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        videoGameListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of Video Game data in the Cursor.
        // There is no Video Game data yet (until the loader finishes) so pass in null for the Cursor.

        VideoGameDbHelper videoGameDbHelper = new VideoGameDbHelper(this);
        SQLiteDatabase db = videoGameDbHelper.getReadableDatabase();

        Cursor cursor = db.query(VideoGameEntry.TABLE_NAME, projection, null, null, null, null, null);
        mAdapter = new VideoGameCursorAdapter(this, cursor);

        videoGameListView.setAdapter(mAdapter);

        // Setup FAB to open Details
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Setup the item click listener
        videoGameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Form the content URI that represents the specific Video Game that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link VideoGameEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.Video Games/Video Games/2"
                // if the Video Game with ID 2 was clicked on.
                Uri currentVideoGameUri = ContentUris.withAppendedId(VideoGameEntry.CONTENT_URI, id);

                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

                // Set the URI on the data field of the intent
                intent.setData(currentVideoGameUri);

                // Launch the {@link EditorActivity} to display the data for the current Video Game.
                startActivity(intent);
            }

        });

        // Kick off the loader
        getLoaderManager().initLoader(VIDEO_GAME_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                VideoGameEntry._ID,
                VideoGameEntry.COLUMN_GAME_TITLE,
                VideoGameEntry.COLUMN_CURRENT_INVENTORY,
                VideoGameEntry.COLUMN_PRICE,
                VideoGameEntry.COLUMN_GENRE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                VideoGameEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link VideoGameCursorAdapter} with this new cursor containing updated Video Game data
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertVideoGame();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllVideoGame();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to insert hardcoded Video Game data into the database. For debugging purposes only.
     */
    private void insertVideoGame() {

        // Create a ContentValues object where column names are the keys,
        // and Test game attributes are the values.
        ContentValues values = new ContentValues();
        values.put(VideoGameEntry.COLUMN_GAME_TITLE, "Test Shooter 2");
        values.put(VideoGameEntry.COLUMN_GENRE, VideoGameEntry.SHOOTER);
        values.put(VideoGameEntry.COLUMN_PRICE, 50);
        values.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, 5);

        // Insert a new row for Test Shooter 2 into the provider using the ContentResolver.
        // Use the {@link VideoGameEntry#CONTENT_URI} to indicate that we want to insert
        // into the Video Game database table.
        // Receive the new content URI that will allow us to access Test Game data in the future.
        getContentResolver().insert(VideoGameEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all video games in the database.
     */
    private void deleteAllVideoGame() {
        int rowsDeleted = getContentResolver().delete(VideoGameEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from video game database");
    }
}
