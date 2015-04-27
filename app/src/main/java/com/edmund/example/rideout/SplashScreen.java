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

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends Activity {

    protected static boolean playSplashSound; //Set by PreferencesManager

    private static Handler mHandler = new Handler();

    /**
     * Media player to play splash sound
     */
    private static MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Splash duration
         */
        long splashInterval;

        // Find out if the user has enabled splash screen
        getUserPreferences();

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.activity_splash);

            if (playSplashSound) {
                // If the user likes Nic Cage, play it!
                mediaPlayer = MediaPlayer.create(SplashScreen.this, R.raw.okay_lets_ride);
                // Stop the media player when it finishes.
                mediaPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                    }
                });

                splashInterval = mediaPlayer.getDuration();
                mediaPlayer.start();
            } else {
                // Default to 1s
                splashInterval = 1000;
            }

            mHandler.postDelayed(mRunnable, splashInterval);
        }

    private void getUserPreferences(){
        playSplashSound = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_KEY_SPLASH_SOUND,true);
        //playSplashSound = true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);

            this.finish();
        }

        private void finish() {
            // Release the media player
            if (playSplashSound) {
                mediaPlayer.release();
            }
        }
    };

}