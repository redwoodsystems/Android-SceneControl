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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends FragmentActivity {
	
	private static final String TAG = SearchActivity.class.getName();
	
    private static final int LOADER_LOCATIONS_SEARCH = 0x1;
    
	private static final int LOADER_TWITTER_SEARCH = 0x1;
    private static final String ARGS_URI    = "com.redwoodsystems.android.apps.ARGS_URI";
    private static final String ARGS_PARAMS = "com.redwoodsystems.android.apps.ARGS_PARAMS";
        
    private ArrayAdapter<String> mAdapter;
    
	Activity mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		Log.d(TAG,"in SearchActivity.onCreate..");
		
		
		 // Since we are using the Android Compatibility library
        // we have to use FragmentActivity. So, we use ListFragment
        // to get the same functionality as ListActivity.
        FragmentManager fm = getSupportFragmentManager();
        
        SearchListFragment list = new SearchListFragment();
        
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(android.R.id.content, list);
        ft.commit();
        
        
		//displayDummyData();
		
		//TODO:
		//load list with locations returned from REST call (use loader)?
	}
	
}
