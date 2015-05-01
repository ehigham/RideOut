package com.opentt.rideout;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import com.opentt.rideout.RideDataContract.RideData;

public class PlaybackOverviewFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    public PlaybackOverviewFragment() {
        // Required empty public constructor
    }

    /** Interface for parent activity communication */
    public interface OnMarkerWindowClickListener{
        public void onMarkerWindowClick(Marker marker);
    }

    /** The Log Tag for this fragment */
    private static final String TAG = "OverviewFragment";

    /** MarkerWindowClickListener */
    private  OnMarkerWindowClickListener activityListener;

    /** The GoogleMap set in onMapReady */
    private GoogleMap mMap;
    private View mapView;

    /** A test marker */
    private Marker mTestMarker;

    /** Ride markers */
    private List<Marker> rideMarkers = new ArrayList<>();

    /** Test Marker Position */
    private LatLng TestMarkerLatLng = new LatLng(51.5033630,-0.1276250);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mapView  = inflater.inflate(R.layout.fragment_playback_overview, container, false);

        MapFragment mapFragment =
                (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_overview);

        if ( mapFragment != null ){
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapFragment not found!");
        }

        return mapView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            activityListener = (OnMarkerWindowClickListener) activity;
        } catch (ClassCastException ex){
            throw new ClassCastException(activity.toString() +
                    " does not implement OnMarkerWindowClickListener");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add ride markers to the map if they exist and zoom to encompass
        new AddMarkersToMap().execute();

        // Set up marker and window click listeners
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // This causes the selected marker to bounce!
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1000;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

        return false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        activityListener.onMarkerWindowClick(marker);
    }

    /** AsyncTask AddMarkersToMap
     *  Takes input arguments: void
     *  Outputs: integer number of rides found
     *  Requires: List<Markers>
     */
    private class AddMarkersToMap extends AsyncTask<Void, Void, Integer> {

        private SQLiteDatabase db;
        private RideDataDbHelper mDbHelper =
                new RideDataDbHelper(getActivity().getApplicationContext());
        private List<MarkerOptions> markerOptionses = new ArrayList<>();

        @Override
        protected Integer doInBackground(Void... params) {

            // Open a readable database
            db = mDbHelper.getReadableDatabase();

            // Some vars for database navigation
            int prevID = 0;
            int thisID = 0;
            double thisLAT;
            double thisLNG;

            // Check to see if there are ride entries
            if ( !mDbHelper.isTableEmpty(db) ){

                String[] projection = {RideData.COLUMN_NAME_RIDE_ID,
                                       RideDataContract.RideData.COLUMN_NAME_LATITUDE,
                                       RideData.COLUMN_NAME_LONGITUDE};
                String selection = RideData.COLUMN_NAME_RIDE_ID;
                String sortOrder = selection + " ASC";

                try{

                    Cursor cursor = db.query(
                            RideDataContract.RideData.TABLE_NAME,
                            projection,
                            selection,
                            null,
                            null,
                            null,
                            sortOrder
                    );

                    if ((cursor != null) && (cursor.moveToFirst())) {
                        do{

                            thisID = cursor.getInt(cursor
                                    .getColumnIndexOrThrow(RideData.COLUMN_NAME_RIDE_ID));
                            thisLAT = cursor.getDouble(cursor
                                    .getColumnIndexOrThrow(RideData.COLUMN_NAME_LATITUDE));
                            thisLNG = cursor.getDouble(cursor
                                    .getColumnIndexOrThrow(RideData.COLUMN_NAME_LONGITUDE));


                            markerOptionses.add( new MarkerOptions()
                                    .title("Ride " + Integer.toString(thisID))
                                    .position(new LatLng(thisLAT, thisLNG))
                                    //.snippet("This is a simple marker")
                                    .icon(BitmapDescriptorFactory.defaultMarker(((float)thisID)*10.0f))
                                    );

                            prevID = thisID;

                            // Increment cursor until next rideID is found
                            while ((cursor.moveToNext()) && (thisID == prevID)) {
                                thisID = cursor.getInt(cursor
                                        .getColumnIndexOrThrow(RideData.COLUMN_NAME_RIDE_ID));
                            }
                        } while ( cursor.moveToNext() );
                    }
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Could not get rideID from database");
                }
            }
            db.close();

            return markerOptionses.size();
        }

        @Override
        protected void onPostExecute(Integer numRides) {
            if ( numRides > 0 ){
                //Post markers on map and add their positions to the LatLngBounds.Builder
                LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                for (MarkerOptions options : markerOptionses){
                    rideMarkers.add( mMap.addMarker(options));
                    bounds.include(options.getPosition());
                }

                // Animate camera to the marker locations
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),50));

            } else{
                Toast.makeText(getActivity().getApplicationContext(),
                        "No ride data found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
