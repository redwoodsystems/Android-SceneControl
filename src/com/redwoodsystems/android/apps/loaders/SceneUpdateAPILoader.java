
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
package com.redwoodsystems.android.apps.loaders;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.redwoodsystems.android.apps.LocationItem;
import com.redwoodsystems.android.apps.Settings;
import com.redwoodsystems.android.apps.utils.HttpUtil;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Base64;
import android.util.Log;

public class SceneUpdateAPILoader extends AsyncTaskLoader<SceneUpdateAPILoader.RESTResponse> {
    private static final String TAG = SceneUpdateAPILoader.class.getName();
    
    // We use this delta to determine if our cached data is 
    // old or not. The value we have here is 10 minutes;
    private static final long STALE_DELTA = 600000;
    
    public enum HTTPVerb {
        GET,
        POST,
        PUT,
        DELETE
    }
    
    public static class RESTResponse {
        private String mData;
        private int    mCode;
        
        public RESTResponse() {
        }
        
        public RESTResponse(String data, int code) {
            mData = data;
            mCode = code;
        }
        
        public String getData() {
            return mData;
        }
        
        public int getCode() {
            return mCode;
        }
    }
    
    private HTTPVerb     mVerb;
    private Uri          mAction;
    private Bundle       mParams;
    private RESTResponse mRestResponse;
    private String 		mClusterName;
    private String 		mUserName;
    private String	    mPassword;
    
    private LocationItem  mLocationItem;
    private Settings   mSettings;
    private String    mActivateSceneName;
    
    private String JSON_REQ_TEMPLATE = 
    		"{\"protocolVersion\" : \"1\", \"schemaVersion\" : \"1.3.0\", \"requestType\" : \"set\", \"requestData\" : { \"location\" : [{\"id\": %d,\"sceneControl\":{\"activeSceneName\": \"%s\"}}]}}";
    private long mLastLoad;
    
    public SceneUpdateAPILoader(Context context, LocationItem locationItem, 
    		Settings settings, String activateSceneName) {
        super(context);
        mLocationItem = locationItem;
        mSettings = settings;
        mActivateSceneName = activateSceneName;
        
        mVerb = HTTPVerb.POST;
        
        mClusterName=settings.getClusterName();
        mUserName = "admin";
        mPassword = settings.getPassword();
        
        //mClusterName="";
        //mUserName = "";
        //mPassword = "";
        
        mAction = Uri.parse("https://"+mClusterName+"/uApi");
        
    }
    
    @Override
    public RESTResponse loadInBackground() {
        try {
            // At the very least we always need an action.
            if (mAction == null) {
                Log.e(TAG, "You did not define an action. REST call canceled.");
                return new RESTResponse(); // We send an empty response back. The LoaderCallbacks<RESTResponse>
                                           // implementation will always need to check the RESTResponse
                                           // and handle error cases like this.
            }
            
            if (mLocationItem == null){
            	Log.e(TAG, "No location found. REST call canceled.");
            	return new RESTResponse();
            }
            
            String jsonReqStr = String.format(JSON_REQ_TEMPLATE, mLocationItem.getLocationId(), mActivateSceneName);
            
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request = null;
            
            request = new HttpPost();
            request.setURI(new URI(mAction.toString()));
            
            HttpPost postRequest = (HttpPost) request;
            
            postRequest.setHeader("Accept","application/json");
            postRequest.setHeader("Content-type","application/json");
            postRequest.setEntity(new StringEntity(jsonReqStr));
            
            if (request != null) {
                
            	//get HttpClient with SSL parameters
            	HttpClient client = HttpUtil.getNewHttpClient();
            	
            	//BASIC Authentication
            	HttpUtil.setBasicAuthCredentials(client, this.mUserName, this.mPassword);
                
                Log.d(TAG, "Executing request: "+ verbToString(mVerb) +": "+ mAction.toString());
                Log.d(TAG, jsonReqStr);
                
                //Validate JSON
                HttpUtil.validateJSONString(jsonReqStr);
                
                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this Loader's thread.
                HttpResponse response = client.execute(request);
                
                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
                String respString = "";
                RESTResponse restResponse = null;
                
                // Here we create our response and send it back to the LoaderCallbacks<RESTResponse> implementation.
                
                if (responseEntity != null){
                	respString = EntityUtils.toString(responseEntity);
                	restResponse = new RESTResponse(respString, statusCode);
                } else {
                	restResponse = new RESTResponse(null, statusCode);
                }
              
                Log.d(TAG, respString);
                return restResponse;
            }
            
            // Request was null if we get here, so let's just send our empty RESTResponse like usual.
            return new RESTResponse();
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ verbToString(mVerb) +": "+ mAction.toString(), e);
            return new RESTResponse();
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            return new RESTResponse();
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTResponse();
        }
        catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTResponse();
        }
    }
    
    @Override
    public void deliverResult(RESTResponse data) {
        // Here we cache our response.
        mRestResponse = data;
        super.deliverResult(data);
    }
    
    @Override
    protected void onStartLoading() {
        if (mRestResponse != null) {
            // We have a cached result, so we can just
            // return right away.
            super.deliverResult(mRestResponse);
        }
        
        // If our response is null or we have hung onto it for a long time,
        // then we perform a force load.
        if (mRestResponse == null || System.currentTimeMillis() - mLastLoad >= STALE_DELTA) forceLoad();
        mLastLoad = System.currentTimeMillis();
    }
    
    @Override
    protected void onStopLoading() {
        // This prevents the AsyncTask backing this
        // loader from completing if it is currently running.
        cancelLoad();
    }
    
    @Override
    protected void onReset() {
        super.onReset();
        
        // Stop the Loader if it is currently running.
        onStopLoading();
        
        // Get rid of our cache if it exists.
        mRestResponse = null;
        
        // Reset our stale timer.
        mLastLoad = 0;
    }

    
    private static String verbToString(HTTPVerb verb) {
        switch (verb) {
            case GET:
                return "GET";
                
            case POST:
                return "POST";
                
            case PUT:
                return "PUT";
                
            case DELETE:
                return "DELETE";
        }
        
        return "";
    }
	
    
}
