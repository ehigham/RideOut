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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.opentt.rideout.RideDataContract.RideEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class RideDataDbHelper extends SQLiteOpenHelper{
    private static final String TAG = RideDataDbHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "RideData.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RideEntry.TABLE_NAME + " (" +
                    RideEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RideEntry.COLUMN_NAME_RIDE_ID        + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_TIME_STAMP     + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_LATITUDE       + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_LONGITUDE      + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_ALTITUDE       + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_SPEED          + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_BEARING        + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_ACCELERATION_X + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_ACCELERATION_Y + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_ACCELERATION_Z + TEXT_TYPE + COMMA_SEP +
                    RideEntry.COLUMN_NAME_LEAN_ANGLE     + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RideEntry.TABLE_NAME;

    public RideDataDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i(TAG, "Database Path = " + db.getPath());
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // TODO: Change method to store old entries in new database
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
     *
     * <p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            Log.e(TAG, "Cannot downgrade database to a version greater than its current " +
                  "version: V" + db.getVersion() + " -> " + newVersion + " : " + getDatabaseName());
        } else {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * Called when the database has been opened.  The implementation
     * should check {@link SQLiteDatabase#isReadOnly} before updating the
     * database.
     * <p>
     * This method is called after the database connection has been configured
     * and after the database schema has been created, upgraded or downgraded as necessary.
     * If the database connection must be configured in some way before the schema
     * is created, upgraded, or downgraded, do it in {@link #onConfigure} instead.
     * </p>
     *
     * @param db The database.
     */
    public void onOpen(SQLiteDatabase db){
        if (db.isReadOnly()){
            //TODO: Do something
        }
    }

    public boolean isTableEmpty(SQLiteDatabase db){
        boolean flag = false;

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + RideEntry.TABLE_NAME, null);

        if ( cursor != null && cursor.moveToFirst() ){
             if ( cursor.getInt(0) == 0 ) { // If table is empty, a "0" is placed in row 1, column 0
                 flag = true;
                 Log.i(TAG,"Found zero entry in row 1: Table " + RideEntry.TABLE_NAME + " is empty");
             }
            cursor.close();
        }

        return flag;
    }

    public void exportDB(SQLiteDatabase db){
        FileChannel source=null;
        FileChannel destination=null;
        String currentDBPath = db.getPath();
        String backupDBPath = "/RideDataBackup";

        // Check to see if external storage is writable
        if ( isExternalStorageWritable() ) {

            File currentDB = new File(currentDBPath);

            String root = Environment.
                    getExternalStorageDirectory().getAbsolutePath();

            File backupDbDir = new File(root + backupDBPath);
            if ( !backupDbDir.exists() ) {
                if( !backupDbDir.mkdir() ) Log.e(TAG,"Could not make backup directory");
            }

            File backupDB = new File(backupDbDir,"backup.db");

            if (!backupDB.exists()) {

                try {
                    Log.i(TAG,"Attempting to create new file at: " + backupDB.getPath());
                    backupDB.createNewFile();
                } catch (IOException ex){
                    Log.e(TAG, "Failed to create new file");
                    ex.printStackTrace();
                }
            }

            try {
                source = new FileInputStream(currentDB).getChannel();
                destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Log.i(TAG, "Database Exported to " + backupDB.getPath());
                //Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else{
            Log.e(TAG, "External Storage not Writable");
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
