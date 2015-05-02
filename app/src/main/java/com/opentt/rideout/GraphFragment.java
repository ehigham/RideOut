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
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.opentt.rideout.RideDataContract.RideData;

public class GraphFragment extends Fragment {

    public GraphFragment() {
        //Blank Constructor
    }

    /** Interface for communication with main activity
     * Instructs GraphFragment which rideID we are plotting
     */
    interface onPlotDataListener{
        public void putID(int rideID);
    }

    private final String TAG = "GraphFragment";

    onPlotDataListener activityListener;

    /** Data series for the plot */
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

    /** GraphView */
    private GraphView graph;

    /** The RideID to plot */
    private int rideID = 1;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            activityListener = (onPlotDataListener) activity;
        } catch (ClassCastException ex){
            throw new ClassCastException(activity.toString() +
                    " does not implement onPlotDataListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        graph = (GraphView) view.findViewById(R.id.data_plot);

        new AddFieldToGraph().execute(RideData.COLUMN_NAME_SPEED);


    }

    private class AddFieldToGraph extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String field = params[0];

            Log.i(TAG, "Getting data for plot");

            // Open a readable database
            RideDataDbHelper mDbHelper = new RideDataDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Some vars for database navigation
            double thisLAT;
            double thisLNG;
            double thisField;

                String[] projection = {RideData.COLUMN_NAME_LATITUDE,
                                       RideData.COLUMN_NAME_LONGITUDE,
                                       field};
                String selection = RideData.COLUMN_NAME_RIDE_ID + " = ? ";
                String[] where = {String.valueOf(rideID)};
                String SortOrder = RideData._ID + " ASC";

                try{

                    Cursor cursor = db.query(
                            RideData.TABLE_NAME,
                            projection, selection, where, null, null, SortOrder);

                    if ((cursor != null) && (cursor.moveToFirst())) {

                        int ColumnLat = cursor
                                .getColumnIndexOrThrow(RideData.COLUMN_NAME_LATITUDE);
                        int ColumnLng = cursor
                                .getColumnIndexOrThrow(RideData.COLUMN_NAME_LONGITUDE);
                        int ColumnField = cursor
                                .getColumnIndexOrThrow(field);


                        thisLAT = cursor.getDouble(ColumnLat);
                        thisLNG = cursor.getDouble(ColumnLng);
                        thisField = cursor.getDouble(ColumnField);

                        Location firstLocation = new Location("unknown");
                        Location location = new Location("unknown");
                        firstLocation.setLatitude(thisLAT);
                        firstLocation.setLongitude(thisLNG);

                        int numPoints = cursor.getCount();

                        double x = 0.0;

                        series.appendData( new DataPoint(x,thisField), false, numPoints);

                        while ( cursor.moveToNext() ) {

                            thisLAT = cursor.getDouble(ColumnLat);
                            thisLNG = cursor.getDouble(ColumnLng);
                            thisField = cursor.getDouble(ColumnField);

                            location.setLatitude(thisLAT);
                            location.setLongitude(thisLNG);

                            x = (double) firstLocation.distanceTo(location);

                            try {
                                series.appendData(new DataPoint(x, thisField), false, numPoints);
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }

                            cursor.close();
                    }
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    throw new IllegalArgumentException("Something else went wrong");
                }
            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Log.i(TAG,"Adding data to plot");
            graph.addSeries(series);

        }
    }
}
