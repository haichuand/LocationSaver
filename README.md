# LocationSaver Term Project for CSC780

LocationSaver is android app to quickly save the user's current location. User can add notes and attach images on the locations. The locations are saved on user's phone. User can see the list of saved locations and edit them. Click on the location opens the location in mapping app installed on the phone.

Weekly Activity Log<br>
09/24/2015<br>
● Created MainActivity, SlidingTabLayout with Location and List tabs<br>
● SlidingTabLayout was from Google iosched repository<br>
● Had trouble putting icons on the tabs<br>
● Plan for next week: put Google map Location tab screen and show user's current location<br>

10/1/2015<br>
● Added MapFragment, FloatingActionButton and AccuracyView to LocationFragment<br>
● Obtained Google API key to use in Google Map<br>
● Implemented LocationListener, ConnectionCallbacks, OnConnectionFailedListener in LocationFragment to zoom to current location and update accuracy through GoogleApiClient<br>
● Implemented OnPageChangeListener in ViewPager to listen for fragment switches, so that navigating away from LocationFragment stops location updates<br>
● Lesson: FragmentPagerAdapter.getItem(int position) returns new fragments by default, so do not use it to retrieve current fragments. Instead, create member variables in main activity to point to current fragments <br>
● Plan for next week: start implementing data base for storing location data <br>

10/8/2015<br>
● Added location list view in ListFragment, which is subclassed from RecyclerView<br>
● Created LocationListAdapter to manage data for location list view, subclassed from RecyclerView.Adapter<br>
● Created SQLite database (LocationDBHandler) to store location data. Current schema specifies these fields: name, longitutde, latitude, address, image_path of location. Connected the database with LocationListAdapter through cursor in constructor of LocationListAdapter. Currently the database is populated with 8 test locations.<br>
● Lesson: need to create small thumbnail images for location list view. Large images will slow down page and list scrolling a lot<br>
● Plan for next week: implement floating action add button in LocationFragment to add current location to database<br>
