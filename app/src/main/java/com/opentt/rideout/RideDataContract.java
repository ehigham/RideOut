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

import android.provider.BaseColumns;

public class RideDataContract {

    // Empty constructor in case of accidental instantiation
    public RideDataContract() {}

    public static abstract class RideData implements BaseColumns {
        public static final String TABLE_NAME = "ride_data";
        public static final String RIDE_ID = "rideID";
        public static final String TIME_STAMP = "timestamp";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ALTITUDE = "altitude";
        public static final String SPEED = "speed";
        public static final String BEARING = "bearing";
        public static final String ACCELERATION_X = "x_acceleration";
        public static final String ACCELERATION_Y = "y_acceleration";
        public static final String ACCELERATION_Z = "z_acceleration";
        public static final String LEAN_ANGLE = "lean_angle";
    }

    public static abstract class RideSummary implements BaseColumns {
        public static final String TABLE_NAME = "ride_summary";
        public static final String RIDE_ID = "rideID";
        public static final String TIME_STAMP = "timestamp";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String DURATION = "duration";
        public static final String DISTANCE_TRAVELLED = "distance_travelled";
        public static final String MAX_SPEED = "max_speed";
        public static final String MAX_LEAN_ANGLE = "max_lean_angle";
    }
}
