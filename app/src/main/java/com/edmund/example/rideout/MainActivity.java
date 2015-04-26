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
import android.provider.ContactsContract;
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
    protected ToggleButton mDataAcquisitionToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar();

        // Locate the UI widgets.
        mDataAcquisitionToggle = (ToggleButton) findViewById(R.id.togglebutton);

    }

    /**
     * Handles the Toggle Updates button, and requests start/removal of location updates.
     *
     */
    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) { // Enable Location Updates
            Intent intent = new Intent(this,DataAcquisitionService.class);
            intent.setAction(DataAcquisitionService.ACTION_START_ACQUISITION);
            startService(intent);

        } else { // Disable Location Updates
            Intent intent = new Intent(this,DataAcquisitionService.class);
            intent.setAction(DataAcquisitionService.ACTION_STOP_ACQUISITION);
            startService(intent);

            }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
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
        // Check to see if the DataAcquisitionService is running and turn off if true.
        // TODO: Disable DataAcquisitionService if running (SORT OUT THREADS!!!)
        Intent intent = new Intent(this,DataAcquisitionService.class);
        intent.setAction(DataAcquisitionService.ACTION_STOP_ACQUISITION);
        startService(intent);

        startActivity(playbackActivity);
        return true;
    }

}