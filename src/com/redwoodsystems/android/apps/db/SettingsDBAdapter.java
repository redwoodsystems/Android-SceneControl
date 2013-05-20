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

import com.redwoodsystems.android.apps.Settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple settings database access helper class. Defines the basic CRUD operations
 * for the app, and gives the ability to list all Settings as well as
 * retrieve or modify settings.
 *
 * All settings are written to a single row this version
 */


public class SettingsDBAdapter {
	
    public static final String KEY_CLUSTER = "cluster_name";
    public static final String KEY_API_USER = "api_user_name";
    public static final String KEY_API_PWD = "api_user_pwd";
    public static final String KEY_LAST_LOCATION_ID = "last_location_id";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "SettingsDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table settings (_id integer primary key autoincrement, "
                    + "cluster_name text not null, api_user_name text not null, api_user_pwd text not null," +
                    " last_location_id integer );";

    private static final String DATABASE_NAME = "rwdataset";
    private static final String DATABASE_TABLE = "settings";
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
            db.execSQL("DROP TABLE IF EXISTS settings");
            onCreate(db);
        }
    }
    
    public SettingsDBAdapter(Context ctx){
    	this.mCtx = ctx;
    }
    
    public SettingsDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new Settings using the title and body provided. If the Settings is
     * successfully created return the new rowId for that Settings, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the Settings
     * @param body the body of the Settings
     * @return rowId or -1 if failed
     */
    public long createSettings(String clusterName, String apiUserName, String apiUserPwd,
    		int lastLocationId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CLUSTER, clusterName);
        initialValues.put(KEY_API_USER, apiUserName);
        initialValues.put(KEY_API_PWD, apiUserPwd);
        initialValues.put(KEY_LAST_LOCATION_ID, lastLocationId);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    
    public long createUpdateSettings(String clusterName, String apiUserName, String apiUserPwd,
    		int lastLocationId) {
        ContentValues args = new ContentValues();
        args.put(KEY_CLUSTER, clusterName);
        args.put(KEY_API_USER, apiUserName);
        args.put(KEY_API_PWD, apiUserPwd);
        args.put(KEY_LAST_LOCATION_ID, lastLocationId);
        
        Cursor c = fetchAllSettings();
        if (c != null && c.getCount() >0){
        	return mDb.update(DATABASE_TABLE, args, null, null);
        } else {
        	long rowId = mDb.insert(DATABASE_TABLE, null, args);
        	if (rowId == -1){
        		return 0;
        	}else {
        		c = fetchAllSettings();
        		Log.d(TAG, "settingsDb.count="+c.getCount());
        		return (c.getCount());
        	}
        }
    }
    
    public Cursor createUpdateSettings(Settings settings){
        ContentValues args = new ContentValues();
        args.put(KEY_CLUSTER, settings.getClusterName());
        args.put(KEY_API_USER, settings.getUserName());
        args.put(KEY_API_PWD, settings.getPassword());
        args.put(KEY_LAST_LOCATION_ID, settings.getLastLocationId());

        Cursor c = fetchAllSettings();
        if (c != null && c.getCount() >0){
        	c.moveToFirst();
        	int cnt = mDb.update(DATABASE_TABLE, args, null, null);
        	if (cnt > 0){
        		return (fetchAllSettings());
        	}
        } else {
        	long rowId = mDb.insert(DATABASE_TABLE, null, args);
        	if (rowId > -1){
        		return (fetchAllSettings());
        	}
        }
        return null;
    }

    /**
     * Delete the Settings with the given rowId
     * 
     * @param rowId id of Settings to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteSettings(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all Settings in the database
     * 
     * @return Cursor over all Settings
     */
    public Cursor fetchAllSettings() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CLUSTER,
                KEY_API_USER, KEY_API_PWD, KEY_LAST_LOCATION_ID}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Settings that matches the given rowId
     * 
     * @param rowId id of Settings to retrieve
     * @return Cursor positioned to matching Settings, if found
     * @throws SQLException if Settings could not be found/retrieved
     */
    public Cursor fetchSettings(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CLUSTER,
                    KEY_API_USER, KEY_API_PWD, KEY_LAST_LOCATION_ID}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the Settings using the details provided. The Settings to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of Settings to update
     * @param title value to set Settings title to
     * @param body value to set Settings body to
     * @return true if the Settings was successfully updated, false otherwise
     */
    public boolean updateSettings(long rowId, String clusterName, String apiUserName,
    		String apiUserPwd, int lastLocationId) {
        ContentValues args = new ContentValues();
        args.put(KEY_CLUSTER, clusterName);
        args.put(KEY_API_USER, apiUserName);
        args.put(KEY_API_PWD, apiUserPwd);
        args.put(KEY_LAST_LOCATION_ID, lastLocationId);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public String getClusterName(Cursor c){
    	if (c!= null && c.getCount()>0){
    		Log.d("DB", "count="+c.getCount());
    		c.moveToFirst();
    		return (c.getString(1));
    	} else {
    		return "";
    	}
    }
    
    public String getUserName(Cursor c){
    	if (c!= null & c.getCount()>0){
    		c.moveToFirst();
    		return (c.getString(2));
    	} else {
    		return "admin";
    	}
    }
    
    public String getPassword(Cursor c){
    	if (c!= null & c.getCount()>0){
    		c.moveToFirst();
    		return (c.getString(3));
    	} else {
    		return "";
    	}
    }
    
    public int getLastLocationId(Cursor c){
    	if (c!= null & c.getCount()>0){
    		c.moveToFirst();
    		return (c.getInt(4));
    	} else {
    		return -1;
    	}
    }
    
    public Settings getSettings(Cursor c){
    	return (new Settings(this.getClusterName(c),
    			this.getUserName(c),
    			this.getPassword(c),
    			this.getLastLocationId(c)));
    }
    
}
