package com.example.android.locationsaver;

import android.content.ContentValues;
import android.content.Context;
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

    public LocationDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " + LocationEntry.TABLE + "(" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " + LocationEntry.COLUMN_NAME +
                " TEXT UNIQUE NOT NULL, " + LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_ADDRESS + " TEXT, " + LocationEntry.COLUMN_IMAGE + " TEXT);";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long insertLocation(LocationItem location) {
        long rowsInserted = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_NAME, location.name);
        values.put(LocationEntry.COLUMN_LATITUDE, location.latitude);
        values.put(LocationEntry.COLUMN_LONGITUDE, location.longitude);
        values.put(LocationEntry.COLUMN_ADDRESS, location.address);
        values.put(LocationEntry.COLUMN_IMAGE, location.imagePath);
        rowsInserted = db.insert(LocationEntry.TABLE, null, values);
        db.close();
        return rowsInserted;
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
            values.put(LocationEntry.COLUMN_IMAGE, location.imagePath);
            if (db.insert(LocationEntry.TABLE, null, values)==-1) {
                break;
            }
            rowsInserted++;
        }
        db.close();
        return rowsInserted;
    }

    public void insertTestRows() {
        LocationItem loc1, loc2, loc3, loc4, loc5, loc6, loc7, loc8;
        loc1 = new LocationItem("Seattle Waterfront", 46.315134, -119.39579,
                "Alaskan Way & Pike St, Seattle, WA 98001, USA", "/sdcard/LocationSaverImages/1.jpg");
        loc2 = new LocationItem("2015-09-07_113247", 36.159431, -121.672289,
                "McWay Waterfall Trail, Big Sur, CA 93920", "/sdcard/LocationSaverImages/2.jpg");
        loc3 = new LocationItem("GGB", 37.791693, -122.484574,
                "Presidio, San Francisco, CA", "/sdcard/LocationSaverImages/3.jpg");
        loc4 = new LocationItem("Test Location", -37.45251, 17.6051341,
                "Daerah Khusus Ibukota Jakarta 10210, Indonesia", "/sdcard/LocationSaverImages/4.jpg");
        loc5 = new LocationItem("NiceView Australia", -34.331451, 145.723574,
                "Warrawidgee NSW 2680 Australia", "/sdcard/LocationSaverImages/5.jpg");
        loc6 = new LocationItem("Mt. Rainier", 37.368146, -122.029694,
                "Mt. Rainier National Park, WA, USA", "/sdcard/LocationSaverImages/6.jpg");
        loc7 = new LocationItem("Tokyo Downtown", 35.680679, 139.738279,
                "1-1 Kiyosu-bashi Dori, Chiyoda-ku, Tokyo, Japan", "/sdcard/LocationSaverImages/7.jpg");
        loc8 = new LocationItem("Doctor's Office", 36.104361, -112.111494,
                "Coconino County, AZ", "/sdcard/LocationSaverImages/8.jpg");
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
        public static final String COLUMN_IMAGE = "image";
    }

    public class LocationItem {
        public String name;
        public double latitude;
        public double longitude;
        public String address;
        public String imagePath;

        public LocationItem (String name, double latitude, double longitude,
                             String address, String imagePath) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.imagePath = imagePath;
        }
    }
}
