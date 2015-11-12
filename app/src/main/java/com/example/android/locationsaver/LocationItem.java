package com.example.android.locationsaver;

/**
 * Created by hduan on 10/21/2015.
 */
public class LocationItem {
    public String name;
    public double latitude;
    public double longitude;
    public String address;
    public String note;
    public String imagePath;
    public long time;

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
