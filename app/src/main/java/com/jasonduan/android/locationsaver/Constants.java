package com.jasonduan.android.locationsaver;

import android.os.Environment;

/**
 * Created by hduan on 10/14/2015.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 100;

    public static final int FAILURE_RESULT = 111;

    public static final int LOCATION_FRAGMENT_POSITION = 0;
    public static final int LIST_FRAGMENT_POSITION = 1;

    public static final String PACKAGE_NAME = "com.jasonduan.android.locationsaver";

    public static final String BUNDLE_LOCATION = PACKAGE_NAME + ".LOCATION";
    public static final String BUNDLE_DB_ROWID = PACKAGE_NAME + ".DB_ROWID";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_FRAGMENT = PACKAGE_NAME + ".LocationFragment";

    public static final String LIST_FRAGMENT = PACKAGE_NAME + ".ListFragment";

    public static final String SOURCE = PACKAGE_NAME + ".SOURCE"; //tag to indicate source of intent

    public static final String IMAGE_DIRECTORY = Environment.
            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .getPath()+"/LocationSaverImages/";

    public static final String THUMBNAIL_IMAGE_URI = "thumbnail image Uri";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 138;

    public static final int EDIT_ENTRY_ACTIVITY_REQUEST_CODE = 945;

    public static final int CLICK_SOURCE_IMAGE = 418;
    public static final int CLICK_SOURCE_TEXT = 204;
    public static final int CLICK_DESELECT = 579;
    public static final int CLICK_ENTER_MULTISELECT_MODE = 356;
    public static final int CLICK_EXIT_MULTISELECT_MODE = 683;
    public static final int CLICK_SELECTION_COUNT_CHANGED = 801;

    public static final String LOCATIONSAVERSERVICE_BROADCAST = PACKAGE_NAME + ".LOCATIONSAVER_SERVICE";
    public static final String LOCATION_NAME = "LOCATION_NAME";
    public static final String LOCATION_DESCRIPTION = "LOCATION_DESCRIPTION";

    public static final String LOCATION_WIDGET_ADD_BUTTON = "LOCATION_WIDGET_ADD_BUTTON";
    public static final String LOCATION_WIDGET_SHOW_LOCATION = "LOCATION_WIDGET_SHOW_LOCATION";

    public static final int LOCATION_SAVED = 4813;
    public static final int LOCATION_IN_PROGRESS = 1914;
    public static final int LOCATION_FAILED = 7076;
    public static final int LOCATION_INACCURATE = 2391;

    public static final String SHAREDPREFERENCE = PACKAGE_NAME + ".SharedPreference";
    public static final String NO_LOCATION_SAVED = "No location saved";
}
