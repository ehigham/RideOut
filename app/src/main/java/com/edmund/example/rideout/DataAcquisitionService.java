package com.edmund.example.rideout;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.edmund.example.rideout.RideDataContract.RideEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class DataAcquisitionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public DataAcquisitionService() {
    }

    /* Log TAG */
    private static final String TAG = "DataAcquisitionService";

    /* Booleans for Location services and hardware sensors. Configurable in settings */
    private static Boolean mRequestingHardwareSensors = false;

    /** Database variables */

    /* Database helper */
    private static RideDataDbHelper mDbHelper;

    /* Database insert variables */
    private static SQLiteDatabase db;
    private static int rideID;
    private static String timestamp;
    private static double latitude;
    private static double longitude;
    private static double altitude;
    private static double speed;
    private static double bearing;
    private static double[] acceleration = new double[3]; // acceleration in x(0), y(1), z(2);
    private static double leanangle;


    /** Location Variables */

    // Use Location Services?
    protected static Boolean mRequestingLocationUpdates = true;
/*            SettingsActivity.mSharedPreferences
                    .getBoolean(SettingsActivity.PREF_KEY_USE_LOCATION_SERVICES, true);*/

    /* The desired interval for location updates. Inexact. Updates may be more or less frequent.*/
    public static long UPDATE_INTERVAL_IN_MILLISECONDS = 100;

    /* The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.*/
    public static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;

    /* Provides the entry point to Google Play services. */
    protected static GoogleApiClient mGoogleApiClient;

    /* Stores parameters for requests to the FusedLocationProviderApi.*/
    protected LocationRequest mLocationRequest;

    /* Represents a geographical location.*/
    protected static Location mCurrentLocation;


    private Handler mHandler = new Handler();
    private Thread acquisitionThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        // TODO: Get user preferences (eg Location/Hardware Sensors)

        // Build the GoogleAPIClient
        if (mRequestingLocationUpdates) buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.i(TAG, "Received start id " + startId + ": " + intent);
        acquisitionThread = new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    try {
                        // Connect the GoogleAPIClient
                        if (mRequestingLocationUpdates) mGoogleApiClient.connect();

                        mDbHelper = new RideDataDbHelper(DataAcquisitionService.this);
                        // Initialise database
                        db = mDbHelper.getReadableDatabase();
                        // Get the RideID for this ride
                        rideID = getNewRideId();
                        // Close the readable database to avoid memory leaks
                        db.close();
                        // Get a writable database for data insertion
                        db = mDbHelper.getWritableDatabase();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        acquisitionThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        acquisitionThread.start();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataAcquisitionService.this, "Starting Data Acquisition",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Log.i(TAG, "Starting Data Acquisition Service with RideID: " + rideID);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }

        if(acquisitionThread.isAlive()) {
            Log.d(TAG,"Killing thread");
            try{
                acquisitionThread.join();
            } catch (InterruptedException ex) {
                Log.w(TAG,"Thread " + acquisitionThread.getName() + " was interrupted.");
            }
        }

        Toast.makeText(this,"Disabling Data Acquisition",Toast.LENGTH_SHORT).show();
        mDbHelper.exportDB(db, this);
        db.close();
    }

    protected static void insertData(){
        // TODO: Fix arguments to put actual values in!

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(RideEntry.COLUMN_NAME_RIDE_ID, rideID);
        values.put(RideEntry.COLUMN_NAME_TIME_STAMP, mCurrentLocation.getTime());
        values.put(RideEntry.COLUMN_NAME_LATITUDE, mCurrentLocation.getLatitude());
        values.put(RideEntry.COLUMN_NAME_LONGITUDE, mCurrentLocation.getLongitude());
        values.put(RideEntry.COLUMN_NAME_ALTITUDE,
                mCurrentLocation.hasAltitude() ? mCurrentLocation.getAltitude() : -1);
        values.put(RideEntry.COLUMN_NAME_SPEED,
                mCurrentLocation.hasSpeed() ? mCurrentLocation.getSpeed() : -1);
        values.put(RideEntry.COLUMN_NAME_BEARING,
                mCurrentLocation.hasBearing() ? mCurrentLocation.getBearing() : -1);
        values.put(RideEntry.COLUMN_NAME_ACCELERATION_X, acceleration[0]);
        values.put(RideEntry.COLUMN_NAME_ACCELERATION_Y, acceleration[1]);
        values.put(RideEntry.COLUMN_NAME_ACCELERATION_Z, acceleration[2]);
        values.put(RideEntry.COLUMN_NAME_LEAN_ANGLE, leanangle);

        // Insert the new row, returning the primary key value of the new row
        long id;
        id = db.insert(RideEntry.TABLE_NAME, null, values);

        Log.i(TAG,"inserted row number = " + id);

        // Check if insert was successful
        if (id == -1){
            Log.e(TAG,"Could not insert new row into database");
            throw new SQLiteException("Could not insert new row");
        }

    }

    /** Checks database for last rideID.
     * If one exists, the rideID is incremented and returned
     * If not, rideID to 1 */
    private int getNewRideId(){
        db = mDbHelper.getReadableDatabase();
        // Initialise the rideID to zero (will be changed if a ride was found in database)
        rideID = 0;

        if (!mDbHelper.isTableEmpty(db)) {
            Log.i(TAG, "Existing database found, Acquiring new rideID");

            // Define a projection that specifies which columns from the database
            // you will actually use after this query
            String[] projection = {
                    RideEntry._ID,
                    RideEntry.COLUMN_NAME_RIDE_ID
            };

            String selection = RideEntry.COLUMN_NAME_RIDE_ID;
            String sortOrder = RideEntry.COLUMN_NAME_RIDE_ID + " DESC";

            try {
                Cursor cursor = db.query(
                        RideEntry.TABLE_NAME,       // Table to query
                        projection,                 // The columns to return
                        selection,                  // The columns for the WHERE clause
                        null,                       // The values for the WHERE clause
                        null,                       // Don't group rows
                        null,                       // Don't filter by row groups
                        sortOrder                   // The sort order
                );

                if (cursor != null) {
                    cursor.moveToFirst();
                    rideID = cursor.getInt(cursor.getColumnIndexOrThrow(RideEntry.COLUMN_NAME_RIDE_ID));
                }
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "Could not retrieve previous RideID from database");
                throw ex;
            }
        }
        db.close();
        return ++rideID;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This app uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(TAG,"Location Updated @: " + mCurrentLocation.getTime());
        insertData();
        Toast.makeText(this,"Location Updated",Toast.LENGTH_SHORT).show();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended, attempting to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

}
