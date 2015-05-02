/**
 * Copyright 2015 Edmund Higham. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.opentt.rideout.RideDataContract.RideSummary;

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

    /** Ride markers */
    private List<Marker> rideMarkers = new ArrayList<>();

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
            int thisID = 0;
            double thisLAT;
            double thisLNG;
            float thisDistanceTravelled;
            String thisDuration;

            // Check to see if there are ride entries
            if ( !mDbHelper.isDataTableEmpty(db) ){

                String[] projection = {RideSummary.COLUMN_NAME_RIDE_ID,
                                       RideSummary.COLUMN_NAME_LATITUDE,
                                       RideSummary.COLUMN_NAME_LONGITUDE,
                                       RideSummary.COLUMN_NAME_DURATION,
                                       RideSummary.COLUMN_NAME_DISTANCE_TRAVELLED};
                String sortOrder = RideSummary.COLUMN_NAME_RIDE_ID + " ASC";

                try{

                    Cursor cursor = db.query(
                            RideSummary.TABLE_NAME,
                            projection, null, null, null, null, sortOrder);

                    if ((cursor != null) && (cursor.moveToFirst())) {


                        // Get column numbers for projected columns
                        int ColumnID = cursor
                                .getColumnIndexOrThrow(RideSummary.COLUMN_NAME_RIDE_ID);
                        int ColumnLat = cursor
                                .getColumnIndexOrThrow(RideSummary.COLUMN_NAME_LATITUDE);
                        int ColumnLng = cursor
                                .getColumnIndexOrThrow(RideSummary.COLUMN_NAME_LONGITUDE);
                        int ColumnDur = cursor
                                .getColumnIndexOrThrow(RideSummary.COLUMN_NAME_DURATION);
                        int ColumnDis = cursor
                                .getColumnIndexOrThrow(RideSummary.COLUMN_NAME_DISTANCE_TRAVELLED);

                        do{

                            thisID = cursor.getInt(ColumnID);
                            thisLAT = cursor.getDouble(ColumnLat);
                            thisLNG = cursor.getDouble(ColumnLng);
                            thisDuration = cursor.getString(ColumnDur);
                            thisDistanceTravelled = cursor.getFloat(ColumnDis);

                            markerOptionses.add( new MarkerOptions()
                                    .title("Ride " + Integer.toString(thisID))
                                    .position(new LatLng(thisLAT, thisLNG))
                                    .snippet(thisDistanceTravelled + "m, " + thisDuration)
                                    .icon(BitmapDescriptorFactory.defaultMarker(((float)thisID)*10.0f))
                                    );

                        } while ( cursor.moveToNext() );
                        cursor.close();
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
