package com.example.android.locationsaver;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Iterator;

public class ListFragment extends Fragment implements LocationListAdapter.LocationListListener{

    private LocationListAdapter mAdapter;
    private Cursor mCursor;
    private Context mContext;
    LocationDBHandler mDbHandler;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getActivity();
        mDbHandler = new LocationDBHandler(mContext);
        SQLiteDatabase db = mDbHandler.getWritableDatabase();
        db.delete(LocationDBHandler.LocationEntry.TABLE, null, null);
        db.close();
        mDbHandler.insertTestRows();
        mCursor = mDbHandler.selectAllRows();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        RecyclerView locationListView = (RecyclerView) v.findViewById(R.id.location_list_view);
        locationListView.setHasFixedSize(true);
        locationListView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new LocationListAdapter(mCursor, this);
        locationListView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_list_fragment, menu);
//        menu.findItem(R.id.action_delete).setVisible(false);
//        menu.findItem(R.id.action_edit).setVisible(false);
//        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            editLocationItem();
            return true;
        }
        else if (id == R.id.action_delete) {
            int itemsSelected = mAdapter.selectedRowList.size();
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
                        .setIcon(android.R.drawable.ic_dialog_alert);
                AlertDialog dlg = builder.create();
                dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),
                        new DialogInterface.OnClickListener(){
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
        return super.onOptionsItemSelected(item);
    }

    private void editLocationItem() {
        int numSelected = mAdapter.selectedRowList.size();
        if (numSelected != 1) {
            Toast.makeText(getActivity(), R.string.select_one_item, Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(getActivity(), EditEntryActivity.class);
            intent.putExtra(Constants.SOURCE, Constants.LIST_FRAGMENT);
            intent.putExtra(Constants.BUNDLE_DB_ROWID, mAdapter.selectedRowList.get(0));
            startActivityForResult(intent, Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE);
        }
    }

    private void deleteLocationItems() {
        SQLiteDatabase db = mDbHandler.getWritableDatabase();
        long rowId;
        for(Iterator<Long> it=mAdapter.selectedRowList.iterator(); it.hasNext(); ){
            rowId = it.next();
            db.delete(LocationDBHandler.LocationEntry.TABLE,
                    LocationDBHandler.LocationEntry._ID + "=" + rowId, null);
        }
        onListItemChanged();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClicked(int position) {

    }

    public void onListItemChanged() {
        mCursor = mDbHandler.selectAllRows();
        mAdapter.changeCursor(mCursor);
        mAdapter.notifyDataSetChanged();
    }

}
