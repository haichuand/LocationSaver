# LocationSaver Term Project for CSC780

LocationSaver is android app to quickly save the user's current location. User can add notes and attach images on the locations. The locations are saved on user's phone. User can see the list of saved locations and edit them. Click on the location opens the location in mapping app installed on the phone.

Weekly Activity Log
09/24/2015
● Created MainActivity, SlidingTabLayout with Location and List tabs
● SlidingTabLayout was from Google iosched repository
● Had trouble putting icons on the tabs.
● Plan for next week: put Google map Location tab screen and show user's current location

10/1/2015
● Added MapFragment, FloatingActionButton and AccuracyView to LocationFragment
● Obtained Google API key to use in Google Map
● Implemented LocationListener, ConnectionCallbacks, OnConnectionFailedListener in LocationFragment to zoom to current location and update accuracy through GoogleApiClient
● Implemented OnPageChangeListener in ViewPager to listen for fragment switches, so that navigating away from LocationFragment stops location updates 
● Lesson 2: FragmentPagerAdapter.getItem(int position) returns new fragments by default, so do not use it to retrieve current fragments. Instead, create member variables in main activity to point to current fragments.
● Plan for next week: start implementing data base for storing location data
