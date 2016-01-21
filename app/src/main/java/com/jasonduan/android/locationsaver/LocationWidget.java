package com.jasonduan.android.locationsaver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Widget to provide one-click saving of current location
 */
public class LocationWidget extends AppWidgetProvider {

    public static final String TAG = "AppWidgetProvider";

    private Handler handler = new Handler();
    private RemoteViews rv;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent serviceIntent = new Intent(context, LocationSaverService.class);
            serviceIntent.putExtra(Constants.SOURCE, Constants.LOCATION_WIDGET_ADD_BUTTON);
            PendingIntent saveLocationIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_add_button, saveLocationIntent);

            Intent serviceIntent1 = new Intent(context, LocationSaverService.class);
            serviceIntent1.putExtra(Constants.SOURCE, Constants.LOCATION_WIDGET_SHOW_LOCATION);
            /*
             * Very important: The requestCode must be a different one, otherwise the previous pendingIntent will be
             * overwritten!!
             */
            PendingIntent showLocationIntent = PendingIntent.getService(context, 1, serviceIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_location_text, showLocationIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        Log.d(TAG, action);
        /*
         * Very important: onReceive() will be called first before onUpdate(). The RemoteViews rv should be initialized
         * only once here, and all updates work on the same rv instance. Otherwise the click listeners on rv may not be
         * properly registered after reboot.
         */
        if (rv == null) {
            rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        }
        //when the widget is updated, the first (most recent) saved location item is shown
        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            Cursor cursor = LocationDBHandler.getDbInstance(context).getReadableDatabase().query(LocationDBHandler.LocationEntry.TABLE,
                    null, null, null, null, null, LocationDBHandler.LocationEntry.COLUMN_TIME + " DESC", "1");
            String name, description;
            if (cursor.moveToFirst()) {
                name = cursor.getString(LocationDBHandler.NAME);
                description = cursor.getString(LocationDBHandler.ADDRESS);
                if (description == null || description.isEmpty()) {
                    description = cursor.getDouble(LocationDBHandler.LATITUDE) + ", " + cursor.getDouble(LocationDBHandler.LONGITUDE);
                }
                else {
                    description = description.replace(System.getProperty("line.separator"), ", ");
                }
            }
            else {
                name = context.getString(R.string.no_saved_location);
                description = context.getString(R.string.press_save_button_hint);
            }
            rv.setTextViewText(R.id.widget_location_name, name);
            rv.setTextViewText(R.id.widget_location_description, description);

//            Intent serviceIntent = new Intent(context, LocationSaverService.class);
//            serviceIntent.putExtra(Constants.SOURCE, Constants.LOCATION_WIDGET_GET_FIRST_LOCATION);
//            context.startService(serviceIntent);
        }

        //whenever an update or disable of the widget is performed by the system, we need to check the
        // boolean value from SharedPreferences to see if "No location Saved" text should be displayed or not
        //ACTION_APPWIDGET_UPDATE is also invoked when a new widget is added to desktop
//        switch (action) {
//            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
//                SharedPreferences preferences = context.getSharedPreferences(Constants.SHAREDPREFERENCE, Context.MODE_PRIVATE);
//                boolean noLocationSaved = preferences.getBoolean(Constants.NO_LOCATION_SAVED, true);
//                if (noLocationSaved) {
//                    rv.setTextViewText(R.id.widget_location_name, context.getString(R.string.no_saved_location));
//                    rv.setTextViewText(R.id.widget_location_description, context.getString(R.string.press_save_button_hint));
//                }
//                break;
//            case AppWidgetManager.ACTION_APPWIDGET_DISABLED: //invoked when a widget is deleted
//                updateSharedPreferences(context, true);
//                break;
//        }

        //set the text display of the widget depending on values from the broadcast intent
        int source = intent.getIntExtra(Constants.SOURCE, -1);
        switch (source) {
            case Constants.LOCATION_IN_PROGRESS:
                rv.setTextViewText(R.id.widget_location_name, context.getString(R.string.saving_location));
                rv.setTextViewText(R.id.widget_location_description, context.getString(R.string.please_wait));
                break;
            case Constants.LOCATION_FAILED:
                rv.setTextViewText(R.id.widget_location_name, context.getString(R.string.location_failed));
                rv.setTextViewText(R.id.widget_location_description, context.getString(R.string.please_try_later));
//                updateSharedPreferences(context, true);
                break;
            case Constants.LOCATION_INACCURATE:
                rv.setTextViewText(R.id.widget_location_name, context.getString(R.string.location_inaccurate));
                rv.setTextViewText(R.id.widget_location_description, context.getString(R.string.please_try_later));
//                updateSharedPreferences(context, true);
                break;
//            case Constants.FIRST_LOCATION_DISPLAY:
//                String name = intent.getStringExtra(Constants.LOCATION_NAME);
//                String description = intent.getStringExtra(Constants.LOCATION_DESCRIPTION);
//                rv.setTextViewText(R.id.widget_location_name, name);
//                rv.setTextViewText(R.id.widget_location_description, description);
//                break;
            case Constants.LOCATION_SAVED:
                String name = intent.getStringExtra(Constants.LOCATION_NAME);
                String description = intent.getStringExtra(Constants.LOCATION_DESCRIPTION);
                rv.setTextViewText(R.id.widget_location_name, name);
                rv.setTextViewText(R.id.widget_location_description, description);
                final Context context1 = context;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context1, R.string.location_saved, Toast.LENGTH_LONG).show();
                    }
                });
//                updateSharedPreferences(context, false);
                break;
        }
        super.onReceive(context, intent);
        ComponentName widget = new ComponentName(context, LocationWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(widget, rv);
    }

    /**
     * Update SharedPreferences to set the boolean value to indicate whether "No location saved"
     * should be displayed on the widget
     * @param context Current context
     * @param noLocationSaved If "No location saved" should be displayed on th widget
     */
//    private void updateSharedPreferences(Context context, boolean noLocationSaved) {
//        SharedPreferences preferences = context.getSharedPreferences(Constants.SHAREDPREFERENCE, Context.MODE_PRIVATE);
//        preferences.edit().putBoolean(Constants.NO_LOCATION_SAVED, noLocationSaved).commit();
//    }
}
