
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
package com.redwoodsystems.android.apps.utils;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.redwoodsystems.android.apps.LocationItem;
import com.redwoodsystems.android.apps.SceneItem;

import android.util.Base64;
import android.util.Log;

public class HttpUtil {
	
	private static final String TAG = HttpUtil.class.getName();
	private static final long HTTP_TIMEOUT = 30000; //ms
	
	public static HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
	        
	        ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
	        
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	public static String getAuthHeader(String userName, String password){
		String s = userName + ":" + password;
		String authHdr = "Basic "+Base64.encode(s.getBytes(), Base64.DEFAULT);
		return authHdr;
	}
	
	public static void setBasicAuthCredentials(HttpClient client, String userName, String password){
		
    	//BASIC Authentication
    	List<String> authPrefs = new ArrayList<String>(2);
    	authPrefs.add(AuthPolicy.BASIC);
    	client.getParams().setParameter("http.auth.scheme-pref", authPrefs);
    	          	
    	((DefaultHttpClient)client).getCredentialsProvider().setCredentials(
    			org.apache.http.auth.AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));
	}
	
	public static Boolean validateJSONString(String jsonStr){
        //Validate JSON
        try {
				JSONObject obj = (JSONObject) new JSONTokener(jsonStr).nextValue();
				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	
    public static List<SceneItem> getScenesFromJson(String json) {
    	
    	//TODO;
    	//1. Implement this method
        ArrayList<SceneItem> sceneList = new ArrayList<SceneItem>();
        int locationId;
        String activeSceneName = "";
    	JSONObject respObj = null;
    	
        try {
        	respObj = new JSONObject(json);        	
        	
			if (respObj != null) {
				locationId = respObj.getInt("id");
				
				JSONObject sceneControl;
				try {
					 sceneControl = respObj.getJSONObject("sceneControl");
				} catch (JSONException e){
					sceneControl = null;
				}
				
				if (sceneControl != null){
					//found some scenes
					activeSceneName = sceneControl.getString("activeSceneName");
					JSONArray sceneArr = sceneControl.getJSONArray("scene");
					
					for (int i=0; i<sceneArr.length(); i++){
						JSONObject sc = sceneArr.getJSONObject(i);
						SceneItem scene = new SceneItem(sc.getString("name"), 
								sc.getInt("order"), locationId);
						if (scene.getSceneName().equals(activeSceneName)){
							scene.setIsActive(true);
						}
						sceneList.add(scene);
					}
					//Sort sceneList by order
					Collections.sort(sceneList, new SceneOrderComparator());
				} 
			}
       }
        catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON.", e);
        }
        
        return sceneList;
    }
    
    static class SceneOrderComparator implements Comparator<SceneItem>{
		public int compare(SceneItem scene1, SceneItem scene2) {
			return (scene1.getOrder() - scene2.getOrder());
		}
    }
    
    public static List<LocationItem> getLocationsFromJson(String json){
    	ArrayList<LocationItem> locationList = new ArrayList<LocationItem>();
    	String responseType = null;
    	JSONObject respObj = null;
    	Boolean retStatus = false;

        try {
        	respObj = new JSONObject(json);        	
        	
			if (respObj != null) {
				responseType = respObj.getString("responseType");
				String errorType="";
				Log.i(TAG, "POST API responseType = " + responseType);
				if (responseType.equals("errorResponse"))  {
					errorType = respObj.getString("responseErrorType");
					Log.i(TAG, "POST API errorType = " + errorType);
					retStatus = false;
				}else {
					retStatus = true;
					JSONObject responseData = respObj.getJSONObject("responseData");
					JSONArray locationArr = responseData.getJSONArray("location");
					
					for (int i=0; i< locationArr.length(); i++){
						JSONObject loc = locationArr.getJSONObject(i);
						int locationId = loc.getInt("id");
						if (locationId >=100){
							String locationName = loc.getString("name");							
							locationList.add(new LocationItem(locationId,locationName));							
						}
					}
				}			
			}
						
       }
        catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON.", e);
        }
    	
		return locationList;
    }

    
    public static Boolean parseUpdateSceneResponse(String json){
    	String responseType = null;
    	JSONObject respObj = null;
    	Boolean retStatus = false;

        try {
        	respObj = new JSONObject(json);        	
        	
			if (respObj != null) {
				responseType = respObj.getString("responseType");
				String errorType="";
				Log.i(TAG, "POST API responseType = " + responseType);
				if (responseType.equals("errorResponse"))  {
					errorType = respObj.getString("responseErrorType");
					Log.i(TAG, "POST API errorType = " + errorType);
					retStatus = false;
				}else {
					retStatus = true;
				}			
			}
						
       }
        catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON.", e);
        }
    	
		return retStatus;
    }
    
	
	
}
