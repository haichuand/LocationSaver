package com.example.android.locationsaver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


/**
 *
 */

public class LocationFragment extends Fragment implements LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener {
    //location update interval in milliseconds
    private static final int UPDATE_INTERVAL=3000;
    private final int FASTEST_UPDATE_INTERVAL=1000;
    //default zooms to street level
    private final float DEFAULT_MAP_ZOOM=18;
    private final String TAG = "LocationFragment";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private boolean mMoveCameraToCurrentLocation;
    //    private MainActivity mActivity;
    private TextView mAccuracyView;
    private int mAccuracy; //accuracy of current location
    private ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.context_menu_locationfragment, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            switch (id) {
                case R.id.action_remove_marker:
                    mMap.clear();
                    mActionMode.finish();
                    break;
                default:
                    assert false;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMap.clear();
            mActionMode = null;
        }
    };

    public LocationFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        mMoveCameraToCurrentLocation = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);
        mAccuracyView = (TextView) view.findViewById(R.id.text_accuracy);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_save_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentLocation();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
//        mMoveCameraToCurrentLocation = true;
    }


    @Override
    public void onPause() {
//        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
//        if(mActivity==null)
//            Log.d(TAG, "onPause: getActivity() returns null");
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient!= null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

//    @Override
//    public void onActivityResult (int requestCode, int resultCode, Intent data) {
//        ((MainActivity) getActivity()).onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    private synchronized void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else if (((MainActivity) getActivity()).mViewPager.getCurrentItem() == 0) { //only when current fragment is being viewed
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
            startLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged() called");
        mCurrentLocation = location;
        mAccuracy = (int) Math.round(mCurrentLocation.getAccuracy()/0.3045);
        mAccuracyView.setText("Accuracy: " + mAccuracy + " feet");
        if (mMoveCameraToCurrentLocation) {
            moveMapCamera();
            mMoveCameraToCurrentLocation = false;
        }
    }


    private void moveMapCamera() {
        if (mCurrentLocation!= null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                    DEFAULT_MAP_ZOOM);
            mMap.animateCamera(cameraUpdate);
        }
    }


    public void saveCurrentLocation() {
        Intent intent = new Intent(getContext(), EditEntryActivity.class);
        intent.putExtra(Constants.SOURCE, Constants.LOCATION_FRAGMENT);
        intent.putExtra(Constants.BUNDLE_LOCATION, mCurrentLocation);
        getActivity().startActivityForResult(intent, Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE);
    }


    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");
        super.onDetach();
    }

    public void showMarkers(List<MarkerOptions> markers) {
        CameraUpdate cu;
        if (markers.size()==0) {
            Log.d(TAG, "showMarkers(List<MarkerOptions>): markers list empty");
            return;
        }

        //if there is only one marker, use default map zoom; otherwise calculate bounds to show all markers
        if (markers.size()==1) {
            mMap.addMarker(markers.get(0));
            cu = CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), DEFAULT_MAP_ZOOM);
        }
        else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder(); //to show all markers on the map
            for (MarkerOptions marker : markers) {
                mMap.addMarker(marker);
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 80; // offset from edges of the map in pixels
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        }

        mMap.animateCamera(cu);
        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
    }


//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setMyLocationEnabled(true);
//    }

}
