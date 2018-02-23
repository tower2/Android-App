package com.example.android.videogameinventory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;
import com.example.android.videogameinventory.data.VideoGameDbHelper;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.android.videogameinventory.R.id.genre_text;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Find all views, buttons, and images in activity_details

    ImageView targetImageLocation;

    TextView titleTextView;
    TextView genreTextView;
    TextView priceTextView;
    TextView inventoryTextView;
    Button addInventoryButton;
    Button decreaseInventoryButton;
    ImageButton deleteGame;
    FloatingActionButton orderFab;
    VideoGameCursorAdapter mAdapter;

    // Uri object to pinpoint specific image
    Uri targetUri;

    // Uri object to pinpoint specific record
    Uri uri;
    // Defined projection that specifies what columns will be used
    String[] projection = {
            VideoGameEntry._ID,
            VideoGameEntry.COLUMN_GAME_TITLE,
            VideoGameEntry.COLUMN_CURRENT_INVENTORY,
            VideoGameEntry.COLUMN_PRICE,
            VideoGameEntry.COLUMN_GENRE
    };
    // initialized  variables that can be used throughout class
    private String inventory;
    private String title;
    private int inventoryInt = 1;
    private int gameIdIndex;
    /**
     * Content URI for the existing Video Game (null if it's a new Video Game)
     */
    private Uri mCurrentVideoGameUri;


    public DetailsActivity(int inventoryInt) {
        this.inventoryInt = inventoryInt;
    }

    public DetailsActivity() {
    }

    public static void showDeleteConfirmationDialog(final DetailsActivity context, final Uri uri) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteVideoGame(context, uri);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static void deleteVideoGame(Activity context, Uri uri) {
        int rowsDeleted = context.getContentResolver().delete(uri, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(context, R.string.video_game_delete_failed, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(context, R.string.video_game_delete_successful,
                    Toast.LENGTH_SHORT).show();
        }
        context.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Defined projection that specifies what columns will be used
        String[] projection = {
                VideoGameEntry._ID,
                VideoGameEntry.COLUMN_GAME_TITLE,
                VideoGameEntry.COLUMN_CURRENT_INVENTORY,
                VideoGameEntry.COLUMN_PRICE,
                VideoGameEntry.COLUMN_GENRE
        };

        VideoGameDbHelper videoGameDbHelper = new VideoGameDbHelper(getBaseContext());
        SQLiteDatabase db = videoGameDbHelper.getReadableDatabase();
        Cursor cursor = db.query(VideoGameEntry.TABLE_NAME, projection, null, null, null, null, null);

        // Locates TextView, Buttons, and FAB
        targetImageLocation = (ImageView) findViewById(R.id.video_game_image);
        titleTextView = (TextView) findViewById(R.id.title_text);
        genreTextView = (TextView) findViewById(R.id.genre_text);
        priceTextView = (TextView) findViewById(R.id.price_text);
        inventoryTextView = (TextView) findViewById(R.id.inventory_text);

        addInventoryButton = (Button) findViewById(R.id.increase_inventory);
        decreaseInventoryButton = (Button) findViewById(R.id.decrease_inventory);
        deleteGame = (ImageButton) findViewById(R.id.delete_game);
        orderFab = (FloatingActionButton) findViewById(R.id.order_fab);


        // Perform button press calculation for addInventory Button
        addInventoryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Get the previous value first
                int oldInventory = Integer.valueOf(inventoryTextView.getText().toString());

                //Determine if previous value is >0(for decrement) & previous value >=0 (for increment)
                if (oldInventory >= 0) {
                    //Add the new value. Make key-pair data and update
                    int newInventory = inventoryInt + oldInventory;

                    // Make sure inventory amount is over and if it is fix it for the user
                    if (newInventory < 100) {

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                        getContentResolver().update(mCurrentVideoGameUri, contentValues, null, null);

                        // Visually confirm change in inventory value
                        // Note: Tried Converting Toast string into string.xml but created an error.
                        Toast.makeText(DetailsActivity.this, "Increased Inventory By 1. ", Toast.LENGTH_SHORT).show();
                    } else {

                        newInventory = 100;

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                        getContentResolver().update(mCurrentVideoGameUri, contentValues, null, null);


                        // Visually confirm change in inventory value
                        // Note: Tried Converting Toast string into string.xml but created an error.
                        Toast.makeText(DetailsActivity.this, "Cannot Exceed 100 units. ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Perform button press calculation for addInventory Button
        decreaseInventoryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Get the previous value first
                int oldInventory = Integer.valueOf(inventoryTextView.getText().toString());

                //Determine if previous value is >0(for decrement) & previous value >=0 (for increment)
                //Add the new value. Make key-pair data and update
                int newInventory = oldInventory - inventoryInt;

                // Make sure inventory amount is over and if it is fix it for the user
                if (newInventory > 0) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                    getContentResolver().update(mCurrentVideoGameUri, contentValues, null, null);

                    // Visually confirm change in inventory value
                    // Note: Tried Converting Toast string into string.xml but created an error.
                    Toast.makeText(DetailsActivity.this, "Decreased Inventory By 1. ", Toast.LENGTH_SHORT).show();
                } else {

                    newInventory = 0;

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, newInventory);
                    getContentResolver().update(mCurrentVideoGameUri, contentValues, null, null);


                    // Visually confirm change in inventory value
                    // Note: Tried Converting Toast string into string.xml but created an error.
                    Toast.makeText(DetailsActivity.this, "Cannot Decrease Below 0 units. ", Toast.LENGTH_SHORT).show();
                }
            }

        });

        // Extract data from specific video game and preceed to adding inventory
        gameIdIndex = cursor.getColumnIndex(VideoGameEntry._ID);

        // Pulls data from MainActivity
        Intent intent = getIntent();
        mCurrentVideoGameUri = intent.getData();

        // Deletes Current Video Game
        deleteGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Universal.showDeleteConfirmationDialog(DetailsActivity.this, mCurrentVideoGameUri);
            }
        });

        // Emails Vendor
        orderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order();
            }
        });

        // Initiate Loader
        getLoaderManager().initLoader(2, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all Video Game attributes, define a projection that contains
        // all columns from the Video Game table
        String[] projection = {
                VideoGameEntry._ID,
                VideoGameEntry.COLUMN_GAME_TITLE,
                VideoGameEntry.COLUMN_GENRE,
                VideoGameEntry.COLUMN_CURRENT_INVENTORY,
                VideoGameEntry.COLUMN_PRICE,
                VideoGameEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentVideoGameUri,   // Query the content URI for the current Video Game
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mAdapter = new VideoGameCursorAdapter(this, cursor);

        // Update {@link VideoGameCursorAdapter} with this new cursor containing updated Video Game data
        mAdapter.swapCursor(cursor);

        // Exit early
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of Video Game attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_GAME_TITLE);
            int genreColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_GENRE);
            int currentInventoryColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_CURRENT_INVENTORY);
            int priceColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(VideoGameEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            int genre = cursor.getInt(genreColumnIndex);
            int currentInventory = cursor.getInt(currentInventoryColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            titleTextView.setText(title);
            inventoryTextView.setText(Integer.toString(currentInventory));
            priceTextView.setText(Integer.toString(price));

            // Genre is a dropdown spinner, so map the constant value from the database
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (genre) {
                case VideoGameEntry.PUZZLE:
                    genreTextView.setText(R.string.genre_puzzle);
                    break;

                case VideoGameEntry.RPG:
                    genreTextView.setText(R.string.genre_RPG);
                    break;

                case VideoGameEntry.ACTION:
                    genreTextView.setText(R.string.genre_action);
                    break;

                case VideoGameEntry.SPORTS:
                    genreTextView.setText(R.string.genre_sports);
                    break;

                case VideoGameEntry.SHOOTER:
                    genreTextView.setText(R.string.genre_shooter);
                    break;

                case VideoGameEntry.VR:
                    genreTextView.setText(R.string.genre_VR);
                    break;

                default:
                    genreTextView.setText(R.string.genre_unknown);
                    break;
            }

            // Permission Request to access image
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck == PERMISSION_GRANTED) {
                // Parse image Uri address and set to setImageUri
                targetUri = Uri.parse(image);

                if (targetUri == null) {
                    return;
                } else {
                    targetImageLocation.setImageURI(targetUri);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Callback called when the data needs to be deleted
        mAdapter.swapCursor(null);

        // If the loader is invalidated, clear out all the data from the input fields.
        titleTextView.setText("");
        inventoryTextView.setText("");
        genreTextView.setText(R.string.genre_unknown); // Select "Unknown" gender
        priceTextView.setText("");
        targetImageLocation.setImageURI(null);

    }

    // Sends email to vendor
    private void order() {
        // Goes to email account to send email to vendor and auto populates email
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto: "));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Video Game Order Form");
        intent.putExtra(Intent.EXTRA_TEXT, "Order " + inventory + " units of title: \n" +
                title + "\n" + genre_text + "\n\n Thank you.");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.missing_email_app, Toast.LENGTH_SHORT).show();
        }
    }

    // Display Toast if Inventory amount tries to go above 100 units
    public void toastOrderAbove() {
        Toast.makeText(this, getString(R.string.above) + 100 + getString(R.string.per_order), Toast.LENGTH_SHORT).show();
    }
}



