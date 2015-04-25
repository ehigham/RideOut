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

package com.edmund.example.rideout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
//import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    protected static final String TAG = "RideOut";

    // UI Widgets.
    protected ToggleButton mLocationsToggle;

    protected static Intent LocationServicesIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar();

        // Locate the UI widgets.
        mLocationsToggle = (ToggleButton) findViewById(R.id.togglebutton);

        //mRequestingLocationUpdates = false;
        LocationServicesIntent = new Intent(this, DataAcquisitionService.class);

        // Ensure Toggle button is off
        mLocationsToggle.setChecked(false);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        //mLocationManager = new LocationManager(this);
    }

    /**
     * Handles the Toggle Updates button, and requests start/removal of location updates.
     *
     */
    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) { // Enable Location Updates
            startService(LocationServicesIntent);

            /*if (!mRequestingLocationUpdates){ // If updates are already requested, do nothing.
                mRequestingLocationUpdates = true;
                mLocationManager.startLocationUpdates();
            }*/

        } else { // Disable Location Updates
            stopService(LocationServicesIntent);
           /* if (mRequestingLocationUpdates){ // If updates are not requested, do nothing.
                mRequestingLocationUpdates = false;
                mLocationManager.stopLocationUpdates();
            }*/
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    protected static void updateUI() {
/*        if (mLocationManager.mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mLocationManager.mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mLocationManager.mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLocationManager.mLastUpdateTime);
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mLocationManager.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        //if (mRequestingLocationUpdates) mLocationManager.resumeLocationServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        //mLocationManager.pauseLocationServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
    //        stopService(LocationServicesIntent);
        // Ensure Toggle button is off
        mLocationsToggle.setChecked(false);
        }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
      /*  savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLocationManager.mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY,mLocationManager.mLastUpdateTime);*/
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPlaybackClicked(View view){
        Intent playbackActivity = new Intent(getApplicationContext(), PlaybackActivity.class);
        startActivity(playbackActivity);
        return true;
    }

}