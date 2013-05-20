/*
Copyright 2013 Redwood Systems Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.redwoodsystems.android.apps.db;

import com.redwoodsystems.android.apps.LocationItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Locations database access helper class. Defines the basic CRUD operations
 * for the app, and gives the ability to list all Locations as well as
 * retrieve or modify Locations.
 *
 * All Locations are written to a single row this version
 */


public class LocationDBAdapter {
	
    public static final String KEY_LOCATION_ID = "location_id";
    public static final String KEY_LOCATION_NAME = "location_name";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "LocationDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table Locations (_id integer primary key autoincrement, "
        + "location_id integer not null, location_name text not null);";

    private static final String DATABASE_NAME = "rwdataloc";
    private static final String DATABASE_TABLE = "Locations";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS Locations");
            onCreate(db);
        }
    }
    
    public LocationDBAdapter(Context ctx){
    	this.mCtx = ctx;
    }
    
    public LocationDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new Location using the title and body provided. If the Locations is
     * successfully created return the new rowId for that Locations, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the Locations
     * @param body the body of the Locations
     * @return rowId or -1 if failed
     */
    public long createLocation(Integer locationId, String locationName) {
    	
    	//First find out if location already exists
    	Cursor c = fetchLocationWithLocId(locationId.longValue());
    	int cnt = c.getCount();
    	
    	if (cnt > 0) {
    		Log.d(TAG,"Location already exists: "+locationId.toString());
    		return -1;
    	} else {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_LOCATION_ID, locationId);
	        initialValues.put(KEY_LOCATION_NAME, locationName);
	        return mDb.insert(DATABASE_TABLE, null, initialValues);
    	}
    }

    /**
     * Delete the Location with the given rowId
     * 
     * @param rowId id of Locations to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteLocation(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteLocationWithLocId(long locationId) {

        return mDb.delete(DATABASE_TABLE, KEY_LOCATION_ID + "=" + locationId, null) > 0;
    }
    
    public boolean deleteAllLocations(){
    	return mDb.delete(DATABASE_TABLE, null, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all Locations in the database
     * 
     * @return Cursor over all Locations
     */
    public Cursor fetchAllLocations() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LOCATION_ID,
        		KEY_LOCATION_NAME}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Locations that matches the given rowId
     * 
     * @param rowId id of Locations to retrieve
     * @return Cursor positioned to matching Locations, if found
     * @throws SQLException if Locations could not be found/retrieved
     */
    public Cursor fetchLocation(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LOCATION_ID,
            		KEY_LOCATION_NAME}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchLocationWithLocId(long locationId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LOCATION_ID,
            		KEY_LOCATION_NAME}, KEY_LOCATION_ID + "=" + locationId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the Location using the details provided. The Locations to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of Locations to update
     * @param title value to set Locations title to
     * @param body value to set Locations body to
     * @return true if the Locations was successfully updated, false otherwise
     */
    public boolean updateLocation(long rowId, Integer locationId, String locationName, String activeSceneName) {
        ContentValues args = new ContentValues();
        args.put(KEY_LOCATION_ID, locationId);
        args.put(KEY_LOCATION_NAME, locationName);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateLocationWithLocId(Integer locationId, String locationName, String activeSceneName) {
        ContentValues args = new ContentValues();
        args.put(KEY_LOCATION_ID, locationId);
        args.put(KEY_LOCATION_NAME, locationName);

        return mDb.update(DATABASE_TABLE, args, KEY_LOCATION_ID + "=" + locationId, null) > 0;
    }
    
    public int getLocationId(Cursor c) {
        return(c.getInt(1));
    }
    
    public String getLocationName(Cursor c) {
        return(c.getString(2));
    }
    
    public LocationItem getLocation(Cursor c){
    	return (new LocationItem(this.getLocationId(c), this.getLocationName(c)));
    }
    

	

}
