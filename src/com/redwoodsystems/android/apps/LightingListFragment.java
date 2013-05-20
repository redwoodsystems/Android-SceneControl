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

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.redwoodsystems.android.apps.db.LocationDBAdapter;
import com.redwoodsystems.android.apps.db.SettingsDBAdapter;
import com.redwoodsystems.android.apps.loaders.SceneUpdateAPILoader;
import com.redwoodsystems.android.apps.loaders.ScenesAPILoader;
import com.redwoodsystems.android.apps.loaders.SceneUpdateAPILoader.RESTResponse;
import com.redwoodsystems.android.apps.utils.HttpUtil;
import com.redwoodsystems.android.apps.widgets.LoaderProgressDialog;

public class LightingListFragment extends ListFragment implements LoaderCallbacks<ScenesAPILoader.RESTResponse> {
	
	private static final String TAG = SearchListFragment.class.getName();
	
	private static final int LOADER_SCENES = 0x2;
	private static final int LOADER_UPDATE_SCENES = 0x3;
    private static final String ARGS_URI    = "com.redwoodsystems.android.apps.ARGS_URI";
    private static final String ARGS_PARAMS = "com.redwoodsystems.android.apps.ARGS_PARAMS";

    Activity mContext;

    //Locations
	LocationDBAdapter mLocationDbHelper;
	Cursor model=null;
	LocationSpinnerAdapter mSpinnerAdapter;
	int mPos;
	LocationItem mCurrentLocation;

	//Settings
	SettingsDBAdapter mSettingsDbHelper;
	Settings settings;
	
	//Scenes
	SceneAdapter mSceneAdapter;
    
    LoaderProgressDialog mProgressBar;
    
    String mSceneNameToActivate;
    String mActiveSceneName;
    
    
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		Log.d(TAG, "LightingListFragment.onCreateView");
		View view = inflater.inflate(R.layout.lighting_layout, null);
		return view;		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "LightingListFragment.onActivityCreated");
		
		mContext = getActivity();
		
		//Settings
		mSettingsDbHelper = new SettingsDBAdapter(getActivity());
		mSettingsDbHelper.open();
		//Cursor settingsCursor = mSettingsDbHelper.fetchAllSettings();
		//settings = mSettingsDbHelper.getSettings(settingsCursor);
		
		//Locations
		mLocationDbHelper = new LocationDBAdapter(getActivity()); 	
		mLocationDbHelper.open();
		
		//Handle the refresh scenes button
		setupRefreshButton();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "LightingListFragment.onPause called");
		//save lastLocationId to the database
		if (mCurrentLocation != null){
			settings.setLastLocationId(mCurrentLocation.locationId);
			mSettingsDbHelper.createUpdateSettings(settings);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "LightingListFragment.onResume called");
		
		//query settings
		Cursor settingsCursor = mSettingsDbHelper.fetchAllSettings();
		settings = mSettingsDbHelper.getSettings(settingsCursor);
		
		//Spinner
		setupLocationSpinner();
		
		//Set Spinner to stored value if present
		//check if stored value still exists. It could have been deleted
		int storedLocationId = settings.getLastLocationId();
		if (storedLocationId > -1){
			Spinner spinner = (Spinner) getActivity().findViewById(R.id.locationSpinner);
			int pos = -1;
			for (int i=0; i<spinner.getCount(); i++){
				Cursor c = (Cursor) spinner.getItemAtPosition(i);
				int locId = mLocationDbHelper.getLocationId(c);
				if (locId == storedLocationId){
					pos = i;
					Log.d(TAG, "setting spinner to stored value = "+Integer.toString(storedLocationId));
					spinner.setSelection(i);
					mCurrentLocation = mLocationDbHelper.getLocation(c);
				}
			}
		}
		
	}

	
	private void setupRefreshButton(){
		ImageButton refreshBtn = (ImageButton) getActivity().findViewById(R.id.refreshButton);
		refreshBtn.setOnClickListener(mRefreshButtonListener);
	}
	
	private void setupLocationSpinner(){
		Log.d(TAG, "setupLocationSpinner called");
		//TODO:
		//Setup lastLocationId if present and user hasn't selected a new location(?)
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.locationSpinner);
        model = mLocationDbHelper.fetchAllLocations();
        mContext.startManagingCursor(model);
        mSpinnerAdapter = new LocationSpinnerAdapter(model);   
        spinner.setAdapter(mSpinnerAdapter);
        
        OnItemSelectedListener spinnerListener = new spinnerOnItemSelectedListener(mContext,mSpinnerAdapter);
        spinner.setOnItemSelectedListener(spinnerListener);
        
        //special case:
        //if there are no locations in the spinner, then setupScenesList will not be called
        //hence, clear the mCurrentLocation and scenesAdapter to avoid displaying old scene values
        if (model.getCount() == 0){
        	mCurrentLocation=null;
        	if (mSceneAdapter != null){
            	Log.d(TAG, "clearing mSceneAdapter");
            	mSceneAdapter.clear();
            	mActiveSceneName = null;
        	}
        	
        	//raise a toast
        	Toast.makeText(getActivity(), "Update Settings and add Locations to use this application", Toast.LENGTH_SHORT).show();
        }
	}
	
	private void setupScenesList(){
		Log.d(TAG, "setupScenesList called");
		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
		TextView dummy = new TextView(getActivity());
		ImageView dummy2 = new ImageView(getActivity());
		listView.addFooterView(dummy, null, true);
		listView.setFooterDividersEnabled(true);
		listView.addHeaderView(dummy2, null, true);
		listView.setHeaderDividersEnabled(true);
		int[] colors = {0, 0xFFF47836, 0}; // Pantone172 for the example
		listView.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
		listView.setDividerHeight(1);
		
		//TODO: set Empty Text inside View
		//setEmptyText("No Scenes found");
		
		mSceneAdapter = new SceneAdapter(getActivity(), R.layout.scene_item_layout, R.id.sceneText);
		
		setListAdapter(mSceneAdapter);
		
        Uri dummyUri = null;
        Bundle params = new Bundle();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_URI, dummyUri);
        args.putParcelable(ARGS_PARAMS, params);
        
        // Initialize the Loader.
        Log.d(TAG, "calling initLoader..");
        getActivity().getSupportLoaderManager().initLoader(LOADER_SCENES, null, 
            		this);    	

	}
	
	private void restartScenesLoader(){
		Log.d(TAG, "restartScenesLoader called");
		
        if (mSceneAdapter != null && mCurrentLocation != null){
        	getActivity().getSupportLoaderManager().restartLoader(LOADER_SCENES, null, this);
        }
	}
	
	//Handle Refresh button
	private OnClickListener mRefreshButtonListener  = new OnClickListener(){
		public void onClick(View v){
			Log.d(TAG, "In mRefreshButtonListener.onClick..");
			restartScenesLoader();
		}
	};
	
	//Scenes Loader callbacks
    public Loader<ScenesAPILoader.RESTResponse> onCreateLoader(int id, Bundle args) {
    	Log.d(TAG, "onCreateLoader called");
    	if (mProgressBar == null){
    		mProgressBar = LoaderProgressDialog.show(mContext, "", "", true, false, null);
    	}else {
    		mProgressBar.show();
    	}
    	
        return new ScenesAPILoader(getActivity(), mCurrentLocation, settings);   
    }
    
    
    //Read response from loader
    public void onLoadFinished(Loader<ScenesAPILoader.RESTResponse> loader, ScenesAPILoader.RESTResponse data) {
        
    	Log.d(TAG, "onLoadFinished called");
    	
    	if (mProgressBar != null){
    		mProgressBar.dismiss();
    	}
    	
    	int    code = data.getCode();
        String json = data.getData();
        
        mSceneAdapter.clear();
        mActiveSceneName = "";
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            List<SceneItem> scenesList = HttpUtil.getScenesFromJson(json);
            // Load our list adapter with our scenes.
            //mSceneAdapter.clear();
            if (scenesList.size() >0){
                for (SceneItem scene : scenesList) {
                	mSceneAdapter.add(scene);
                	if (scene.getIsActive()){
                		mActiveSceneName = scene.sceneName;
                	}
                }            	
            } else {
            	Toast.makeText(getActivity(), "Scenes not available for this location.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Failed to load scenes. Please check your network connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLoaderReset(Loader<ScenesAPILoader.RESTResponse> loader) {
    	Log.d(TAG, "onLoaderReset called");
    	mSceneAdapter.clear();
    	mActiveSceneName = "";
    }
        
    
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//TODO:
		// Control Scenes
		// Get clicked scene name
		Log.d(TAG, "onListItemClick called");

		//uncheck currently active scene
		int prevCheckedPos = -1;
		SceneItem prevCheckedScene;

		for (int i=0; i < l.getChildCount(); i++){
			View vw = (View)l.getChildAt(i);
			if (vw != null){
				ImageView checkMark = (ImageView) vw.findViewById(R.id.sceneCheck);
				if (checkMark != null){
					checkMark.setImageResource(R.drawable.unchecked);
				}
			}
		}

		SceneItem scene  = (SceneItem)l.getItemAtPosition(position);
		Log.d(TAG, "clicked scene="+scene.getSceneName()+" isActive="+scene.getIsActive());
		
		ImageView checkMark = (ImageView) v.findViewById(R.id.sceneCheck);
		if (scene.getIsActive()){
			//scene was active earlier. So inactivate scenes
			Log.d(TAG, "inactivating scene");
			mSceneNameToActivate = "";
			checkMark.setImageResource(R.drawable.unchecked);
		} else {
			Log.d(TAG, "activating scene="+mSceneNameToActivate);
			mSceneNameToActivate = scene.getSceneName();
			//((CheckedTextView) v).setChecked(true);
			checkMark.setImageResource(R.drawable.checked);
		}
		
		//update and redraw scenes
		updateScene();
	}
	
	public void updateScene(){
		//Update scene
		 // Initialize the Loader.
        Log.d(TAG, "calling initLoader..");
        UpdateScenesLoaderCallbacks callbacks = new UpdateScenesLoaderCallbacks();
        getActivity().getSupportLoaderManager().restartLoader(LOADER_UPDATE_SCENES, null, callbacks);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		mLocationDbHelper.close();
		mSettingsDbHelper.close();
	}
	
	
	//Custom Cursor Adapter to load Location Spinner
    class LocationSpinnerAdapter extends CursorAdapter {
    	LocationSpinnerAdapter(Cursor c) {
          super(mContext, c);
        }
        
        @Override
        public void bindView(View row, Context ctxt,
                             Cursor c) {
          LocationHolder holder=(LocationHolder)row.getTag();
          
          holder.populateFrom(c, mLocationDbHelper);
        }
        
        @Override
        public View newView(Context ctxt, Cursor c,
                             ViewGroup parent) {
          LayoutInflater inflater=mContext.getLayoutInflater();
          View row=inflater.inflate(R.layout.spinner_item, parent, false);
          LocationHolder holder=new LocationHolder(row);
          
          row.setTag(holder);
          
          return(row);
        }

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			return super.getDropDownView(position, convertView, parent);
		}

		@Override
		public View newDropDownView(Context context, Cursor cursor,
				ViewGroup parent) {
		  LayoutInflater inflater=mContext.getLayoutInflater();
		  View row=inflater.inflate(R.layout.spinner_item, parent, false);

		  LocationHolder holder=new LocationHolder(row);
		  row.setTag(holder);
		  
		  return(row);
		}  
        
    }
    
    class LocationHolder{
    	private TextView name=null;
    	
    	LocationHolder(View row){
    		name=(TextView)row.findViewById(android.R.id.text1);
    	}
    	
    	void populateFrom(Cursor c, LocationDBAdapter dbAdapter){
    		name.setText(dbAdapter.getLocationName(c));
    	}
    }
    
    public class spinnerOnItemSelectedListener implements OnItemSelectedListener {
    	
    	LocationSpinnerAdapter mLocalAdapter;
        Activity mLocalContext;
    	
        public spinnerOnItemSelectedListener(Activity c, LocationSpinnerAdapter ad) {
            this.mLocalContext = c;
            this.mLocalAdapter = ad;
        }
        
        public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
        	Log.d(TAG, "Spinner...onItemSelected called..pos="+Integer.toString(pos));
			// TODO Auto-generated method stub
			LightingListFragment.this.mPos = pos;
			Cursor c	= (Cursor) parent.getItemAtPosition(pos);
			
			//set the current location
			LightingListFragment.this.mCurrentLocation = LightingListFragment.this.mLocationDbHelper.getLocation(c);
			
			if (LightingListFragment.this.mCurrentLocation != null){
				//if loader is not setup yet
				if (mSceneAdapter == null) {
					LightingListFragment.this.setupScenesList();
				} else {
					LightingListFragment.this.restartScenesLoader();
				}
				
			}
		}
		
        public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
    	
    } // spinnerOnItemSelectedListener
    
    
    public class UpdateScenesLoaderCallbacks implements LoaderCallbacks<SceneUpdateAPILoader.RESTResponse>{
    	
    	public Loader<RESTResponse> onCreateLoader(int id, Bundle args) {
    		// TODO Auto-generated method stub
    		Log.d(TAG, "onCreateLoader called");
        	if (mProgressBar == null){
        		mProgressBar = LoaderProgressDialog.show(mContext, "", "", true, false, null);
        	}else {
        		mProgressBar.show();
        	}
        	
            return new SceneUpdateAPILoader(mContext, mCurrentLocation, 
            		settings, mSceneNameToActivate);   
    	}

    	public void onLoadFinished(Loader<SceneUpdateAPILoader.RESTResponse> loader, SceneUpdateAPILoader.RESTResponse data) {
        	Log.d(TAG, "onLoadFinished called");
        	
        	if (mProgressBar != null){
        		mProgressBar.dismiss();
        	}
        	
        	int    code = data.getCode();
            String json = data.getData();
            
            // Check to see if we got an HTTP 200 code and have some data.
            if (code == 200 && !json.equals("")) {
            	Boolean retStatus = HttpUtil.parseUpdateSceneResponse(json);
            	if (retStatus){
            		//Scene updated successfully
            		//Trigger refresh of scenes
            		LightingListFragment.this.restartScenesLoader();
            	} else {
            		Toast.makeText(getActivity(), "Failed to update scene", Toast.LENGTH_SHORT).show();
            	}
            }
            else {
                Toast.makeText(getActivity(), "Failed to update scene. Please check your network connection", Toast.LENGTH_SHORT).show();
            }
    		
    	}

    	public void onLoaderReset(Loader<SceneUpdateAPILoader.RESTResponse> loader) {
    		// TODO Auto-generated method stub
    	}
    	
    } //UpdateScenesLoaderCallbacks
    
    
    public class SceneAdapter extends ArrayAdapter<SceneItem>{
    	
    	private Context mContext;
    	private int mResource;
    	private int mTextFieldId;
    	
        public SceneAdapter(Context context, int resource, int textViewResourceId) {
            super(context,resource,textViewResourceId);
            mContext = context;
            mResource = resource;
            mTextFieldId = textViewResourceId;
            
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView text;
            
            Log.d("SceneAdapter", "getView called");
            
            if (convertView == null){
            	LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            	view = inflater.inflate(mResource, null);
            } else {
            	view = convertView;
            }
            
            try {
                if (mTextFieldId == 0) {
                    //  If no custom field is assigned, assume the whole resource is a TextView
                    text = (TextView) view;
                } else {
                    //  Otherwise, find the TextView field within the layout
                    text = (TextView) view.findViewById(mTextFieldId);
                }
            } catch (ClassCastException e) {
                Log.e("SceneAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException(
                        "SceneAdapter requires the resource ID to be a TextView", e);
            }
            
            SceneItem scene = (SceneItem)this.getItem(position);
            
            if (scene instanceof CharSequence) {
                text.setText((CharSequence)scene.getSceneName());
            } else {
                text.setText(scene.getSceneName());
            }
            

            ImageView img = (ImageView) view.findViewById(R.id.sceneCheck);
            //set checkbox only if scene if active
            if (scene.getIsActive()){
            	img.setImageResource(R.drawable.checked);
            	//Pantone291
            	//view.setBackgroundColor(0xFFA4D7F4);
            } else {
            	img.setImageResource(R.drawable.unchecked);
            	//view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }
        
    	
    } //ScenesAdapter
	

}
