package com.example.android.videogameinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.videogameinventory.data.VideoGameContract.VideoGameEntry;

import static android.content.Intent.ACTION_PICK;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the Video Game data loader
     */
    private static final int EXISTING_VIDEO_GAME_LOADER = 0;

    // URI for pick image
    private static final int PICK_IMAGE = 100;

    /*
        Targets image within the devices photo gallery
     */
    ImageView targetImageLocation;

    // Access com.example.android.videogameinventory.Universal class to input dialogs
    private Universal universal;

    /**
     * Content URI for the existing Video Game (null if it's a new Video Game)
     */
    private Uri mCurrentVideoGameUri;

    /**
     * EditText field to enter the Video Game title
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the current Video Game Inventory
     */
    private EditText mCurrentInventoryNumberEditText;

    /**
     * Spinner field to enter the Genre Spinner
     */
    private Spinner mGenreSpinner;

    /**
     * EditText field to enter the Video Game price
     */
    private EditText mPriceEditText;

    private Uri targetUri;

    /*
        ImageView field to enter the Image
     */
    private ImageView mAddImageView;
    private String titleMissing;
    private String inventoryMissing;
    private String genreMissing;
    private String priceMissing;
    private String graphicMissing;
    /**
     * Genre of the video game. The possible valid values are in the VideoGameContract.java file:
     * {@link VideoGameEntry}, {@link VideoGameEntry}, or
     * {@link VideoGameEntry}.
     */
    private int mGenre = VideoGameEntry.UNKNOWN;

    /**
     * Boolean flag that keeps track of whether the Video Game has been edited (true) or not (false)
     */
    private boolean mVideoGameHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mVideoGameHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVideoGameHasChanged = true;
            return false;
        }
    };

    public EditorActivity(String titleMissing, String inventoryMissing, String genreMissing, String priceMissing, String graphicMissing) {
        this.titleMissing = titleMissing;
        this.inventoryMissing = inventoryMissing;
        this.genreMissing = genreMissing;
        this.priceMissing = priceMissing;
        this.graphicMissing = graphicMissing;
    }

    public EditorActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new Video Game or editing an existing one.
        Intent intent = getIntent();
        mCurrentVideoGameUri = intent.getData();

        // If the intent DOES NOT contain a Video Game content URI, then we know that we are
        // creating a new Video Game.
        if (mCurrentVideoGameUri == null) {
            // This is a new Video Game, so change the app bar to say "Add a Video Game"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a Video Game that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing Video Game, so change app bar to say "Edit Video Game"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            // Initialize a loader to read the video game data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_VIDEO_GAME_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_title_name);
        mCurrentInventoryNumberEditText = (EditText) findViewById(R.id.edit_current_inventory_number);
        mGenreSpinner = (Spinner) findViewById(R.id.spinner_genre);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mAddImageView = (ImageView) findViewById(R.id.video_game_image);
        ImageView emptyShelter = (ImageView) findViewById(R.id.empty_shelter_image);
        TextView emptyTitleText = (TextView) findViewById(R.id.empty_title_text);
        TextView emptySubtitleText = (TextView) findViewById(R.id.empty_subtitle_text);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        TextView genreText = (TextView) findViewById(R.id.genre_text);
        TextView priceText = (TextView) findViewById(R.id.price_text);
        TextView inventoryText = (TextView) findViewById(R.id.inventory_text);
        TextView name = (TextView) findViewById(R.id.name);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mCurrentInventoryNumberEditText.setOnTouchListener(mTouchListener);
        mGenreSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        setupSpinner();

        // Locate Add Image button within Editor xml
        Button addImage = (Button) findViewById(R.id.add_image);

        //Locates Video Game Image within Details xml
        targetImageLocation = (ImageView) findViewById(R.id.video_game_image);

        // When button is pressed intent will go to internal photo gallery
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the genre of the video game.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genreSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_genre_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenreSpinner.setAdapter(genreSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.genre_puzzle))) {
                        mGenre = VideoGameEntry.PUZZLE;
                    } else if (selection.equals(getString(R.string.genre_RPG))) {
                        mGenre = VideoGameEntry.RPG;
                    } else if (selection.equals(getString(R.string.genre_action))) {
                        mGenre = VideoGameEntry.ACTION;
                    } else if (selection.equals(getString(R.string.genre_sports))) {
                        mGenre = VideoGameEntry.SHOOTER;
                    } else if (selection.equals(getString(R.string.genre_VR))) {
                        mGenre = VideoGameEntry.VR;
                    } else {
                        mGenre = VideoGameEntry.UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGenre = VideoGameEntry.UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save Video Game into database.
     */
    private void saveVideoGame() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mNameEditText.getText().toString().trim();
        String inventoryString = mCurrentInventoryNumberEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        int inventory;
        int price;

        try {
            inventory = Integer.parseInt(inventoryString);
        } catch (NumberFormatException e) {
            inventory = 0;
        }

        try {
            price = Integer.parseInt(priceString);
        } catch (NumberFormatException e) {
            price = 0;
        }

        // Check if this is supposed to be a new Video Game
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(titleString) || (targetUri == null) || TextUtils.isEmpty(inventoryString)) {

            // Displays to user to fix areas not filled
            Toast.makeText(this, "Please fill in all the required entry(s)", Toast.LENGTH_SHORT).show();

            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and Video Game attributes from the editor are the values.
        ContentValues values = new ContentValues();

        values.put(VideoGameEntry.COLUMN_GAME_TITLE, titleString);
        values.put(VideoGameEntry.COLUMN_CURRENT_INVENTORY, inventory);
        values.put(VideoGameEntry.COLUMN_GENRE, mGenre);
        values.put(VideoGameEntry.COLUMN_PRICE, price);

        // If targetUri image is null insert default image into database
        if (targetUri == null) {

            ImageView mAddImageView = (ImageView) findViewById(R.id.video_game_image);
            mAddImageView.setImageResource(R.drawable.no_photo);
        } else {

            // Stores selected image
            values.put(VideoGameEntry.COLUMN_IMAGE, targetUri.toString());
        }

        // Determine if this is a new or existing Video Game by checking if mCurrentVideoGameUri is null or not
        if (mCurrentVideoGameUri == null) {
            // This is a NEW Video Game, so insert a new Video Game into the provider,
            // returning the content URI for the new Video Game.
            Uri newUri = getContentResolver().insert(VideoGameEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_game_successful),
                        Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new Video Game, hide the "Delete" menu item.
        if (mCurrentVideoGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save Video Game to database
                saveVideoGame();
                // Exit activity
                finish();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete Video Game from database
                universal.showDeleteConfirmationDialog(this, mCurrentVideoGameUri);
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case R.id.home:
                // If the Video Game hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mVideoGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                universal.showUnsavedChangesDialog(discardButtonClickListener, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the Video Game hasn't changed, continue with handling back button press
        if (!mVideoGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        universal.showUnsavedChangesDialog(discardButtonClickListener, this);
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

        // Bail early if the cursor is null or there is less than 1 row in the cursor
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
            targetUri = Uri.parse(image);
            mAddImageView.setImageURI(targetUri);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(title);
            mCurrentInventoryNumberEditText.setText(Integer.toString(currentInventory));
            mPriceEditText.setText(Integer.toString(price));

            // Genre is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options.
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (genre) {
                case VideoGameEntry.PUZZLE:
                    mGenreSpinner.setSelection(0);
                    break;
                case VideoGameEntry.RPG:
                    mGenreSpinner.setSelection(1);
                    break;
                case VideoGameEntry.ACTION:
                    mGenreSpinner.setSelection(2);
                    break;
                case VideoGameEntry.SPORTS:
                    mGenreSpinner.setSelection(3);
                    break;
                case VideoGameEntry.SHOOTER:
                    mGenreSpinner.setSelection(4);
                    break;
                case VideoGameEntry.VR:
                    mGenreSpinner.setSelection(5);
                    break;
                default:
                    mGenreSpinner.setSelection(6);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mCurrentInventoryNumberEditText.setText("");
        mGenreSpinner.setSelection(6); // Select "Unknown" gender
        mPriceEditText.setText("");
        mAddImageView.setImageURI(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            // Pulls Uri data and assign to targetUri
            targetUri = data.getData();

            // Insert targetUri into targetImageLocation
            targetImageLocation.setImageURI(targetUri);
        }
    }
}
