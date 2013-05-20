
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class LocationsActivity extends Activity {
	
	private static final String TAG = LocationsActivity.class.getName();
	
	Activity mContext;
	LocationDBAdapter mDbHelper;
	Cursor model=null;
	LocationAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "In LocationsActivity..onCreate");
		
		setContentView(R.layout.locations_layout);
		
		ImageButton add_btn = (ImageButton) findViewById(R.id.add_btn);
		
		add_btn.setOnClickListener(mAddClickListener);
		mContext = this;
		
		ListView list=(ListView)findViewById(R.id.locationListView);
		
        mDbHelper = new LocationDBAdapter(this);
        mDbHelper.open();
        
	}
	
	
	
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "In LocationsActivity..onPause");
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d(TAG, "In LocationsActivity..onRestart");
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "In LocationsActivity..onResume");
		//refresh the cursor
		ListView list=(ListView)findViewById(R.id.locationListView);
        model = mDbHelper.fetchAllLocations();
        Log.d(TAG, "In LocationsActivity..locations db count ="+model.getCount());
        
        startManagingCursor(model);
        mAdapter = new LocationAdapter(model);
        list.setAdapter(mAdapter);
        
        list.setOnItemLongClickListener(mLongClickListener);
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, "In LocationsActivity..onStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG, "In LocationsActivity..onStop");
	}



	private OnClickListener mAddClickListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(mContext, SearchActivity.class);
        	//Intent intent = new Intent(mContext, TestFragmentActivity.class);
        	startActivity(intent);
        }
    };
    
    private OnItemLongClickListener mLongClickListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> adapterView, View itemView,
				int position, long id) {
			// TODO
			// Set icon and theme
			model.moveToPosition(position);
			int locationId = mDbHelper.getLocationId(model);
			Log.d(TAG, "Long click on locationId = "+Integer.valueOf(locationId).toString());
			
			Dialog dialog = createDeleteDialog();
			dialog.show();
			
			return false;
		}
    };
    
    private Dialog createDeleteDialog(){
    	//TODO:
    	//1. Apply theme to the dialog
    	
    	Dialog dialog = new AlertDialog.Builder(LocationsActivity.this)
        .setTitle(R.string.delete_title)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	int locationId = mDbHelper.getLocationId(model);
            	mDbHelper.deleteLocationWithLocId(locationId);
            	model.requery();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	//Do nothing            	
            }
        })
        .create();
    	
    	return dialog;
    }
    
    
    class LocationAdapter extends CursorAdapter {
        LocationAdapter(Cursor c) {
          super(LocationsActivity.this, c);
        }
        
        @Override
        public void bindView(View row, Context ctxt,
                             Cursor c) {
        	LocationHolder holder=(LocationHolder)row.getTag();
          
          holder.populateFrom(c, mDbHelper);
        }
        
        @Override
        public View newView(Context ctxt, Cursor c,
                             ViewGroup parent) {
          LayoutInflater inflater=getLayoutInflater();
          //View row=inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
          View row=inflater.inflate(R.layout.locations_item_layout, parent, false);
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
    	
    	void populateFrom(Cursor c, LocationDBAdapter dbAdpter){
    		name.setText(dbAdpter.getLocationName(c));
    	}
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		Log.d(TAG, "In LocationsActivity..onDestroy");
		mDbHelper.close();
	}
    
	
    
    

}
