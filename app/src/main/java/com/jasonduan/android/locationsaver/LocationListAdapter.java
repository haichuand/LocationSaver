package com.jasonduan.android.locationsaver;


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

/**
 * Adapter for ListFragment to load location items from the database
 */
public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {
    /* List of selected items from the location item list */
    ArrayList<Integer> mSelectedItemList = new ArrayList<>();
    boolean isMultiSelect; //flag for whether in multi-selection state
    private LocationListListener mListener;
    private Cursor mCursor;

    /**
     * Constructor for the Adapter
     * @param cursor Cursor from the database
     * @param listener Listener to process entry click events
     */
    public LocationListAdapter(Cursor cursor, LocationListListener listener) {
        mCursor = cursor;
        mListener = listener;
        isMultiSelect = false;
    }

    /**
     * Returns a new LocationViewHolder whenever required
     * @param parent
     * @param viewType
     * @return A new LocationViewHolder Instance
     */
    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new LocationViewHolder(v);
    }

    /**
     * Called whenever an existing LocationViewHolder is bound to a new location entry. This is where
     * the view for individual location item is instantiated and click lisenters set
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(final LocationViewHolder viewHolder, final int position) {
        mCursor.moveToPosition(position);

        Boolean isSelected = mSelectedItemList.contains(position); //if the item is selected
        viewHolder.itemView.setSelected(isSelected);
        viewHolder.mCheckbox.setVisibility(mSelectedItemList.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        viewHolder.mCheckbox.setOnCheckedChangeListener(null); //prevent the check box from being changed by the next line of code
        viewHolder.mCheckbox.setChecked(isSelected);
        viewHolder.mCheckbox.setOnCheckedChangeListener(viewHolder);

        //Listeners are be set separately depending on if in multi-select mode or not
        if (isMultiSelect) {
            viewHolder.mLocationImage.setOnClickListener(viewHolder.mMultiSelectClickListener);
            viewHolder.mTextLayout.setOnClickListener(viewHolder.mMultiSelectClickListener);
        }
        else {
            viewHolder.mLocationImage.setOnClickListener(viewHolder.mImageClickListener);
            viewHolder.mTextLayout.setOnClickListener(viewHolder.mTextClickListener);
        }

        //set the text fields in the location item view
        String name = mCursor.getString(LocationDBHandler.NAME);
        viewHolder.mLocationName.setText(name);
        viewHolder.mLocationCoordinates.setText(mCursor.getDouble(LocationDBHandler.LATITUDE)
                + ", " + mCursor.getDouble(LocationDBHandler.LONGITUDE));
        String address = mCursor.getString(LocationDBHandler.ADDRESS);
        if (address != null) {
            address = address.replace("\n", ", ");
        }
        viewHolder.mLocationAddress.setText(address);
        //set the image to user image if available. Otherwise set to default image
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

    public interface LocationListListener {
        void onListItemClicked(int clickSource);
    }

    /**
     * Class to hold individual location item views. Most of the click listeners are set here
     */
    class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        TextView mLocationName, mLocationCoordinates, mLocationAddress;
        ImageView mLocationImage;
        View mTextLayout;
        CheckBox mCheckbox;

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

            mTextLayout.setOnLongClickListener(this);
            mCheckbox.setOnCheckedChangeListener(this);
            mLocationImage.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (itemView.isSelected()) {
                mSelectedItemList.remove(Integer.valueOf(position));
                LocationListAdapter.this.notifyItemChanged(position);
                //exit multi-selection mode when the selected item list becomes empty
                if (mSelectedItemList.size() == 0) {
                    LocationListAdapter.this.notifyDataSetChanged();
                    isMultiSelect = false;
                    mListener.onListItemClicked(Constants.CLICK_EXIT_MULTISELECT_MODE);
                    return true;
                }
            }
            else {
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

        /**
         * Listener for when the checked state of the checkbox is changed
         * @param compoundButton
         * @param b
         */
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int position = getAdapterPosition();
            //The selected item list still needs to be updated when the checkbox is checked, because
            //the mMultiSelectClickListener is not activated by checkbox
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

