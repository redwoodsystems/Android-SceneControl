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

import com.redwoodsystems.android.apps.db.SettingsDBAdapter;

import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class SceneControllerActivity extends TabActivity {
	
	private static final String TAG = SceneControllerActivity.class.getName();
	TabHost mTabHost;
	
	SettingsDBAdapter mSettingsDbHelper;
	Settings settings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In SceneControl4TestActivity..onCreate");
        setContentView(R.layout.main);
        
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        
        TabSpec lightingSpec = mTabHost.newTabSpec("Lighting");
        lightingSpec.setIndicator("Lighting", getResources().getDrawable(R.drawable.lighting_tab));        
        Intent lightingIntent = new Intent(this, LightingActivity.class);
        lightingSpec.setContent(lightingIntent);
        //Intent lightingIntent = new Intent(this, TestFragmentActivity.class);
        //lightingSpec.setContent(lightingIntent);

        
        TabSpec locationsSpec = mTabHost.newTabSpec("Locations");
        locationsSpec.setIndicator("Locations", getResources().getDrawable(R.drawable.locations_tab));        
        Intent locationIntent = new Intent(this, LocationsActivity.class);
        locationsSpec.setContent(locationIntent);

        TabSpec settingsSpec = mTabHost.newTabSpec("Settings");
        settingsSpec.setIndicator("Settings", getResources().getDrawable(R.drawable.settings_tab));        
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsSpec.setContent(settingsIntent);

        mTabHost.addTab(lightingSpec);
        mTabHost.addTab(locationsSpec);
        mTabHost.addTab(settingsSpec);
        
		mSettingsDbHelper = new SettingsDBAdapter(this);
		mSettingsDbHelper.open();
		Cursor c = mSettingsDbHelper.fetchAllSettings();
		settings = mSettingsDbHelper.getSettings(c);
		
		Log.d(TAG, "settings="+settings);
		
		if (settings.getClusterName() !=null && !settings.getClusterName().equals("") 
				&& settings.getPassword() !=null && !settings.getPassword().equals("") ){
			//Ok
		} else {
			mTabHost.setCurrentTab(2);
		}
		
    }
    
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		Log.d(TAG, "In SceneControl4TestActivity..onDestroy");
		mSettingsDbHelper.close();
	}
}