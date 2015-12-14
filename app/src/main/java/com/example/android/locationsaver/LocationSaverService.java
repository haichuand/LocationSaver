package com.example.android.locationsaver;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
 *
 */
public class LocationSaverService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = "LocationSaverService";
    private static final long UPDATE_INTERVAL=1000L; //update interval of location request, Unit: ms
    private static final float accuracyThreshold = 20; //threshold of accuracy to save current location, Unit: meters
    private static final long timeoutThreshold = 60000L; //threshold of time out to stop service, Unit: ms
    private long timeoutMilliseconds;
    private Handler handler;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location mLocation;

    ResultReceiver mResultReceiver = new ResultReceiver(handler) {

        /* Receives data sent from FetchAddressService and updates the address field.*/
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mLocation == null) {
                Log.e(TAG, "mLocation is null");
                return;
            }

            if (resultCode == Constants.SUCCESS_RESULT) {
                String addressText = resultData.getString(Constants.RESULT_DATA_KEY)
                        .replace(System.getProperty("line.separator"), " ");
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
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        timeoutMilliseconds = System.currentTimeMillis();

        if (intent != null) {
            String source = intent.getStringExtra(Constants.SOURCE);
            if (source != null) {
                switch(source) {
                    case Constants.LOCATION_WIDGET_ADD_BUTTON:
                        Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
                        broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_IN_PROGRESS);
                        sendBroadcast(broadcastIntent);
                        saveCurrentLocation();
                        break;
                    case Constants.LOCATION_WIDGET_SHOW_LOCATION:
                        Intent mainActivityIntent = new Intent(this, MainActivity.class);
                        mainActivityIntent.putExtra(Constants.SOURCE, Constants.LIST_FRAGMENT);
                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivityIntent);
                        break;
                    default:
                        assert false;
                }

            }
        }
    }

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
//        this.stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {

        //if timeout period passes without getting into required accuracy, broadcast failed message
        if (System.currentTimeMillis() - timeoutMilliseconds > timeoutThreshold) {
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

        float accuracy = location.getAccuracy();
        if (accuracy <= accuracyThreshold) {
            mLocation = location;
            getAddress(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private String addLocationToDb(Location location, String address) {
        String name = DateFormat.getDateTimeInstance().format(new Date());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LocationItem item = new LocationItem(name, latitude, longitude, address, null, null, System.currentTimeMillis());
        LocationDBHandler dbHandler = new LocationDBHandler(this);
        dbHandler.insertLocation(item);
        return name;
    }

    /**
     * Send broadcast to widget
     * @param name
     * @param description
     */
    private void sendLocationSavedBroadcast(String name, String description) {
        Intent broadcastIntent = new Intent(Constants.LOCATIONSAVERSERVICE_BROADCAST);
        broadcastIntent.putExtra(Constants.SOURCE, Constants.LOCATION_SAVED);
        broadcastIntent.putExtra(Constants.LOCATION_NAME, name);
        broadcastIntent.putExtra(Constants.LOCATION_DESCRIPTION, description);
        sendBroadcast(broadcastIntent);
    }

    //get text representation of address in mLocation
    private void getAddress(Location location) {
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.BUNDLE_LOCATION, location);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }

}

