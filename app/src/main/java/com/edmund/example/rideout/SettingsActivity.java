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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
                        OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_SAMPLE_FREQUENCY = "pref_sample_frequency";
    public static SharedPreferences mSharedPreferences;

    /**
     * Preference Keys
     */
    public static final String PREF_KEY_SPLASH_SOUND = "pref_key_splash_sound";
    public static final String PREF_KEY_USE_LOCATION_SERVICES = "pref_key_use_location_services";
    public static final String PREF_KEY_SAMPLE_FREQUENCY  = "pref_key_sample_frequency";
    public static final String PREF_KEY_ENABLE_LINEAR_ACCELEROMETERS = "pref_key_enable_linear_accelerometers";
    public static final String PREF_KEY_ENABLE_GYROS  = "pref_key_enable_gyros";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SAMPLE_FREQUENCY)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, "2 seconds"));
        }
    }

    public void onFactorySettingsReset(){

    }
}
