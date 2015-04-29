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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Settings";
    public static SharedPreferences mSharedPreferences;

    /**
     * Preference Keys
     */
    public static final String PREF_KEY_SPLASH_SOUND = "pref_key_splash_sound";
    public static final String PREF_KEY_USE_LOCATION_SERVICES = "pref_key_use_location_services";
    public static final String PREF_KEY_SAMPLE_FREQUENCY  = "pref_key_sample_frequency";
    public static final String PREF_KEY_ENABLE_LINEAR_ACCELEROMETERS = "pref_key_enable_linear_accelerometers";
    public static final String PREF_KEY_ENABLE_GYROS  = "pref_key_enable_gyros";
    public static final String PREF_KEY_RESET_PREFERENCES  = "pref_key_reset_preferences";
    public static final String PREF_KEY_LEGAL_INFO  = "pref_key_legal_info";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if ( key.equals(PREF_KEY_RESET_PREFERENCES) ){
            final Preference pref = findPreference(PREF_KEY_RESET_PREFERENCES);
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle(R.string.pref_reset_preferences);
            builder.setMessage(R.string.pref_reset_data_dialog);
            builder.setCancelable(true);

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "Resetting Things");
                    //TODO: Reset settings
                    Toast.makeText(getApplicationContext(),"Preferences Reset!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

        }
    }
}