package com.example.android.locationsaver;


import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {
    private static final int LOCATION_NAME_INDEX = 1, LOCATION_LATITUDE_INDEX = 2,
            LOCATION_LONGITUDE_INDEX = 3, LOCATION_ADDRESS_INDEX = 4, LOCATION_IMAGE_INDEX = 5;

    interface LocationListListener {
        void onListItemClicked(int position);
    }

    private LocationListListener mListener;
    private Cursor mCursor;

    public LocationListAdapter(Cursor cursor, LocationListListener listener) {
        mCursor = cursor;
        mListener = listener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        viewHolder.mLocationName.setText(mCursor.getString(LOCATION_NAME_INDEX));
        viewHolder.mLocationCoordinates.setText(mCursor.getString(LOCATION_LATITUDE_INDEX)
                + ", " + mCursor.getString(LOCATION_LONGITUDE_INDEX));
        viewHolder.mLocationAddress.setText(mCursor.getString(LOCATION_ADDRESS_INDEX));

        File imgFile = new  File(mCursor.getString(LOCATION_IMAGE_INDEX));
        if(imgFile.exists()){
            Uri uri = Uri.fromFile(imgFile);
            viewHolder.mLocationImage.setImageURI(uri);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView mLocationName, mLocationCoordinates, mLocationAddress;
        public ImageView mLocationImage;

        public LocationViewHolder (View v) {
            super(v);
            //Todo: setOnClickListener() to create intent to open location in mapping app

            mLocationName = (TextView) v.findViewById(R.id.location_name);
            mLocationCoordinates = (TextView) v.findViewById(R.id.location_coordinates);
            mLocationAddress = (TextView) v.findViewById(R.id.location_address);
            mLocationImage = (ImageView) v.findViewById(R.id.location_image);
        }

    }
}
