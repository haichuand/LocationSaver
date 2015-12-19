package com.jasonduan.android.locationsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Database handler for saved locations in the app
 */
public class LocationDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationSaverDB";

    //columns in the database
    public static final int _ID = 0;
    public static final int NAME = 1;
    public static final int LATITUDE = 2;
    public static final int LONGITUDE = 3;
    public static final int ADDRESS = 4;
    public static final int NOTE = 5;
    public static final int IMAGE = 6;
    public static final long TIME = 7;

    public LocationDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the database schema
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " + LocationEntry.TABLE + "(" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " + LocationEntry.COLUMN_NAME +
                " TEXT UNIQUE NOT NULL, " + LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " + LocationEntry.COLUMN_ADDRESS
                + " TEXT, " + LocationEntry.COLUMN_NOTE + " TEXT, " + LocationEntry.COLUMN_IMAGE + " TEXT, "
                + LocationEntry.COLUMN_TIME + " INTEGER NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Insert a location into the database
     * @param location The location to be inserted
     * @return The rowId of the inserted location
     */
    public long insertLocation(LocationItem location) {
        long rowId = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_NAME, location.name);
        values.put(LocationEntry.COLUMN_LATITUDE, location.latitude);
        values.put(LocationEntry.COLUMN_LONGITUDE, location.longitude);
        values.put(LocationEntry.COLUMN_ADDRESS, location.address);
        values.put(LocationEntry.COLUMN_NOTE, location.note);
        values.put(LocationEntry.COLUMN_IMAGE, location.imagePath);
        values.put(LocationEntry.COLUMN_TIME, location.time);

        rowId = db.insert(LocationEntry.TABLE, null, values);
        db.close();
        return rowId;
    }

    /**
     * Bulk insert a list of locations into the database
     * @param locations The location list containing location items
     * @return The number of rows inserted
     */
    public int insertLocations (List<LocationItem> locations) {
        int rowsInserted = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        for (LocationItem location : locations) {
            ContentValues values = new ContentValues();
            values.put(LocationEntry.COLUMN_NAME, location.name);
            values.put(LocationEntry.COLUMN_LATITUDE, location.latitude);
            values.put(LocationEntry.COLUMN_LONGITUDE, location.longitude);
            values.put(LocationEntry.COLUMN_ADDRESS, location.address);
            values.put(LocationEntry.COLUMN_NOTE, location.note);
            values.put(LocationEntry.COLUMN_IMAGE, location.imagePath);
            values.put(LocationEntry.COLUMN_TIME, location.time);
            if (db.insert(LocationEntry.TABLE, null, values)==-1) {
                break;
            }
            rowsInserted++;
        }
        db.close();
        return rowsInserted;
    }

    /**
     * Select all rows in the location database
     * @return Cursor to all rows
     */
    public Cursor selectAllRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocationDBHandler.LocationEntry.TABLE
                + " ORDER BY _ID DESC", null);
        return cursor;
    }

    /**
     * Insert sample locations into the database for testing
     */
    public void insertTestRows() {
        LocationItem loc1, loc2, loc3, loc4, loc5, loc6, loc7, loc8;
        String sdPath = Constants.IMAGE_DIRECTORY;
        loc1 = new LocationItem("Seattle Waterfront", 47.607795, -122.342424, "Alaskan Way & Pike St, Seattle, WA 98001, USA", "The most beautiful waterfront!", sdPath+"1_tn.jpg", 1407004217000L);
        loc2 = new LocationItem("McWay Waterfall", 36.159431, -121.672289, "McWay Waterfall Trail, Big Sur, CA 93920", "Beautiful waterfall by the Pacific Ocean", sdPath+"2_tn.jpg", 1441650767000L);
        loc3 = new LocationItem("Golden Gate Bridge", 37.791693, -122.484574, "Presidio, San Francisco, CA", "Beach by Golden Gate Bridge", sdPath+"3_tn.jpg", 1422469635000L);
        loc4 = new LocationItem("Yosemite Falls", 37.747565, -119.596386, "", "Iconic falls in Yosemite Valley", sdPath+"4_tn.jpg", 1432486111000L);
        loc5 = new LocationItem("Delicate Arch", 38.743650, -109.499252, "", "Beautiful sandstone arch in the high desert of Utah", sdPath+"5_tn.jpg", 1372964485000L);
        loc6 = new LocationItem("Mt. Rainier", 46.787869, -121.736205, "", "On Skyline trail in Paradise area", sdPath+"6_tn.jpg", 1374775429000L);
        loc7 = new LocationItem("Hana, Maui", 20.788188, -156.003554, "", "Black sand beach state park", sdPath+"7_tn.jpg", 1419989741000L);
        loc8 = new LocationItem("Lake Tahoe", 38.968932, -120.089656, "", "On Rubicon trail", sdPath+"8_tn.jpg", 1436204124000L);
        List<LocationItem> list = new ArrayList<LocationItem>();
        list.add(loc1);
        list.add(loc2);
        list.add(loc3);
        list.add(loc4);
        list.add(loc5);
        list.add(loc6);
        list.add(loc7);
        list.add(loc8);
        insertLocations(list);
    }

    /**
     * Columns in the database
     */
    public class LocationEntry implements BaseColumns {

        public static final String TABLE = "location";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_TIME = "time";
    }
}
