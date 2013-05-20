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

public class Settings {
	
	private String clusterName;
	private String userName;
	private String password;
	private int lastLocationId;
	private LocationItem lastLocation;
	
	public Settings(){
		super();
		this.clusterName = "";
		this.userName = "admin";
		this.password = "";
		this.lastLocationId = -1;
		this.lastLocation = null;
	}
	
	public Settings(String clusterName, String userName, String password,
			int lastLocationId) {
		super();
		this.clusterName = clusterName;
		this.userName = userName;
		this.password = password;
		this.lastLocationId = lastLocationId;
	}
	
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getLastLocationId() {
		return lastLocationId;
	}
	public void setLastLocationId(int lastLocationId) {
		this.lastLocationId = lastLocationId;
	}
	public LocationItem getLastLocation() {
		return lastLocation;
	}
	public void setLastLocation(LocationItem lastLocation) {
		this.lastLocation = lastLocation;
	}
	
	public String toString(){
		return (this.clusterName+" : "+this.password+" : "+this.lastLocationId);
	}

	
	
}
