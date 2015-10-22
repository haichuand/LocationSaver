package com.example.android.locationsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fudou on 10/7/2015.
 */
public class LocationDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationSaverDB";

    public static final int _ID = 0;
    public static final int NAME = 1;
    public static final int LATITUDE = 2;
    public static final int LONGITUDE = 3;
    public static final int ADDRESS = 4;
    public static final int NOTE = 5;
    public static final int IMAGE = 6;

    public LocationDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " + LocationEntry.TABLE + "(" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " + LocationEntry.COLUMN_NAME +
                " TEXT UNIQUE NOT NULL, " + LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " + LocationEntry.COLUMN_ADDRESS
                + " TEXT, " + LocationEntry.COLUMN_NOTE + " TEXT, " + LocationEntry.COLUMN_IMAGE + " TEXT);";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

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
        rowId = db.insert(LocationEntry.TABLE, null, values);
        db.close();
        return rowId;
    }

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
            if (db.insert(LocationEntry.TABLE, null, values)==-1) {
                break;
            }
            rowsInserted++;
        }
        db.close();
        return rowsInserted;
    }

    public Cursor selectAllRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocationDBHandler.LocationEntry.TABLE
                + " ORDER BY _ID DESC", null);
        return cursor;
    }


    public void insertTestRows() {
        LocationItem loc1, loc2, loc3, loc4, loc5, loc6, loc7, loc8;
        String sdPath = Constants.IMAGE_DIRECTORY;
        loc1 = new LocationItem("Seattle Waterfront", 46.315134, -119.39579,
                "Alaskan Way & Pike St, Seattle, WA 98001, USA", "The most beautiful waterfront!", sdPath+"1.jpg");
        loc2 = new LocationItem("2015-09-07_113247", 36.159431, -121.672289,
                "McWay Waterfall Trail, Big Sur, CA 93920", "Incredible view", sdPath+"2.jpg");
        loc3 = new LocationItem("GGB", 37.791693, -122.484574,
                "Presidio, San Francisco, CA", "Golden Gate Bridge", sdPath+"3.jpg");
        loc4 = new LocationItem("Test Location", -37.45251, 17.6051341,
                "Daerah Khusus Ibukota Jakarta 10210, Indonesia", "", sdPath+"4.jpg");
        loc5 = new LocationItem("NiceView Australia", -34.331451, 145.723574,
                "Warrawidgee NSW 2680 Australia", "Somewhere in Australia", sdPath+"5.jpg");
        loc6 = new LocationItem("Mt. Rainier", 37.368146, -122.029694,
                "Mt. Rainier National Park, WA, USA", "Best hiking destination", sdPath+"6.jpg");
        loc7 = new LocationItem("Tokyo Downtown", 35.680679, 139.738279,
                "1-1 Kiyosu-bashi Dori, Chiyoda-ku, Tokyo, Japan", "", sdPath+"7.jpg");
        loc8 = new LocationItem("Doctor's Office", 36.104361, -112.111494,
                "Coconino County, AZ", "", sdPath+"8.jpg");
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

    public class LocationEntry implements BaseColumns {

        public static final String TABLE = "location";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_IMAGE = "image";
    }
}
