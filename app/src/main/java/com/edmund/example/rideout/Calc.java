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

import android.util.Log;

public final class Calc {

    /**
     * Computes the difference string2 - string1
     * @param string1 of format HH:mm:ss.SSS
     * @param string2 of format HH:mm:ss.SSS
     * @return float in seconds
     */
    public static String HrsMinsSecsDiff(String string1, String string2) {
        Log.d("Calc","Call to HrsMinsSecsDiff: " + string1 + "\t" + string2);

       // if (string1.length() == 0) { return "00.00";}

       // float diff = HrsMinsSecs2Float(string2) - HrsMinsSecs2Float(string1);

        //return String.format("%2.2f",diff);

        return "00.00";
    }

    /**
     *
     * @param tmpTime of format HH:mm:ss.SSS
     * @return float in seconds
     * @throws NumberFormatException
     */
    private static float HrsMinsSecs2Float(String tmpTime) {
        tmpTime = tmpTime.trim();

        float hours = Float.valueOf(tmpTime.substring(0, 2));
        float minutes = Float.valueOf(tmpTime.substring(3, 5));
        float seconds = Float.valueOf(tmpTime.substring(6, 8));
        float millis = Float.valueOf(tmpTime.substring(9,tmpTime.length()));

        seconds += minutes * 60.0f + hours * 3600.0f;

        return seconds + millis * 0.001f;
    }

    /**
     * Converts an integer time to Hours:Minutes:Seconds format
     * @param seconds integer time in seconds to convert to Hrs:Mins:Seconds
     * @return time format in HH:MM:SS
     */
    private static String int2HrsMinsSecs(int seconds){
        int hrs = 0;
        int mins = 0;

        if (seconds > 3600){
            hrs = seconds / 3600;
            mins = (seconds % 3600) / 60;
            seconds = seconds % 60;
        } else if (seconds > 60 && seconds < 3600) {
            mins = (seconds % 3600) / 60;
            seconds = seconds % 60;
        }

        return String.format("%02d:%02d:%02d", hrs, mins, seconds);
    }

    /*
    public static double speed(Location current, Location previous){


    }
    */

}
