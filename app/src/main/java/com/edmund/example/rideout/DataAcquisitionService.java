package com.edmund.example.rideout;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.util.Log;

import com.edmund.example.rideout.RideDataContract.RideEntry;

import java.sql.Date;

public class DataAcquisitionService extends Service {
    public DataAcquisitionService() {
    }

    /** log TAG */
    private static final String TAG = "DataAcquisitionService";

    /** Booleans for Location services and hardware sensors. Configurable in settings */
    private static Boolean mReqestingLocation = true;
    private static Boolean mRequestingHardwareSensors = false;

    /** Intents for Location and Hardware Sensors Services */
    private static Intent Intent_Location;

    /** Database helper */
    private static RideDataDbHelper mDbHelper;

    /** Database */
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


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate(){
        // TODO: Get user preferences (eg Location/Hardware Sensors)
        // TODO: Check if a mySQLite database exists, if not create one.
        Intent_Location = new Intent(this,LocationService.class);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // TODO: Create new mySQLite entry for ride
        mDbHelper = new RideDataDbHelper(this);
        // Find if the database has been initialised and get the rideID for the next ride
        db = mDbHelper.getReadableDatabase();
        rideID = getNewRideId();
        db.close();
        db = mDbHelper.getWritableDatabase();

        Log.i(TAG, "Starting Data Acquisition Service with RideID: " + rideID);

        if (mReqestingLocation){
            startService(Intent_Location);
        }

        if (mRequestingHardwareSensors){
            // TODO: Request Hardware Sensors
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        stopService(Intent_Location);
        mDbHelper.exportDB(db, this);
        db.close();
    }

    protected static void insertData(){
        // TODO: Fix arguments to put actual values in!

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(RideEntry.COLUMN_NAME_RIDE_ID, rideID);
        values.put(RideEntry.COLUMN_NAME_TIME_STAMP, LocationService.mCurrentLocation.getTime());
        values.put(RideEntry.COLUMN_NAME_LATITUDE, LocationService.mCurrentLocation.getLatitude());
        values.put(RideEntry.COLUMN_NAME_LONGITUDE, LocationService.mCurrentLocation.getLongitude());
        values.put(RideEntry.COLUMN_NAME_ALTITUDE, LocationService.mCurrentLocation.hasAltitude() ? LocationService.mCurrentLocation.getAltitude() : -1);
        values.put(RideEntry.COLUMN_NAME_SPEED, LocationService.mCurrentLocation.hasSpeed() ? LocationService.mCurrentLocation.getSpeed() : -1);
        values.put(RideEntry.COLUMN_NAME_BEARING, LocationService.mCurrentLocation.hasBearing() ? LocationService.mCurrentLocation.getBearing() : -1);
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
     * If one exists, the ID is incremented and stored in rideID
     * If not, no database has been created and so sets rideID to 1 */
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
            String selectionArgs[] = null;
            String sortOrder = RideEntry.COLUMN_NAME_RIDE_ID + " DESC";

            try {
                Cursor cursor = db.query(
                        RideEntry.TABLE_NAME,       // Table to query
                        projection,                 // The columns to return
                        selection,                  // The columns for the WHERE clause
                        selectionArgs,              // The values for the WHERE clause
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

}
