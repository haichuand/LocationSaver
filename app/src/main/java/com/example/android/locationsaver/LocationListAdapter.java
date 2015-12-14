package com.example.android.locationsaver;


import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {
    //    ArrayList<Long> mSelectedRowList = new ArrayList<>();
    ArrayList<Integer> mSelectedItemList = new ArrayList<>();
    boolean isMultiSelect; //flag for whether in multi-selection state
    private LocationListListener mListener;
    private Cursor mCursor;
    public LocationListAdapter(Cursor cursor, LocationListListener listener) {
        mCursor = cursor;
        mListener = listener;
        isMultiSelect = false;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LocationViewHolder viewHolder, final int position) {
        mCursor.moveToPosition(position);

        Boolean isSelected = mSelectedItemList.contains(position);
        viewHolder.itemView.setSelected(isSelected);
        viewHolder.mCheckbox.setVisibility(mSelectedItemList.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        viewHolder.mCheckbox.setOnCheckedChangeListener(null);
        viewHolder.mCheckbox.setChecked(isSelected);
        viewHolder.mCheckbox.setOnCheckedChangeListener(viewHolder);

        if (isMultiSelect) {
            viewHolder.mLocationImage.setOnClickListener(viewHolder.mMultiSelectClickListener);
            viewHolder.mTextLayout.setOnClickListener(viewHolder.mMultiSelectClickListener);
        }
        else {
            viewHolder.mLocationImage.setOnClickListener(viewHolder.mImageClickListener);
            viewHolder.mTextLayout.setOnClickListener(viewHolder.mTextClickListener);
        }



//        viewHolder.mRowId = mCursor.getLong(LocationDBHandler._ID);
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
        if (imgString != null) {
            imgFile = new File(imgString);
            if (imgFile.exists()) {
                Uri uri = Uri.fromFile(imgFile);
                viewHolder.mLocationImage.setImageURI(uri);
                return;
            }
        }
        viewHolder.mLocationImage.setImageResource(R.drawable.icon_image);
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

    interface LocationListListener {
        void onListItemClicked(int clickSource);
    }

    //    private void changeOptionsMenu() {
//        if (mSelectedRowList.isEmpty()) {
//            menu.findItem(R.id.action_delete).setVisible(false);
//            menu.findItem(R.id.action_edit).setVisible(false);
//            getActivity().invalidateOptionsMenu();
//        }
//    }
    class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        TextView mLocationName, mLocationCoordinates, mLocationAddress;
        ImageView mLocationImage;
        View mTextLayout;
        CheckBox mCheckbox;
        //        long mRowId = -1;
        //on clicking image, launches EditEntryActivity to edit this entry
        View.OnClickListener mImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.view_click_animator));
                if (mSelectedItemList.size() == 0) {
                    mSelectedItemList.add(getAdapterPosition());
                    mListener.onListItemClicked(Constants.CLICK_SOURCE_IMAGE);
                } else {
                    mListener.onListItemClicked(Constants.CLICK_DESELECT);
                }
            }
        };
        //on clicking text, opens mapping app via intent
        View.OnClickListener mTextClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.view_click_animator));
                if (mSelectedItemList.size() == 0) {
                    mSelectedItemList.add(getAdapterPosition());
                    mListener.onListItemClicked(Constants.CLICK_SOURCE_TEXT);
                } else {
                    mListener.onListItemClicked(Constants.CLICK_DESELECT);
                }
            }
        };
        //listener for both image and text in multiselection mode
        View.OnClickListener mMultiSelectClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationViewHolder.this.onLongClick(view);
            }
        };

        public LocationViewHolder(View v) {
            super(v);
            mLocationName = (TextView) v.findViewById(R.id.location_name);
            mLocationCoordinates = (TextView) v.findViewById(R.id.location_coordinates);
            mLocationAddress = (TextView) v.findViewById(R.id.location_address);
            mLocationImage = (ImageView) v.findViewById(R.id.location_image);
            mCheckbox = (CheckBox) v.findViewById(R.id.checkBox);
            mTextLayout = v.findViewById(R.id.location_text_layout);
            //change image and text click listeners depending on if in multiselect mode


            mTextLayout.setOnLongClickListener(this);
            mCheckbox.setOnCheckedChangeListener(this);
            mLocationImage.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (itemView.isSelected()) {
//                itemView.setSelected(false);
//                mSelectedRowList.remove(Long.valueOf(mRowId));
                mSelectedItemList.remove(Integer.valueOf(position));
                LocationListAdapter.this.notifyItemChanged(position);
                if (mSelectedItemList.size() == 0) { //exit multi-selection mode
                    LocationListAdapter.this.notifyDataSetChanged();
                    isMultiSelect = false;
                    mListener.onListItemClicked(Constants.CLICK_EXIT_MULTISELECT_MODE);
                    return true;
                }
            }
            else {
//                itemView.setSelected(true);
//                mSelectedRowList.add(mRowId);
                mSelectedItemList.add(position);
                LocationListAdapter.this.notifyItemChanged(position);
                if (mSelectedItemList.size() == 1) {
                    LocationListAdapter.this.notifyDataSetChanged();
                    isMultiSelect = true;
                    mListener.onListItemClicked(Constants.CLICK_ENTER_MULTISELECT_MODE);
                }
            }
            mListener.onListItemClicked(Constants.CLICK_SELECTION_COUNT_CHANGED);
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int position = getAdapterPosition();
            if (mSelectedItemList.size()>0) {
                if (b) {
                    mSelectedItemList.add(position);
                } else {
                    mSelectedItemList.remove(Integer.valueOf(position));
                }
                mListener.onListItemClicked(Constants.CLICK_SELECTION_COUNT_CHANGED);
                notifyItemChanged(position);
                if(mSelectedItemList.size() == 0) {
                    notifyDataSetChanged();
                    isMultiSelect = false;
                    mListener.onListItemClicked(Constants.CLICK_EXIT_MULTISELECT_MODE);
                }
            }
        }
    }
}

