package com.jasonduan.android.locationsaver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Main activity of the app
 */
public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentCallback,
        ViewPager.OnPageChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final float TAB_ALPHA_SELECTED = 1.0f;
    private static final float TAB_ALPHA_UNSELECTED = 0.5f;
    public static final int LOCATION_PERMISSION = 15;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Toolbar mToolbar;
    private String TAG = "MainActivity";
    private TabLayout mTabLayout;
    private TabPagerAdapter mTabPagerAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //obtain necessary permissions for API level 23 and over (Marshmallow)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
             Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, LOCATION_PERMISSION);
        }
        else {
            makeImageDirectory();
        }

        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mTabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        mTabPagerAdapter = new TabPagerAdapter(this.getSupportFragmentManager(), this);
        mTabPagerAdapter.add(null, R.drawable.icon_map);
        mTabPagerAdapter.add(null, R.drawable.icon_list);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        setTabLayoutViewPager(mViewPager);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            String fragmentToShow = intent.getStringExtra(Constants.SOURCE);
            if (fragmentToShow != null && fragmentToShow.equals(Constants.LIST_FRAGMENT)) {
                onLocationListChanged();
                mViewPager.setCurrentItem(Constants.LIST_FRAGMENT_POSITION);
                intent.removeExtra(Constants.SOURCE);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("MainActivity", "onActivityResult() called");
        if (requestCode == Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mViewPager.setCurrentItem(Constants.LIST_FRAGMENT_POSITION);
            onLocationListChanged();
        }
    }

    @Override
    public void showMarkersOnMap(List<MarkerOptions> markers) {
        if (mViewPager == null) return;
        mViewPager.setCurrentItem(Constants.LOCATION_FRAGMENT_POSITION);
        LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
        locationFragment.mMoveCameraToCurrentLocation = false;
        locationFragment.showMarkers(markers);
    }

    public void makeImageDirectory() {
        File file = new File(Constants.IMAGE_DIRECTORY);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, R.string.error_making_directory, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
        if (locationFragment == null) return;
        switch (position) {
            case Constants.LOCATION_FRAGMENT_POSITION:
                locationFragment.startLocationUpdates();
                break;
            case Constants.LIST_FRAGMENT_POSITION:
                locationFragment.stopLocationUpdates();
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public void setTabLayoutViewPager(final ViewPager viewPager) {
        mTabLayout.setupWithViewPager(viewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            private int position;

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    view.setAlpha(TAB_ALPHA_SELECTED);
                }

                viewPager.setCurrentItem(tab.getPosition());
                position = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    view.setAlpha(TAB_ALPHA_UNSELECTED);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);

            if (tab != null) {
                View view = adapter.getTabView(i);
                if (i > 0) view.setAlpha(TAB_ALPHA_UNSELECTED);
                tab.setCustomView(view);
            }
        }
    }

    /**
     * Find the current fragment at designated position
     * @param position The position of the fragment
     * @return The fragment at specified position
     */
    private Fragment findCurrentFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + position);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.jasonduan.android.locationsaver/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.jasonduan.android.locationsaver/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * Call ListFragment to notify that location list has changed and needs to refresh
     */
    private void onLocationListChanged() {
        ListFragment listFragment = (ListFragment) findCurrentFragment(Constants.LIST_FRAGMENT_POSITION);
        if (listFragment != null) {
            listFragment.onListItemChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean permissionsGranted = true; //if all permissions are granted
        int writeExternalStorageIndex = -1; //index of WRITE_EXTERNAL_STORAGE permission

        for (int i=0; i<grantResults.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                writeExternalStorageIndex = i;
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                permissionsGranted = false;
        }
        if (!permissionsGranted) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.must_have_permission, Toast.LENGTH_LONG).show();
                }
            });
            finish();
        }

        if (writeExternalStorageIndex > 0) {
            makeImageDirectory();
            //insert test location if application is launched for the first time
            final SharedPreferences preferences = getSharedPreferences(Constants.SHAREDPREFERENCES, Context.MODE_PRIVATE);
            Boolean firstStart = preferences.getBoolean(Constants.FIRST_START, true);
            if (firstStart) {
                //use another thread to extract sample images from resources and save to sd card, then insert sample location to database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream os;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false; //so that Android will not rescale images on different dpi devices
                        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ggb, options);
                        File file = new File(Constants.IMAGE_DIRECTORY, "ggb.JPG");
                        try {
                            os = new FileOutputStream(file);
                            bm.compress(Bitmap.CompressFormat.JPEG, 80, os);
                            os.flush();
                            os.close();
                        }
                        catch(Exception e) {
                            return;
                        }
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.ggb_tn, options);
                        file = new File(Constants.IMAGE_DIRECTORY, "ggb_tn.JPG");
                        try {
                            os = new FileOutputStream(file);
                            bm.compress(Bitmap.CompressFormat.JPEG, 80, os);
                            os.flush();
                            os.close();
                            LocationDBHandler.getDbInstance(MainActivity.this).insertSampleLocation();
                            onLocationListChanged();
                            preferences.edit().putBoolean(Constants.FIRST_START, false).apply();
                        }
                        catch(Exception e) {
                            return;
                        }
                    }
                }).run();
            }
        }
    }
}
