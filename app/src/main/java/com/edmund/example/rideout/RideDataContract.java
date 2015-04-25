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

import android.provider.BaseColumns;

public class RideDataContract {

    // Empty constructor in case of accidental instantiation
    public RideDataContract() {}

    public static abstract class RideEntry implements BaseColumns {
        public static final String TABLE_NAME = "ride_data";
        public static final String COLUMN_NAME_RIDE_ID = "rideID";
        public static final String COLUMN_NAME_TIME_STAMP = "timestamp";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_ALTITUDE = "altitude";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_BEARING = "bearing";
        public static final String COLUMN_NAME_ACCELERATION_X = "x_acceleration";
        public static final String COLUMN_NAME_ACCELERATION_Y = "y_acceleration";
        public static final String COLUMN_NAME_ACCELERATION_Z = "z_acceleration";
        public static final String COLUMN_NAME_LEAN_ANGLE = "lean_angle";
    }
}
