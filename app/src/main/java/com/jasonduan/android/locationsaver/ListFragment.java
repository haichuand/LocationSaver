package com.jasonduan.android.locationsaver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fragment to show all saved locations in a list. It uses RecyclerView with an adapter.
 */
public class ListFragment extends Fragment implements LocationListAdapter.LocationListListener {

    LocationDBHandler mDbHandler;
    private LocationListAdapter mAdapter;
    private Cursor mCursor;
    private ActionMode mActionMode;

    /* ActionBar activated by long pressing a location item to enter into multi-select mode */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.context_menu_listfragment, menu);
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
                case R.id.action_edit:
                    editLocationItem();
                    break;
                case R.id.action_delete:
                    promptToDeleteLocationItems();
                    break;
                case R.id.action_show_location:
                    showLocationsOnMap();
                    if (mActionMode != null)
                        mActionMode.finish();
                    break;
                default:
                    assert false;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.mSelectedItemList.clear();
            mAdapter.isMultiSelect = false;
            mAdapter.notifyDataSetChanged();
            mActionMode = null;
        }
    };

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHandler = LocationDBHandler.getDbInstance(getContext());
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        RecyclerView locationListView = (RecyclerView) v.findViewById(R.id.location_list_view);
        locationListView.setHasFixedSize(true);
        locationListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCursor = mDbHandler.selectAllRows();
        mAdapter = new LocationListAdapter(mCursor, this);
        locationListView.setAdapter(mAdapter);
        return v;
    }

    /**
     * Launch EditEntryActivity to edit location item
     */
    private void editLocationItem() {
        int numSelected = mAdapter.mSelectedItemList.size();
        if (numSelected != 1) {
            Toast.makeText(getActivity(), R.string.select_one_item, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), EditEntryActivity.class);
            intent.putExtra(Constants.SOURCE, Constants.LIST_FRAGMENT);
            mCursor.moveToPosition(mAdapter.mSelectedItemList.get(0));
            intent.putExtra(Constants.BUNDLE_DB_ROWID, mCursor.getLong(LocationDBHandler._ID));
            getActivity().startActivityForResult(intent, Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE);
            mAdapter.mSelectedItemList.clear();
        }
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    /**
     * Prompt the user to confirm deletion of location items(s)
     */
    private void promptToDeleteLocationItems() {
        int itemsSelected = mAdapter.mSelectedItemList.size();
        if (itemsSelected > 0) {
            String message;
            if (itemsSelected == 1) {
                message = getString(R.string.delete_item_message_1) + " " + itemsSelected
                        + " " + getString(R.string.delete_item_message_2);
            } else {
                message = getString(R.string.delete_items_message_1) + " " + itemsSelected
                        + " " + getString(R.string.delete_items_message_2);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_items_alert).setMessage(message)
                    .setIcon(R.drawable.icon_warning);
            AlertDialog dlg = builder.create();
            dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteLocationItems();
                        }
                    });

            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

        /*
         * Show the modal dialog. Once the user has clicked on a button, the
         * dialog is automatically removed.
         */
            dlg.show();
        }
    }

    /**
     * Show selected location(s) on Google Map in LocationFragment
     */
    private void showLocationsOnMap() {
        ArrayList<MarkerOptions> markerList = new ArrayList<>();
        for (int selectedItem : mAdapter.mSelectedItemList) {
            mCursor.moveToPosition(selectedItem);
            MarkerOptions newMarker = new MarkerOptions()
                    .position(new LatLng(mCursor.getDouble(LocationDBHandler.LATITUDE), mCursor.getDouble(LocationDBHandler.LONGITUDE)))
                    .title(mCursor.getString(LocationDBHandler.NAME))
                    .snippet(mCursor.getString(LocationDBHandler.NOTE));
            markerList.add(newMarker);
        }
        ListFragmentCallback mainActivity = (ListFragmentCallback) getActivity();
        mainActivity.showMarkersOnMap(markerList);
    }

    /**
     * Delete selected location items and associated images
     */
    private void deleteLocationItems() {
        SQLiteDatabase db = mDbHandler.getWritableDatabase();
        long rowId;

        for (Iterator<Integer> it = mAdapter.mSelectedItemList.iterator(); it.hasNext(); ) {
            mCursor.moveToPosition(it.next());
            /* delete images associated with the location item*/
            String imagePath = mCursor.getString(LocationDBHandler.IMAGE);
            File imageFile;
            if (imagePath != null) {
                imageFile = new File(imagePath);
                //first delete thumbnail image, then delete full size image
                if (imageFile.delete()) {
                    int suffixIndex = imagePath.lastIndexOf("_tn.");
                    String fullSizeImagePath = "";
                    if (suffixIndex > 0) {
                        fullSizeImagePath = imagePath.substring(0, suffixIndex) + imagePath.substring(suffixIndex + 3);
                        imageFile = new File(fullSizeImagePath);
                        imageFile.delete();
                    }
                }
            }
            rowId = mCursor.getLong(LocationDBHandler._ID);
            db.delete(LocationDBHandler.LocationEntry.TABLE,
                    LocationDBHandler.LocationEntry._ID + "=" + rowId, null);
        }

        mAdapter.mSelectedItemList.clear();
        onListItemChanged();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onResume() {
        onListItemChanged();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mDbHandler.close();
        super.onDestroy();
    }

    /**
     * Perform actions depending on the source of user click from ListFragment
     * @param clickSource Source of user click
     */
    @Override
    public void onListItemClicked(int clickSource) {
        switch (clickSource) {
            case Constants.CLICK_SOURCE_IMAGE:
                editLocationItem();
                break;
            case Constants.CLICK_SOURCE_TEXT:
                showLocationInMappingApp();
                break;
            case Constants.CLICK_DESELECT:
                Toast.makeText(getActivity(), R.string.deselect_all_items, Toast.LENGTH_SHORT).show();
                break;
            case Constants.CLICK_ENTER_MULTISELECT_MODE:
//                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);
                AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
                mActionMode = appCompatActivity.startSupportActionMode(mActionModeCallback);
                mAdapter.notifyDataSetChanged();
                break;
            case Constants.CLICK_EXIT_MULTISELECT_MODE:
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                mAdapter.notifyDataSetChanged();
                break;
            case Constants.CLICK_SELECTION_COUNT_CHANGED:
                int selectionCount = mAdapter.mSelectedItemList.size();
                if (mActionMode != null) {
                    mActionMode.setTitle(selectionCount > 0 ? "" + selectionCount : "");
                }
                break;
            default:
                assert false;
        }

    }

    /**
     * Refresh list view when list items changed through insert, delete or edit
     */
    public void onListItemChanged() {
        mCursor = mDbHandler.selectAllRows();
        mAdapter.changeCursor(mCursor);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Launch the phone's mapping app(s) through intent to view/navigate to the location
     */
    private void showLocationInMappingApp() {
        mCursor.moveToPosition(mAdapter.mSelectedItemList.get(0));
        double latitude = mCursor.getDouble(LocationDBHandler.LATITUDE);
        double longitude = mCursor.getDouble(LocationDBHandler.LONGITUDE);
        String name = mCursor.getString(LocationDBHandler.NAME);
        // change name - to any text you want to display
        String uriString = "geo:" + latitude + "," + longitude + "?q=" + latitude + ","
                + longitude + "(" + name + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        startActivity(intent);
        mAdapter.mSelectedItemList.clear();
    }

    /* call back interface to show selected location items on map */
    public interface ListFragmentCallback {
        void showMarkersOnMap (List<MarkerOptions> markers);
    }
}
