package com.example.android.videogameinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;

import static android.R.attr.id;
import static com.example.android.videogameinventory.data.VideoGameDbHelper.LOG_TAG;

/**
 * Created by jermainegoins on 8/3/17.
 */

public class VideoGameProvider extends ContentProvider {

    private static final String TAG = VideoGameProvider.class.getSimpleName();

    // URI matcher code for the content URI for the pets table
    private static final int VIDEO_GAME = 100;

    // URI matcher code for the content URI for a single pet in the pets table
    private static final int VIDEO_GAME_ID = 101;

    /*
        UriMatcher object to match a content URI to a corresponding code. The input passed into the
        constructor represents the code to return for the root URI. It's common to use NO_MATCH as
        the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(VideoGameContract.CONTENT_AUTHORITY, VideoGameContract.PATH_VIDEOGAME, VIDEO_GAME);
        sUriMatcher.addURI(VideoGameContract.CONTENT_AUTHORITY, VideoGameContract.PATH_VIDEOGAME + "/#", VIDEO_GAME_ID);
    }

    String[] projection = {
            VideoGameEntry._ID,
            VideoGameEntry.COLUMN_GAME_TITLE,
            VideoGameEntry.COLUMN_GENRE,
            VideoGameEntry.COLUMN_CURRENT_INVENTORY,
            VideoGameEntry.COLUMN_PRICE,
            VideoGameEntry.COLUMN_IMAGE
    };

    private VideoGameDbHelper videoGameDbHelper;

    @Override
    public boolean onCreate() {
        videoGameDbHelper = new VideoGameDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = videoGameDbHelper.getReadableDatabase();

        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case VIDEO_GAME:
                // For the VIDEO_GAME code, query the video games table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the video games table.

                cursor = db.query(VideoGameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            case VIDEO_GAME_ID:
                // For the VIDEO_GAME_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.videogameinventory/videogameinventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                // This will perform a query on the video games table where the _id equals 3

                selection = VideoGameContract.VideoGameEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };

                // Cursor containing that row of the table.
                cursor = db.query(VideoGameEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        /*
            Set notification URI on the Cursor, so we know what content URI the Cursor was created for.
            If the data at this URI changes,we know we need to update the Cursor.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIDEO_GAME:
                return VideoGameEntry.CONTENT_LIST_TYPE;

            case VIDEO_GAME_ID:
                return VideoGameEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIDEO_GAME:
                return insertVideoGame(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a video game into the db with the given content values. Return the new content URI
     * for that specific row in the db.
     */
    private Uri insertVideoGame(Uri uri, ContentValues values) {

        checkMe(values);

        SQLiteDatabase db = videoGameDbHelper.getWritableDatabase();

        long id = db.insert(VideoGameEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.d(TAG, "Cannot insert row for " + uri);
        }

        // Notify all listeners that the data has changed for the Video Game content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = videoGameDbHelper.getWritableDatabase();
        // Track the number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case VIDEO_GAME:
                // Delete all rows that match that specific selection and/or selection args
                // Delete all rows that match the selection and selection args
                // For case VIDEO_GAME:

                rowsDeleted = db.delete(VideoGameEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case VIDEO_GAME_ID:
                // Delete a single row given be the ID in the URI
                selection = VideoGameEntry._ID + "=?";

                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };

                // Delete a single row given by the ID in the URI
                // For case VIDEO_GAME_ID:
                rowsDeleted = db.delete(VideoGameEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case VIDEO_GAME:
                return updateVideoGame(uri, values, selection, selectionArgs);

            case VIDEO_GAME_ID:

                /*
                    Extract the ID from the URI, so we know which row to update. Selection will be
                    "_id=?" and selection argument will be a string array containing the actual ID
                 */
                selection = VideoGameEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri)
                        )};

                return updateVideoGame(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not support for " + uri);
        }
    }

    /*
        Update pets in the db with the given content values, Apply the changes to the rows
        specified in the selection and selection argument (which could be 0 or 1 or more pets. Return
        the number of the rows that were successfully updated
     */
    private int updateVideoGame(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        checkMe(values);

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = videoGameDbHelper.getWritableDatabase();

        int updatedRows = db.update(VideoGameEntry.TABLE_NAME, values, selection, selectionArgs);
        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }

    public void checkMe(ContentValues values) {

        // Check that the Title is not null
        if (values.containsKey(VideoGameEntry.COLUMN_GAME_TITLE)) {
            String title = values.getAsString(VideoGameEntry.COLUMN_GAME_TITLE);

            if (title == null) {
                throw new IllegalArgumentException("Video game requires a title.");
            }
        }

        // Check that the Genre is not null
        if (values.containsKey(VideoGameEntry.COLUMN_GENRE)) {
            String genre = values.getAsString(VideoGameEntry.COLUMN_GENRE);

            if (genre == null) {
                throw new IllegalArgumentException("Video game requires a genre.");
            }
        }

        // Check that the Inventory is not null
        if (values.containsKey(VideoGameEntry.COLUMN_CURRENT_INVENTORY)) {
            String inventory = values.getAsString(VideoGameEntry.COLUMN_CURRENT_INVENTORY);

            if (inventory == null) {
                throw new IllegalArgumentException("Video game requires number of Stock.");
            }
        }

        // Check that the Price is not null
        if (values.containsKey(VideoGameEntry.COLUMN_PRICE)) {
            String price = values.getAsString(VideoGameEntry.COLUMN_PRICE);

            if (price == null) {
                throw new IllegalArgumentException("Video game requires a Price.");
            }
        }

        // If the id = -1, then the insert failed. Log the error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row");
        }

    }
}
