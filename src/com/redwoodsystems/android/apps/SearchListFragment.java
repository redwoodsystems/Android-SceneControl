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

import java.util.List;

import com.redwoodsystems.android.apps.db.LocationDBAdapter;
import com.redwoodsystems.android.apps.db.SettingsDBAdapter;
import com.redwoodsystems.android.apps.loaders.LocationsAPILoader;
import com.redwoodsystems.android.apps.utils.HttpUtil;
import com.redwoodsystems.android.apps.widgets.LoaderProgressDialog;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SearchListFragment extends ListFragment implements LoaderCallbacks<LocationsAPILoader.RESTResponse> {
	
	private static final String TAG = SearchListFragment.class.getName();
	
	private static final int LOADER_LOCATION_SEARCH = 0x1;
    private static final String ARGS_URI    = "com.redwoodsystems.android.apps.ARGS_URI";
    private static final String ARGS_PARAMS = "com.redwoodsystems.android.apps.ARGS_PARAMS";
    
    ArrayAdapter<LocationItem> mAdapter;

	private LocationDBAdapter mDbHelper;
	
	private EditText filterText;
	
	LoaderProgressDialog mProgressBar;
	
	SettingsDBAdapter mSettingsDbHelper;
	Settings settings;
    


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		Log.d(TAG, "SearchListFragment.onCreateView");
		View view = inflater.inflate(R.layout.search_layout, null);
		//Button cancel_btn = (Button)findViewById(R.id.cancel_btn);
		//cancel_btn.setOnClickListener(mCancelButtonListener);
		return view;		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "SearchListFragment.onActivityCreated called");
		
		Button cancel_btn = (Button) getActivity().findViewById(R.id.cancel_btn);
		cancel_btn.setOnClickListener(mCancelButtonListener);
		
		filterText = (EditText) getActivity().findViewById(R.id.searchText);
		filterText.addTextChangedListener(filterTextWatcher);
		filterText.setOnEditorActionListener(mSearchEditorActionListener);
		
		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
		
		
		mSettingsDbHelper = new SettingsDBAdapter(getActivity());
		mSettingsDbHelper.open();
		
		Cursor c = mSettingsDbHelper.fetchAllSettings();
		settings = mSettingsDbHelper.getSettings(c);
		
		mDbHelper = new LocationDBAdapter(getActivity());
        mDbHelper.open();
		
		mAdapter = new ArrayAdapter<LocationItem>(getActivity(), R.layout.search_item_layout);
		setListAdapter(mAdapter);
        
        // This is our REST action.
        Uri dummyUri = null;
        Bundle params = new Bundle();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_URI, dummyUri);
        args.putParcelable(ARGS_PARAMS, params);
        
        // Initialize the Loader.
        getActivity().getSupportLoaderManager().initLoader(LOADER_LOCATION_SEARCH, args, 
        		this);
        
		
	}
	
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "SearchListFragment.onPause called");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "SearchListFragment.onResume called");
	}


	private OnClickListener mCancelButtonListener  = new OnClickListener(){
		public void onClick(View v){
			//Intent intent = new Intent(getActivity(), LocationsActivity.class);
			//startActivity(intent);
			//Call finish so we return to the tabbed activity
			getActivity().finish();
		}
	};
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	        mAdapter.getFilter().filter(s);
	    }

	};
	
	
    public Loader<LocationsAPILoader.RESTResponse> onCreateLoader(int id, Bundle args) {
        if (args != null) {
        	
        	if (mProgressBar == null){
        		mProgressBar = LoaderProgressDialog.show(getActivity(), "", "", true, false, null);
        	}else {
        		mProgressBar.show();
        	}
        	
            return new LocationsAPILoader(getActivity(), settings);   
        }
        
        return null;
    }

    public void onLoadFinished(Loader<LocationsAPILoader.RESTResponse> loader, LocationsAPILoader.RESTResponse data) {
    	
    	if (mProgressBar != null){
    		mProgressBar.hide();
    	}
    	
        int    code = data.getCode();
        String json = data.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            
            List<LocationItem> locationList = HttpUtil.getLocationsFromJson(json);
            
            // Load our list adapter with our Locations.
            mAdapter.clear();
            for (LocationItem location : locationList) {
                mAdapter.add(location);
            }
            
        }
        else {
            Toast.makeText(getActivity(), "Failed to load Location data. Check your internet settings.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLoaderReset(Loader<LocationsAPILoader.RESTResponse> loader) {
    	mAdapter.clear();
    }
    
    

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//TODO:
		//Add location to the db (delete and recreate if it already exists)
		super.onListItemClick(l, v, position, id);
		
		LocationItem location = (LocationItem)l.getItemAtPosition(position);
		
		Log.d(TAG,"Clicked - locationId="+location.getLocationId());
		
		if (mDbHelper != null){
			mDbHelper.createLocation(location.getLocationId(), location.getLocationName());
		}
		
		//Intent intent = new Intent(getActivity(), LocationsActivity.class);
		//startActivity(intent);
		//Call finish so we return to the tabbed activity.
		getActivity().finish();
        		
	}
	
	OnEditorActionListener mSearchEditorActionListener = new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	    	Log.d(TAG, "onEditorAction called");
	        boolean handled = false;
	        if (actionId == EditorInfo.IME_ACTION_DONE) {
	        	dismissKeyboard();
	            handled = true;
	        }
	        return handled;
	    }
	};

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d(TAG, "SearchListFragment.onDestroyView called");
		mDbHelper.close();
		mSettingsDbHelper.close();
	}
	
	private void dismissKeyboard(){
		if (this.getActivity().getCurrentFocus() != null 
				&& this.getActivity().getCurrentFocus() instanceof EditText){
			InputMethodManager inputManager = 
			        (InputMethodManager) this.
			        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(
			        this.getActivity().getCurrentFocus().getWindowToken(),
			        InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	
    
}
