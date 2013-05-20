
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

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TestListFragment extends ListFragment {
	
	private static final String TAG = TestListFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		Log.d(TAG, "TestListFragment.onCreateView");
		View view = inflater.inflate(R.layout.test_fragment_layout, null);
		return view;		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		ArrayList<String> colors = new ArrayList<String>();
		colors.add("Red");
		colors.add("Green");
		colors.add("Yellow");
		
		//context can be obtained from getActivity()
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_label_list,
				colors);
        
        setListAdapter(adapter);
		
	}
	
	
	
}
