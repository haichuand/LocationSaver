package com.example.android.locationsaver;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LocationFragment extends Fragment implements LocationListener, ConnectionCallbacks,
    OnConnectionFailedListener {
    //location update interval in milliseconds
    private final int UPDATE_INTERVAL=5000;
    private final int FASTEST_UPDATE_INTERVAL=1000;
    //default zooms to street level
    private final float DEFAULT_MAP_ZOOM=18;
    private final String TAG = "LocationFragment";

    private OnFragmentInteractionListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private boolean mMoveCameraToCurrentLocation;
    private MainActivity mActivity;
    private TextView mAccuracyView;
    private int mAccuracy; //accuracy of current location

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        createLocationRequest();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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

        if (mGoogleApiClient==null) {
            buildGoogleApiClient();
        }
        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
        else if (mActivity.mViewPager.getCurrentItem()==0) { //only when current fragment is being viewed
            startLocationUpdates();
        }
        mMoveCameraToCurrentLocation = true;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
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
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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
        mActivity = (MainActivity) getActivity();
        if (mActivity.mViewPager.getCurrentItem()==0)
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
        mAccuracy = Math.round(mCurrentLocation.getAccuracy()*3);
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
        Intent intent = new Intent(mActivity, EditEntryActivity.class);
        intent.putExtra(Constants.SOURCE, Constants.LOCATION_FRAGMENT);
        intent.putExtra(Constants.BUNDLE_LOCATION, mCurrentLocation);
        startActivity(intent);
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");
        super.onDetach();
        mListener = null;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
