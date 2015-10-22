package com.example.android.locationsaver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditEntryActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = "EditEntryActivity";
    private Toolbar mToolbar;
    private EditText mNameView, mCoordView, mNoteView, mAddressView;
    private ImageView mImageView;
    private Location mLocation;
    //flag to indicate if we want to get address through FetchAddressService
    private View.OnClickListener mImageClickListener;
    protected GoogleApiClient mGoogleApiClient;

    /*Receiver registered with this activity to get the response from FetchAddressIntentService.*/
    private AddressResultReceiver mResultReceiver;
    //image file saved from camera app
    private String mSaveImagePath, mThumbnailImagePath;
    private double mLongitude, mLatitude;

    private LocationDBHandler mDbHandler;
    long mRowId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mNameView = (EditText) findViewById(R.id.name_input);
        mCoordView = (EditText) findViewById(R.id.coords_input);
        mNoteView = (EditText) findViewById(R.id.note_input);
        mAddressView = (EditText) findViewById(R.id.address_input);
        mImageView = (ImageView) findViewById(R.id.entry_image);
        //make the name field select all on click for easier editing
        mResultReceiver = new AddressResultReceiver(new Handler());
        mImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraForImage();
            }
        };
        buildGoogleApiClient();

//        if (savedInstanceState == null) {
//            Intent intent = getIntent();
//            if (intent != null) {
//                String source = intent.getStringExtra(Constants.SOURCE);
//                //intent is coming from LocationFragment to save location
//                if (source.equals(Constants.LOCATION_FRAGMENT)) {
//                    if (TextUtils.isEmpty(mNameView.getText())) {
//                        mNameView.setHint(DateFormat.getDateTimeInstance().format(new Date()));
//                    }
//                    mImageView.setImageResource(R.drawable.icon_camera);
//                    mImageView.setOnClickListener(mImageClickListener);
//                    mLocation = intent.getParcelableExtra(Constants.BUNDLE_LOCATION);
//                    int accuracyFeet = (int) mLocation.getAccuracy() * 3;
//                    mLatitude = mLocation.getLatitude();
//                    mLongitude = mLocation.getLongitude();
//                    mCoordView.setText(mLatitude + ", " + mLongitude);
//                    mNoteView.setText("Accuracy: " + accuracyFeet + " feet");
//                }
//                //intent is coming from ListFragment to edit list
//                else if (source.equals(Constants.LIST_FRAGMENT)) {
//                    mRowId = intent.getLongExtra(Constants.BUNDLE_DB_ROWID, -1);
//                    String sqlString = "SELECT * FROM " + LocationDBHandler.LocationEntry.TABLE +
//                            " WHERE " + LocationDBHandler.LocationEntry._ID + "=" + mRowId;
//                    Cursor cursor = mDbHandler.getReadableDatabase().rawQuery(sqlString, null);
//                    mNameView.setText(cursor.getString(LocationDBHandler.NAME));
//                    mCoordView.setText(cursor.getDouble(LocationDBHandler.LATITUDE) + ", " +
//                            cursor.getDouble(LocationDBHandler.LONGITUDE));
//                    mAddressView.setText(cursor.getString(LocationDBHandler.ADDRESS));
//                    mNoteView.setText(cursor.getString(LocationDBHandler.NOTE));
//                    mThumbnailImagePath = cursor.getString(LocationDBHandler.IMAGE);
//                    if (mThumbnailImagePath != null) {
//                        mImageView.setImageURI(Uri.parse(mThumbnailImagePath));
//                    }
//                    else {
//                        mImageView.setImageURI(null);
//                    }
//                }
//            }
//        }
//        else {
//            mThumbnailImagePath = savedInstanceState.getString(Constants.THUMBNAIL_IMAGE_URI);
//            if(mThumbnailImagePath != null) {
//                mImageView.setImageURI(Uri.parse(mThumbnailImagePath));
//            }
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbHandler = new LocationDBHandler(this);
        Intent intent = getIntent();
        if (intent != null) {
            String source = intent.getStringExtra(Constants.SOURCE);
            //intent is coming from LocationFragment to save location
            if (source.equals(Constants.LOCATION_FRAGMENT)) {
                if (TextUtils.isEmpty(mNameView.getText())) {
                    mNameView.setHint(DateFormat.getDateTimeInstance().format(new Date()));
                }
                mImageView.setImageResource(R.drawable.icon_camera);
                mImageView.setOnClickListener(mImageClickListener);
                mLocation = intent.getParcelableExtra(Constants.BUNDLE_LOCATION);
                int accuracyFeet = (int) mLocation.getAccuracy() * 3;
                mLatitude = mLocation.getLatitude();
                mLongitude = mLocation.getLongitude();
                mCoordView.setText(mLatitude + ", " + mLongitude);
                mNoteView.setText("Accuracy: " + accuracyFeet + " feet");
            }
            //intent is coming from ListFragment to edit list
            else if (source.equals(Constants.LIST_FRAGMENT)) {
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
                        mImageView.setImageURI(Uri.parse(mThumbnailImagePath));
                    } else {
                        mImageView.setImageURI(null);
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        mDbHandler.close();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle instanceState) {
        if (mThumbnailImagePath != null) {
            instanceState.putString(Constants.THUMBNAIL_IMAGE_URI, mThumbnailImagePath);
        }
        super.onSaveInstanceState(instanceState);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void startCameraForImage() {
        // create Intent to take a picture and return control to the calling application

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("EditEntryActivity", "onActivityResult called");
        if (requestCode == Constants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            new makeThumbnailTask(mImageView).execute(mSaveImagePath);
        }
    }


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.BUNDLE_LOCATION, mLocation);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void cancelActivity(View v) {
        deleteImages();
        this.finish();
    }

    public void saveLocation(View v) {
        //if the activity is launched through clicking an entry in ListFragment, rowId will be set
        if (mRowId == -1) {
            CharSequence nameText = mNameView.getText();
            if (TextUtils.isEmpty(nameText)) {
                nameText = mNameView.getHint();
            }
            mDbHandler.insertLocation(new LocationItem(nameText.toString(), mLatitude, mLongitude,
                    mAddressView.getText().toString(), mNoteView.getText().toString(), mThumbnailImagePath));
        } else {
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
     * Receiver for data sent from FetchAddressService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressService and updates the address field.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == Constants.SUCCESS_RESULT) {
                String addressText = resultData.getString(Constants.RESULT_DATA_KEY);
                mAddressView.setText(addressText);
            }
        }
    }

    class makeThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public makeThumbnailTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imagePath = params[0];
            Bitmap originalBitmap = null;
            try {
                originalBitmap = BitmapFactory.decodeFile(imagePath);
            } catch (OutOfMemoryError e) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                try {
                    originalBitmap = BitmapFactory.decodeFile(imagePath, options);
                } catch (OutOfMemoryError error) {
                    options.inSampleSize = 2;
                    originalBitmap = BitmapFactory.decodeFile(imagePath, options);
                }
            } catch (Exception e) {
                Log.d("EditEntryActivity", "Error decoding bitmap " + imagePath);
            }

            if (originalBitmap == null) {
                Log.d("EditEntryActivity", "Failure getting Bitmap from " + imagePath);
                return null;
            }
            final int maxSize = getResources().getInteger(R.integer.thumbnail_size_pixel);
            int outWidth;
            int outHeight;
            int inWidth = originalBitmap.getWidth();
            int inHeight = originalBitmap.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            Bitmap resized = Bitmap.createScaledBitmap(originalBitmap, outWidth, outHeight, false);
            int dotIndex = imagePath.lastIndexOf(".");
            String baseImageName;
            if (dotIndex > 0) {
                baseImageName = imagePath.substring(0, dotIndex);
            } else {
                Log.d("makeThumbnailTask", "Error extracting base image name");
                return null;
            }
            mThumbnailImagePath = baseImageName + "_tn.jpg";
            File thumbnailImageFile = new File(mThumbnailImagePath);
            try {
                FileOutputStream fos = new FileOutputStream(thumbnailImageFile);
                resized.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("makeThumbnailTask", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("makeThumbnailTask", "Error accessing file: " + e.getMessage());
            }
            return resized;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewReference.get();
            if (imageView == null) {
                return;
            }
            if (bitmap == null) {
                //deleteImages();
                imageView.setImageResource(R.drawable.icon_camera);
                imageView.setOnClickListener(mImageClickListener);
                return;
            }
            imageView.setImageBitmap(bitmap);
        }
    }

}
