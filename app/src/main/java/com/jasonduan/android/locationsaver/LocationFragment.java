package com.jasonduan.android.locationsaver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


/**
 * Fragment to show Google Map and let user save current location
 */
public class LocationFragment extends Fragment implements LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener, OnMapReadyCallback {
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
    boolean mMoveCameraToCurrentLocation; //if we want to move map view to current location
    private TextView mAccuracyView;
    private String accuracyString1, accuracyString2;
    private int mAccuracy; //accuracy of current location
    private ActionMode mActionMode;

    /* ActionMode menu to let user remove markers from map*/
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
    public void onAttach(Context context) {
        super.onAttach(context);
        buildGoogleApiClient(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        mMoveCameraToCurrentLocation = true; //set to true when fragment is first created
        accuracyString1 = getString(R.string.text_accuracy_string1);
        accuracyString2 = getString(R.string.text_accuracy_string2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mAccuracyView = (TextView) view.findViewById(R.id.text_accuracy);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_save_location);
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentLocation();
            }
        });
        if (mMap == null) {
            mMapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap == null) {
            mMapFragment.getMapAsync(this);
        }
        else {
            startLocationUpdates();
        }
    }


    @Override
    public void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    /**
     * Disconnects Google API Client to save power
     */
    @Override
    public void onStop() {
        if (mGoogleApiClient!= null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Build Google API Client for use to get current location
     * @param context
     */
    private synchronized void buildGoogleApiClient(Context context) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

    }

    /**
     * Set location request parameters
     */
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
            buildGoogleApiClient(getContext());
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        //only when current fragment is being viewed and location permission is granted
        else if (((MainActivity) getActivity()).mViewPager.getCurrentItem() == 0 &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Start location update when Google API Client is connected
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
            startLocationUpdates();
    }

    /**
     * Reconnects Google API Client when connection is suspended
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
//        Log.d(TAG, "GoogleApiClient connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.d(TAG, "GoogleApiClient connection failed");
    }

    /**
     * Call back from Goolge API Client whenever the location is changed
     * @param location Current location
     */
    @Override
    public void onLocationChanged(Location location) {
//        Log.d(TAG, "onLocationChanged() called");
        mCurrentLocation = location;
        //convert accuracy from meters to feet
        mAccuracy = (int) Math.round(mCurrentLocation.getAccuracy()/0.3045);
        mAccuracyView.setText(accuracyString1 + " " + mAccuracy + " " + accuracyString2);

        //moves camera to current location only once, so that when user navigates back, the previous
        //view is kept
        if (mMoveCameraToCurrentLocation) {
            moveMapCamera();
            mMoveCameraToCurrentLocation = false;
        }
    }

    /**
     * Move map view to current location and set zoom level to default zoom
     */
    private void moveMapCamera() {
        if (mCurrentLocation!= null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                    DEFAULT_MAP_ZOOM);
            mMap.animateCamera(cameraUpdate);
        }
    }

    /**
     * Launches EditEntryActivity to edit and save current location
     */
    public void saveCurrentLocation() {
        Intent intent = new Intent(getContext(), EditEntryActivity.class);
        intent.putExtra(Constants.SOURCE, Constants.LOCATION_FRAGMENT);
        intent.putExtra(Constants.BUNDLE_LOCATION, mCurrentLocation);
        getActivity().startActivityForResult(intent, Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Show a list of markers on Google Map by automatically setting the necessary zoom level.
     * If there is only one marker in the list, set the zoom level to default
     * @param markers Markers to show on the map
     */
    public void showMarkers(List<MarkerOptions> markers) {
        CameraUpdate cu;
        if (markers.size()==0) {
//            Log.d(TAG, "showMarkers(List<MarkerOptions>): markers list empty");
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                CameraUpdate center = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_MAP_ZOOM);
                mMap.moveCamera(center);
            }
            startLocationUpdates();
        }
    }
}
