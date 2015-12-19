package com.jasonduan.android.locationsaver;

/**
 * Describes an individual location as used in the app
 */
public class LocationItem {
    public String name;
    public double latitude;
    public double longitude;
    public String address;
    public String note;
    public String imagePath;
    public long time; //current system time in UTC milliseconds

    public LocationItem (String name, double latitude, double longitude,
                         String address, String note, String imagePath, long time) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.imagePath = imagePath;
        this.note = note;
        this.time = time;
    }
}
