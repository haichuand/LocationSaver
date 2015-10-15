package com.example.android.locationsaver;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListFragment extends Fragment implements LocationListAdapter.LocationListListener{

    private LocationListAdapter mAdapter;
    private Cursor mCursor;
    private Context mContext;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SQLiteDatabase db;
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LocationDBHandler dbHandler = new LocationDBHandler(mContext);
        db = dbHandler.getWritableDatabase();
        db.delete(LocationDBHandler.LocationEntry.TABLE, null, null);
        dbHandler.insertTestRows();
        db = dbHandler.getReadableDatabase();
        mCursor=db.rawQuery("SELECT * from " + LocationDBHandler.LocationEntry.TABLE, null);
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
    public void onListItemClicked(int position) {

    }

}
