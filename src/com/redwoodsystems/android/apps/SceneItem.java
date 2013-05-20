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

public class SceneItem {
	
	String sceneName;
	int order;
	Boolean isActive;
	int locationId;
	LocationItem location;
	
	public SceneItem(String sceneName, int order, int locationId) {
		super();
		this.sceneName = sceneName;
		this.order = order;
		this.locationId = locationId;
		this.isActive = false;
		this.location = null;
	}
	
	public String getSceneName() {
		return sceneName;
	}
	public void setSceneName(String sceneName) {
		this.sceneName = sceneName;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public LocationItem getLocation() {
		return location;
	}
	public void setLocation(LocationItem location) {
		this.location = location;
	}
	
	public String toString(){
		return this.sceneName;
	}
	

}
