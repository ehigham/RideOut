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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceClickListener {

    private static final String TAG = "Settings";
    public static SharedPreferences mSharedPreferences;

    /**
     * Preference Keys
     */
    public static final String PREF_KEY_SPLASH_SOUND = "pref_key_splash_sound";
    public static final String PREF_KEY_USE_LOCATION_SERVICES = "pref_key_use_location_services";
    public static final String PREF_KEY_SAMPLE_FREQUENCY  = "pref_key_sample_frequency";
    public static final String PREF_KEY_RESET_PREFERENCES  = "pref_key_reset_preferences";
    public static final String PREF_KEY_CLEAR_DATA  = "pref_key_clear_data";

    private final Context context = this;

    private SettingsFragment mSettingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettingsFragment)
                .commit();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String key = preference.getKey();

        if ( key.equals(PREF_KEY_RESET_PREFERENCES) ){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.pref_reset_preferences);
            builder.setMessage(R.string.pref_reset_preferences_dialog);
            builder.setCancelable(true);

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "Resetting Preferences");
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(context,"Preferences Reset!", Toast.LENGTH_SHORT).show();
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
            return true;

        } else if ( key.equals(PREF_KEY_CLEAR_DATA) ){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.pref_clear_data);
            builder.setMessage(R.string.pref_reset_data_dialog);
            builder.setCancelable(true);

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "Resetting Data");
                    //TODO: Clear data
                    Toast.makeText(context,"Data Cleared!", Toast.LENGTH_SHORT).show();
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
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Preference resetPref = mSettingsFragment.findPreference(PREF_KEY_RESET_PREFERENCES);
        Preference resetData = mSettingsFragment.findPreference(PREF_KEY_CLEAR_DATA);

        if ( resetPref != null ) {
            resetPref.setOnPreferenceClickListener(this);
        } else{
            Log.e(TAG, "Couldn't find preference ResetPreferences");
        }

        if ( resetPref != null ) {
            resetData.setOnPreferenceClickListener(this);
        } else{
            Log.e(TAG, "Couldn't find preference ResetData");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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