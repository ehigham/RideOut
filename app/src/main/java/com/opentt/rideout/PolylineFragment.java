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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.opentt.rideout.RideDataContract.RideData;

public class PolylineFragment extends Fragment
        implements OnMapReadyCallback {


    /** Log TAG */
    private final String TAG = "PolylineFragment";

    public PolylineFragment() {
        // Required empty public constructor
    }

    /** The GoogleMap set in onMapReady */
    private GoogleMap mMap;
    private View mapView;

    private int RideID = 1;


    /** Polyline */
    Polyline mPolyline;

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

        /*try{
            activityListener = (OnMarkerWindowClickListener) activity;
        } catch (ClassCastException ex){
            throw new ClassCastException(activity.toString() +
                    " does not implement OnMarkerWindowClickListener");
        }*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add ride markers to the map if they exist and zoom to encompass
        new AddPolylineToMap().execute();
    }

    /** AsyncTask AddMarkersToMap
     *  Takes input arguments: void
     *  Outputs: integer number of rides found
     *  Requires: List<Markers>
     */
    private class AddPolylineToMap extends AsyncTask<Void, Void, Void> {

        private SQLiteDatabase db;
        private RideDataDbHelper mDbHelper =
                new RideDataDbHelper(getActivity().getApplicationContext());
        private PolylineOptions polylineOptions = new PolylineOptions();
        private LatLngBounds.Builder bounds = new LatLngBounds.Builder();


        protected Void doInBackground(Void... params) {

            // Open a readable database
            db = mDbHelper.getReadableDatabase();

            // Check to see if there are ride entries
            if (!mDbHelper.isDataTableEmpty(db)) {

                String[] projection = {RideData.COLUMN_NAME_LATITUDE,
                        RideData.COLUMN_NAME_LONGITUDE,};
                String selection = RideData.COLUMN_NAME_RIDE_ID + " = ? ";
                String[] where = {String.valueOf(RideID)};
                String sortOrder = RideData._ID + " ASC";

                try {

                    Cursor cursor = db.query(
                            RideData.TABLE_NAME,
                            projection, selection, where, null, null, sortOrder);

                    if ((cursor != null) && (cursor.moveToFirst())) {

                        // Some vars for database navigation
                        double thisLAT;
                        double thisLNG;

                        // Get projection column numbers
                        int ColumnLat = cursor
                                .getColumnIndexOrThrow(RideData.COLUMN_NAME_LATITUDE);
                        int ColumnLng = cursor
                                .getColumnIndexOrThrow(RideData.COLUMN_NAME_LONGITUDE);

                        // LatLng for bounds and PolylineOptions
                        LatLng thisLatLng;


                        do {

                            thisLAT = cursor.getDouble(ColumnLat);
                            thisLNG = cursor.getDouble(ColumnLng);

                            thisLatLng = new LatLng(thisLAT, thisLNG);

                            polylineOptions.add(thisLatLng);
                            bounds.include(thisLatLng);

                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Could not get rideID from database");
                }
            }
            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            polylineOptions.width(5.0f);
            mPolyline = mMap.addPolyline(polylineOptions);
            mPolyline.setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),50));

        }
    }
}
