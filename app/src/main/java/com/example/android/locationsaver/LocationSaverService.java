package com.example.android.locationsaver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationSaverService extends IntentService {

    private static final String TAG = "LocationSaverService";

    public LocationSaverService() {
        super("LocationSaverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String source = intent.getStringExtra(Constants.SOURCE);
            if (source != null && source.equals(LocationWidget.TAG)) {
                saveCurrentLocation();
            }

            //send broadcast to widget
            Intent broadcastIntent = new Intent(Constants.LOCATION_SAVED_BROADCAST);
            broadcastIntent.putExtra(Constants.LOCATION_TEXT, "Test location name");
            sendBroadcast(broadcastIntent);
        }

    }

    private void saveCurrentLocation() {
        Log.d(TAG, "saveCurrentLocation() called");
    }

}

