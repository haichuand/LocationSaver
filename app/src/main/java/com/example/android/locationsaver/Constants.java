package com.example.android.locationsaver;

import android.os.Environment;

/**
 * Created by hduan on 10/14/2015.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME =
            "com.example.android.locationsaver";

    public static final String BUNDLE_LOCATION = PACKAGE_NAME + ".LOCATION";
    public static final String BUNDLE_DB_ROWID = PACKAGE_NAME + ".DB_ROWID";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_FRAGMENT = PACKAGE_NAME + ".LocationFragment";

    public static final String LIST_FRAGMENT = PACKAGE_NAME + ".ListFragment";

    public static final String SOURCE = "SOURCE_FRAGMENT"; //tag to indicate source of intent

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
}
