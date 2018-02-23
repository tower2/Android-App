package com.example.android.videogameinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;

/**
 * Created by jermainegoins on 8/3/17.
 */

public class VideoGameCursorAdapter extends CursorAdapter {

    VideoGameCursorAdapter mCursorAdapter;

    // Defined projection that specifies what columns will be used
    String[] projection = {
            VideoGameEntry._ID,
            VideoGameEntry.COLUMN_GAME_TITLE,
            VideoGameEntry.COLUMN_CURRENT_INVENTORY,
            VideoGameEntry.COLUMN_PRICE,
            VideoGameEntry.COLUMN_GENRE
    };

    /**
     * Constructs a new {@link VideoGameCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public VideoGameCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
        // videoGameDbHelper = new VideoGameDbHelper(Context context);
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
     * This method binds the Video Game data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current video game can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView genreTextView = (TextView) view.findViewById(R.id.genre_text);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text);
        TextView inventoryTextView = (TextView) view.findViewById(R.id.inventory_text);

        // Find the columns of Video Game attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_GAME_TITLE);
        int genreColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_GENRE);
        int priceColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_PRICE);
        final int inventoryColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_CURRENT_INVENTORY);
        final int gameIdColumnIndex = cursor.getColumnIndex(VideoGameEntry._ID);

        // Read the Video Game attributes from the Cursor for the current Video Game
        String title = cursor.getString(titleColumnIndex);
        final int inventory = cursor.getInt(inventoryColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        int genre = Integer.parseInt(cursor.getString(genreColumnIndex));
        final int gameId = cursor.getInt(gameIdColumnIndex);

        // Update the TextViews with the attributes for the current Video Game
        nameTextView.setText(title);
        priceTextView.setText(String.valueOf("$ " + price));
        inventoryTextView.setText(String.valueOf(context.getString(R.string.stock) + " " + inventory));

        switch (genre) {
            case VideoGameEntry.PUZZLE:
                genreTextView.setText(R.string.genre_puzzle);
                break;
            case VideoGameEntry.RPG:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_RPG));
                break;
            case VideoGameEntry.ACTION:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_action));
                break;
            case VideoGameEntry.SPORTS:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_sports));
                break;
            case VideoGameEntry.SHOOTER:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_shooter));
                break;
            case VideoGameEntry.VR:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_VR));
                break;
            default:
                genreTextView.setText(context.getString(R.string.genre) + " " + context.getString(R.string.genre_unknown));
                break;
        }

        // Find and set sold and addInventory button
        /**
         * Note: addInventoryButton is not implemented because it creates an error, Attempt to invoke
         * null value, but complete method was copied from soldButton method. Only things that was
         * changed was the object name, "+" instead of "-", and changing the Toast message. When I
         * performed a function test it will increase as excepted and decrease as excepted depending
         * on the method that is being implemented. I have no clue why addInventoryButton is the problem.
         */
        Button addInventoryButton = (Button) view.findViewById(R.id.increase_inventory);
        Button soldButton = (Button) view.findViewById(R.id.sold_game);

        // Perform button press calculation for Sold Button
        soldButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Make sure inventory amount is over and if it is fix it for the user
                if (inventory > 0) {

                    // Set newInventory value and continue process to into value into database
                    int newInventory = inventory - 1;
                    Uri videoGameUri = ContentUris.withAppendedId(VideoGameEntry.CONTENT_URI, gameId);

                    ContentValues values = new ContentValues();
                    values.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                    context.getContentResolver().update(videoGameUri, values, null, null);

                    // Visually confirm change in inventory value
                    Toast.makeText(context, context.getString(R.string.game_sold), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.sold_out), Toast.LENGTH_SHORT).show();
                    // Set newInventory value and continue process to into value into database
                    int newInventory = 0;
                    Uri videoGameUri = ContentUris.withAppendedId(VideoGameEntry.CONTENT_URI, gameId);

                    ContentValues values = new ContentValues();
                    values.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                    context.getContentResolver().update(videoGameUri, values, null, null);
                }
            }
        });
    }
}