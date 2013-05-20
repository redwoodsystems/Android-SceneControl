
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
package com.redwoodsystems.android.apps;

import com.redwoodsystems.android.apps.db.LocationDBAdapter;
import com.redwoodsystems.android.apps.db.SettingsDBAdapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SettingsActivity extends Activity {
	
	private static final String TAG = SettingsActivity.class.getName();
	
	Activity mContext;
	SettingsDBAdapter mSettingsDbHelper;
	Cursor settingsModel=null;
	Settings settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "SettingsActivity .. onCreate");
		setContentView(R.layout.settings_layout);
		
		EditText clusterNameEditText = (EditText) findViewById(R.id.clusterName);
		clusterNameEditText.setOnEditorActionListener(mClusterNameEditorActionListemer);
		
		EditText passwordEditText = (EditText) findViewById(R.id.password);
		passwordEditText.setOnEditorActionListener(mPasswordEditorActionListemer);
		
		mSettingsDbHelper = new SettingsDBAdapter(this);
		mSettingsDbHelper.open();
		
		
		settingsModel = mSettingsDbHelper.fetchAllSettings();
		startManagingCursor(settingsModel);
		
		if (settingsModel != null && settingsModel.getCount() > 0){
			Log.d(TAG, "SettingsActivity .. found db settings");
			settings = mSettingsDbHelper.getSettings(settingsModel);
			Log.d(TAG, "settings="+settings);
			clusterNameEditText.setText(settings.getClusterName());
			passwordEditText.setText(settings.getPassword());
		} else {
			settings = new Settings();
		}
	}
	
	
	


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "In SettingsActivity..onDestroy");
		mSettingsDbHelper.close();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "In SettingsActivity..onPause");
		//save values to the database
		EditText clusterNameEditText = (EditText) findViewById(R.id.clusterName);
		EditText passwordEditText = (EditText) findViewById(R.id.password);
		String clusterName = clusterNameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		saveSettings(clusterName, password);
		
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d(TAG, "In SettingsActivity..onRestart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "In SettingsActivity..onResume");
		
		if (settings.getClusterName() !=null && !settings.getClusterName().equals("") 
				&& settings.getPassword() !=null && !settings.getPassword().equals("") ){
			//Ok
		} else {
			Toast.makeText(this, "Update Settings and add Locations to use this application", Toast.LENGTH_SHORT).show();
		}
	}



	OnEditorActionListener mClusterNameEditorActionListemer = new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	    	Log.d(TAG, "onEditorAction called");
	        boolean handled = false;

	        String clusterName = v.getText().toString();
	        EditText editText = (EditText) findViewById(R.id.password);
	        String password = editText.getText().toString();
	        
	        if (actionId == EditorInfo.IME_ACTION_NEXT) {
	        	saveSettings(clusterName,password);
	        	editText.requestFocus();
	            handled = true;
	        }
	        return handled;
	    }
	};
	
	OnEditorActionListener mPasswordEditorActionListemer = new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	    	Log.d(TAG, "onEditorAction called");
	        boolean handled = false;
	        
	        EditText editText = (EditText) findViewById(R.id.clusterName);
	        String clusterName = editText.getText().toString();
	        String password = v.getText().toString();
	        
	        if (actionId == EditorInfo.IME_ACTION_DONE) {
	        	saveSettings(clusterName,password);
	        	dismissKeyboard();
	            handled = true;
	        }
	        return handled;
	    }
	};
	
	
	private void dismissKeyboard(){
		if (this.getCurrentFocus() != null && this.getCurrentFocus() instanceof EditText){
			InputMethodManager inputManager = 
			        (InputMethodManager) this.
			            getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(
			        this.getCurrentFocus().getWindowToken(),
			        InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	private Settings initSettings(){
    	if (settingsModel != null && settingsModel.getCount() > 0){
    		if (settings == null){
    			settings = mSettingsDbHelper.getSettings(settingsModel);
    		}
    	} else {
    		if (settings == null){
    			settings = new Settings();
    		}
    	}
    	return settings;
	}
	
	private void saveSettings(String clusterName, String password){
		Log.d(TAG, "saveSettings called");
    	//make sure settings points to latest values
    	initSettings();
    	settings.setClusterName(clusterName);
    	settings.setPassword(password);
    	
    	Settings prevSettings = mSettingsDbHelper.getSettings(settingsModel);
    	if ((prevSettings.getClusterName().equals(settings.getClusterName()))
    			&& prevSettings.getPassword().equals(settings.getPassword())){
    		//no change
    		Log.d(TAG, "Settings unchanged from database.");
    	} else {
    		
    		//erase lastLocationId
    		settings.setLastLocationId(-1);
        	//sync settings to DB
        	settingsModel = mSettingsDbHelper.createUpdateSettings(settings);
        	settings = mSettingsDbHelper.getSettings(settingsModel);
        	
        	//delete locations
        	LocationDBAdapter locationsDbHelper = new LocationDBAdapter(this);
        	locationsDbHelper.open();
        	
        	Cursor locModel = locationsDbHelper.fetchAllLocations();
        	if (locModel != null && locModel.getCount() >0){
        		Log.d(TAG, "Deleting all locations from database");
        		locationsDbHelper.deleteAllLocations();
        		Log.d(TAG, "locations db count ="+locationsDbHelper.fetchAllLocations().getCount());
        	}
        	locationsDbHelper.close();
    	}
    	
    	Log.d(TAG, "settings="+settings);
	}
	
	
}