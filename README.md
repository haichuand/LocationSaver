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

10/22/2015<br>
● Implemented save button in EditEntryActivity to insert item into database.<br>
● Added "edit" and "delete" action buttons to Toolbar in ListFragment.<br>
● Used State List Drawable to allow selection of multiple items in ListFragment.<br>
● Plan for next week: debug current program and implement short click to open location in mapping app.<br>

10/29/2015<br>
● Changed location item layout in ListFragment for better navigation<br>
● Added click listener to image to edit the item in EditEntryActivity, and click listener on text to open the location in mapping app via implicit intent<br>
● Changed LongClickListener to enter or multi-selection mode. When in multi-selection mode, a checkbox appears in each item for faster selection<br>
● Added contextual toolbar through ActionMode in multi-selection mode, with title displaying selection count and delete and edit icons<br>
● Plan for next week: change style of EditEntryActivity to be consistent with app theme; add get location function in EditEntryActivity<br>

11/05/2015<br>
● Adjusted margins of list items in ListFragment for better layout<br>
● Changed click listeners for list items in multiselect mode for better UI experience<br>
● Removed "Save" and "Cancel" buttons in EditEntryActivity and replaced with Toolbar icons to perform action<br>
● Added getAddress menu icon in EditEntryActivity to fill address field through FetchAddressService<br>
● Plan for next week: add "show locations on map" functionality and start implementing sorting functionality in ListFragment<br>

11/12/2015<br>
● Added "show locations on map" icon to context menu in ListFragment to show selected location items as markers on map through callback interface to MainActivity<br>
● MainActivity propages back to LocationFragment and calculates the correct map bounds to show all selected location items<br>
● When showing markers, LocationFragment also enters context menu through ActionMode to let users clear markers<br>
● Updated database; added time column (UTC milliseconds) to location itmes for implementing sorting through time in the future<br>
● Plan for next week: start implementing the sort/search location list functionality<br>

11/19/2015<br>
● Created layout of widget, with two TextViews for displaying location name and address, and an add location ImageView as button to save current location<br>
● Extended AppWidgetProvider class as the class to handle widget creation and updates<br>
● Created LocationSaverService to handle background location saving for widget<br>
● On pressing button in widget, it sends a PendingIntent to start LocationSaverService. Once the location is saved to database, LocationSaverService broadcast results to widget<br>
● Also changed TabPagerAdapter in MainActivity to use icons instead of tab tiles and to show animation of tab changing<br>
● Plan for next week: implement the LocationSaverService to get location, save location and update widget.