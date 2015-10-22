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
import java.util.ArrayList;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {
    ArrayList<Long> selectedRowList = new ArrayList<>();

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
        viewHolder.mRowId = mCursor.getLong(LocationDBHandler._ID);
        String name = mCursor.getString(LocationDBHandler.NAME);
        viewHolder.mLocationName.setText(name);
        viewHolder.mLocationCoordinates.setText(mCursor.getDouble(LocationDBHandler.LATITUDE)
                + ", " + mCursor.getDouble(LocationDBHandler.LONGITUDE));
        String address = mCursor.getString(LocationDBHandler.ADDRESS);
        if (address != null) {
            address = address.replace("\n", ", ");
        }
        viewHolder.mLocationAddress.setText(address);
        String imgString = mCursor.getString(LocationDBHandler.IMAGE);
        File imgFile;
        if (imgString!=null){
            imgFile = new  File(imgString);
            if(imgFile.exists()) {
                Uri uri = Uri.fromFile(imgFile);
                viewHolder.mLocationImage.setImageURI(uri);
            }
        }
        else {
            viewHolder.mLocationImage.setImageURI(null);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void changeCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
    }

//    private void changeOptionsMenu() {
//        if (selectedRowList.isEmpty()) {
//            menu.findItem(R.id.action_delete).setVisible(false);
//            menu.findItem(R.id.action_edit).setVisible(false);
//            getActivity().invalidateOptionsMenu();
//        }
//    }
    class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView mLocationName, mLocationCoordinates, mLocationAddress;
        ImageView mLocationImage;
        long mRowId = -1;

        public LocationViewHolder (View v) {
            super(v);
            //Todo: setOnClickListener() to create intent to open location in mapping app
            v.setSelected(false);
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (view.isSelected()) {
                        view.setSelected(false);
                        selectedRowList.remove(new Long(mRowId));
//                        changeOptionsMenu();
                    }
                    else {
                        view.setSelected(true);
                        selectedRowList.add(new Long(mRowId));
                    }
                    return true;
                }
            });

            mLocationName = (TextView) v.findViewById(R.id.location_name);
            mLocationCoordinates = (TextView) v.findViewById(R.id.location_coordinates);
            mLocationAddress = (TextView) v.findViewById(R.id.location_address);
            mLocationImage = (ImageView) v.findViewById(R.id.location_image);
        }

    }
}
