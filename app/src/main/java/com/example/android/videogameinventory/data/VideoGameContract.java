package com.example.android.videogameinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jermainegoins on 8/3/17.
 */

public final class VideoGameContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.videogameinventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_VIDEOGAME = "video_game";

    private VideoGameContract() {
    }

    // Creates table
    public static final class VideoGameEntry implements BaseColumns {

        // The content URI to access the video game data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VIDEOGAME);

        // Name of table
        public final static String TABLE_NAME = "videoGames";

        // Name of columns
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_GAME_TITLE = "title";
        public final static String COLUMN_GENRE = "genre";
        public final static String COLUMN_CURRENT_INVENTORY = "inventory";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_IMAGE = "image";

        public final static int PUZZLE = 0;
        public final static int RPG = 1;
        public final static int ACTION = 2;
        public final static int SPORTS = 3;
        public final static int SHOOTER = 4;
        public final static int VR = 5;
        public final static int UNKNOWN = 6;

        /*
            The MIME type of the CONTENT_URI for a list of VIDEO GAMES
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOGAME;

        /*
            The MIME type of the CONTENT_URI for a specific VIDEO GAME
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOGAME;

    }
}
