package com.jasonduan.android.locationsaver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activity for edit details of a location entry. It can be launched by the floating action button
 * in LocationFragment or clicking the location item image in ListFragment
 */
public class EditEntryActivity extends AppCompatActivity {

    private static final String TAG = "EditEntryActivity";
    /* tags to indicate if imageView is icon or image */
    private static final int ICON_VIEW = 101;
    private static final int IMAGE_VIEW = 202;

    private EditText mNameView, mCoordView, mNoteView, mAddressView;
    private ImageView mImageView;
    private Location mLocation;
    private boolean isGetAddress; //flag to indicate if we want to get address through FetchAddressService
    private View.OnClickListener mImageClickListener;
    private boolean mDeleteImageFlag;
    /* Receiver registered with this activity to get the response from FetchAddressIntentService.*/
    private ResultReceiver mResultReceiver;

    private String mSaveImagePath, mThumbnailImagePath; //image file paths saved from camera app
    private double mLongitude, mLatitude;
    private Handler mHandler;
    private LocationDBHandler mDbHandler;
    long mRowId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mNameView = (EditText) findViewById(R.id.name_input);
        mCoordView = (EditText) findViewById(R.id.coords_input);
        mNoteView = (EditText) findViewById(R.id.note_input);
        mAddressView = (EditText) findViewById(R.id.address_input);
        mImageView = (ImageView) findViewById(R.id.entry_image);
        //make the name field select all on click for easier editing
        mHandler = new Handler();

        /* mResultReceiver receives data sent from FetchAddressService and updates the address field.*/
        mResultReceiver = new ResultReceiver(mHandler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

                if (resultCode == Constants.SUCCESS_RESULT) {
                    String addressText = resultData.getString(Constants.RESULT_DATA_KEY);
                    mAddressView.setText(addressText);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditEntryActivity.this, getString(R.string.address_received), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (resultCode == Constants.FAILURE_RESULT) {
                    final String errorMessage = resultData.getString(Constants.RESULT_DATA_KEY);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditEntryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        };

        /* Listener to animate click on image or text */
        mImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(EditEntryActivity.this, R.anim.view_click_animator));
                startCameraForImage();
            }
        };

        //to prevent keyboard from appearing when activity is first launched
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeleteImageFlag = false;
        mDbHandler = LocationDBHandler.getDbInstance(this);
        Intent intent = getIntent();
        if (intent == null) return;

        //get where the activity is launched from (LocationFragment, ListFragment or Widget)
        String source = intent.getStringExtra(Constants.SOURCE);
        if (source == null) return;

        //intent is coming from LocationFragment to save location
        if (source.equals(Constants.LOCATION_FRAGMENT)) {
            isGetAddress = true; //get address from FetchAddressService
            if (TextUtils.isEmpty(mNameView.getText())) {
                mNameView.setHint(DateFormat.getDateTimeInstance().format(new Date()));
            }
            mImageView.setImageResource(R.drawable.icon_camera);
            mImageView.setTag(ICON_VIEW);
            mImageView.setOnClickListener(mImageClickListener);
            mLocation = intent.getParcelableExtra(Constants.BUNDLE_LOCATION);
            int accuracyFeet = (int) (mLocation.getAccuracy()/Constants.FOOT_TO_METER);
            mLatitude = mLocation.getLatitude();
            mLongitude = mLocation.getLongitude();
            mCoordView.setText(mLatitude + ", " + mLongitude);
            mNoteView.setText("Accuracy: " + accuracyFeet + " feet");
            mDeleteImageFlag = true;
        }
        //intent is coming from ListFragment to edit list
        else if (source.equals(Constants.LIST_FRAGMENT)) {
            isGetAddress = false; //do not get address from FetchAddressService
            mRowId = intent.getLongExtra(Constants.BUNDLE_DB_ROWID, -1);
            String sqlString = "SELECT * FROM " + LocationDBHandler.LocationEntry.TABLE +
                    " WHERE " + LocationDBHandler.LocationEntry._ID + "=" + mRowId;
            Cursor cursor = mDbHandler.getReadableDatabase().rawQuery(sqlString, null);
            if (cursor.moveToFirst()) {
                mNameView.setText(cursor.getString(LocationDBHandler.NAME));
                mCoordView.setText(cursor.getDouble(LocationDBHandler.LATITUDE) + ", " +
                        cursor.getDouble(LocationDBHandler.LONGITUDE));
                mAddressView.setText(cursor.getString(LocationDBHandler.ADDRESS));
                mNoteView.setText(cursor.getString(LocationDBHandler.NOTE));
                mThumbnailImagePath = cursor.getString(LocationDBHandler.IMAGE);

                if (mThumbnailImagePath != null) {
                    //if full size image is present, set ImageView to use full size image; otherwise use thumbnail image
                    int suffixIndex = mThumbnailImagePath.lastIndexOf("_tn.");
                    String fullSizeImagePath = "";
                    if (suffixIndex > 0) {
                        fullSizeImagePath = mThumbnailImagePath.substring(0, suffixIndex)
                                + mThumbnailImagePath.substring(suffixIndex + 3);
                    }

                    Uri imageUri;
                    File imageFile = new File(fullSizeImagePath);
                    if (imageFile.exists()) {
                        imageUri = Uri.parse(fullSizeImagePath);
                        mImageView.setImageURI(imageUri);
                        mImageView.setTag(IMAGE_VIEW);
                        return;
                    }
                    else {
                        imageFile = new File(mThumbnailImagePath);
                        if (imageFile.exists()) {
                            imageUri = Uri.parse(mThumbnailImagePath);
                            mImageView.setImageURI(imageUri);
                            mImageView.setTag(IMAGE_VIEW);
                            return;
                        }
                    }
                }
                mImageView.setImageResource(R.drawable.icon_camera);
                mImageView.setTag(ICON_VIEW);
                mImageView.setOnClickListener(mImageClickListener);
            }
        }

        /* set different image sizes for icon and user image */
        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
        //convert dp to actual pixels
        final float scale = getResources().getDisplayMetrics().density;
        switch ((int) mImageView.getTag()) {
            case ICON_VIEW:
                params.width = params.height = (int) (80 * scale); //80 dp
                break;
            case IMAGE_VIEW:
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mImageView.setMaxWidth((int) (430 * scale)); //430 dp
                mImageView.setMaxHeight((int) (240 * scale)); //240 dp
                break;
        }

        if (isGetAddress) {
            getAddress();
        }

        //clear the intent source code
        intent.removeExtra(Constants.SOURCE);
    }

    @Override
    public void onPause() {
        mDbHandler.close();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle instanceState) {
        if (mThumbnailImagePath != null) {
            instanceState.putString(Constants.THUMBNAIL_IMAGE_URI, mThumbnailImagePath);
        }
        super.onSaveInstanceState(instanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editentry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                saveLocation();
                break;
            case R.id.cancel:
                cancelActivity();
                break;
            case R.id.get_address:
                setLocationFromEdiText();
                getAddress();
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Start the phone's camera app through intent and return the image to  this activity
     */
    private void startCameraForImage() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String currentDateTime = sdf.format(new Date());
        mSaveImagePath = Constants.IMAGE_DIRECTORY + currentDateTime + ".jpg";
        File savedImageFile = new File(mSaveImagePath);
        if (!savedImageFile.getParentFile().exists()) {
            if (!savedImageFile.getParentFile().mkdirs()) {
                Toast.makeText(this, getString(R.string.error_making_directory) + " " + savedImageFile.getPath(),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
        Uri fileUri = Uri.fromFile(savedImageFile); // create a file to save the image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        // start the image capture Intent
        startActivityForResult(intent, Constants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Receives result code from the camera app. If the result code is ok, starts AsyncTask to
     * resize the image.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            new makeThumbnailTask(mImageView).execute(mSaveImagePath);
        }
    }

    /**
     * Launches FetchAddressService through intent to get text representation of address in mLocation
     */
    private void getAddress() {
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.BUNDLE_LOCATION, mLocation);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }

    /**
     * sets mLocation from the EditText field of Coords
     */
    private void setLocationFromEdiText() {
        String[] coords = mCoordView.getText().toString().split(",");
        if (coords.length == 2) {
            Location location = null;
            try {
                double latitude = Double.valueOf(coords[0].trim());
                double longitude = Double.valueOf(coords[1].trim());
                location = new Location("FromEdiText");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.incorrect_coords, Toast.LENGTH_SHORT).show();
            }
            if (location != null)
                mLocation = location;
        }
    }

    /**
     * Called when user cancels the EditEntryActivity. Deletes images on exit.
     */
    private void cancelActivity() {
        if (mDeleteImageFlag) {
            deleteImages();
        }
        this.finish();
    }

    /**
     * Save or update the current location in database
     */
    private void saveLocation() {
        /* if the activity is launched through clicking an entry in ListFragment, rowId will be set
         * Otherwise the mRowId will be -1 */
        if (mRowId == -1) {
            CharSequence nameText = mNameView.getText();
            if (TextUtils.isEmpty(nameText)) {
                nameText = mNameView.getHint();
            }
            mDbHandler.insertLocation(new LocationItem(nameText.toString(), mLatitude, mLongitude,
                    mAddressView.getText().toString(), mNoteView.getText().toString(), mThumbnailImagePath, System.currentTimeMillis()));
        }
        else {
            ContentValues values = new ContentValues();
            if (mNameView.getText().length() == 0) {
                Toast.makeText(this, R.string.name_field_required, Toast.LENGTH_SHORT).show();
                return;
            }
            String coord[] = mCoordView.getText().toString().split(",");
            Double latitude, longitude;
            try {
                latitude = Double.parseDouble(coord[0]);
                longitude = Double.parseDouble(coord[1]);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.number_field_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            values.put(LocationDBHandler.LocationEntry.COLUMN_NAME, mNameView.getText().toString());
            values.put(LocationDBHandler.LocationEntry.COLUMN_LATITUDE, latitude);
            values.put(LocationDBHandler.LocationEntry.COLUMN_LONGITUDE, longitude);
            values.put(LocationDBHandler.LocationEntry.COLUMN_ADDRESS, mAddressView.getText().toString());
            values.put(LocationDBHandler.LocationEntry.COLUMN_NOTE, mNoteView.getText().toString());
            values.put(LocationDBHandler.LocationEntry.COLUMN_IMAGE, mThumbnailImagePath);
            SQLiteDatabase db = mDbHandler.getWritableDatabase();
            db.update(LocationDBHandler.LocationEntry.TABLE, values,
                    LocationDBHandler.LocationEntry._ID + "=" + mRowId, null);
            db.close();
        }
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Deletes both the thumbnail image and the original image
     */
    private void deleteImages() {
        File file;
        if (mThumbnailImagePath != null && !mThumbnailImagePath.isEmpty()) {
            file = new File(mThumbnailImagePath);
            if (file != null)
                file.delete();
        }
        if (mSaveImagePath != null && !mSaveImagePath.isEmpty()) {
            file = new File(mSaveImagePath);
            if (file != null)
                file.delete();
        }
    }

    /**
     * AsyncTask to resize images taken by camera app. Because the images from camera app often has
     * much higher resolution than is needed for the app, they are resized for faster loading and
     * display. Two image sizes are generated: one with max dimension of 1920 pixels (for display
     * in EditEntryActivity and one with max dimension of 480 (for display in ListFragment).
     */
    class makeThumbnailTask extends AsyncTask<String, Void, String> {
        private final WeakReference<ImageView> imageViewReference;

        public makeThumbnailTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected String doInBackground(String... params) {
            final int maxSize = 1920;
            String imagePath = params[0];
            Bitmap originalBitmap = null;
            Bitmap orientedBitmap = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                //Returns null, sizes are in the options variable
                BitmapFactory.decodeFile(imagePath, options);
                double maxDim = (options.outWidth > options.outHeight ? options.outWidth : options.outHeight);
                int inSampleSize;
                //try to determine the correct inSampleSize for the image
                for (inSampleSize = 1; maxDim / inSampleSize / maxSize >= 2.0; inSampleSize *= 2) ;

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                originalBitmap = BitmapFactory.decodeFile(imagePath, options);

                //The image could be incorrectly oriented depending on the orientation of the phone when
                //the image is taken. We use a utility method to re-orient the image
                orientedBitmap = ImageUtils.rotateBitmap(imagePath, originalBitmap);
            } catch (Exception e) {
                Log.e("EditEntryActivity", "Error decoding bitmap " + imagePath);
            }

            if (orientedBitmap == null) {
                Log.e("EditEntryActivity", "Failure getting Bitmap from " + imagePath);
                return null;
            }


            int outWidth;
            int outHeight;
            int inWidth = orientedBitmap.getWidth();
            int inHeight = orientedBitmap.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }

            //resizes image to max dimension 1920 and overwrites original file
            Bitmap resized = Bitmap.createScaledBitmap(orientedBitmap, outWidth, outHeight, false);
            File imageFile = new File(imagePath);
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                resized.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e("makeThumbnailTask", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e("makeThumbnailTask", "Error accessing file: " + e.getMessage());
            }

            //resizes image to max dimension 480 for show in ListFragment
            Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(resized, (int) (outWidth / 4), (int) (outHeight / 4), false);
            int dotIndex = imagePath.lastIndexOf(".");
            String baseImageName;
            if (dotIndex > 0) {
                baseImageName = imagePath.substring(0, dotIndex);
            } else {
                Log.e("makeThumbnailTask", "Error extracting base image name");
                return null;
            }
            mThumbnailImagePath = baseImageName + "_tn.jpg";
            imageFile = new File(mThumbnailImagePath);
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e("makeThumbnailTask", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e("makeThumbnailTask", "Error accessing file: " + e.getMessage());
            }
            return imagePath;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(String imagePath) {
            ImageView imageView = imageViewReference.get();
            if (imageView == null) {
                return;
            }
            if (imagePath == null || imagePath.isEmpty()) {
                imageView.setImageResource(R.drawable.icon_camera);
                imageView.setOnClickListener(mImageClickListener);
                return;
            }
            imageView.setImageURI(Uri.parse(imagePath));
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            final float scale = getResources().getDisplayMetrics().density;
            mImageView.setMaxWidth((int) (430 * scale));
            mImageView.setMaxHeight((int) (240 * scale));
            mImageView.setTag(IMAGE_VIEW);
        }
    }
}
