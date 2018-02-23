package com.example.android.videogameinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;

/**
 * Created by jermainegoins on 8/3/17.
 */

public class VideoGameDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = VideoGameDbHelper.class.getSimpleName();

    // Name of database file
    private static final String DATABASE_NAME = "gameDatabase.db";

    // Database version number, must increment if version changes
    private static final int DATABASE_VERSION = 1;

    // Video Game Database constructor
    public VideoGameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create DB template for Video Game Inventory App
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a string that contains SQL statement to create the Video Game table
        String SQL_CREATE_VIDEO_GAME_TABLE = "CREATE TABLE " + VideoGameEntry.TABLE_NAME + " ("
                + VideoGameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VideoGameEntry.COLUMN_GAME_TITLE + " TEXT NOT NULL, "
                + VideoGameEntry.COLUMN_GENRE + " TEXT NOT NULL, "
                + VideoGameEntry.COLUMN_CURRENT_INVENTORY + " INTEGER NOT NULL, "
                + VideoGameEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + VideoGameEntry.COLUMN_IMAGE + " TEXT);";

        // Run SQL statement
        db.execSQL(SQL_CREATE_VIDEO_GAME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VideoGameEntry.TABLE_NAME);
        onCreate(db);
    }
}