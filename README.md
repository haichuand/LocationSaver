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

10/15/2015<br>
● Created EditEntryActivity to let user save current location as well as to edit existing location entries<br>
● Hooked FloatingActionButton in LocationFragment with EditEntryActivity, so that on pressing the button, LocationFragment launches EditEntryActivity through an ex;licit intent and passes the current location object.<br>
● EditEntryActivity populates the name field with current date and time, then gets the location object and use it to populate the coordinates field. It also gets address in text through GoogleApiClient Geocoder and shows in address field. The note field is populated with accuracy through location object.<br>
● On pressing the camera icon in EditEntryActivity, the phone's camera app is lauched through startActivityForResult with an implicit intent and extras containing the image file path. The camera saves the image in the designated path upon completion.<br>
● Implemented onActivityResult callback in EditEntryActivity to process image from camera. Lauches an Asynctask to get bitmap of the image and scale it to generate a thumbnail image for display.<br>
● Problems encountered: 1. Application does not launch in Marshmallow due to permission issues. Runs fine in Lollipop. 2. Sometimes images from camera app won't decode correctly. Seems to happen in photos taken under low light.<br>
● Plan for next week: debug, then implment Save button to save location in database.<br>
