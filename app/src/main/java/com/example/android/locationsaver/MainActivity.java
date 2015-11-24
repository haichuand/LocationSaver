package com.example.android.locationsaver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentCallback, ViewPager.OnPageChangeListener {

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
        if (requestCode==Constants.EDIT_ENTRY_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) {
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
        if (mViewPager ==  null) return;
        mViewPager.setCurrentItem(Constants.LOCATION_FRAGMENT_POSITION);
        LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
        locationFragment.showMarkers(markers);
    }

    public void makeImageDirectory() {
        String imageDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath() + "/LocationSaverImages";
        File file = new File(imageDirectory);
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

}
