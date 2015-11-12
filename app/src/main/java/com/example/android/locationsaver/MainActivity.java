package com.example.android.locationsaver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentCallback {
    private String TAG = "MainActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Toolbar mToolbar;
    SlidingTabLayout mSlidingTabs;
//    LocationFragment mLocationFragment;
//    ListFragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this.getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //stop location update when user switch out of LocationFragment
                LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
                if (position == 1)  {
                    locationFragment.onPause();
                }
                else {
                    locationFragment.onResume();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSlidingTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        mSlidingTabs.setDistributeEvenly(true);
//        mSlidingTabs.setCustomTabView(R.layout.tab_view, R.id.tab_name_img);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        mSlidingTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        mSlidingTabs.setViewPager(mViewPager);

        new Thread(new Runnable() {
            @Override
            public void run() {
                makeImageDirectory();
            }
        }).run();
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
            mViewPager.setCurrentItem(1);
            ListFragment listFragment = (ListFragment) findCurrentFragment(1);
            listFragment.onListItemChanged();
        }
    }

    private Fragment findCurrentFragment(int position) {
        return getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + position);
    }

    @Override
    public void showMarkersOnMap(List<MarkerOptions> markers) {
        if (mViewPager ==  null) return;
        mViewPager.setCurrentItem(0);
        LocationFragment locationFragment = (LocationFragment) findCurrentFragment(0);
        locationFragment.showMarkers(markers);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
            implements SlidingTabLayout.TabIconProvider {

        private final int iconRes[] = {
                R.drawable.icon_location,
                R.drawable.icon_list
        };

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return new LocationFragment();
//                if (mLocationFragment == null) {
//                    mLocationFragment = new LocationFragment();
//                }
//                return mLocationFragment;
            } else {
                return new ListFragment();
//                if (mListFragment == null) {
//                    mListFragment = new ListFragment();
//                }
//                return mListFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_location_tab);
                case 1:
                    return getString(R.string.title_list_tab);
            }
            return null;
        }

        @Override
        public int getPageIconResId(int position) {
            return iconRes[position];
        }

    }

    public void makeImageDirectory() {
        String imageDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath()+"/LocationSaverImages";
        File file = new File(imageDirectory);
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Error making directory "+file.getPath());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//    }

}
