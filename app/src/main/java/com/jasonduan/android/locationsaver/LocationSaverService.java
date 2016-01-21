package com.jasonduan.android.locationsaver;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Service to handle widget clicks
 */
public class LocationSaverService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = "LocationSaverService";
    private static final long UPDATE_INTERVAL=1000L; //update interval of location request, Unit: ms
    private static final float accuracyThreshold = 20; //threshold of accuracy to save current location, Unit: meters
    private static final long timeoutThreshold = 60000L; //threshold of time out to stop service, Unit: ms
    private long timeoutCounter; //counter for time spent in the service
    private Handler handler;
    private LocationDBHandler dbHandler;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location mLocation;

    ResultReceiver mResultReceiver = new ResultReceiver(handler) {

        /* Receives data sent from FetchAddressService and updates the address text.*/
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mLocation == null) {
                Log.e(TAG, "mLocation is null");
                return;
            }

            //sets the address field depending on if address text is obtained from FetchAddressService
            if (resultCode == Constants.SUCCESS_RESULT) {
                String addressText = resultData.getString(Constants.RESULT_DATA_KEY)
                        .replace(System.getProperty("line.separator"), ", ");
                String name = addLocationToDb(mLocation, addressText);
                sendLocationSavedBroadcast(name, addressText);
            }
            else if (resultCode == Constants.FAILURE_RESULT){
                String name = addLocationToDb(mLocation, null);
                sendLocationSavedBroadcast(name, mLocation.getLatitude() + ", " + mLocation.getLongitude());
            }
            else {
                Log.e(TAG, "resultCode mismatch");
            }
        }

    };

    public LocationSaverService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        dbHandler = LocationDBHandler.getDbInstance(this);
        //check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocationSaverService.this, R.string.no_location_permission, Toast.LENGTH_LONG).show();
                }
            });
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        dbHandler.close();
        super.onDestroy();
    }

    /**
     * Intents from the widget are handled here
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        //sets the timeout counter when a new intent is received
        timeoutCounter = System.currentTimeMillis();

        if (intent != null) {
            String source = intent.getStringExtra(Constants.SOURCE);
            if (source != null) {
                switch(source) {
                    //get current location and save to db, broadcasting in progress message to widget
                    case Constants.LOCATION_WIDGET_ADD_BUTTON:
                        Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
                        broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_IN_PROGRESS);
                        sendBroadcast(broadcastIntent);
                        saveCurrentLocation();
                        break;
                    case Constants.LOCATION_WIDGET_SHOW_LOCATION: //launches ListFragment in app to show location
                        Intent mainActivityIntent = new Intent(this, MainActivity.class);
                        mainActivityIntent.putExtra(Constants.SOURCE, Constants.LIST_FRAGMENT);
                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivityIntent);
                        break;
                    //retrieve first location item in database and broadcast to widget
                    //if database contains no items, broadcast "no location saved" message
//                    case Constants.LOCATION_WIDGET_GET_FIRST_LOCATION:
//                        broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
//                        broadcastIntent.putExtra(Constants.SOURCE, Constants.FIRST_LOCATION_DISPLAY);
//                        Cursor cursor = dbHandler.getReadableDatabase().query(LocationDBHandler.LocationEntry.TABLE,
//                                null, null, null, null, null, LocationDBHandler.LocationEntry.COLUMN_TIME + " DESC", "1");
//                        if (cursor.moveToFirst()) {
//                            String name = cursor.getString(LocationDBHandler.NAME);
//                            String description = cursor.getString(LocationDBHandler.ADDRESS);
//                            if (description == null || description.isEmpty()) {
//                                description = cursor.getDouble(LocationDBHandler.LATITUDE) + ", " + cursor.getDouble(LocationDBHandler.LONGITUDE);
//                            }
//
//                            broadcastIntent.putExtra(Constants.LOCATION_NAME, name);
//                            broadcastIntent.putExtra(Constants.LOCATION_DESCRIPTION, description);
//                        }
//                        else {
//                            broadcastIntent.putExtra(Constants.LOCATION_NAME, getString(R.string.no_saved_location));
//                            broadcastIntent.putExtra(Constants.LOCATION_DESCRIPTION, getString(R.string.press_save_button_hint));
//                        }
//                        sendBroadcast(broadcastIntent);
//                        break;
                    default:
                        assert false;
                }

            }
        }
    }

    /**
     * Create location request and connects with Google API Client
     */
    private void saveCurrentLocation() {
        //creates location request if it is null
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        if (googleApiClient == null) {
            googleApiClient= new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
        broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_FAILED);
        sendBroadcast(broadcastIntent);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LocationSaverService.this, R.string.googleapiclient_connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        //if timeout period passes without getting into required accuracy, broadcast failed message
        if (System.currentTimeMillis() - timeoutCounter > timeoutThreshold) {
            Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
            broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_INACCURATE);
            sendBroadcast(broadcastIntent);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocationSaverService.this, R.string.cannot_achieve_accuracy, Toast.LENGTH_LONG).show();
                }
            });

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        //saves the location to database when the accuracy is lower than the threshold
        float accuracy = location.getAccuracy();
        if (accuracy <= accuracyThreshold) {
            mLocation = location;
            getAddress(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    /**
     * Add a location item to database
     * @param location The location to be saved to database
     * @param address The address field of the location item
     * @return Name of the saved location, which is the current date and time
     */
    private String addLocationToDb(Location location, String address) {
        String name = DateFormat.getDateTimeInstance().format(new Date());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        int accuracyFeet = (int) (location.getAccuracy()/Constants.FOOT_TO_METER);
        String note = "Accuracy: " + accuracyFeet + " ft";
        LocationItem item = new LocationItem(name, latitude, longitude, address, note, null, System.currentTimeMillis());
        dbHandler.insertLocation(item);
        return name;
    }

    /**
     * Send broadcast to widget when the location is successfully saved
     * @param name The name of the location to be displayed
     * @param description The description of the location, which can be address text (if it can be obtained)
     *                    or coordinates of the location
     */
    private void sendLocationSavedBroadcast(String name, String description) {
        Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
        broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_SAVED);
        broadcastIntent.putExtra(Constants.LOCATION_NAME, name);
        broadcastIntent.putExtra(Constants.LOCATION_DESCRIPTION, description);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Launch FetchAddressService to get address text of a location
     * @param location The location to get address text
     */
    private void getAddress(Location location) {
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.BUNDLE_LOCATION, location);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }

}

