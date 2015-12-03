package com.example.android.locationsaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentCallback,
        ViewPager.OnPageChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final float TAB_ALPHA_SELECTED = 1.0f;
    private static final float TAB_ALPHA_UNSELECTED = 0.5f;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                makeImageDirectory();
            }
        }).run();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 0);
        }
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
//            Toast.makeText(this, "fragemtnToShow="+fragmentToShow, Toast.LENGTH_LONG).show();
            if (fragmentToShow != null && fragmentToShow.equals(Constants.LIST_FRAGMENT)) {
                ListFragment listFragment = (ListFragment) findCurrentFragment(1);
                if (listFragment != null) {
                    listFragment.onListItemChanged();
                }

                mViewPager.setCurrentItem(1);
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
        Log.d("MainActivity", "onActivityResult() called");
        if (requestCode == Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mViewPager.setCurrentItem(Constants.LIST_FRAGMENT_POSITION);
            ListFragment listFragment = (ListFragment) findCurrentFragment(1);
            listFragment.onListItemChanged();
        }
    }

//    private Fragment findCurrentFragment(int position) {
//        return getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + position);
//    }

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
            if (!file.mkdirs())
                Log.d(TAG, "Error making directory " + file.getPath());
        }
    }

    @Override
    public void onPageSelected(int position) {
        LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
        if (locationFragment == null) return;
        switch (position) {
            case 0:
                locationFragment.startLocationUpdates();
                break;
            case 1:
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
//                if (tab.getPosition() == position) {
//                    TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
//                    Fragment fragment = adapter.getItem(tab.getPosition());

//                    if (fragment instanceof ScrollableInterface) {
//                        ScrollableInterface scrollable = (ScrollableInterface) fragment;
//                        scrollable.scrollToTop();
//                    }
//                }
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
                Uri.parse("android-app://com.example.android.locationsaver/http/host/path")
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
                Uri.parse("android-app://com.example.android.locationsaver/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.must_have_permission, Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        makeImageDirectory();
    }


}
