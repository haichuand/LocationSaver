package com.example.android.locationsaver;

import android.support.v4.app.Fragment;

public class TabPagerItem {

    public String title;
    public int icon;
    public Fragment fragment;

    public TabPagerItem(String title, int icon, Fragment fragment) {
        this.title = title;
        this.icon = icon;
        this.fragment = fragment;
    }
}